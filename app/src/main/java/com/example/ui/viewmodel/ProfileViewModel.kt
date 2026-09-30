package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.DemoRepository
import com.example.core.model.Role
import com.example.core.model.StudentProfile
import com.example.core.model.User
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class ProfileUiState(
  val user: User = User("", "", "", Role.STUDENT),
  val profile: StudentProfile = StudentProfile("", "", "", "", "", 0, ""),
  val completedTasksCount: Int = 0,
  val activeItemsCount: Int = 0,
  val savedAnnouncementsCount: Int = 0,
  val completionPercentage: Int = 100
)

class ProfileViewModel(
  private val repository: DemoRepository = DemoRepository
) : ViewModel() {

  val uiState: StateFlow<ProfileUiState> = combine(
    repository.currentUser,
    repository.studentProfile,
    repository.tasks,
    repository.announcements
  ) { user, profile, tasks, announcements ->
    ProfileUiState(
      user = user,
      profile = profile,
      completedTasksCount = tasks.count { it.isCompleted },
      activeItemsCount = tasks.count { !it.isCompleted },
      savedAnnouncementsCount = announcements.count { it.isBookmarked },
      completionPercentage = profile.calculateCompletion()
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = ProfileUiState()
  )

  fun switchRole(role: Role) {
    repository.switchUserRole(role)
  }

  fun updateInterests(interests: List<String>) {
    val current = repository.studentProfile.value
    repository.updateStudentProfile(
      department = current.department,
      year = current.year,
      section = current.section,
      interests = interests
    )
  }
}
