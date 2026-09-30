package com.example.ui.components.priority

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.core.model.PriorityLevel
import com.example.ui.theme.PriorityCriticalBg
import com.example.ui.theme.PriorityCriticalRed
import com.example.ui.theme.PriorityHighBg
import com.example.ui.theme.PriorityHighOrange
import com.example.ui.theme.PriorityLowBg
import com.example.ui.theme.PriorityLowSlate
import com.example.ui.theme.PriorityMediumBg
import com.example.ui.theme.PriorityMediumBlue

@Composable
fun PriorityBadge(
  priority: PriorityLevel,
  modifier: Modifier = Modifier,
  showDot: Boolean = true,
  testTag: String = "priority_badge"
) {
  val isDark = isSystemInDarkTheme()
  val (contentColor, containerColor) = getPriorityColors(priority, isDark)

  Box(
    modifier = modifier
      .background(containerColor, shape = RoundedCornerShape(6.dp))
      .border(0.5.dp, contentColor.copy(alpha = 0.3f), shape = RoundedCornerShape(6.dp))
      .padding(horizontal = 8.dp, vertical = 3.dp)
      .testTag("${testTag}_${priority.name.lowercase()}")
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (showDot) {
        Box(
          modifier = Modifier
            .size(6.dp)
            .background(contentColor, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(5.dp))
      }
      Text(
        text = priority.label,
        color = contentColor,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 0.5.sp
      )
    }
  }
}

fun getPriorityColors(priority: PriorityLevel, isDark: Boolean): Pair<Color, Color> {
  return when (priority) {
    PriorityLevel.CRITICAL -> {
      val text = PriorityCriticalRed
      val bg = if (isDark) PriorityCriticalRed.copy(alpha = 0.2f) else PriorityCriticalBg
      Pair(text, bg)
    }
    PriorityLevel.HIGH -> {
      val text = PriorityHighOrange
      val bg = if (isDark) PriorityHighOrange.copy(alpha = 0.2f) else PriorityHighBg
      Pair(text, bg)
    }
    PriorityLevel.MEDIUM -> {
      val text = PriorityMediumBlue
      val bg = if (isDark) PriorityMediumBlue.copy(alpha = 0.2f) else PriorityMediumBg
      Pair(text, bg)
    }
    PriorityLevel.LOW -> {
      val text = PriorityLowSlate
      val bg = if (isDark) PriorityLowSlate.copy(alpha = 0.2f) else PriorityLowBg
      Pair(text, bg)
    }
  }
}
