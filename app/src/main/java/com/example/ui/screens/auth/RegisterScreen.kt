package com.example.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.core.constants.AppConstants
import com.example.ui.components.app.ButtonVariant
import com.example.ui.components.app.OneCampusAppScaffold
import com.example.ui.components.app.OneCampusButton
import com.example.ui.components.app.OneCampusTopAppBar

@Composable
fun RegisterScreen(
  onRegisterSuccess: () -> Unit,
  onNavigateToLogin: () -> Unit,
  modifier: Modifier = Modifier
) {
  var fullName by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var confirmPassword by remember { mutableStateOf("") }
  var isPasswordVisible by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  OneCampusAppScaffold(
    topBar = {
      OneCampusTopAppBar(
        title = "Create Student Account",
        showBackButton = true,
        onBackClick = onNavigateToLogin
      )
    },
    modifier = modifier.testTag("register_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "Join ${AppConstants.APP_NAME}",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )
      Text(
        text = "Set up your student profile to receive tailored circulars and deadline alerts.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
      )

      // Full Name
      OutlinedTextField(
        value = fullName,
        onValueChange = { fullName = it; errorMessage = null },
        label = { Text("Full Name") },
        placeholder = { Text("e.g. Tharun Kumar") },
        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("register_name_input")
      )

      Spacer(modifier = Modifier.height(12.dp))

      // College Email
      OutlinedTextField(
        value = email,
        onValueChange = { email = it; errorMessage = null },
        label = { Text("College Email Address") },
        placeholder = { Text("e.g. tharun.cse@college.edu") },
        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("register_email_input")
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Password
      OutlinedTextField(
        value = password,
        onValueChange = { password = it; errorMessage = null },
        label = { Text("Password") },
        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
        trailingIcon = {
          IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
            Icon(
              imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
              contentDescription = null
            )
          }
        },
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("register_password_input")
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Confirm Password
      OutlinedTextField(
        value = confirmPassword,
        onValueChange = { confirmPassword = it; errorMessage = null },
        label = { Text("Confirm Password") },
        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("register_confirm_password_input")
      )

      if (errorMessage != null) {
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = errorMessage!!,
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall
        )
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Create Account Button -> Launches Onboarding
      OneCampusButton(
        text = "Continue to Onboarding →",
        onClick = {
          if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
            errorMessage = "Please fill in all required fields."
          } else if (password != confirmPassword) {
            errorMessage = "Passwords do not match."
          } else {
            onRegisterSuccess()
          }
        },
        fullWidth = true,
        variant = ButtonVariant.PRIMARY,
        testTag = "register_submit_button"
      )

      Spacer(modifier = Modifier.height(20.dp))

      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Text(
          text = "Already have an account? ",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "Sign In",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.clickable { onNavigateToLogin() }
        )
      }
    }
  }
}
