package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.constants.AppConstants
import com.example.ui.theme.AiGlowCyan
import com.example.ui.theme.CampusNavy
import com.example.ui.theme.ElectricIndigo
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onNavigateNext: () -> Unit,
  modifier: Modifier = Modifier
) {
  val scale = remember { Animatable(0.7f) }
  val alpha = remember { Animatable(0f) }

  val infiniteTransition = rememberInfiniteTransition(label = "pulse_glow")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1.0f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(1400, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  LaunchedEffect(key1 = true) {
    scale.animateTo(
      targetValue = 1f,
      animationSpec = tween(
        durationMillis = 800,
        easing = FastOutSlowInEasing
      )
    )
    alpha.animateTo(
      targetValue = 1f,
      animationSpec = tween(
        durationMillis = 600,
        easing = LinearEasing
      )
    )
    delay(1600)
    onNavigateNext()
  }

  Box(
    modifier = modifier
      .fillMaxSize()
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(
            MaterialTheme.colorScheme.background,
            MaterialTheme.colorScheme.surface
          )
        )
      )
      .testTag("splash_screen"),
    contentAlignment = Alignment.Center
  ) {
    // Subtle background glow behind logo
    Box(
      modifier = Modifier
        .size(180.dp)
        .scale(pulseScale)
        .clip(CircleShape)
        .background(
          brush = Brush.radialGradient(
            colors = listOf(
              ElectricIndigo.copy(alpha = 0.25f),
              Color.Transparent
            )
          )
        )
    )

    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier
        .scale(scale.value)
        .alpha(alpha.value)
        .padding(24.dp)
    ) {
      // App Icon Branding Container
      Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 8.dp,
        modifier = Modifier.size(96.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Filled.School,
            contentDescription = "OneCampus AI Logo",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(52.dp)
          )
          Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = AiGlowCyan,
            modifier = Modifier
              .size(22.dp)
              .align(Alignment.TopEnd)
              .padding(top = 8.dp, end = 8.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = AppConstants.APP_NAME,
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.5.sp,
        color = MaterialTheme.colorScheme.onBackground
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "One Platform. Every College Update.",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(4.dp))

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
      ) {
        Text(
          text = "✨ AI-Powered Student Command Center",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
      }
    }
  }
}
