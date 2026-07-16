package com.stan.libbylight

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
}
