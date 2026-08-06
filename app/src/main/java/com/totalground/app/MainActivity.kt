package com.totalground.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.totalground.app.ui.navigation.NavGraph
import com.totalground.app.ui.theme.TotalGroundTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TotalGroundTheme {
                NavGraph()
            }
        }
    }
}
