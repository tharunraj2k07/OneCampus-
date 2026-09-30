package com.example.ui.components.personalization

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.RelevanceInfo

@Composable
fun RelevanceBadge(
  score: Int,
  level: String,
  modifier: Modifier = Modifier
) {
  val (badgeColor, textColor, labelIcon, labelText) = when (level.uppercase()) {
    "VERY_HIGH" -> Quad(
      Color(0xFF7C3AED).copy(alpha = 0.12f),
      Color(0xFF7C3AED),
      Icons.Default.Stars,
      "$score% • Top Match"
    )
    "HIGH" -> Quad(
      Color(0xFF0284C7).copy(alpha = 0.12f),
      Color(0xFF0284C7),
      Icons.Default.TrackChanges,
      "$score% • High Match"
    )
    "MEDIUM" -> Quad(
      Color(0xFF059669).copy(alpha = 0.10f),
      Color(0xFF059669),
      Icons.Default.CheckCircle,
      "$score% • Relevant"
    )
    else -> Quad(
      MaterialTheme.colorScheme.surfaceVariant,
      MaterialTheme.colorScheme.onSurfaceVariant,
      Icons.Default.CheckCircle,
      "$score% • General"
    )
  }

  Row(
    modifier = modifier
      .testTag("relevance_badge")
      .clip(RoundedCornerShape(8.dp))
      .background(badgeColor)
      .border(1.dp, textColor.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
      .padding(horizontal = 8.dp, vertical = 4.dp),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(4.dp)
  ) {
    Icon(
      imageVector = labelIcon,
      contentDescription = "Relevance Indicator",
      tint = textColor,
      modifier = Modifier.size(13.dp)
    )
    Text(
      text = labelText,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = textColor
    )
  }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun PersonalizedExplanationCard(
  relevance: RelevanceInfo,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .testTag("personalized_explanation_card"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = Color(0xFF7C3AED).copy(alpha = 0.05f)
    ),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF7C3AED).copy(alpha = 0.3f))
    )
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(Color(0xFF7C3AED).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.TrackChanges,
              contentDescription = "Personalization Match",
              tint = Color(0xFF7C3AED),
              modifier = Modifier.size(18.dp)
            )
          }

          Column {
            Text(
              text = "WHY THIS MATTERS TO YOU",
              fontSize = 12.sp,
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = 0.8.sp,
              color = Color(0xFF7C3AED)
            )
            Text(
              text = "AI Personalization Match",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        RelevanceBadge(score = relevance.score, level = relevance.level)
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Progress bar visualizing 0-100 relevance
      Column {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Personal Match Score",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "${relevance.score} / 100",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF7C3AED)
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
          progress = { relevance.score.toFloat() / 100f },
          modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = Color(0xFF7C3AED),
          trackColor = Color(0xFF7C3AED).copy(alpha = 0.15f)
        )
      }

      if (relevance.explanation.isNotEmpty()) {
        Spacer(modifier = Modifier.height(12.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          relevance.explanation.forEach { item ->
            Row(
              verticalAlignment = Alignment.Top,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF059669),
                modifier = Modifier
                  .size(16.dp)
                  .padding(top = 1.dp)
              )
              Text(
                text = item,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
        }
      }
    }
  }
}
