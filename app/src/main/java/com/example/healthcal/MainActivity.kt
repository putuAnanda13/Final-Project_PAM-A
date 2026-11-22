package com.example.healthcal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.healthcal.ui.theme.HealthCalTheme
import android.content.Intent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.healthcal.data.supabase
import com.example.healthcal.ui.AppRoot
import io.github.jan.supabase.auth.handleDeeplinks

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supabase.handleDeeplinks(intent)
        setContent {
            HealthCalTheme {
                MaterialTheme {
                    Surface { AppRoot() }
                }
            }
        }
    }
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        supabase.handleDeeplinks(intent)
    }
}


