package com.example.ui.screens.student

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.OneCampusApp
import com.example.core.data.DemoRepository
import com.example.core.data.NotificationRepository
import com.example.core.navigation.BottomNavTab
import com.example.ui.components.OfflineBanner
import com.example.ui.components.app.OneCampusBottomNavigation

@Composable
fun StudentMainShell(
  onNavigateToAnnouncementDetail: (String) -> Unit,
  onNavigateToFeed: () -> Unit,
  onNavigateToSearch: () -> Unit,
  onNavigateToBookmarks: () -> Unit,
  onNavigateToSettings: () -> Unit,
  onNavigateToFacultyMode: () -> Unit,
  onLogout: () -> Unit,
  modifier: Modifier = Modifier
) {
  var currentTab by rememberSaveable { mutableStateOf<String>(BottomNavTab.Dashboard.route) }
  val demoNotifs by DemoRepository.notifications.collectAsState()
  val roomUnreadCount by NotificationRepository.getInstance(OneCampusApp.getAppContext()).unreadCount.collectAsState(initial = 0)
  val unreadCount = maxOf(roomUnreadCount, demoNotifs.count { !it.isRead })

  val currentNavTab = when (currentTab) {
    BottomNavTab.Calendar.route -> BottomNavTab.Calendar
    BottomNavTab.AIAssistant.route -> BottomNavTab.AIAssistant
    BottomNavTab.Notifications.route -> BottomNavTab.Notifications
    BottomNavTab.Profile.route -> BottomNavTab.Profile
    else -> BottomNavTab.Dashboard
  }

  Scaffold(
    bottomBar = {
      OneCampusBottomNavigation(
        currentTab = currentNavTab,
        onTabSelected = { tab ->
          currentTab = tab.route
        },
        unreadNotificationsCount = unreadCount
      )
    },
    modifier = modifier
      .fillMaxSize()
      .testTag("student_main_shell")
  ) { innerPadding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(bottom = innerPadding.calculateBottomPadding())
    ) {
      OfflineBanner()
      Box(modifier = Modifier.fillMaxSize().weight(1f)) {
        when (currentNavTab) {
        BottomNavTab.Dashboard -> {
          StudentDashboardScreen(
            onNavigateToAnnouncementDetail = onNavigateToAnnouncementDetail,
            onNavigateToFeed = onNavigateToFeed,
            onNavigateToSearch = onNavigateToSearch,
            onNavigateToBookmarks = onNavigateToBookmarks,
            onNavigateToNotifications = { currentTab = BottomNavTab.Notifications.route },
            onNavigateToCalendar = { currentTab = BottomNavTab.Calendar.route },
            onNavigateToAIAssistant = { currentTab = BottomNavTab.AIAssistant.route }
          )
        }
        BottomNavTab.Calendar -> {
          CalendarScreen(
            onNavigateToAnnouncementDetail = onNavigateToAnnouncementDetail
          )
        }
        BottomNavTab.AIAssistant -> {
          AIAssistantScreen()
        }
        BottomNavTab.Notifications -> {
          NotificationsScreen(
            onNavigateToAnnouncementDetail = onNavigateToAnnouncementDetail
          )
        }
        BottomNavTab.Profile -> {
          ProfileScreen(
            onNavigateToSettings = onNavigateToSettings,
            onNavigateToFacultyMode = onNavigateToFacultyMode,
            onLogout = onLogout
          )
        }
      }
    }
  }
}
}
