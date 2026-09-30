package com.example.ui.components.priority

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.PriorityLevel

@Composable
fun PriorityScoreDisplay(
  priority: PriorityLevel,
  score: Double,
  reason: String = "",
  explanations: List<String> = emptyList(),
  modifier: Modifier = Modifier,
  testTag: String = "priority_score_display"
) {
  val isDark = isSystemInDarkTheme()
  val (contentColor, containerColor) = getPriorityColors(priority, isDark)
  // Normalized 0 to 100
  val normalizedScore = if (score <= 1.0) (score * 100).toInt() else score.toInt().coerceIn(0, 100)
  val progressFraction = (normalizedScore / 100f).coerceIn(0f, 1f)

  Column(
    modifier = modifier
      .fillMaxWidth()
      .background(containerColor, shape = RoundedCornerShape(14.dp))
      .border(1.dp, contentColor.copy(alpha = 0.35f), shape = RoundedCornerShape(14.dp))
      .padding(14.dp)
      .testTag(testTag)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Filled.Bolt,
          contentDescription = null,
          tint = contentColor,
          modifier = Modifier.padding(end = 6.dp)
        )
        Column {
          Text(
            text = "Hybrid Priority: $normalizedScore/100",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor
          )
          Text(
            text = "AI Urgency + Rules Engine",
            style = MaterialTheme.typography.labelSmall,
            color = contentColor.copy(alpha = 0.8f),
            fontSize = 11.sp
          )
        }
      }
      PriorityBadge(priority = priority)
    }

    Spacer(modifier = Modifier.height(10.dp))

    LinearProgressIndicator(
      progress = { progressFraction },
      modifier = Modifier
        .fillMaxWidth()
        .height(6.dp)
        .clip(RoundedCornerShape(3.dp)),
      color = contentColor,
      trackColor = contentColor.copy(alpha = 0.2f)
    )

    val displayReasons = if (explanations.isNotEmpty()) {
      explanations
    } else if (reason.isNotBlank()) {
      listOf(reason)
    } else {
      emptyList()
    }

    if (displayReasons.isNotEmpty()) {
      Spacer(modifier = Modifier.height(10.dp))
      Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        displayReasons.forEach { item ->
          Row(
            verticalAlignment = Alignment.Top,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "•",
              color = contentColor,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(end = 6.dp)
            )
            Text(
              text = item,
              style = MaterialTheme.typography.bodySmall,
              color = contentColor.copy(alpha = 0.9f),
              fontSize = 12.sp,
              lineHeight = 16.sp
            )
          }
        }
      }
    }
  }
}
