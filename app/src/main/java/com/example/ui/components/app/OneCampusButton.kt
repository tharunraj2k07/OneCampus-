package com.example.ui.components.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.PriorityCriticalRed

enum class ButtonVariant {
  PRIMARY,
  SECONDARY,
  OUTLINE,
  TEXT,
  DESTRUCTIVE
}

@Composable
fun OneCampusButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  variant: ButtonVariant = ButtonVariant.PRIMARY,
  icon: ImageVector? = null,
  isLoading: Boolean = false,
  enabled: Boolean = true,
  fullWidth: Boolean = false,
  testTag: String = "one_campus_button"
) {
  val buttonModifier = modifier
    .then(if (fullWidth) Modifier.fillMaxWidth() else Modifier)
    .defaultMinSize(minHeight = 48.dp)
    .testTag(testTag)

  val shape = RoundedCornerShape(12.dp)

  when (variant) {
    ButtonVariant.PRIMARY -> {
      Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = buttonModifier
      ) {
        ButtonContent(text = text, icon = icon, isLoading = isLoading, contentColor = MaterialTheme.colorScheme.onPrimary)
      }
    }
    ButtonVariant.SECONDARY -> {
      Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant,
          contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = buttonModifier
      ) {
        ButtonContent(text = text, icon = icon, isLoading = isLoading, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }
    ButtonVariant.OUTLINE -> {
      OutlinedButton(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = shape,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.takeIf { enabled } ?: Color.Gray.copy(alpha = 0.3f)),
        colors = ButtonDefaults.outlinedButtonColors(
          contentColor = MaterialTheme.colorScheme.primary
        ),
        modifier = buttonModifier
      ) {
        ButtonContent(text = text, icon = icon, isLoading = isLoading, contentColor = MaterialTheme.colorScheme.primary)
      }
    }
    ButtonVariant.TEXT -> {
      TextButton(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = shape,
        colors = ButtonDefaults.textButtonColors(
          contentColor = MaterialTheme.colorScheme.primary
        ),
        modifier = buttonModifier
      ) {
        ButtonContent(text = text, icon = icon, isLoading = isLoading, contentColor = MaterialTheme.colorScheme.primary)
      }
    }
    ButtonVariant.DESTRUCTIVE -> {
      Button(
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
          containerColor = PriorityCriticalRed,
          contentColor = Color.White
        ),
        modifier = buttonModifier
      ) {
        ButtonContent(text = text, icon = icon, isLoading = isLoading, contentColor = Color.White)
      }
    }
  }
}

@Composable
private fun ButtonContent(
  text: String,
  icon: ImageVector?,
  isLoading: Boolean,
  contentColor: Color
) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center,
    modifier = Modifier.padding(horizontal = 4.dp)
  ) {
    if (isLoading) {
      CircularProgressIndicator(
        modifier = Modifier.size(18.dp),
        strokeWidth = 2.dp,
        color = contentColor
      )
      Spacer(modifier = Modifier.width(8.dp))
    } else if (icon != null) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        modifier = Modifier.size(18.dp),
        tint = contentColor
      )
      Spacer(modifier = Modifier.width(8.dp))
    }
    Text(
      text = text,
      style = MaterialTheme.typography.labelLarge,
      fontWeight = FontWeight.SemiBold,
      color = contentColor
    )
  }
}
