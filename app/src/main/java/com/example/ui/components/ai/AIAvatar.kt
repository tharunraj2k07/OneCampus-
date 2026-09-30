package com.example.ui.components.ai

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.AiGlowCyan
import com.example.ui.theme.AiGlowPurple
import com.example.ui.theme.BrandBluePrimary

@Composable
fun AIAvatar(
  modifier: Modifier = Modifier,
  size: Dp = 36.dp,
  iconSize: Dp = 18.dp,
  testTag: String = "ai_avatar"
) {
  Box(
    contentAlignment = Alignment.Center,
    modifier = modifier
      .size(size)
      .background(
        brush = Brush.linearGradient(
          colors = listOf(BrandBluePrimary, AiGlowPurple, AiGlowCyan)
        ),
        shape = CircleShape
      )
      .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
      .testTag(testTag)
  ) {
    Icon(
      imageVector = Icons.Filled.AutoAwesome,
      contentDescription = "AI Avatar",
      tint = Color.White,
      modifier = Modifier.size(iconSize)
    )
  }
}

@Composable
fun AIThinkingIndicator(
  modifier: Modifier = Modifier,
  testTag: String = "ai_thinking_indicator"
) {
  val transition = rememberInfiniteTransition(label = "dot_pulse")

  val scale1 by transition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, delayMillis = 0, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "dot1"
  )
  val scale2 by transition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, delayMillis = 200, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "dot2"
  )
  val scale3 by transition.animateFloat(
    initialValue = 0.4f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(
      animation = tween(600, delayMillis = 400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "dot3"
  )

  Row(
    horizontalArrangement = Arrangement.spacedBy(4.dp),
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier.testTag(testTag)
  ) {
    Box(
      modifier = Modifier
        .size(8.dp)
        .scale(scale1)
        .background(MaterialTheme.colorScheme.primary, CircleShape)
    )
    Box(
      modifier = Modifier
        .size(8.dp)
        .scale(scale2)
        .background(AiGlowPurple, CircleShape)
    )
    Box(
      modifier = Modifier
        .size(8.dp)
        .scale(scale3)
        .background(AiGlowCyan, CircleShape)
    )
  }
}
