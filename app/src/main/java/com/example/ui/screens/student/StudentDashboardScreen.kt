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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.constants.AppConstants
import com.example.core.model.PriorityLevel
import com.example.ui.components.announcement.AnnouncementCard
import com.example.ui.components.app.ButtonVariant
import com.example.ui.components.app.OneCampusAppScaffold
import com.example.ui.components.app.OneCampusButton
import com.example.ui.components.app.OneCampusCard
import com.example.ui.components.category.CategoryChip
import com.example.ui.theme.AiGlowCyan
import com.example.ui.theme.AiGlowPurple
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.BrandBluePrimary
import com.example.ui.theme.ElectricIndigo
import com.example.ui.viewmodel.DashboardViewModel

@Composable
fun StudentDashboardScreen(
  onNavigateToAnnouncementDetail: (String) -> Unit,
  onNavigateToFeed: () -> Unit,
  onNavigateToSearch: () -> Unit,
  onNavigateToBookmarks: () -> Unit,
  onNavigateToNotifications: () -> Unit,
  onNavigateToCalendar: () -> Unit,
  onNavigateToAIAssistant: () -> Unit,
  viewModel: DashboardViewModel = viewModel(),
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  var selectedCategory by remember { mutableStateOf("All") }

  val filteredAnnouncements = remember(selectedCategory, uiState.allAnnouncements) {
    if (selectedCategory == "All") {
      uiState.allAnnouncements
    } else {
      uiState.allAnnouncements.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }
  }

  val onBookmarkToggle = remember(viewModel) {
    { id: String -> viewModel.toggleBookmark(id) }
  }

  OneCampusAppScaffold(
    topBar = {
      // Modern Top Bar with Student Greeting & Action Icons
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp)
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(44.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = uiState.studentProfile.fullName.firstOrNull()?.toString() ?: "T",
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 18.sp
              )
            }
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Good Evening, ${uiState.studentProfile.fullName.split(" ").firstOrNull() ?: "Student"} 👋",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onBackground
            )
            Text(
              text = "${uiState.studentProfile.department} • Year ${uiState.studentProfile.year} (Sec ${uiState.studentProfile.section})",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = FontWeight.Medium
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onNavigateToSearch,
            modifier = Modifier.testTag("dashboard_search_button")
          ) {
            Icon(Icons.Filled.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurface)
          }
          IconButton(
            onClick = onNavigateToBookmarks,
            modifier = Modifier.testTag("dashboard_bookmarks_button")
          ) {
            Icon(Icons.Filled.Bookmark, contentDescription = "Bookmarks", tint = MaterialTheme.colorScheme.onSurface)
          }
          IconButton(
            onClick = onNavigateToNotifications,
            modifier = Modifier.testTag("dashboard_notifications_button")
          ) {
            BadgedBox(
              badge = {
                if (uiState.unreadNotificationsCount > 0) {
                  Badge {
                    Text(uiState.unreadNotificationsCount.toString())
                  }
                }
              }
            ) {
              Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
            }
          }
        }
      }
    },
    modifier = modifier.testTag("student_dashboard_screen")
  ) {
    LazyColumn(
      contentPadding = PaddingValues(bottom = 24.dp),
      verticalArrangement = Arrangement.spacedBy(18.dp),
      modifier = Modifier.fillMaxSize()
    ) {
      // 1. ✨ AI Daily Summary Hero Card
      item {
        OneCampusCard(
          modifier = Modifier.padding(horizontal = 16.dp),
          containerColor = Color.Transparent
        ) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .background(
                brush = Brush.linearGradient(
                  colors = listOf(BrandBlueDark, BrandBluePrimary, ElectricIndigo)
                ),
                shape = RoundedCornerShape(20.dp)
              )
              .padding(18.dp)
          ) {
            Column {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
              ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Surface(
                    shape = CircleShape,
                    color = AiGlowCyan.copy(alpha = 0.2f),
                    modifier = Modifier.size(28.dp)
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = AiGlowCyan,
                        modifier = Modifier.size(16.dp)
                      )
                    }
                  }
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    text = "✨ YOUR AI SUMMARY",
                    color = AiGlowCyan,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                  )
                }

                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = Color(0xFFEF4444).copy(alpha = 0.9f)
                ) {
                  Text(
                    text = "${uiState.criticalItemsCount} Critical Action Items",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              Text(
                text = "You have ${uiState.activeItemsCount} active items. 2 require your immediate attention today, including the Zoho recruitment drive deadline in 5 hours.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium
              )

              Spacer(modifier = Modifier.height(14.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = Color.White.copy(alpha = 0.15f),
                  modifier = Modifier.clickable { onNavigateToAIAssistant() }
                ) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                  ) {
                    Text(
                      text = "Ask AI Copilot",
                      color = Color.White,
                      style = MaterialTheme.typography.labelMedium,
                      fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                      imageVector = Icons.Filled.ArrowForward,
                      contentDescription = null,
                      tint = Color.White,
                      modifier = Modifier.size(14.dp)
                    )
                  }
                }

                Text(
                  text = "Personalized for 3rd Year CSE",
                  color = Color.White.copy(alpha = 0.7f),
                  style = MaterialTheme.typography.labelSmall
                )
              }
            }
          }
        }
      }

      // 2. Today's Progress Card
      item {
        OneCampusCard(
          modifier = Modifier.padding(horizontal = 16.dp)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth()
            ) {
              Column {
                Text(
                  text = "Today's Progress",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold
                )
                Text(
                  text = "${uiState.completedTasksCount} of ${uiState.totalTasksCount} tasks completed",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
              ) {
                Text(
                  text = "${if (uiState.totalTasksCount > 0) (uiState.completedTasksCount * 100 / uiState.totalTasksCount) else 0}%",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val progress = if (uiState.totalTasksCount > 0) {
              uiState.completedTasksCount.toFloat() / uiState.totalTasksCount.toFloat()
            } else 0f

            LinearProgressIndicator(
              progress = { progress },
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
            )
          }
        }
      }

      // 3. 🔴 CRITICAL NOW SECTION
      if (uiState.criticalAnnouncements.isNotEmpty()) {
        item {
          Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "CRITICAL NOW",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFEF4444),
                letterSpacing = 0.5.sp
              )
            }

            Spacer(modifier = Modifier.height(8.dp))

            uiState.criticalAnnouncements.take(1).forEach { criticalItem ->
              Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                  containerColor = Color(0xFFEF4444).copy(alpha = 0.08f)
                ),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { onNavigateToAnnouncementDetail(criticalItem.id) }
              ) {
                Column(modifier = Modifier.padding(16.dp)) {
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = Color(0xFFEF4444)
                    ) {
                      Text(
                        text = "🔴 CLOSING TODAY",
                        color = Color.White,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                      )
                    }

                    Text(
                      text = "5 Hours Remaining",
                      color = Color(0xFFEF4444),
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold
                    )
                  }

                  Spacer(modifier = Modifier.height(8.dp))

                  Text(
                    text = criticalItem.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                  )

                  Spacer(modifier = Modifier.height(4.dp))

                  Text(
                    text = criticalItem.aiSummary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                  )

                  Spacer(modifier = Modifier.height(12.dp))

                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "Eligibility: CGPA ≥ 7.0 (Yours: 8.65 ✓)",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.primary,
                      fontWeight = FontWeight.SemiBold
                    )

                    OneCampusButton(
                      text = criticalItem.requiredAction ?: "Register Now",
                      onClick = { onNavigateToAnnouncementDetail(criticalItem.id) },
                      variant = ButtonVariant.PRIMARY
                    )
                  }
                }
              }
            }
          }
        }
      }

      // 4. UPCOMING DEADLINES SECTION (Quick Interactive Checklist)
      item {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Upcoming Deadlines",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
              )
            }
            TextButton(onClick = onNavigateToCalendar) {
              Text("View Calendar →", fontWeight = FontWeight.SemiBold)
            }
          }

          Spacer(modifier = Modifier.height(6.dp))

          uiState.upcomingDeadlines.take(3).forEach { task ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
                .clickable { task.announcementId?.let { onNavigateToAnnouncementDetail(it) } }
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
              ) {
                Checkbox(
                  checked = task.isCompleted,
                  onCheckedChange = { viewModel.toggleTaskCompletion(task.id) }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                  ) {
                    Text(
                      text = task.category,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                      text = "•",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                      text = task.requiredAction,
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }

                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = if (task.priority == PriorityLevel.CRITICAL) Color(0xFFEF4444).copy(alpha = 0.15f)
                  else MaterialTheme.colorScheme.primaryContainer
                ) {
                  Text(
                    text = task.priority.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (task.priority == PriorityLevel.CRITICAL) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
          }
        }
      }

      // 5. CATEGORIES ROW
      item {
        Column {
          Text(
            text = "Categories",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp)
          )
          Spacer(modifier = Modifier.height(8.dp))
          LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            val categories = listOf("All") + AppConstants.CATEGORIES.take(7)
            items(categories, key = { it }) { category ->
              CategoryChip(
                category = category,
                isSelected = selectedCategory == category,
                onSelected = { selectedCategory = it }
              )
            }
          }
        }
      }

      // 6. RECOMMENDED FOR YOU (Personalized Feed Section)
      item {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
        ) {
          Column {
            Text(
              text = if (selectedCategory == "All") "Recommended for You" else "$selectedCategory Updates",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
            Text(
              text = "Personalized matching CSE Year 3 & your interests",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          TextButton(onClick = onNavigateToFeed) {
            Text("Feed →", fontWeight = FontWeight.SemiBold)
          }
        }
      }

      items(filteredAnnouncements, key = { it.id }) { announcement ->
        AnnouncementCard(
          announcement = announcement,
          onClick = { onNavigateToAnnouncementDetail(announcement.id) },
          onBookmarkToggle = { onBookmarkToggle(announcement.id) },
          onActionClick = { onNavigateToAnnouncementDetail(announcement.id) },
          modifier = Modifier.padding(horizontal = 16.dp)
        )
      }
    }
  }
}
