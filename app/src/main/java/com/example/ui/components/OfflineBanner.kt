package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.sync.NetworkConnectivityManager
import com.example.core.sync.SyncManager
import kotlinx.coroutines.launch

@Composable
fun OfflineBanner(
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val connectivityManager = NetworkConnectivityManager.getInstance(context)
  val isOnline by connectivityManager.isOnline.collectAsState()
  val scope = rememberCoroutineScope()

  AnimatedVisibility(
    visible = !isOnline,
    enter = expandVertically(),
    exit = shrinkVertically()
  ) {
    Row(
      modifier = modifier
        .fillMaxWidth()
        .background(MaterialTheme.colorScheme.errorContainer)
        .padding(horizontal = 16.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f)
      ) {
        Icon(
          imageVector = Icons.Default.CloudOff,
          contentDescription = "Offline indicator",
          tint = MaterialTheme.colorScheme.onErrorContainer,
          modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Offline — Showing cached data",
          style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onErrorContainer
          )
        )
      }

      IconButton(
        onClick = {
          scope.launch {
            SyncManager.getInstance(context).syncNow()
          }
        },
        modifier = Modifier.size(32.dp)
      ) {
        Icon(
          imageVector = Icons.Default.Refresh,
          contentDescription = "Retry sync",
          tint = MaterialTheme.colorScheme.onErrorContainer,
          modifier = Modifier.size(18.dp)
        )
      }
    }
  }
}
