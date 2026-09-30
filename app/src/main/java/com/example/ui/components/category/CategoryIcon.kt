package com.example.ui.components.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun CategoryIcon(
  category: String,
  modifier: Modifier = Modifier,
  size: Dp = 36.dp,
  iconSize: Dp = 20.dp,
  customColor: Color? = null
) {
  val meta = CategoryConstants.getMeta(category)
  val color = customColor ?: meta.accentColor

  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
      .size(size)
      .clip(RoundedCornerShape(10.dp))
      .background(color.copy(alpha = 0.12f))
      .padding(4.dp)
  ) {
    Icon(
      imageVector = meta.icon,
      contentDescription = meta.label,
      tint = color,
      modifier = Modifier.size(iconSize)
    )
  }
}
