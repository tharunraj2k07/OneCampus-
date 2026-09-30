package com.example.ui.screens.faculty

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.data.DemoRepository
import com.example.ui.components.announcement.DeadlineIndicator
import com.example.ui.components.app.ButtonVariant
import com.example.ui.components.app.OneCampusAppScaffold
import com.example.ui.components.app.OneCampusButton
import com.example.ui.components.app.OneCampusCard
import com.example.ui.components.app.OneCampusTopAppBar
import com.example.ui.components.category.CategoryBadge
import com.example.ui.components.priority.PriorityBadge
import com.example.ui.components.priority.PriorityScoreDisplay

@Composable
fun AnnouncementPreviewScreen(
  announcementId: String,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val announcements by DemoRepository.announcements.collectAsState()
  val announcement = announcements.find { it.id == announcementId } ?: announcements.firstOrNull()

  OneCampusAppScaffold(
    topBar = {
      OneCampusTopAppBar(
        title = "Student View Preview",
        showBackButton = true,
        onBackClick = onNavigateBack
      )
    },
    modifier = modifier.testTag("announcement_preview_screen")
  ) {
    if (announcement != null) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          CategoryBadge(category = announcement.category)
          PriorityBadge(priority = announcement.priority)
        }

        Text(
          text = announcement.title,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )

        Text(
          text = "Published by ${announcement.publisherName} (${announcement.publisherRole.name.replace('_', ' ')})",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        PriorityScoreDisplay(
          priority = announcement.priority,
          score = 0.88,
          reason = announcement.priorityReason ?: "High impact circular for targeted department"
        )

        OneCampusCard {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "🤖 AI Summary for Students",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = announcement.aiSummary,
              style = MaterialTheme.typography.bodyMedium,
              lineHeight = 20.sp
            )
          }
        }

        Text(
          text = "Full Circular Text",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )

        Text(
          text = announcement.rawContent,
          style = MaterialTheme.typography.bodyMedium,
          lineHeight = 22.sp,
          color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(10.dp))

        OneCampusButton(
          text = "Return to Dashboard",
          onClick = onNavigateBack,
          fullWidth = true,
          variant = ButtonVariant.PRIMARY
        )
      }
    }
  }
}
