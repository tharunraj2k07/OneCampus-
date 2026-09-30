package com.example.ui.screens.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.constants.AppConstants
import com.example.ui.components.app.OneCampusAppScaffold
import com.example.ui.components.app.OneCampusCard
import com.example.ui.components.app.OneCampusTopAppBar
import com.example.ui.viewmodel.NotificationViewModel

@Composable
fun SettingsScreen(
  onNavigateBack: () -> Unit,
  viewModel: NotificationViewModel = viewModel(),
  modifier: Modifier = Modifier
) {
  val prefs by viewModel.preferences.collectAsState()
  var selectedThemeIndex by remember { mutableIntStateOf(0) } // 0: System, 1: Light, 2: Dark

  OneCampusAppScaffold(
    topBar = {
      OneCampusTopAppBar(
        title = "App Settings",
        showBackButton = true,
        onBackClick = onNavigateBack
      )
    },
    modifier = modifier.testTag("settings_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
      // 1. Notification Preferences
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Filled.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.padding(start = 6.dp))
          Text(
            text = "Notifications & AI Alerts",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OneCampusCard {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Push Notifications", fontWeight = FontWeight.SemiBold)
                Text("Receive real-time push alerts on this device", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Switch(
                checked = prefs.pushEnabled,
                onCheckedChange = { viewModel.updatePreference(pushEnabled = it) }
              )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Critical Push Alerts", fontWeight = FontWeight.SemiBold)
                Text("Instant alerts for high-priority notices & emergency circulars", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Switch(
                checked = prefs.criticalAlerts,
                onCheckedChange = { viewModel.updatePreference(criticalAlerts = it) }
              )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Deadline Countdown Reminders", fontWeight = FontWeight.SemiBold)
                Text("Receive reminders before assignment & registration closures", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Switch(
                checked = prefs.deadlineReminders,
                onCheckedChange = { viewModel.updatePreference(deadlineReminders = it) }
              )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text("Placement & Drive Alerts", fontWeight = FontWeight.SemiBold)
                Text("Eligible job openings, aptitude tests, and interview schedules", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Switch(
                checked = prefs.placementAlerts,
                onCheckedChange = { viewModel.updatePreference(placementAlerts = it) }
              )
            }
          }
        }
      }

      // 2. Appearance Section
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Filled.DarkMode, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.padding(start = 6.dp))
          Text(
            text = "Appearance & Theme",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OneCampusCard {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Theme Preference",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(10.dp))
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
              val themeOptions = listOf("System", "Light", "Dark")
              themeOptions.forEachIndexed { index, label ->
                SegmentedButton(
                  selected = selectedThemeIndex == index,
                  onClick = { selectedThemeIndex = index },
                  shape = SegmentedButtonDefaults.itemShape(index = index, count = 3)
                ) {
                  Text(label)
                }
              }
            }
          }
        }
      }

      // 3. About Section
      Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
          Spacer(modifier = Modifier.padding(start = 6.dp))
          Text(
            text = "About OneCampus AI",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OneCampusCard {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(text = AppConstants.APP_NAME, fontWeight = FontWeight.Bold)
            Text(text = "Phase 3 Production Build (v1.0)", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = AppConstants.TAGLINE, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            ) {
              Text(
                text = "✨ Built with Jetpack Compose & Material 3",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
