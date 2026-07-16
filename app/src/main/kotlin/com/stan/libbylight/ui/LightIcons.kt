package com.stan.libbylight.ui

import com.stan.libbylight.R

/**
 * Trimmed down from the full light-sdk LightIcons enum (which has ~150+
 * entries) to only what this app uses. Same structure/naming as the
 * original so it's a drop-in superset if more icons are added later --
 * just copy more drawables into res/drawable and add more objects here.
 */
sealed class LightIconConfiguration(
    val name: String,
    val darkModeResource: Int,
    val lightModeResource: Int,
)

object LightIcons {
    object PLAY : LightIconConfiguration(
        name = "play",
        darkModeResource = R.drawable.ic_play_white,
        lightModeResource = R.drawable.ic_play_black
    )
    object PAUSE : LightIconConfiguration(
        name = "pause",
        darkModeResource = R.drawable.ic_pause_white,
        lightModeResource = R.drawable.ic_pause_black
    )
    object REWIND : LightIconConfiguration(
        name = "rewind",
        darkModeResource = R.drawable.ic_rewind_white,
        lightModeResource = R.drawable.ic_rewind_black
    )
    object FAST_FORWARD : LightIconConfiguration(
        name = "fast-forward",
        darkModeResource = R.drawable.ic_fast_forward_white,
        lightModeResource = R.drawable.ic_fast_forward_black
    )
    object SKIP_BACKWARD_FIFTEEN : LightIconConfiguration(
        name = "skip-backward-fifteen",
        darkModeResource = R.drawable.ic_skip_backward_fifteen_white,
        lightModeResource = R.drawable.ic_skip_backward_fifteen_white
    )
    object SKIP_FORWARD_FIFTEEN : LightIconConfiguration(
        name = "skip-forward-fifteen",
        darkModeResource = R.drawable.ic_skip_forward_fifteen_white,
        lightModeResource = R.drawable.ic_skip_forward_fifteen_white
    )
    object BACK : LightIconConfiguration(
        name = "back",
        darkModeResource = R.drawable.ic_back_white,
        lightModeResource = R.drawable.ic_back_black
    )
    object ADD : LightIconConfiguration(
        name = "add",
        darkModeResource = R.drawable.ic_add_white,
        lightModeResource = R.drawable.ic_add_black
    )
    object CLOSE : LightIconConfiguration(
        name = "close",
        darkModeResource = R.drawable.ic_close_white,
        lightModeResource = R.drawable.ic_close_black
    )
    object SETTINGS : LightIconConfiguration(
        name = "settings",
        darkModeResource = R.drawable.ic_settings_white,
        lightModeResource = R.drawable.ic_settings_white
    )
    /** Invisible spacer -- used by LightTopBar/LightBarButton to keep
     *  layout balanced when a button slot is empty. */
    object SPACER : LightIconConfiguration(
        name = "spacer",
        darkModeResource = R.drawable.ic_spacer,
        lightModeResource = R.drawable.ic_spacer
    )
}
