package com.example.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SupervisorAccount
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.model.Role
import com.example.ui.components.app.ButtonVariant
import com.example.ui.components.app.OneCampusAppScaffold
import com.example.ui.components.app.OneCampusButton
import com.example.ui.components.app.OneCampusCard
import com.example.ui.components.app.OneCampusTopAppBar
import com.example.ui.viewmodel.ProfileViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProfileScreen(
  onNavigateToSettings: () -> Unit,
  onNavigateToFacultyMode: () -> Unit,
  onLogout: () -> Unit,
  viewModel: ProfileViewModel = viewModel(),
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

  OneCampusAppScaffold(
    topBar = {
      OneCampusTopAppBar(
        title = "Student Profile",
        showBackButton = false,
        actions = {
          IconButton(onClick = onNavigateToSettings) {
            Icon(Icons.Filled.Settings, contentDescription = "Settings")
          }
        }
      )
    },
    modifier = modifier.testTag("profile_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 12.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Profile Header with Initials Avatar
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
          shape = CircleShape,
          color = MaterialTheme.colorScheme.primaryContainer,
          shadowElevation = 4.dp,
          modifier = Modifier.size(88.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(
              text = uiState.profile.fullName.split(" ")
                .mapNotNull { it.firstOrNull()?.toString() }
                .take(2)
                .joinToString("")
                .ifBlank { "TK" },
              fontWeight = FontWeight.ExtraBold,
              fontSize = 30.sp,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text = uiState.profile.fullName,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold
        )

        Text(
          text = uiState.profile.collegeEmail,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      // Profile Completion Card
      OneCampusCard {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Profile Optimization",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.primaryContainer
            ) {
              Text(
                text = "${uiState.completionPercentage}% Complete",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
              )
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
          LinearProgressIndicator(
            progress = { uiState.completionPercentage / 100f },
            modifier = Modifier
              .fillMaxWidth()
              .height(6.dp)
              .clip(RoundedCornerShape(3.dp))
          )
        }
      }

      // 3 Statistics Cards
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          modifier = Modifier.weight(1f)
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
          ) {
            Icon(Icons.Filled.AssignmentTurnedIn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = uiState.completedTasksCount.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text(text = "Completed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }

        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          modifier = Modifier.weight(1f)
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
          ) {
            Icon(Icons.Filled.PendingActions, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = uiState.activeItemsCount.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text(text = "Pending", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }

        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
          modifier = Modifier.weight(1f)
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(12.dp)
          ) {
            Icon(Icons.Filled.Bookmark, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = uiState.savedAnnouncementsCount.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text(text = "Saved", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }

      // Academic Credentials Card
      OneCampusCard {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Academic Credentials",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(12.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
              Text("Department", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
              Text("${uiState.profile.department} (Year ${uiState.profile.year} • Sec ${uiState.profile.section})", fontWeight = FontWeight.SemiBold)
            }
            Column(horizontalAlignment = Alignment.End) {
              Text("CGPA", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
              Text("${uiState.profile.cgpa} / 10.0", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
            }
          }
          Spacer(modifier = Modifier.height(10.dp))
          HorizontalDivider()
          Spacer(modifier = Modifier.height(10.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
              Text("Register Number", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
              Text(uiState.profile.registerNumber, fontWeight = FontWeight.SemiBold)
            }
            Column(horizontalAlignment = Alignment.End) {
              Text("Status", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
              Text("Active Student", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            }
          }
        }
      }

      // AI Notification Interests Tags
      OneCampusCard {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Personalized AI Focus Areas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.height(10.dp))
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            uiState.profile.interests.forEach { interest ->
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
              ) {
                Text(
                  text = interest,
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                  style = MaterialTheme.typography.labelMedium,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }
        }
      }

      // Switch Role Demo Button
      OneCampusButton(
        text = "Switch to Faculty Publisher View",
        onClick = {
          viewModel.switchRole(Role.FACULTY)
          onNavigateToFacultyMode()
        },
        icon = Icons.Filled.SupervisorAccount,
        variant = ButtonVariant.SECONDARY,
        fullWidth = true,
        testTag = "switch_role_button"
      )

      // Sign Out Button
      OneCampusButton(
        text = "Sign Out",
        onClick = onLogout,
        icon = Icons.Filled.Logout,
        variant = ButtonVariant.DESTRUCTIVE,
        fullWidth = true,
        testTag = "logout_button"
      )

      Spacer(modifier = Modifier.height(12.dp))
    }
  }
}
