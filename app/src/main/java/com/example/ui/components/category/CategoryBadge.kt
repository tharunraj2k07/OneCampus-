package com.example.ui.components.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CategoryBadge(
  category: String,
  modifier: Modifier = Modifier,
  showIcon: Boolean = true,
  testTag: String = "category_badge"
) {
  val meta = CategoryConstants.getMeta(category)

  Box(
    modifier = modifier
      .background(meta.accentColor.copy(alpha = 0.12f), shape = RoundedCornerShape(6.dp))
      .padding(horizontal = 8.dp, vertical = 3.dp)
      .testTag("${testTag}_${category.lowercase().replace(" ", "_")}")
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (showIcon) {
        Icon(
          imageVector = meta.icon,
          contentDescription = null,
          tint = meta.accentColor,
          modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
      }
      Text(
        text = meta.label,
        color = meta.accentColor,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp
      )
    }
  }
}
