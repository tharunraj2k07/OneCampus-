package com.example.ui.screens.faculty

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.ManageAccounts
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.data.DemoRepository
import com.example.core.model.Role
import com.example.ui.components.announcement.AnnouncementCompactCard
import com.example.ui.components.app.ButtonVariant
import com.example.ui.components.app.OneCampusAppScaffold
import com.example.ui.components.app.OneCampusButton
import com.example.ui.components.app.OneCampusCard
import com.example.ui.components.app.OneCampusTopAppBar
import com.example.ui.viewmodel.FacultyAnnouncementViewModel

@Composable
fun FacultyDashboardScreen(
  onCreateAnnouncement: () -> Unit,
  onManageAnnouncements: () -> Unit,
  onPreviewAnnouncement: (String) -> Unit,
  onSwitchToStudentMode: () -> Unit,
  viewModel: FacultyAnnouncementViewModel = viewModel(),
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

  OneCampusAppScaffold(
    topBar = {
      OneCampusTopAppBar(
        title = "Faculty Publisher Portal",
        showBackButton = false,
        actions = {
          TextButton(
            onClick = {
              DemoRepository.switchUserRole(Role.STUDENT)
              onSwitchToStudentMode()
            },
            modifier = Modifier.testTag("switch_to_student_mode_button")
          ) {
            Icon(Icons.Filled.SwapHoriz, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
            Text("Student Mode")
          }
        }
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = onCreateAnnouncement,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.testTag("faculty_fab_create")
      ) {
        Icon(Icons.Filled.Add, contentDescription = "Create Announcement")
      }
    },
    modifier = modifier.testTag("faculty_dashboard_screen")
  ) {
    LazyColumn(
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      // 1. Faculty Profile & Actions Card
      item {
        OneCampusCard(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Dr. S. Ramanathan",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Associate Professor • Department of CSE",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
              OneCampusButton(
                text = "New Circular",
                onClick = onCreateAnnouncement,
                icon = Icons.Filled.Campaign,
                variant = ButtonVariant.PRIMARY,
                modifier = Modifier.weight(1f)
              )
              OneCampusButton(
                text = "Manage All",
                onClick = onManageAnnouncements,
                icon = Icons.Filled.ManageAccounts,
                variant = ButtonVariant.SECONDARY,
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
      }

      // 2. Metrics Statistics Grid
      item {
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
              Icon(Icons.Filled.Campaign, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.height(4.dp))
              Text(text = uiState.announcements.size.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              Text(text = "Active Notices", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
              Icon(Icons.Filled.Group, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.height(4.dp))
              Text(text = "${uiState.studentsReachedCount}+", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              Text(text = "Students Reached", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
              Icon(Icons.Filled.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.height(4.dp))
              Text(text = uiState.upcomingDeadlinesCount.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
              Text(text = "Deadlines Set", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }

      // 3. Published Circulars List
      item {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = "Active Circulars (${uiState.announcements.size})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          TextButton(onClick = onManageAnnouncements) {
            Text("View All")
          }
        }
      }

      items(uiState.announcements, key = { it.id }) { ann ->
        AnnouncementCompactCard(
          announcement = ann,
          onClick = { onPreviewAnnouncement(ann.id) }
        )
      }
    }
  }
}
