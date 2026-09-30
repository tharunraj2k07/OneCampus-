package com.example.ui.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.AnnouncementRepository
import com.example.core.data.DemoRepository
import com.example.core.data.TaskRepository
import com.example.core.model.Announcement
import com.example.core.model.PriorityLevel
import com.example.core.model.StudentProfile
import com.example.core.model.TaskItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class DashboardUiState(
  val studentProfile: StudentProfile = StudentProfile("", "", "", "", "", 0, ""),
  val criticalAnnouncements: List<Announcement> = emptyList(),
  val upcomingDeadlines: List<TaskItem> = emptyList(),
  val todayTasks: List<TaskItem> = emptyList(),
  val recommendedAnnouncements: List<Announcement> = emptyList(),
  val allAnnouncements: List<Announcement> = emptyList(),
  val completedTasksCount: Int = 0,
  val totalTasksCount: Int = 0,
  val activeItemsCount: Int = 0,
  val criticalItemsCount: Int = 0,
  val unreadNotificationsCount: Int = 0,
  val isLoading: Boolean = false
)

class DashboardViewModel(
  private val demoRepo: DemoRepository = DemoRepository,
  private val announcementRepo: AnnouncementRepository = AnnouncementRepository.getInstance(),
  private val taskRepo: TaskRepository = TaskRepository.getInstance()
) : ViewModel() {

  init {
    refreshData()
  }

  fun refreshData() {
    viewModelScope.launch {
      announcementRepo.refreshPersonalizedFeed()
      taskRepo.refreshTasks()
    }
  }

  val uiState: StateFlow<DashboardUiState> = combine(
    demoRepo.studentProfile,
    announcementRepo.feedAnnouncements,
    taskRepo.tasks,
    demoRepo.notifications
  ) { profile, announcements, tasks, notifs ->
    val activeAnnouncements = if (announcements.isNotEmpty()) announcements else demoRepo.announcements.value
    val activeTasks = if (tasks.isNotEmpty()) tasks else demoRepo.tasks.value

    val critical = activeAnnouncements.filter { it.priority == PriorityLevel.CRITICAL }
    val recommended = activeAnnouncements.filter { ann ->
      (ann.relevance != null && ann.relevance.score >= 60) ||
        ann.interestTags.any { tag -> profile.interests.contains(tag) } ||
        ann.targetDepartments.contains(profile.department)
    }
    val completedCount = activeTasks.count { it.isCompleted }
    val unreadNotifs = notifs.count { !it.isRead }

    DashboardUiState(
      studentProfile = profile,
      criticalAnnouncements = critical,
      upcomingDeadlines = activeTasks.sortedBy { it.deadlineEpochMs ?: Long.MAX_VALUE },
      todayTasks = activeTasks,
      recommendedAnnouncements = recommended,
      allAnnouncements = activeAnnouncements,
      completedTasksCount = completedCount,
      totalTasksCount = activeTasks.size,
      activeItemsCount = activeTasks.count { !it.isCompleted },
      criticalItemsCount = critical.size,
      unreadNotificationsCount = unreadNotifs,
      isLoading = false
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = DashboardUiState(isLoading = true)
  )

  fun toggleBookmark(announcementId: String) {
    announcementRepo.toggleBookmark(announcementId)
  }

  fun toggleTaskCompletion(taskId: String) {
    viewModelScope.launch {
      taskRepo.toggleTask(taskId)
    }
  }

  fun syncTasks() {
    viewModelScope.launch {
      taskRepo.syncTasks()
    }
  }
}

