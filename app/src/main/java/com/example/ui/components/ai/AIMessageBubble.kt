package com.example.ui.components.ai

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.example.ui.theme.BrandBlueLight
import com.example.ui.theme.BrandBluePrimary

@Composable
fun AIMessageBubble(
  text: String,
  modifier: Modifier = Modifier,
  isLoading: Boolean = false,
  testTag: String = "ai_message_bubble"
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp)
      .testTag(testTag),
    horizontalArrangement = Arrangement.Start,
    verticalAlignment = Alignment.Top
  ) {
    AIAvatar(modifier = Modifier.padding(top = 2.dp))

    Spacer(modifier = Modifier.width(10.dp))

    Surface(
      shape = RoundedCornerShape(
        topStart = 4.dp,
        topEnd = 16.dp,
        bottomStart = 16.dp,
        bottomEnd = 16.dp
      ),
      color = MaterialTheme.colorScheme.surfaceVariant,
      border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
      modifier = Modifier.weight(1f, fill = false)
    ) {
      Box(modifier = Modifier.padding(14.dp)) {
        if (isLoading) {
          AIThinkingIndicator()
        } else {
          Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 20.sp
          )
        }
      }
    }
  }
}

@Composable
fun StudentMessageBubble(
  text: String,
  modifier: Modifier = Modifier,
  testTag: String = "student_message_bubble"
) {
  Row(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 6.dp)
      .testTag(testTag),
    horizontalArrangement = Arrangement.End,
    verticalAlignment = Alignment.Top
  ) {
    Surface(
      shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 4.dp,
        bottomStart = 16.dp,
        bottomEnd = 16.dp
      ),
      color = MaterialTheme.colorScheme.primary,
      modifier = Modifier.weight(1f, fill = false)
    ) {
      Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onPrimary,
        lineHeight = 20.sp,
        modifier = Modifier.padding(14.dp)
      )
    }
  }
}

@Composable
fun AISuggestedQuestionChip(
  question: String,
  onClick: (String) -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = "ai_suggested_chip"
) {
  Surface(
    shape = RoundedCornerShape(20.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
    modifier = modifier
      .clip(RoundedCornerShape(20.dp))
      .clickable { onClick(question) }
      .testTag(testTag)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
      Text(
        text = question,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.primary,
        fontSize = 12.sp
      )
    }
  }
}
