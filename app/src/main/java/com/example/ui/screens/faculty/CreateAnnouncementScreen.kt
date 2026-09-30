package com.example.ui.screens.faculty

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.constants.AppConstants
import com.example.ui.components.app.ButtonVariant
import com.example.ui.components.app.OneCampusAppScaffold
import com.example.ui.components.app.OneCampusButton
import com.example.ui.components.app.OneCampusCard
import com.example.ui.components.app.OneCampusTopAppBar
import com.example.ui.components.category.CategoryChip
import com.example.ui.theme.AiGlowCyan
import com.example.ui.viewmodel.FacultyAnnouncementViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CreateAnnouncementScreen(
  onNavigateBack: () -> Unit,
  onPublishSuccess: () -> Unit,
  viewModel: FacultyAnnouncementViewModel = viewModel(),
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()

  var title by remember { mutableStateOf("") }
  var content by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("Assignment") }
  var selectedDepartment by remember { mutableStateOf("CSE") }
  var selectedYear by remember { mutableStateOf(3) }
  var selectedSection by remember { mutableStateOf("All") }
  var externalLink by remember { mutableStateOf("") }
  var hasDeadline by remember { mutableStateOf(true) }

  OneCampusAppScaffold(
    topBar = {
      OneCampusTopAppBar(
        title = "Create Announcement",
        showBackButton = true,
        onBackClick = onNavigateBack
      )
    },
    modifier = modifier.testTag("create_announcement_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Verified Publisher Note banner
      OneCampusCard(
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
        accentColor = AiGlowCyan
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.padding(12.dp)
        ) {
          Icon(
            imageVector = Icons.Filled.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
          )
          Text(
            text = "Official Faculty Circular. Publisher identity and credentials will be verified and stamped on publication.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 10.dp)
          )
        }
      }

      Text("Announcement Title", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      OutlinedTextField(
        value = title,
        onValueChange = { title = it },
        placeholder = { Text("e.g., Mini Project Review Phase 1 Architecture") },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("announcement_title_input")
      )

      Text("Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        AppConstants.CATEGORIES.take(8).forEach { category ->
          CategoryChip(
            category = category,
            isSelected = selectedCategory == category,
            onSelected = { selectedCategory = it }
          )
        }
      }

      Text("Circular Content", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      OutlinedTextField(
        value = content,
        onValueChange = { content = it },
        placeholder = { Text("Enter circular details, requirements, venue, instructions...") },
        minLines = 4,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("announcement_content_input")
      )

      Text("Target Department", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        listOf("ALL", "CSE", "IT", "ECE", "EEE", "MECH", "CIVIL", "AI&DS").forEach { dept ->
          FilterChip(
            selected = selectedDepartment == dept,
            onClick = { selectedDepartment = dept },
            label = { Text(dept) }
          )
        }
      }

      Text("Target Academic Year", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(1, 2, 3, 4).forEach { yr ->
          FilterChip(
            selected = selectedYear == yr,
            onClick = { selectedYear = yr },
            label = { Text("Year $yr") }
          )
        }
      }

      Text("External Application / Resource Link (Optional)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
      OutlinedTextField(
        value = externalLink,
        onValueChange = { externalLink = it },
        placeholder = { Text("https://portal.onecampus.edu/assignment/submit") },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
      )

      Spacer(modifier = Modifier.height(10.dp))

      // Dual Action Buttons: Save Draft & Publish
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        OneCampusButton(
          text = "Save Draft",
          icon = Icons.Filled.Save,
          onClick = {
            if (title.trim().length >= 3 && content.trim().length >= 5) {
              val deadlineEpoch = if (hasDeadline) System.currentTimeMillis() + (86400_000L * 3) else null
              viewModel.saveDraftAnnouncement(
                title = title.trim(),
                rawContent = content.trim(),
                category = selectedCategory,
                department = selectedDepartment,
                year = selectedYear,
                section = if (selectedSection == "All") "" else selectedSection,
                deadlineEpochMs = deadlineEpoch,
                externalLink = externalLink.takeIf { it.isNotBlank() }
              ) { success, msg ->
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                if (success) onPublishSuccess()
              }
            } else {
              Toast.makeText(context, "Please provide title (min 3 chars) and content (min 5 chars)", Toast.LENGTH_SHORT).show()
            }
          },
          variant = ButtonVariant.OUTLINE,
          enabled = title.isNotBlank() && content.isNotBlank() && !uiState.isPublishing,
          modifier = Modifier.weight(1f),
          testTag = "save_draft_button"
        )

        OneCampusButton(
          text = if (uiState.isPublishing) "Publishing..." else "Publish Now",
          icon = Icons.Filled.Send,
          onClick = {
            if (title.trim().length >= 3 && content.trim().length >= 5) {
              val deadlineEpoch = if (hasDeadline) System.currentTimeMillis() + (86400_000L * 3) else null
              viewModel.publishNewAnnouncement(
                title = title.trim(),
                rawContent = content.trim(),
                category = selectedCategory,
                department = selectedDepartment,
                year = selectedYear,
                section = if (selectedSection == "All") "" else selectedSection,
                deadlineEpochMs = deadlineEpoch,
                externalLink = externalLink.takeIf { it.isNotBlank() }
              ) { success, msg ->
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                if (success) onPublishSuccess()
              }
            } else {
              Toast.makeText(context, "Please provide title (min 3 chars) and content (min 5 chars)", Toast.LENGTH_SHORT).show()
            }
          },
          variant = ButtonVariant.PRIMARY,
          enabled = title.isNotBlank() && content.isNotBlank() && !uiState.isPublishing,
          modifier = Modifier.weight(1f),
          testTag = "publish_submit_button"
        )
      }
    }
  }
}

