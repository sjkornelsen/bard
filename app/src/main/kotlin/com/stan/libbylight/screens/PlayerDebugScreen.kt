package com.stan.libbylight.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.stan.libbylight.LibbyBridge
import com.stan.libbylight.LibbyWebPlayer
import com.stan.libbylight.library.LoanItem
import com.stan.libbylight.player.PlayerState
import kotlinx.coroutines.delay

private const val TAG = "LibbyLight"

@Composable
fun PlayerDebugScreen() {
    var state by remember { mutableStateOf(PlayerState()) }
    var currentUrl by remember { mutableStateOf("") }
    var sessionReady by remember { mutableStateOf(false) }
    var showNativeShelf by remember { mutableStateOf(false) }
    var revealLibby by remember { mutableStateOf(false) }

    var loans by remember { mutableStateOf<List<LoanItem>>(emptyList()) }
    var shelfLoading by remember { mutableStateOf(false) }
    var shelfMessage by remember { mutableStateOf("") }
    var lastBridgeUrl by remember { mutableStateOf("") }

    val isLoanPage = currentUrl.contains("/open/loan/")
    val showNativeUi = sessionReady && !revealLibby

    Box(modifier = Modifier.fillMaxSize()) {
        // Keep Libby fully mounted and rendered underneath the native UI.
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
                // Real Libby remains available for login and emergency recovery.
                if (sessionReady) {
                    RecoveryBar(
                        onHideLibby = {
                            revealLibby = false
                            showNativeShelf = !isLoanPage
                        },
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                }
            }

            showNativeShelf || !isLoanPage -> {
                NativeShelf(
                    loans = loans,
                    loading = shelfLoading,
                    message = shelfMessage,
                    onRefresh = {
                        shelfLoading = true
                        shelfMessage = "Loading loans…"
                        LibbyWebPlayer.loadLibraryPage()
                    },
                    onRevealLibby = { revealLibby = true },
                    onOpenLoan = { loan ->
                        showNativeShelf = false
                        state = PlayerState(diagnostic = "Connecting to audiobook player…")
                        LibbyWebPlayer.openLoan(loan.loanUrl)
                    },
                )
            }

            else -> {
                NativePlayer(
                    state = state,
                    onShelf = {
                        showNativeShelf = true
                        shelfLoading = true
                        shelfMessage = "Loading loans…"
                        LibbyWebPlayer.loadLibraryPage()
                    },
                    onRevealLibby = { revealLibby = true },
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
                    showNativeShelf = !currentUrl.contains("/open/loan/")
                    if (!currentUrl.contains("/open/loan/")) {
                        shelfLoading = true
                        shelfMessage = "Loading loans…"
                        LibbyWebPlayer.loadLibraryPage()
                    }
                }
            }

            if (currentUrl != lastBridgeUrl) {
                lastBridgeUrl = currentUrl
                LibbyBridge.onTopLevelUrlChanged(currentUrl)

                if (currentUrl.contains("/open/loan/")) {
                    showNativeShelf = false
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
                        shelfLoading = false
                        shelfMessage = ""
                    }
                }
            }

            delay(700)
        }
    }

    LaunchedEffect(showNativeShelf) {
        if (!showNativeShelf || !sessionReady) return@LaunchedEffect

        shelfLoading = true
        shelfMessage = "Loading loans…"

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
                        shelfLoading = false
                        shelfMessage = ""
                    }
                }
                if (loans.isNotEmpty()) return@LaunchedEffect
            }
        }

        if (loans.isEmpty()) {
            shelfLoading = false
            shelfMessage = "No audiobook loans found. Tap Refresh after Libby finishes loading."
        }
    }

    DisposableEffect(Unit) {
        onDispose { LibbyWebPlayer.detachFromCurrentParent() }
    }
}

@Composable
private fun RecoveryBar(
    onHideLibby: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        LightButton("Hide Libby", width = 100, onClick = onHideLibby)
    }
}

@Composable
private fun NativeShelf(
    loans: List<LoanItem>,
    loading: Boolean,
    message: String,
    onRefresh: () -> Unit,
    onRevealLibby: () -> Unit,
    onOpenLoan: (LoanItem) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 18.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("LIBRARY", color = Color.White, fontSize = 21.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LightButton("Refresh", width = 80, onClick = onRefresh)
                LightButton("Libby", width = 68, onClick = onRevealLibby)
            }
        }

        Spacer(Modifier.height(18.dp))

        if (loading && loans.isEmpty()) {
            CircularProgressIndicator(color = Color.White)
            Spacer(Modifier.height(12.dp))
        }

        if (message.isNotBlank()) {
            Text(message, color = Color.LightGray, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            items(loans, key = { it.loanUrl }) { loan ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenLoan(loan) }
                        .padding(vertical = 15.dp),
                ) {
                    Text(loan.title, color = Color.White, fontSize = 17.sp)
                    if (loan.author.isNotBlank()) {
                        Text(loan.author, color = Color.LightGray, fontSize = 13.sp)
                    }

                    val detail = listOf(loan.progressText, loan.dueText)
                        .filter { it.isNotBlank() }
                        .joinToString("  •  ")

                    if (detail.isNotBlank()) {
                        Text(detail, color = Color.Gray, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun NativePlayer(
    state: PlayerState,
    onShelf: () -> Unit,
    onRevealLibby: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            LightButton("Shelf", width = 70, onClick = onShelf)
            LightButton("Libby", width = 70, onClick = onRevealLibby)
        }

        Spacer(Modifier.height(38.dp))

        Text(
            text = state.title.ifBlank { "Audiobook" },
            color = Color.White,
            fontSize = 23.sp,
            maxLines = 2,
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = "${formatTime(state.positionSeconds)} / ${formatTime(state.durationSeconds)}",
            color = Color.LightGray,
            fontSize = 15.sp,
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = if (state.isPlaying) "PLAYING" else "PAUSED",
            color = Color.Gray,
            fontSize = 12.sp,
        )

        Spacer(Modifier.height(42.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            LightButton("−15") { LibbyBridge.back15() }
            LightButton(if (state.isPlaying) "Pause" else "Play") {
                if (state.isPlaying) LibbyBridge.pause() else LibbyBridge.play()
            }
            LightButton("+15") { LibbyBridge.forward15() }
        }

        Spacer(Modifier.height(34.dp))

        Text("SPEED", color = Color.Gray, fontSize = 12.sp)
        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            SpeedButton("1×", state.playbackSpeed, 1.0)
            SpeedButton("1.25×", state.playbackSpeed, 1.25)
            SpeedButton("1.5×", state.playbackSpeed, 1.5)
            SpeedButton("1.75×", state.playbackSpeed, 1.75)
            SpeedButton("2×", state.playbackSpeed, 2.0)
        }

        Spacer(Modifier.height(22.dp))

        Text(
            text = "Current: ${trimSpeed(state.playbackSpeed)}×",
            color = Color.LightGray,
            fontSize = 13.sp,
        )

        Spacer(Modifier.weight(1f))

        if (!state.controlsFound) {
            Text(
                text = state.diagnostic,
                color = Color(0xFFFFB4AB),
                fontSize = 10.sp,
                maxLines = 2,
            )
        }
    }
}

@Composable
private fun SpeedButton(
    label: String,
    currentSpeed: Double,
    speed: Double,
) {
    val selected = kotlin.math.abs(currentSpeed - speed) < 0.01

    Button(
        onClick = { LibbyBridge.setSpeed(speed) },
        modifier = Modifier.width(67.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color.LightGray else Color.White,
            contentColor = Color.Black,
        ),
        contentPadding = ButtonDefaults.ContentPadding,
    ) {
        Text(label, fontSize = 10.sp, maxLines = 1)
    }
}

@Composable
private fun LightButton(
    label: String,
    width: Int = 82,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier.width(width.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = Color.Black,
        ),
        contentPadding = ButtonDefaults.ContentPadding,
    ) {
        Text(label, fontSize = 12.sp, maxLines = 1)
    }
}

private fun formatTime(seconds: Double): String {
    if (seconds <= 0 || seconds.isNaN()) return "--:--"
    val total = seconds.toLong()
    val hours = total / 3600
    val minutes = (total % 3600) / 60
    val secs = total % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, secs)
    else "%d:%02d".format(minutes, secs)
}

private fun trimSpeed(speed: Double): String =
    if (speed % 1.0 == 0.0) speed.toInt().toString() else speed.toString()
