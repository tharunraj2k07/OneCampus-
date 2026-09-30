package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.OneCampusApp
import com.example.core.data.DemoRepository
import com.example.core.data.NotificationRepository
import com.example.core.model.NotificationItem
import com.example.core.network.dto.NotificationPreferencesDto
import com.example.core.network.dto.UpdateNotificationPreferencesRequestDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class NotificationUiState(
  val notifications: List<NotificationItem> = emptyList(),
  val selectedFilter: String = "All",
  val unreadCount: Int = 0,
  val isLoading: Boolean = false,
  val preferences: NotificationPreferencesDto = NotificationPreferencesDto()
)

class NotificationViewModel(
  private val notifRepo: NotificationRepository = NotificationRepository.getInstance(OneCampusApp.getAppContext()),
  private val demoRepo: DemoRepository = DemoRepository
) : ViewModel() {

  private val _selectedFilter = MutableStateFlow("All")
  val selectedFilter = _selectedFilter.asStateFlow()

  private val _preferences = MutableStateFlow(NotificationPreferencesDto())
  val preferences: StateFlow<NotificationPreferencesDto> = _preferences.asStateFlow()

  private val _isRefreshing = MutableStateFlow(false)
  val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

  val uiState: StateFlow<NotificationUiState> = combine(
    notifRepo.notifications,
    demoRepo.notifications,
    _selectedFilter,
    _preferences
  ) { roomList, demoList, filter, prefs ->
    // Combine room notifications with demo repository notifications for instant rich preview
    val combinedMap = linkedMapOf<String, NotificationItem>()
    for (item in roomList) combinedMap[item.id] = item
    for (item in demoList) {
      if (!combinedMap.containsKey(item.id)) {
        combinedMap[item.id] = item
      }
    }
    val allNotifs = combinedMap.values.toList().sortedByDescending { it.timestampEpochMs }

    val filtered = when (filter) {
      "Critical Alerts" -> allNotifs.filter { it.type == "DEADLINE" || it.priority.name == "CRITICAL" }
      "Assignments" -> allNotifs.filter { it.type == "ANNOUNCEMENT" || it.type == "TASK" }
      "Placement" -> allNotifs.filter { it.type == "PLACEMENT" }
      "Events" -> allNotifs.filter { it.type == "AI_SUMMARY" || it.type == "GENERAL" }
      else -> allNotifs
    }

    NotificationUiState(
      notifications = filtered,
      selectedFilter = filter,
      unreadCount = allNotifs.count { !it.isRead },
      isLoading = false,
      preferences = prefs
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = NotificationUiState(isLoading = true)
  )

  init {
    refresh()
    loadPreferences()
  }

  fun refresh() {
    viewModelScope.launch {
      _isRefreshing.value = true
      notifRepo.refreshNotifications()
      _isRefreshing.value = false
    }
  }

  fun loadPreferences() {
    viewModelScope.launch {
      val res = notifRepo.getPreferences()
      if (res.isSuccess) {
        _preferences.value = res.getOrDefault(NotificationPreferencesDto())
      }
    }
  }

  fun updatePreference(
    pushEnabled: Boolean? = null,
    criticalAlerts: Boolean? = null,
    placementAlerts: Boolean? = null,
    assignmentAlerts: Boolean? = null,
    eventAlerts: Boolean? = null,
    deadlineReminders: Boolean? = null
  ) {
    viewModelScope.launch {
      val cur = _preferences.value
      val updated = cur.copy(
        pushEnabled = pushEnabled ?: cur.pushEnabled,
        criticalAlerts = criticalAlerts ?: cur.criticalAlerts,
        placementAlerts = placementAlerts ?: cur.placementAlerts,
        assignmentAlerts = assignmentAlerts ?: cur.assignmentAlerts,
        eventAlerts = eventAlerts ?: cur.eventAlerts,
        deadlineReminders = deadlineReminders ?: cur.deadlineReminders
      )
      _preferences.value = updated

      notifRepo.updatePreferences(
        UpdateNotificationPreferencesRequestDto(
          pushEnabled = pushEnabled,
          criticalAlerts = criticalAlerts,
          placementAlerts = placementAlerts,
          assignmentAlerts = assignmentAlerts,
          eventAlerts = eventAlerts,
          deadlineReminders = deadlineReminders
        )
      )
    }
  }

  fun setFilter(filter: String) {
    _selectedFilter.value = filter
  }

  fun markAllRead() {
    viewModelScope.launch {
      demoRepo.markAllNotificationsRead()
      notifRepo.markAllAsRead()
    }
  }

  fun markAsRead(notifId: String) {
    viewModelScope.launch {
      demoRepo.markNotificationRead(notifId)
      notifRepo.markAsRead(notifId)
    }
  }
}
