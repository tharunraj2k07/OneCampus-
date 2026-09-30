package com.example.ui.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.app.ButtonVariant
import com.example.ui.components.app.OneCampusAppScaffold
import com.example.ui.components.app.OneCampusButton
import com.example.ui.components.app.OneCampusTopAppBar

@Composable
fun ForgotPasswordScreen(
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  var email by remember { mutableStateOf("") }
  var isSent by remember { mutableStateOf(false) }

  OneCampusAppScaffold(
    topBar = {
      OneCampusTopAppBar(
        title = "Reset Password",
        showBackButton = true,
        onBackClick = onNavigateBack
      )
    },
    modifier = modifier.testTag("forgot_password_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "Forgot your password?",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
      )

      Spacer(modifier = Modifier.height(8.dp))

      Text(
        text = "Enter your verified college email address to receive password reset instructions.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(24.dp))

      OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("College Email") },
        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("reset_email_input")
      )

      Spacer(modifier = Modifier.height(24.dp))

      OneCampusButton(
        text = if (isSent) "Link Sent! Check Inbox" else "Send Reset Link",
        onClick = { isSent = true },
        enabled = !isSent,
        fullWidth = true,
        variant = ButtonVariant.PRIMARY,
        testTag = "send_reset_button"
      )
    }
  }
}
