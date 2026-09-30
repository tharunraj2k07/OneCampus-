package com.example.ui.components.announcement

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Announcement
import com.example.ui.components.app.ButtonVariant
import com.example.ui.components.app.OneCampusButton
import com.example.ui.components.app.OneCampusCard
import com.example.ui.components.category.CategoryBadge
import com.example.ui.components.category.CategoryConstants
import com.example.ui.components.personalization.RelevanceBadge
import com.example.ui.components.priority.PriorityBadge

@Composable
fun AnnouncementCard(
  announcement: Announcement,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  onBookmarkToggle: (() -> Unit)? = null,
  onActionClick: (() -> Unit)? = null,
  isBookmarked: Boolean = announcement.isBookmarked,
  testTag: String = "announcement_card"
) {
  val meta = remember(announcement.category) { CategoryConstants.getMeta(announcement.category) }

  OneCampusCard(
    onClick = onClick,
    accentColor = meta.accentColor,
    modifier = modifier.testTag("${testTag}_${announcement.id}")
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      // Top Meta Row: Category + Priority + Score + Bookmark
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          CategoryBadge(category = announcement.category)
          PriorityBadge(priority = announcement.priority)

          if (announcement.relevance != null) {
            RelevanceBadge(score = announcement.relevance.score, level = announcement.relevance.level)
          } else {
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                if (announcement.aiAnalysisStatus == "COMPLETED") {
                  Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = "AI Analyzed",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(11.dp)
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                }
                Text(
                  text = "${announcement.priorityScore}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.Bold,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }

        if (onBookmarkToggle != null) {
          BookmarkButton(
            isBookmarked = isBookmarked,
            onToggle = onBookmarkToggle,
            modifier = Modifier.testTag("card_bookmark_${announcement.id}")
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Title
      Text(
        text = announcement.title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(6.dp))

      // AI Summary
      Text(
        text = announcement.aiSummary.ifBlank { announcement.rawContent },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Deadlines & Relevance Row
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        if (announcement.deadlineEpochMs != null) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            DeadlineIndicator(deadlineEpochMs = announcement.deadlineEpochMs)
            TimeRemainingIndicator(deadlineEpochMs = announcement.deadlineEpochMs)
          }
        } else {
          Text(
            text = "By ${announcement.publisherName}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        RelevanceIndicator(relevanceText = "High Match")
      }

      // Action Button (if required action is specified)
      if (!announcement.requiredAction.isNullOrBlank() && onActionClick != null) {
        Spacer(modifier = Modifier.height(12.dp))
        OneCampusButton(
          text = announcement.requiredAction,
          onClick = onActionClick,
          variant = ButtonVariant.PRIMARY,
          fullWidth = true,
          testTag = "action_btn_${announcement.id}"
        )
      }
    }
  }
}
