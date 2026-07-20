package com.stan.libbylight.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.thelightphone.lp3Keyboard.ui.CapsLockedLayout
import com.thelightphone.lp3Keyboard.ui.DefaultLp3KeyboardViewModel
import com.thelightphone.lp3Keyboard.ui.EmojiLayout
import com.thelightphone.lp3Keyboard.ui.ExtendedCharKeyboard
import com.thelightphone.lp3Keyboard.ui.KeyboardOptions
import com.thelightphone.lp3Keyboard.ui.LayoutOptions
import com.thelightphone.lp3Keyboard.ui.LowerCaseLayout
import com.thelightphone.lp3Keyboard.ui.Lp3RepeatableKeyboardCallback
import com.thelightphone.lp3Keyboard.ui.NumberLayout
import com.thelightphone.lp3Keyboard.ui.SpecialKey
import com.thelightphone.lp3Keyboard.ui.SymbolsLayout
import com.thelightphone.lp3Keyboard.ui.UpperCaseLayout
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun LightUrlInputEditor(
    value: String,
    onValueChange: (String) -> Unit,
    message: String,
    submitting: Boolean,
    onBack: () -> Unit,
    onDone: () -> Unit,
) {
    val currentValue = remember { mutableStateOf(value) }
    val callback = remember {
        UrlKeyboardCallback(
            value = { currentValue.value },
            update = {
                currentValue.value = it
                onValueChange(it)
            },
        )
    }
    val options = remember {
        MutableStateFlow(
            KeyboardOptions(
                emojis = emptyList(),
                displayReturn = false,
                displayVoice = false,
                enableKeyAnimation = true,
            ),
        )
    }
    val keyboard: DefaultLp3KeyboardViewModel = viewModel(
        key = "BardAddFeedKeyboard",
        factory = keyboardFactory(callback, options),
    )

    Column(modifier = Modifier.fillMaxSize()) {
        LightTopBar(
            leftButton = LightBarButton.LightIcon(
                icon = LightIcons.BACK,
                onClick = onBack,
                contentDescription = "Back to RSS Feeds",
            ),
            center = LightTopBarCenter.Text("Add Feed"),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 2f.gridUnitsAsDp()),
        ) {
            Spacer(Modifier.height(1f.gridUnitsAsDp()))
            BasicText(
                text = currentValue.value,
                style = LightThemeTokens.typography.heading.copy(
                    color = LightThemeTokens.colors.content,
                    textDecoration = TextDecoration.Underline,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            if (message.isNotBlank()) {
                Spacer(Modifier.height(0.5f.gridUnitsAsDp()))
                LightText(message, variant = LightTextVariant.Detail, lighten = true)
            }
        }
        LightEmbeddedLp3Keyboard(keyboard)
        LightBottomBar(
            items = listOf(
                LightBarButton.Text(
                    text = if (submitting) "ADDING…" else "DONE",
                    onClick = if (submitting) ({}) else onDone,
                ),
            ),
        )
    }
}

private class UrlKeyboardCallback(
    private val value: () -> String,
    private val update: (String) -> Unit,
) : Lp3RepeatableKeyboardCallback {
    override fun onKeyPressed(code: Int) = Unit
    override fun onKeyReleased(code: Int) = insert(code)
    override fun onKeyLongPressed(code: Int) = Unit
    override fun onKeyRepeated(code: Int) = insert(code)
    override fun onSpecialKeyPressed(key: SpecialKey) = Unit
    override fun onSpecialKeyLongPressed(key: SpecialKey) = Unit
    override fun onSpecialKeyRepeated(key: SpecialKey) {
        if (key == SpecialKey.Space) update(value() + " ")
    }
    override fun onSpecialKeyReleased(key: SpecialKey) {
        when (key) {
            SpecialKey.Backspace -> update(value().dropLast(1))
            SpecialKey.Space -> update(value() + " ")
            else -> Unit
        }
    }
    private fun insert(code: Int) = update(value() + buildString { appendCodePoint(code) })
}

private fun keyboardFactory(
    callback: Lp3RepeatableKeyboardCallback,
    options: MutableStateFlow<KeyboardOptions>,
): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        DefaultLp3KeyboardViewModel(
            delegateCallback = callback,
            keyboardOptionsFlow = options,
            optionsForLayout = { layout ->
                LayoutOptions(
                    displayCloseButton = when (layout) {
                        EmojiLayout, is ExtendedCharKeyboard -> true
                        CapsLockedLayout, LowerCaseLayout, NumberLayout,
                        SymbolsLayout, UpperCaseLayout -> false
                    },
                )
            },
        ) as T
}
