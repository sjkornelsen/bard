package com.stan.libbylight

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.stan.libbylight.player.LocalPlaybackController
import com.stan.libbylight.library.AudiobookProgressStore
import com.stan.libbylight.screens.PlayerDebugScreen

/**
 * Step 2: Libby's normal UI remains fully usable.
 * The temporary native player panel appears only on /open/loan/ pages.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PlayerDebugScreen()
        }
    }

    override fun onStop() {
        LocalPlaybackController.persistProgress()
        AudiobookProgressStore.flushLatest()
        super.onStop()
    }

    override fun onDestroy() {
        LocalPlaybackController.persistProgress()
        AudiobookProgressStore.flushLatest()
        super.onDestroy()
    }
}
