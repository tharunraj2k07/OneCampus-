package com.example.core.navigation

sealed class Screen(val route: String) {
  // Auth Flow
  object Splash : Screen("splash")
  object Login : Screen("login")
  object Register : Screen("register")
  object ForgotPassword : Screen("forgot_password")
  object Onboarding : Screen("onboarding")

  // Student Flow
  object MainNav : Screen("main_nav")
  object AnnouncementFeed : Screen("announcement_feed")
  object AnnouncementDetail : Screen("announcement_detail/{announcementId}") {
    fun createRoute(announcementId: String) = "announcement_detail/$announcementId"
  }
  object GlobalSearch : Screen("global_search")
  object Bookmarks : Screen("bookmarks")
  object Settings : Screen("settings")

  // Faculty Flow
  object FacultyDashboard : Screen("faculty_dashboard")
  object FacultyCreateAnnouncement : Screen("faculty_create_announcement")
  object FacultyManageAnnouncements : Screen("faculty_manage_announcements")
  object FacultyAnnouncementPreview : Screen("faculty_announcement_preview/{announcementId}") {
    fun createRoute(announcementId: String) = "faculty_announcement_preview/$announcementId"
  }
}

sealed class BottomNavTab(
  val route: String,
  val title: String,
  val testTag: String
) {
  object Dashboard : BottomNavTab("dashboard_tab", "Home", "nav_tab_home")
  object Calendar : BottomNavTab("calendar_tab", "Calendar", "nav_tab_calendar")
  object AIAssistant : BottomNavTab("ai_assistant_tab", "AI Assistant", "nav_tab_ai")
  object Notifications : BottomNavTab("notifications_tab", "Alerts", "nav_tab_notifications")
  object Profile : BottomNavTab("profile_tab", "Profile", "nav_tab_profile")
}

