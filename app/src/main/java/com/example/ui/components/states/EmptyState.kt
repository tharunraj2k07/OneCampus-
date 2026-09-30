package com.example.ui.components.states

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.app.ButtonVariant
import com.example.ui.components.app.OneCampusButton

@Composable
fun EmptyState(
  title: String,
  description: String,
  modifier: Modifier = Modifier,
  icon: ImageVector = Icons.Outlined.Inbox,
  actionButtonText: String? = null,
  onActionClick: (() -> Unit)? = null,
  testTag: String = "empty_state_view"
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(32.dp)
      .testTag(testTag),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
      modifier = Modifier.size(64.dp)
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
      text = title,
      style = MaterialTheme.typography.titleMedium,
      fontWeight = FontWeight.Bold,
      color = MaterialTheme.colorScheme.onSurface,
      textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = description,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      textAlign = TextAlign.Center
    )

    if (actionButtonText != null && onActionClick != null) {
      Spacer(modifier = Modifier.height(24.dp))
      OneCampusButton(
        text = actionButtonText,
        onClick = onActionClick,
        variant = ButtonVariant.PRIMARY,
        testTag = "${testTag}_button"
      )
    }
  }
}

@Composable
fun EmptyStateView(
  title: String,
  message: String,
  modifier: Modifier = Modifier,
  icon: ImageVector = Icons.Outlined.Inbox,
  actionText: String? = null,
  onActionClick: (() -> Unit)? = null
) {
  EmptyState(
    title = title,
    description = message,
    modifier = modifier,
    icon = icon,
    actionButtonText = actionText,
    onActionClick = onActionClick
  )
}
