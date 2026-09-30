package com.example.ui.components.app

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun OneCampusCard(
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
  shape: Shape = RoundedCornerShape(16.dp),
  containerColor: Color = MaterialTheme.colorScheme.surface,
  contentColor: Color = MaterialTheme.colorScheme.onSurface,
  elevation: Dp = 1.dp,
  border: BorderStroke? = null,
  accentColor: Color? = null,
  accentWidth: Dp = 4.dp,
  testTag: String = "one_campus_card",
  content: @Composable () -> Unit
) {
  val cardModifier = modifier
    .fillMaxWidth()
    .testTag(testTag)
    .then(
      if (onClick != null) {
        Modifier.clickable(onClick = onClick)
      } else {
        Modifier
      }
    )

  Card(
    shape = shape,
    colors = CardDefaults.cardColors(
      containerColor = containerColor,
      contentColor = contentColor
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = elevation),
    border = border ?: BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
    modifier = cardModifier
  ) {
    if (accentColor != null) {
      Box {
        Surface(
          color = accentColor,
          modifier = Modifier
            .matchParentSize()
            .clip(shape)
        ) {}
        Surface(
          color = containerColor,
          shape = shape,
          modifier = Modifier
            .fillMaxWidth()
            .padding(start = accentWidth)
        ) {
          content()
        }
      }
    } else {
      content()
    }
  }
}
