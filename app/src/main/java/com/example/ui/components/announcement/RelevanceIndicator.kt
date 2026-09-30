package com.example.ui.components.announcement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.theme.AiGlowCyan
import com.example.ui.theme.SuccessGreen

@Composable
fun RelevanceIndicator(
  relevanceText: String = "98% Match",
  modifier: Modifier = Modifier,
  testTag: String = "relevance_indicator"
) {
  Box(
    modifier = modifier
      .background(AiGlowCyan.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp))
      .padding(horizontal = 6.dp, vertical = 2.dp)
      .testTag(testTag)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        imageVector = Icons.Filled.AutoAwesome,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(11.dp)
      )
      Spacer(modifier = Modifier.width(3.dp))
      Text(
        text = relevanceText,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp
      )
    }
  }
}

@Composable
fun BookmarkButton(
  isBookmarked: Boolean,
  onToggle: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = "bookmark_button"
) {
  IconButton(
    onClick = onToggle,
    modifier = modifier.testTag(testTag)
  ) {
    Icon(
      imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
      contentDescription = if (isBookmarked) "Remove bookmark" else "Bookmark announcement",
      tint = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}

@Composable
fun CompletionButton(
  isCompleted: Boolean,
  onToggle: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = "completion_button"
) {
  IconButton(
    onClick = onToggle,
    modifier = modifier.testTag(testTag)
  ) {
    Icon(
      imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircleOutline,
      contentDescription = if (isCompleted) "Mark incomplete" else "Mark complete",
      tint = if (isCompleted) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
    )
  }
}
