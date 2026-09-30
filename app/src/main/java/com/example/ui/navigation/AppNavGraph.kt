package com.example.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.core.navigation.Screen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.auth.ForgotPasswordScreen
import com.example.ui.screens.auth.LoginScreen
import com.example.ui.screens.auth.RegisterScreen
import com.example.ui.screens.auth.StudentOnboardingScreen
import com.example.ui.screens.faculty.AnnouncementPreviewScreen
import com.example.ui.screens.faculty.CreateAnnouncementScreen
import com.example.ui.screens.faculty.FacultyDashboardScreen
import com.example.ui.screens.faculty.ManageAnnouncementsScreen
import com.example.ui.screens.student.AnnouncementDetailScreen
import com.example.ui.screens.student.AnnouncementFeedScreen
import com.example.ui.screens.student.BookmarksScreen
import com.example.ui.screens.student.GlobalSearchScreen
import com.example.ui.screens.student.SettingsScreen
import com.example.ui.screens.student.StudentMainShell

@Composable
fun AppNavGraph(
  navController: NavHostController = rememberNavController(),
  initialAnnouncementId: String? = null,
  initialTaskId: String? = null,
  modifier: Modifier = Modifier
) {
  NavHost(
    navController = navController,
    startDestination = Screen.Splash.route,
    enterTransition = { fadeIn(animationSpec = tween(300)) },
    exitTransition = { fadeOut(animationSpec = tween(300)) },
    modifier = modifier
  ) {
    // 1. Splash Screen
    composable(Screen.Splash.route) {
      SplashScreen(
        onNavigateNext = {
          if (initialAnnouncementId != null) {
            navController.navigate(Screen.AnnouncementDetail.createRoute(initialAnnouncementId)) {
              popUpTo(Screen.Splash.route) { inclusive = true }
            }
          } else {
            navController.navigate(Screen.Login.route) {
              popUpTo(Screen.Splash.route) { inclusive = true }
            }
          }
        }
      )
    }

    // 2. Auth Flow
    composable(Screen.Login.route) {
      LoginScreen(
        onLoginSuccess = {
          if (initialAnnouncementId != null) {
            navController.navigate(Screen.AnnouncementDetail.createRoute(initialAnnouncementId)) {
              popUpTo(Screen.Login.route) { inclusive = true }
            }
          } else {
            navController.navigate(Screen.MainNav.route) {
              popUpTo(Screen.Login.route) { inclusive = true }
            }
          }
        },
        onNavigateToRegister = { navController.navigate(Screen.Register.route) },
        onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
      )
    }

    composable(Screen.Register.route) {
      RegisterScreen(
        onRegisterSuccess = { navController.navigate(Screen.Onboarding.route) },
        onNavigateToLogin = { navController.popBackStack() }
      )
    }

    composable(Screen.ForgotPassword.route) {
      ForgotPasswordScreen(
        onNavigateBack = { navController.popBackStack() }
      )
    }

    composable(Screen.Onboarding.route) {
      StudentOnboardingScreen(
        onCompleteOnboarding = {
          navController.navigate(Screen.MainNav.route) {
            popUpTo(Screen.Register.route) { inclusive = true }
          }
        }
      )
    }

    // 3. Student Main Shell Flow
    composable(Screen.MainNav.route) {
      StudentMainShell(
        onNavigateToAnnouncementDetail = { id ->
          navController.navigate(Screen.AnnouncementDetail.createRoute(id))
        },
        onNavigateToFeed = { navController.navigate(Screen.AnnouncementFeed.route) },
        onNavigateToSearch = { navController.navigate(Screen.GlobalSearch.route) },
        onNavigateToBookmarks = { navController.navigate(Screen.Bookmarks.route) },
        onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
        onNavigateToFacultyMode = { navController.navigate(Screen.FacultyDashboard.route) },
        onLogout = {
          navController.navigate(Screen.Login.route) {
            popUpTo(Screen.MainNav.route) { inclusive = true }
          }
        }
      )
    }

    composable(Screen.AnnouncementFeed.route) {
      AnnouncementFeedScreen(
        onNavigateBack = { navController.popBackStack() },
        onNavigateToDetail = { id ->
          navController.navigate(Screen.AnnouncementDetail.createRoute(id))
        },
        onNavigateToSearch = { navController.navigate(Screen.GlobalSearch.route) }
      )
    }

    composable(
      route = Screen.AnnouncementDetail.route,
      arguments = listOf(navArgument("announcementId") { type = NavType.StringType })
    ) { backStackEntry ->
      val announcementId = backStackEntry.arguments?.getString("announcementId") ?: ""
      AnnouncementDetailScreen(
        announcementId = announcementId,
        onNavigateBack = { navController.popBackStack() }
      )
    }

    composable(Screen.GlobalSearch.route) {
      GlobalSearchScreen(
        onNavigateBack = { navController.popBackStack() },
        onNavigateToDetail = { id ->
          navController.navigate(Screen.AnnouncementDetail.createRoute(id))
        }
      )
    }

    composable(Screen.Bookmarks.route) {
      BookmarksScreen(
        onNavigateBack = { navController.popBackStack() },
        onNavigateToDetail = { id ->
          navController.navigate(Screen.AnnouncementDetail.createRoute(id))
        }
      )
    }

    composable(Screen.Settings.route) {
      SettingsScreen(
        onNavigateBack = { navController.popBackStack() }
      )
    }

    // 4. Faculty Flow
    composable(Screen.FacultyDashboard.route) {
      FacultyDashboardScreen(
        onCreateAnnouncement = { navController.navigate(Screen.FacultyCreateAnnouncement.route) },
        onManageAnnouncements = { navController.navigate(Screen.FacultyManageAnnouncements.route) },
        onPreviewAnnouncement = { id ->
          navController.navigate(Screen.FacultyAnnouncementPreview.createRoute(id))
        },
        onSwitchToStudentMode = {
          navController.navigate(Screen.MainNav.route) {
            popUpTo(Screen.FacultyDashboard.route) { inclusive = true }
          }
        }
      )
    }

    composable(Screen.FacultyCreateAnnouncement.route) {
      CreateAnnouncementScreen(
        onNavigateBack = { navController.popBackStack() },
        onPublishSuccess = {
          navController.navigate(Screen.FacultyDashboard.route) {
            popUpTo(Screen.FacultyCreateAnnouncement.route) { inclusive = true }
          }
        }
      )
    }

    composable(Screen.FacultyManageAnnouncements.route) {
      ManageAnnouncementsScreen(
        onNavigateBack = { navController.popBackStack() },
        onNavigateToPreview = { id ->
          navController.navigate(Screen.FacultyAnnouncementPreview.createRoute(id))
        }
      )
    }

    composable(
      route = Screen.FacultyAnnouncementPreview.route,
      arguments = listOf(navArgument("announcementId") { type = NavType.StringType })
    ) { backStackEntry ->
      val announcementId = backStackEntry.arguments?.getString("announcementId") ?: ""
      AnnouncementPreviewScreen(
        announcementId = announcementId,
        onNavigateBack = { navController.popBackStack() }
      )
    }
  }
}
