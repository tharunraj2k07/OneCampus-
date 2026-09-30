package com.example.ui.components.priority

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.PriorityLevel

@Composable
fun PriorityIndicator(
  priority: PriorityLevel,
  modifier: Modifier = Modifier,
  size: Dp = 8.dp,
  showLabel: Boolean = false,
  testTag: String = "priority_indicator"
) {
  val isDark = isSystemInDarkTheme()
  val (color, _) = getPriorityColors(priority, isDark)

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier.testTag(testTag)
  ) {
    Box(
      modifier = Modifier
        .size(size)
        .background(color, shape = CircleShape)
    )
    if (showLabel) {
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = priority.label,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = color,
        fontSize = 11.sp
      )
    }
  }
}
