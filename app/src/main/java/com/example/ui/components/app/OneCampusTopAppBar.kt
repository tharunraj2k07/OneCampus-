package com.example.ui.components.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OneCampusTopAppBar(
  title: String,
  modifier: Modifier = Modifier,
  subtitle: String? = null,
  showBackButton: Boolean = false,
  onBackClick: (() -> Unit)? = null,
  actions: @Composable RowScope.() -> Unit = {},
  elevation: Dp = 0.dp,
  backgroundColor: Color = MaterialTheme.colorScheme.background,
  contentColor: Color = MaterialTheme.colorScheme.onBackground,
  testTag: String = "one_campus_top_bar"
) {
  Surface(
    color = backgroundColor,
    contentColor = contentColor,
    shadowElevation = elevation,
    modifier = modifier
      .fillMaxWidth()
      .testTag(testTag)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .padding(horizontal = 4.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      if (showBackButton && onBackClick != null) {
        IconButton(
          onClick = onBackClick,
          modifier = Modifier
            .size(48.dp)
            .testTag("top_bar_back_button")
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Back",
            tint = contentColor
          )
        }
      } else {
        Box(modifier = Modifier.size(16.dp))
      }

      Column(
        modifier = Modifier
          .weight(1f)
          .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.Center
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = contentColor,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        if (!subtitle.isNullOrBlank()) {
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = contentColor.copy(alpha = 0.7f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }
      }

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.End,
        content = actions
      )
    }
  }
}
