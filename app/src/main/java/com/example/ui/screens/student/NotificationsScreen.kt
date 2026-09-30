package com.example.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.app.OneCampusAppScaffold
import com.example.ui.components.app.OneCampusCard
import com.example.ui.components.app.OneCampusTopAppBar
import com.example.ui.components.states.EmptyStateView
import com.example.ui.theme.AiGlowCyan
import com.example.ui.viewmodel.NotificationViewModel

@Composable
fun NotificationsScreen(
  onNavigateToAnnouncementDetail: (String) -> Unit,
  viewModel: NotificationViewModel = viewModel(),
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

  val filterOptions = listOf("All", "Critical Alerts", "Assignments", "Placement", "Events")

  OneCampusAppScaffold(
    topBar = {
      OneCampusTopAppBar(
        title = "Notifications & Alerts",
        showBackButton = false,
        actions = {
          if (uiState.unreadCount > 0) {
            TextButton(
              onClick = { viewModel.markAllRead() },
              modifier = Modifier.testTag("notif_mark_all_read")
            ) {
              Icon(Icons.Filled.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Mark all read", style = MaterialTheme.typography.labelSmall)
            }
          }
        }
      )
    },
    modifier = modifier.testTag("notifications_screen")
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Notification Filter Chips Row
      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        items(filterOptions) { filter ->
          FilterChip(
            selected = uiState.selectedFilter == filter,
            onClick = { viewModel.setFilter(filter) },
            label = { Text(filter) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
              selectedLabelColor = MaterialTheme.colorScheme.primary
            )
          )
        }
      }

      if (uiState.notifications.isEmpty()) {
        EmptyStateView(
          title = "No Notifications",
          message = "You have no unread notifications for the selected category."
        )
      } else {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          items(uiState.notifications, key = { it.id }) { notif ->
            OneCampusCard(
              onClick = {
                viewModel.markAsRead(notif.id)
                if (notif.announcementId != null) {
                  onNavigateToAnnouncementDetail(notif.announcementId)
                }
              },
              containerColor = if (notif.isRead) {
                MaterialTheme.colorScheme.surface
              } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f)
              },
              modifier = Modifier.testTag("notif_card_${notif.id}")
            ) {
              Row(
                verticalAlignment = Alignment.Top,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(14.dp)
              ) {
                // Icon Avatar
                Surface(
                  shape = CircleShape,
                  color = when (notif.type) {
                    "DEADLINE" -> Color(0xFFEF4444).copy(alpha = 0.15f)
                    "PLACEMENT" -> MaterialTheme.colorScheme.primaryContainer
                    "AI_SUMMARY" -> AiGlowCyan.copy(alpha = 0.2f)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                  },
                  modifier = Modifier.size(40.dp)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = when (notif.type) {
                        "DEADLINE" -> Icons.Filled.Warning
                        "PLACEMENT" -> Icons.Filled.Work
                        "AI_SUMMARY" -> Icons.Filled.AutoAwesome
                        else -> Icons.Filled.Assignment
                      },
                      contentDescription = null,
                      tint = when (notif.type) {
                        "DEADLINE" -> Color(0xFFEF4444)
                        "PLACEMENT" -> MaterialTheme.colorScheme.primary
                        "AI_SUMMARY" -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                      },
                      modifier = Modifier.size(20.dp)
                    )
                  }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = notif.title,
                      style = MaterialTheme.typography.bodyMedium,
                      fontWeight = if (notif.isRead) FontWeight.SemiBold else FontWeight.ExtraBold,
                      color = MaterialTheme.colorScheme.onSurface,
                      modifier = Modifier.weight(1f)
                    )
                    if (!notif.isRead) {
                      Box(
                        modifier = Modifier
                          .size(8.dp)
                          .background(MaterialTheme.colorScheme.primary, CircleShape)
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(4.dp))

                  Text(
                    text = notif.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                  )

                  Spacer(modifier = Modifier.height(6.dp))

                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                      imageVector = Icons.Filled.AccessTime,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.outline,
                      modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = when (notif.type) {
                        "DEADLINE" -> "15 mins ago"
                        "PLACEMENT" -> "1 day ago"
                        "AI_SUMMARY" -> "Today 8:00 AM"
                        else -> "6 hours ago"
                      },
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.outline
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}
