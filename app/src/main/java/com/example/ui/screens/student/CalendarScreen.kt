package com.example.ui.screens.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.constants.AppConstants
import com.example.core.model.PriorityLevel
import com.example.ui.components.announcement.DeadlineIndicator
import com.example.ui.components.announcement.TimeRemainingIndicator
import com.example.ui.components.app.OneCampusAppScaffold
import com.example.ui.components.app.OneCampusCard
import com.example.ui.components.app.OneCampusTopAppBar
import com.example.ui.components.category.CategoryBadge
import com.example.ui.components.category.CategoryChip
import com.example.ui.components.priority.PriorityBadge
import com.example.ui.components.states.EmptyStateView
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.CalendarViewMode
import com.example.ui.viewmodel.CalendarViewModel

@Composable
fun CalendarScreen(
  onNavigateToAnnouncementDetail: (String) -> Unit,
  viewModel: CalendarViewModel = viewModel(),
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsState()

  val days = listOf("Mon\n29", "Tue\n30", "Wed\n31", "Thu\n1", "Fri\n2", "Sat\n3", "Sun\n4")

  OneCampusAppScaffold(
    topBar = {
      OneCampusTopAppBar(
        title = "Deadlines & Schedule",
        showBackButton = false
      )
    },
    modifier = modifier.testTag("calendar_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
      // 1. View Mode Segmented Controls (Weekly / Monthly / Agenda)
      SingleChoiceSegmentedButtonRow(
        modifier = Modifier.fillMaxWidth()
      ) {
        SegmentedButton(
          selected = uiState.viewMode == CalendarViewMode.WEEKLY,
          onClick = { viewModel.setViewMode(CalendarViewMode.WEEKLY) },
          shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
        ) {
          Text("Weekly")
        }
        SegmentedButton(
          selected = uiState.viewMode == CalendarViewMode.MONTHLY,
          onClick = { viewModel.setViewMode(CalendarViewMode.MONTHLY) },
          shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
        ) {
          Text("Monthly")
        }
        SegmentedButton(
          selected = uiState.viewMode == CalendarViewMode.AGENDA,
          onClick = { viewModel.setViewMode(CalendarViewMode.AGENDA) },
          shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
        ) {
          Text("Agenda")
        }
      }

      Spacer(modifier = Modifier.height(14.dp))

      // 2. Interactive Calendar Days Row Strip
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(days.size) { index ->
          val isSelected = uiState.selectedDayIndex == index
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
              .size(width = 54.dp, height = 64.dp)
              .clip(RoundedCornerShape(14.dp))
              .clickable { viewModel.selectDayIndex(index) }
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = days[index],
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                fontSize = 13.sp,
                lineHeight = 16.sp
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 3. Category Filter Chips
      LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        val filterOptions = listOf("All", "Placement", "Assignment", "Registration", "Coding Contest", "Workshop", "Internship")
        items(filterOptions) { category ->
          CategoryChip(
            category = category,
            isSelected = uiState.filterCategory == category,
            onSelected = { viewModel.setFilterCategory(it) }
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // 4. Tasks Summary Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Active Deadlines (${uiState.tasks.size})",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "${uiState.completedCount} Completed • ${uiState.pendingCount} Pending",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.SemiBold
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      // 5. Tasks List / Empty State
      if (uiState.tasks.isEmpty()) {
        EmptyStateView(
          title = "No Deadlines for this Filter",
          message = "You're all caught up! Switch filters or select another day to view schedule."
        )
      } else {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = PaddingValues(bottom = 24.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          items(uiState.tasks, key = { it.id }) { task ->
            OneCampusCard(
              onClick = { task.announcementId?.let { onNavigateToAnnouncementDetail(it) } },
              modifier = Modifier.testTag("task_item_${task.id}")
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp)
              ) {
                IconButton(
                  onClick = { viewModel.toggleTaskCompletion(task.id) }
                ) {
                  Icon(
                    imageVector = if (task.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircleOutline,
                    contentDescription = "Toggle Complete",
                    tint = if (task.isCompleted) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    CategoryBadge(category = task.category)
                    PriorityBadge(priority = task.priority)
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (task.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                  )
                  Spacer(modifier = Modifier.height(2.dp))
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Text(
                      text = "Action: ${task.requiredAction}",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                TimeRemainingIndicator(deadlineEpochMs = task.deadlineEpochMs)
              }
            }
          }
        }
      }
    }
  }
}
