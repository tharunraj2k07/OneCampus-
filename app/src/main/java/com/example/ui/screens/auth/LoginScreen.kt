package com.example.ui.screens.auth

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.constants.AppConstants
import com.example.core.data.DemoRepository
import com.example.core.model.Role
import com.example.ui.components.app.ButtonVariant
import com.example.ui.components.app.OneCampusAppScaffold
import com.example.ui.components.app.OneCampusButton
import com.example.ui.components.app.OneCampusCard
import com.example.ui.theme.AiGlowCyan

@Composable
fun LoginScreen(
  onLoginSuccess: (Role) -> Unit,
  onNavigateToRegister: () -> Unit,
  onNavigateToForgotPassword: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedRoleTab by remember { mutableStateOf(0) } // 0 -> Student, 1 -> Faculty
  var email by remember { mutableStateOf("tharun.cse@college.edu") }
  var password by remember { mutableStateOf("password123") }
  var isPasswordVisible by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  OneCampusAppScaffold(
    modifier = modifier.testTag("login_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      // Branding Header
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(72.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Filled.School,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(40.dp)
          )
          Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = AiGlowCyan,
            modifier = Modifier
              .size(18.dp)
              .align(Alignment.TopEnd)
              .padding(top = 6.dp, end = 6.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = AppConstants.APP_NAME,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
      )

      Text(
        text = "Intelligent Campus Communication Platform",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Demo Mode Switcher Tab
      TabRow(
        selectedTabIndex = selectedRoleTab,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        indicator = {},
        divider = {},
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
      ) {
        Tab(
          selected = selectedRoleTab == 0,
          onClick = {
            selectedRoleTab = 0
            email = "tharun.cse@college.edu"
            DemoRepository.switchUserRole(Role.STUDENT)
          },
          text = {
            Text(
              text = "Student Demo",
              fontWeight = if (selectedRoleTab == 0) FontWeight.Bold else FontWeight.Normal,
              color = if (selectedRoleTab == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        )
        Tab(
          selected = selectedRoleTab == 1,
          onClick = {
            selectedRoleTab = 1
            email = "ramanathan.cse@college.edu"
            DemoRepository.switchUserRole(Role.FACULTY)
          },
          text = {
            Text(
              text = "Faculty Demo",
              fontWeight = if (selectedRoleTab == 1) FontWeight.Bold else FontWeight.Normal,
              color = if (selectedRoleTab == 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        )
      }

      Spacer(modifier = Modifier.height(20.dp))

      // College Email Input
      OutlinedTextField(
        value = email,
        onValueChange = {
          email = it
          errorMessage = null
        },
        label = { Text("College Email ID") },
        placeholder = { Text("e.g. tharun.cse@college.edu") },
        leadingIcon = {
          Icon(Icons.Filled.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("login_email_input")
      )

      Spacer(modifier = Modifier.height(14.dp))

      // Password Input
      OutlinedTextField(
        value = password,
        onValueChange = {
          password = it
          errorMessage = null
        },
        label = { Text("Password") },
        leadingIcon = {
          Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        trailingIcon = {
          IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
            Icon(
              imageVector = if (isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
              contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
            )
          }
        },
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("login_password_input")
      )

      if (errorMessage != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = errorMessage!!,
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall
        )
      }

      // Forgot Password Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        TextButton(onClick = onNavigateToForgotPassword) {
          Text(
            text = "Forgot Password?",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Login Button
      OneCampusButton(
        text = if (selectedRoleTab == 0) "Sign In as Student" else "Sign In as Faculty",
        onClick = {
          if (email.isBlank() || password.isBlank()) {
            errorMessage = "Please enter both email and password."
          } else {
            val role = if (selectedRoleTab == 0) Role.STUDENT else Role.FACULTY
            DemoRepository.switchUserRole(role)
            onLoginSuccess(role)
          }
        },
        fullWidth = true,
        variant = ButtonVariant.PRIMARY,
        testTag = "login_submit_button"
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Register Navigation Link
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
      ) {
        Text(
          text = "New student? ",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "Create Account",
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.clickable { onNavigateToRegister() }
        )
      }
    }
  }
}
