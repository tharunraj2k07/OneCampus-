package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.navigation.AppNavGraph
import com.example.ui.theme.OneCampusTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val announcementId = intent?.getStringExtra("announcement_id")
    val taskId = intent?.getStringExtra("task_id")

    setContent {
      OneCampusTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          AppNavGraph(
            initialAnnouncementId = announcementId,
            initialTaskId = taskId
          )
        }
      }
    }
  }
}


