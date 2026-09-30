package com.example.ui.components.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.navigation.BottomNavTab
import com.example.ui.theme.AiGlowCyan

@Composable
fun OneCampusBottomNavigation(
  currentTab: BottomNavTab,
  onTabSelected: (BottomNavTab) -> Unit,
  unreadNotificationsCount: Int = 3,
  modifier: Modifier = Modifier
) {
  val tabs = listOf(
    BottomNavTab.Dashboard,
    BottomNavTab.Calendar,
    BottomNavTab.AIAssistant,
    BottomNavTab.Notifications,
    BottomNavTab.Profile
  )

  NavigationBar(
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 8.dp,
    modifier = modifier
      .fillMaxWidth()
      .testTag("student_bottom_navigation")
  ) {
    tabs.forEach { tab ->
      val isSelected = currentTab == tab
      val (selectedIcon, unselectedIcon) = getTabIcons(tab)

      NavigationBarItem(
        selected = isSelected,
        onClick = { onTabSelected(tab) },
        icon = {
          if (tab == BottomNavTab.Notifications && unreadNotificationsCount > 0) {
            BadgedBox(
              badge = {
                Badge(
                  containerColor = MaterialTheme.colorScheme.error,
                  contentColor = MaterialTheme.colorScheme.onError
                ) {
                  Text(
                    text = if (unreadNotificationsCount > 99) "99+" else "$unreadNotificationsCount",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            ) {
              Icon(
                imageVector = if (isSelected) selectedIcon else unselectedIcon,
                contentDescription = tab.title,
                modifier = Modifier.size(24.dp)
              )
            }
          } else if (tab == BottomNavTab.AIAssistant) {
            // Enhanced sparkle icon styling for AI Assistant
            Box(contentAlignment = Alignment.Center) {
              if (isSelected) {
                Box(
                  modifier = Modifier
                    .size(32.dp)
                    .background(AiGlowCyan.copy(alpha = 0.2f), shape = CircleShape)
                )
              }
              Icon(
                imageVector = if (isSelected) selectedIcon else unselectedIcon,
                contentDescription = tab.title,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
              )
            }
          } else {
            Icon(
              imageVector = if (isSelected) selectedIcon else unselectedIcon,
              contentDescription = tab.title,
              modifier = Modifier.size(24.dp)
            )
          }
        },
        label = {
          Text(
            text = tab.title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
          )
        },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = MaterialTheme.colorScheme.primary,
          selectedTextColor = MaterialTheme.colorScheme.primary,
          indicatorColor = MaterialTheme.colorScheme.primaryContainer,
          unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
          unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        modifier = Modifier.testTag(tab.testTag)
      )
    }
  }
}

private fun getTabIcons(tab: BottomNavTab): Pair<ImageVector, ImageVector> {
  return when (tab) {
    BottomNavTab.Dashboard -> Pair(Icons.Filled.Home, Icons.Outlined.Home)
    BottomNavTab.Calendar -> Pair(Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth)
    BottomNavTab.AIAssistant -> Pair(Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome)
    BottomNavTab.Notifications -> Pair(Icons.Filled.Notifications, Icons.Outlined.Notifications)
    BottomNavTab.Profile -> Pair(Icons.Filled.Person, Icons.Outlined.Person)
  }
}
