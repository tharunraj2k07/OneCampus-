package com.example.ui.components.announcement

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PriorityCriticalRed
import com.example.ui.theme.PriorityHighOrange
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DeadlineIndicator(
  deadlineEpochMs: Long?,
  modifier: Modifier = Modifier,
  showIcon: Boolean = true,
  testTag: String = "deadline_indicator"
) {
  if (deadlineEpochMs == null) return

  val dateFormat = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
  val formattedDate = dateFormat.format(Date(deadlineEpochMs))

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier.testTag(testTag)
  ) {
    if (showIcon) {
      Icon(
        imageVector = Icons.Outlined.Event,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(14.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
    }
    Text(
      text = "Due $formattedDate",
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      fontSize = 12.sp
    )
  }
}

@Composable
fun TimeRemainingIndicator(
  deadlineEpochMs: Long?,
  modifier: Modifier = Modifier,
  testTag: String = "time_remaining_indicator"
) {
  if (deadlineEpochMs == null) return

  val now = System.currentTimeMillis()
  val diff = deadlineEpochMs - now

  val (text, color) = when {
    diff < 0 -> Pair("Expired", PriorityCriticalRed)
    diff < 3600_000 * 5 -> Pair("${(diff / 3600_000).coerceAtLeast(1)}h left", PriorityCriticalRed)
    diff < 3600_000 * 24 -> Pair("${diff / 3600_000}h left", PriorityHighOrange)
    diff < 3600_000 * 24 * 3 -> Pair("${diff / (3600_000 * 24)}d left", PriorityHighOrange)
    else -> Pair("${diff / (3600_000 * 24)}d left", MaterialTheme.colorScheme.primary)
  }

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier.testTag(testTag)
  ) {
    Icon(
      imageVector = Icons.Outlined.AccessTime,
      contentDescription = null,
      tint = color,
      modifier = Modifier.size(13.dp)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = text,
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.Bold,
      color = color,
      fontSize = 11.sp
    )
  }
}
