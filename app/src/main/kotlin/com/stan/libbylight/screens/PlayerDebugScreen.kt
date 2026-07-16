package com.stan.libbylight.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.viewinterop.AndroidView
import com.stan.libbylight.LibbyBridge
import com.stan.libbylight.LibbyWebPlayer
import com.stan.libbylight.library.LoanItem
import com.stan.libbylight.player.PlayerState
import com.stan.libbylight.ui.LightBarButton
import com.stan.libbylight.ui.LightBottomBar
import com.stan.libbylight.ui.LightIcon
import com.stan.libbylight.ui.LightIcons
import com.stan.libbylight.ui.LightLazyScrollView
import com.stan.libbylight.ui.LightScrollView
import com.stan.libbylight.ui.LightText
import com.stan.libbylight.ui.LightTextVariant
import com.stan.libbylight.ui.LightTheme
import com.stan.libbylight.ui.LightThemeTokens
import com.stan.libbylight.ui.LightTopBar
import com.stan.libbylight.ui.LightTopBarCenter
import com.stan.libbylight.ui.gridUnitsAsDp
import com.stan.libbylight.ui.lightClickable
import kotlinx.coroutines.delay
import kotlin.math.roundToLong

private const val TAG = "LibbyLight"

@Composable
fun PlayerDebugScreen() {
    var state by remember { mutableStateOf(PlayerState()) }
    var currentUrl by remember { mutableStateOf("") }
    var sessionReady by remember { mutableStateOf(false) }
    var showBooks by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    var revealLibby by remember { mutableStateOf(false) }

    var loans by remember { mutableStateOf<List<LoanItem>>(emptyList()) }
    var activeLoan by remember { mutableStateOf<LoanItem?>(null) }
    var lastActiveLoanUrl by remember { mutableStateOf("") }
    var booksLoading by remember { mutableStateOf(false) }
    var booksMessage by remember { mutableStateOf("") }
    var lastBridgeUrl by remember { mutableStateOf("") }

    val isLoanPage = currentUrl.contains("/open/loan/")
    val showNativeUi = sessionReady && !revealLibby

    Box(modifier = Modifier.fillMaxSize()) {
        // Libby remains mounted and rendered beneath Bard's native UI.
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                LibbyWebPlayer.detachFromCurrentParent()
                LibbyWebPlayer.attachToActivity(context).also { webView ->
                    val url = webView.url.orEmpty()
                    if (url.isBlank() || url == "about:blank") {
                        Log.d(TAG, "WebView was blank; loading Libby")
                        LibbyWebPlayer.loadLoginPage()
                    }
                }
            },
            update = { webView ->
                if (!showNativeUi) webView.requestFocus()
            },
        )

        when {
            !sessionReady || revealLibby -> {
                if (sessionReady) {
                    LibbyRecoveryBar(
                        onDone = {
                            revealLibby = false
                            showBooks = !isLoanPage
                        },
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                }
            }

            showSettings -> {
                SettingsScreen(
                    onBack = { showSettings = false },
                    onOpenLibby = {
                        showSettings = false
                        revealLibby = true
                    },
                )
            }

            showBooks || !isLoanPage -> {
                BooksScreen(
                    loans = loans,
                    loading = booksLoading,
                    message = booksMessage,
                    onSettings = { showSettings = true },
                    onPlayer = (activeLoan?.loanUrl ?: lastActiveLoanUrl.takeIf { it.isNotBlank() })
                        ?.let { loanUrl ->
                            {
                                showBooks = false
                                if (!currentUrl.startsWith(loanUrl)) {
                                    state = PlayerState(diagnostic = "Connecting to audiobook player…")
                                    LibbyWebPlayer.openLoan(loanUrl)
                                }
                            }
                        },
                    onOpenLoan = { loan ->
                        activeLoan = loan
                        lastActiveLoanUrl = loan.loanUrl
                        showBooks = false
                        state = PlayerState(diagnostic = "Connecting to audiobook player…")
                        LibbyWebPlayer.openLoan(loan.loanUrl)
                    },
                )
            }

            else -> {
                PlayerScreen(
                    state = state,
                    loan = activeLoan,
                    onSeekTo = LibbyBridge::seekTo,
                    onBooks = {
                        showBooks = true
                        booksLoading = false
                        booksMessage = ""
                    },
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            val webView = LibbyWebPlayer.requireWebView()
            currentUrl = webView.url.orEmpty()

            LibbyWebPlayer.checkIsLoggedIn { ready ->
                if (ready && !sessionReady) {
                    sessionReady = true
                    showBooks = !currentUrl.contains("/open/loan/")
                    if (!currentUrl.contains("/open/loan/")) {
                        booksLoading = true
                        booksMessage = "Loading books…"
                        LibbyWebPlayer.loadLibraryPage()
                    }
                }
            }

            if (currentUrl != lastBridgeUrl) {
                lastBridgeUrl = currentUrl
                LibbyBridge.onTopLevelUrlChanged(currentUrl)

                if (currentUrl.contains("/open/loan/")) {
                    showBooks = false
                    lastActiveLoanUrl = currentUrl.substringBefore('?').substringBefore('#')
                    activeLoan = loans.firstOrNull { currentUrl.startsWith(it.loanUrl) } ?: activeLoan
                    state = PlayerState(diagnostic = "Connecting to audiobook player…")
                }
            }

            if (sessionReady && currentUrl.contains("/open/loan/")) {
                LibbyBridge.getPlayerState { newState -> state = newState }
            }

            if (sessionReady && currentUrl.contains("/shelf")) {
                LibbyWebPlayer.scrapeAudiobookLoans { found ->
                    if (found.isNotEmpty()) {
                        loans = found
                        booksLoading = false
                        booksMessage = ""
                    }
                }
            }

            delay(700)
        }
    }

    LaunchedEffect(showBooks) {
        if (!showBooks || !sessionReady) return@LaunchedEffect

        // Keep an active loan page mounted so playback continues behind Books.
        if (currentUrl.contains("/open/loan/") && loans.isNotEmpty()) {
            booksLoading = false
            booksMessage = ""
            return@LaunchedEffect
        }

        booksLoading = true
        booksMessage = "Loading books…"

        if (!currentUrl.contains("/shelf")) {
            LibbyWebPlayer.loadLibraryPage()
        }

        repeat(16) {
            delay(400)
            currentUrl = LibbyWebPlayer.requireWebView().url.orEmpty()
            if (currentUrl.contains("/shelf")) {
                LibbyWebPlayer.scrapeAudiobookLoans { found ->
                    if (found.isNotEmpty()) {
                        loans = found
                        booksLoading = false
                        booksMessage = ""
                    }
                }
                if (loans.isNotEmpty()) return@LaunchedEffect
            }
        }

        if (loans.isEmpty()) {
            booksLoading = false
            booksMessage = "No Libby audiobooks found."
        }
    }

    DisposableEffect(Unit) {
        onDispose { LibbyWebPlayer.detachFromCurrentParent() }
    }
}

@Composable
private fun BardSurface(content: @Composable () -> Unit) {
    LightTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(LightThemeTokens.colors.background),
        ) {
            content()
        }
    }
}

@Composable
private fun LibbyRecoveryBar(
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LightTheme {
        LightTopBar(
            center = LightTopBarCenter.Text("Libby"),
            rightButton = LightBarButton.Text("Done", onClick = onDone),
            modifier = modifier.background(LightThemeTokens.colors.background),
        )
    }
}

@Composable
private fun BooksScreen(
    loans: List<LoanItem>,
    loading: Boolean,
    message: String,
    onSettings: () -> Unit,
    onPlayer: (() -> Unit)?,
    onOpenLoan: (LoanItem) -> Unit,
) {
    BardSurface {
        Column(modifier = Modifier.fillMaxSize()) {
            when {
                loading && loans.isEmpty() -> BooksStatus("Loading books…", Modifier.weight(1f))
                message.isNotBlank() && loans.isEmpty() -> BooksStatus(message, Modifier.weight(1f))
                loans.isEmpty() -> BooksStatus("No Libby audiobooks", Modifier.weight(1f))
                else -> {
                    LightLazyScrollView(
                        uniformItemHeightGridUnits = 6.5f,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(
                                start = 1.5f.gridUnitsAsDp(),
                                top = 0.75f.gridUnitsAsDp(),
                            ),
                    ) {
                        items(loans, key = { it.loanUrl }) { loan ->
                            BookRow(loan = loan, onClick = { onOpenLoan(loan) })
                        }
                    }
                }
            }

            LightBottomBar(
                horizontalPaddingUnits = 0.5f,
                items = listOf(
                    LightBarButton.LightIcon(
                        icon = LightIcons.SETTINGS,
                        onClick = onSettings,
                        contentDescription = "Settings",
                    ),
                    onPlayer?.let {
                        LightBarButton.LightIcon(
                            icon = LightIcons.PLAY,
                            onClick = it,
                            contentDescription = "Current player",
                        )
                    },
                ),
            )
        }
    }
}

@Composable
private fun BooksStatus(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 2f.gridUnitsAsDp()),
        contentAlignment = Alignment.Center,
    ) {
        LightText(
            text = message,
            variant = LightTextVariant.Paragraph,
            align = TextAlign.Center,
            lighten = true,
        )
    }
}

@Composable
private fun BookRow(loan: LoanItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.5f.gridUnitsAsDp())
            .lightClickable(
                onClickLabel = "Play ${loan.title}",
                role = Role.Button,
                onClick = onClick,
            )
            .padding(
                end = 1f.gridUnitsAsDp(),
                top = 0.25f.gridUnitsAsDp(),
                bottom = 0.25f.gridUnitsAsDp(),
            ),
        verticalArrangement = Arrangement.Top,
    ) {
        LightText(
            text = loan.title,
            variant = LightTextVariant.Subheading,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (loan.author.isNotBlank()) {
            LightText(
                text = loan.author,
                variant = LightTextVariant.Detail,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        val status = listOf(normalizeProgress(loan.progressText), normalizeDueText(loan.dueText))
            .filter { it.isNotBlank() }
            .joinToString("  ·  ")
        if (status.isNotBlank()) {
            LightText(
                text = status,
                variant = LightTextVariant.Superfine,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                lighten = true,
            )
        }
    }
}

@Composable
private fun PlayerScreen(
    state: PlayerState,
    loan: LoanItem?,
    onSeekTo: (positionMilliseconds: Long) -> Unit,
    onBooks: () -> Unit,
) {
    var showSpeedPicker by remember { mutableStateOf(false) }
    var requestedSpeed by remember { mutableStateOf<Double?>(null) }
    var scrubProgress by remember { mutableStateOf<Float?>(null) }
    var pendingSeekMilliseconds by remember { mutableStateOf<Long?>(null) }
    val title = loan?.title?.takeIf { it.isNotBlank() }
        ?: state.title.takeIf { it.isNotBlank() && it != "Libby" }
        ?: "Audiobook"
    val liveProgress = if (state.durationSeconds > 0) {
        (state.positionSeconds / state.durationSeconds).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }
    val hasTiming = state.durationSeconds > 0
    val pendingProgress = pendingSeekMilliseconds?.let { pending ->
        if (hasTiming) (pending / (state.durationSeconds * 1000.0)).toFloat().coerceIn(0f, 1f)
        else null
    }
    val displayedProgress = scrubProgress ?: pendingProgress ?: liveProgress
    val displayedPositionSeconds = when {
        scrubProgress != null -> scrubProgress!!.toDouble() * state.durationSeconds
        pendingSeekMilliseconds != null -> pendingSeekMilliseconds!! / 1000.0
        else -> state.positionSeconds
    }
    val displayedSpeed = requestedSpeed ?: state.playbackSpeed

    LaunchedEffect(state.playbackSpeed, requestedSpeed) {
        val requested = requestedSpeed ?: return@LaunchedEffect
        if (kotlin.math.abs(state.playbackSpeed - requested) < 0.01) {
            requestedSpeed = null
        }
    }

    LaunchedEffect(state.positionSeconds, pendingSeekMilliseconds) {
        val pending = pendingSeekMilliseconds ?: return@LaunchedEffect
        val actualMilliseconds = (state.positionSeconds * 1000.0).roundToLong()
        if (kotlin.math.abs(actualMilliseconds - pending) <= 2_000L) {
            pendingSeekMilliseconds = null
        }
    }

    LaunchedEffect(pendingSeekMilliseconds) {
        val pending = pendingSeekMilliseconds ?: return@LaunchedEffect
        delay(3_000)
        if (pendingSeekMilliseconds == pending) pendingSeekMilliseconds = null
    }

    BardSurface {
        if (showSpeedPicker) {
            SpeedPicker(
                currentSpeed = displayedSpeed,
                onSelect = { speed ->
                    requestedSpeed = speed
                    LibbyBridge.setSpeed(speed)
                    showSpeedPicker = false
                },
                onClose = { showSpeedPicker = false },
            )
            return@BardSurface
        }

        Column(modifier = Modifier.fillMaxSize()) {
            LightTopBar(
                leftButton = LightBarButton.LightIcon(
                    icon = LightIcons.BACK,
                    onClick = onBooks,
                    contentDescription = "Back to Books",
                ),
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 2f.gridUnitsAsDp()),
            ) {
                Spacer(Modifier.height(1f.gridUnitsAsDp()))
                LightText(
                    text = loan?.author?.takeIf { it.isNotBlank() } ?: "Libby",
                    variant = LightTextVariant.Detail,
                    align = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(0.75f.gridUnitsAsDp()))
                LightText(
                    text = title,
                    variant = LightTextVariant.Heading,
                    align = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.height(0.75f.gridUnitsAsDp()))
                LightText(
                    text = if (hasTiming) formatPlaybackTime(state.durationSeconds) else "--:--",
                    variant = LightTextVariant.Detail,
                    align = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(2.5f.gridUnitsAsDp()))
                PlaybackProgress(
                    progress = displayedProgress,
                    enabled = hasTiming,
                    onScrub = { scrubProgress = it },
                    onScrubCancelled = { scrubProgress = null },
                    onSeek = { fraction ->
                        val targetMilliseconds =
                            (state.durationSeconds * 1000.0 * fraction).roundToLong()
                                .coerceIn(0L, (state.durationSeconds * 1000.0).roundToLong())
                        scrubProgress = null
                        pendingSeekMilliseconds = targetMilliseconds
                        onSeekTo(targetMilliseconds)
                    },
                )

                Spacer(Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    PlayerIconAction(
                        icon = LightIcons.SKIP_BACKWARD_FIFTEEN,
                        description = "Rewind 15 seconds",
                        iconWidth = 3f,
                        iconHeight = 3.25f,
                        onClick = { LibbyBridge.back15() },
                    )
                    PlayerIconAction(
                        icon = if (state.isPlaying) LightIcons.PAUSE else LightIcons.PLAY,
                        description = if (state.isPlaying) "Pause" else "Play",
                        iconWidth = 2.5f,
                        iconHeight = 2.5f,
                        touchSize = 6f,
                        onClick = {
                            if (state.isPlaying) LibbyBridge.pause() else LibbyBridge.play()
                        },
                    )
                    PlayerIconAction(
                        icon = LightIcons.SKIP_FORWARD_FIFTEEN,
                        description = "Forward 15 seconds",
                        iconWidth = 3f,
                        iconHeight = 3.25f,
                        onClick = { LibbyBridge.forward15() },
                    )
                }
                Spacer(Modifier.height(1f.gridUnitsAsDp()))
                LightText(
                    text = if (hasTiming) {
                        formatPlaybackTime(displayedPositionSeconds)
                    } else {
                        "--:--"
                    },
                    variant = LightTextVariant.Fine,
                    align = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(Modifier.weight(1f))
            }

            LightBottomBar(
                items = listOf(
                    LightBarButton.Text(
                        "${trimSpeed(displayedSpeed)}×",
                        onClick = { showSpeedPicker = true },
                    ),
                ),
            )
        }
    }
}

@Composable
private fun PlaybackProgress(
    progress: Float,
    enabled: Boolean,
    onScrub: (Float) -> Unit,
    onScrubCancelled: () -> Unit,
    onSeek: (Float) -> Unit,
) {
    var trackWidthPixels by remember { mutableStateOf(0) }
    fun progressAt(offset: Offset): Float =
        if (trackWidthPixels > 0) (offset.x / trackWidthPixels).coerceIn(0f, 1f) else 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(3f.gridUnitsAsDp())
            .onSizeChanged { trackWidthPixels = it.width }
            .pointerInput(enabled, trackWidthPixels) {
                if (!enabled || trackWidthPixels <= 0) return@pointerInput
                detectTapGestures { offset -> onSeek(progressAt(offset)) }
            }
            .pointerInput(enabled, trackWidthPixels) {
                if (!enabled || trackWidthPixels <= 0) return@pointerInput
                var proposedProgress = progress
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        proposedProgress = progressAt(offset)
                        onScrub(proposedProgress)
                    },
                    onDragCancel = onScrubCancelled,
                    onDragEnd = { onSeek(proposedProgress) },
                ) { change, _ ->
                    proposedProgress = progressAt(change.position)
                    onScrub(proposedProgress)
                    change.consume()
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .height(0.08f.gridUnitsAsDp())
                .fillMaxWidth()
                .background(LightThemeTokens.colors.contentSecondary),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(0.35f.gridUnitsAsDp())
                .background(LightThemeTokens.colors.content),
        )
    }
}

@Composable
private fun PlayerIconAction(
    icon: com.stan.libbylight.ui.LightIconConfiguration,
    description: String,
    iconWidth: Float = 2f,
    iconHeight: Float = 2f,
    touchSize: Float = 5f,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(touchSize.gridUnitsAsDp())
            .lightClickable(
                onClickLabel = description,
                role = Role.Button,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        LightIcon(
            icon = icon,
            width = iconWidth,
            height = iconHeight,
            contentDescription = description,
        )
    }
}

@Composable
private fun SpeedPicker(
    currentSpeed: Double,
    onSelect: (Double) -> Unit,
    onClose: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        LightTopBar(
            leftButton = LightBarButton.LightIcon(
                icon = LightIcons.BACK,
                onClick = onClose,
                contentDescription = "Back to Player",
            ),
            center = LightTopBarCenter.Text("Playback Speed"),
        )
        LightScrollView(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(start = 2f.gridUnitsAsDp()),
        ) {
            Spacer(Modifier.height(0.5f.gridUnitsAsDp()))
            listOf(1.0, 1.25, 1.5, 1.75, 2.0).forEach { speed ->
                val isSelected = kotlin.math.abs(currentSpeed - speed) < 0.01
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.5f.gridUnitsAsDp())
                        .semantics { selected = isSelected }
                        .lightClickable(
                            onClickLabel = "Set speed to ${trimSpeed(speed)} times",
                            role = Role.RadioButton,
                        ) { onSelect(speed) },
                    contentAlignment = Alignment.CenterStart,
                ) {
                    LightText(
                        text = "${trimSpeed(speed)}×",
                        variant = LightTextVariant.Heading,
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsScreen(
    onBack: () -> Unit,
    onOpenLibby: () -> Unit,
) {
    BardSurface {
        Column(modifier = Modifier.fillMaxSize()) {
            LightTopBar(
                leftButton = LightBarButton.LightIcon(
                    icon = LightIcons.BACK,
                    onClick = onBack,
                    contentDescription = "Back",
                ),
                center = LightTopBarCenter.Text("Settings"),
            )

            LightScrollView(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(start = 1.5f.gridUnitsAsDp()),
            ) {
                Spacer(Modifier.height(1f.gridUnitsAsDp()))
                SettingsRow(
                    title = "Libby",
                    onClick = onOpenLibby,
                )
                SettingsRow(title = "Local Books")
                SettingsRow(title = "RSS Feeds")
            }
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(5.5f.gridUnitsAsDp())
            .lightClickable(
                onClickLabel = "Open $title settings",
                role = Role.Button,
                onClick = onClick,
            )
            .padding(end = 1f.gridUnitsAsDp()),
        verticalArrangement = Arrangement.Center,
    ) {
        LightText(
            text = title,
            variant = LightTextVariant.Heading,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

private fun formatPlaybackTime(seconds: Double): String {
    val total = if (seconds.isFinite()) seconds.toLong().coerceAtLeast(0) else 0
    return if (total >= 3600) {
        val hours = total / 3600
        val minutes = (total % 3600) / 60
        val remainingSeconds = total % 60
        "%d:%02d:%02d".format(hours, minutes, remainingSeconds)
    } else {
        val minutes = total / 60
        val remainingSeconds = total % 60
        "%d:%02d".format(minutes, remainingSeconds)
    }
}

private fun normalizeProgress(progressText: String): String =
    Regex("(\\d+(?:\\.\\d+)?)\\s*%")
        .find(progressText)
        ?.groupValues
        ?.get(1)
        ?.let { "$it%" }
        .orEmpty()

private fun normalizeDueText(dueText: String): String {
    val clean = dueText.replace(Regex("\\s+"), " ").trim()
    if (clean.isBlank()) return ""

    val duration = Regex("(\\d+\\s+days?)", RegexOption.IGNORE_CASE)
        .find(clean)
        ?.value
    if (duration != null) return "Due in $duration"

    return if (clean.startsWith("due", ignoreCase = true)) clean else "Due $clean"
}

private fun trimSpeed(speed: Double): String =
    if (speed % 1.0 == 0.0) speed.toInt().toString() else speed.toString()
