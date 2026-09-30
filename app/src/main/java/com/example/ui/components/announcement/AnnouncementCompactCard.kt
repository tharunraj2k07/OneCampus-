package com.example.ui.components.announcement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.core.model.Announcement
import com.example.ui.components.app.OneCampusCard
import com.example.ui.components.category.CategoryBadge
import com.example.ui.components.category.CategoryConstants
import com.example.ui.components.priority.PriorityIndicator

@Composable
fun AnnouncementCompactCard(
  announcement: Announcement,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = "announcement_compact_card"
) {
  val meta = CategoryConstants.getMeta(announcement.category)

  OneCampusCard(
    onClick = onClick,
    accentColor = meta.accentColor,
    modifier = modifier.testTag("${testTag}_${announcement.id}")
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp)
    ) {
      Column(
        modifier = Modifier.weight(1f)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          CategoryBadge(category = announcement.category, showIcon = false)
          PriorityIndicator(priority = announcement.priority, showLabel = true)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = announcement.title,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        if (announcement.deadlineEpochMs != null) {
          Spacer(modifier = Modifier.height(4.dp))
          TimeRemainingIndicator(deadlineEpochMs = announcement.deadlineEpochMs)
        }
      }
    }
  }
}
