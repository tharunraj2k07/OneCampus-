package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.DemoRepository
import com.example.core.data.TaskRepository
import com.example.core.model.TaskItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CalendarViewMode {
  WEEKLY,
  MONTHLY,
  AGENDA
}

data class CalendarUiState(
  val tasks: List<TaskItem> = emptyList(),
  val viewMode: CalendarViewMode = CalendarViewMode.WEEKLY,
  val selectedDayIndex: Int = 0,
  val filterCategory: String = "All",
  val completedCount: Int = 0,
  val pendingCount: Int = 0
)

class CalendarViewModel(
  private val taskRepo: TaskRepository = TaskRepository.getInstance(),
  private val demoRepo: DemoRepository = DemoRepository
) : ViewModel() {

  init {
    viewModelScope.launch {
      taskRepo.refreshTasks()
    }
  }

  private val _viewMode = MutableStateFlow(CalendarViewMode.WEEKLY)
  val viewMode = _viewMode.asStateFlow()

  private val _selectedDayIndex = MutableStateFlow(0)
  val selectedDayIndex = _selectedDayIndex.asStateFlow()

  private val _filterCategory = MutableStateFlow("All")
  val filterCategory = _filterCategory.asStateFlow()

  val uiState: StateFlow<CalendarUiState> = combine(
    taskRepo.tasks,
    _viewMode,
    _selectedDayIndex,
    _filterCategory
  ) { tasks, mode, dayIdx, category ->
    val activeTasks = if (tasks.isNotEmpty()) tasks else demoRepo.tasks.value
    val filtered = if (category == "All") activeTasks else activeTasks.filter { it.category.equals(category, ignoreCase = true) }
    CalendarUiState(
      tasks = filtered,
      viewMode = mode,
      selectedDayIndex = dayIdx,
      filterCategory = category,
      completedCount = activeTasks.count { it.isCompleted },
      pendingCount = activeTasks.count { !it.isCompleted }
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = CalendarUiState()
  )

  fun setViewMode(mode: CalendarViewMode) {
    _viewMode.value = mode
  }

  fun selectDayIndex(index: Int) {
    _selectedDayIndex.value = index
  }

  fun setFilterCategory(category: String) {
    _filterCategory.value = category
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

