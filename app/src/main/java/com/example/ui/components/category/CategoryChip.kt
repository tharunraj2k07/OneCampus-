package com.example.ui.components.category

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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

@Composable
fun CategoryChip(
  category: String,
  isSelected: Boolean,
  onSelected: (String) -> Unit,
  modifier: Modifier = Modifier,
  showIcon: Boolean = true,
  testTag: String = "category_chip"
) {
  val meta = CategoryConstants.getMeta(category)

  val containerColor = if (isSelected) {
    MaterialTheme.colorScheme.primary
  } else {
    MaterialTheme.colorScheme.surfaceVariant
  }

  val contentColor = if (isSelected) {
    MaterialTheme.colorScheme.onPrimary
  } else {
    MaterialTheme.colorScheme.onSurfaceVariant
  }

  Surface(
    shape = RoundedCornerShape(20.dp),
    color = containerColor,
    border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)) else null,
    modifier = modifier
      .clip(RoundedCornerShape(20.dp))
      .clickable { onSelected(category) }
      .testTag("${testTag}_${category.lowercase().replace(" ", "_")}")
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
      if (showIcon) {
        Icon(
          imageVector = meta.icon,
          contentDescription = null,
          tint = if (isSelected) contentColor else meta.accentColor,
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
      }
      Text(
        text = meta.label,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = contentColor,
        fontSize = 13.sp
      )
    }
  }
}
