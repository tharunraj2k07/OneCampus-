package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.AnnouncementRepository
import com.example.core.model.Announcement
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

enum class FacultyTab {
  PUBLISHED,
  DRAFT,
  ARCHIVED
}

data class FacultyUiState(
  val announcements: List<Announcement> = emptyList(),
  val filteredAnnouncements: List<Announcement> = emptyList(),
  val selectedTab: FacultyTab = FacultyTab.PUBLISHED,
  val totalPublishedCount: Int = 0,
  val totalDraftCount: Int = 0,
  val totalArchivedCount: Int = 0,
  val activeCount: Int = 0,
  val studentsReachedCount: Int = 480,
  val upcomingDeadlinesCount: Int = 0,
  val isPublishing: Boolean = false,
  val actionFeedbackMessage: String? = null
)

class FacultyAnnouncementViewModel(
  private val repository: AnnouncementRepository = AnnouncementRepository.getInstance()
) : ViewModel() {

  private val _selectedTab = MutableStateFlow(FacultyTab.PUBLISHED)
  val selectedTab = _selectedTab.asStateFlow()

  private val _isPublishing = MutableStateFlow(false)
  private val _actionFeedbackMessage = MutableStateFlow<String?>(null)

  init {
    refreshFacultyAnnouncements()
  }

  val uiState: StateFlow<FacultyUiState> = combine(
    repository.facultyAnnouncements,
    _selectedTab,
    _isPublishing,
    _actionFeedbackMessage
  ) { list, tab, publishing, feedback ->
    val publishedItems = list.filter { it.status == "PUBLISHED" }
    val draftItems = list.filter { it.status == "DRAFT" }
    val archivedItems = list.filter { it.status == "ARCHIVED" }

    val currentFiltered = when (tab) {
      FacultyTab.PUBLISHED -> publishedItems
      FacultyTab.DRAFT -> draftItems
      FacultyTab.ARCHIVED -> archivedItems
    }

    FacultyUiState(
      announcements = list,
      filteredAnnouncements = currentFiltered,
      selectedTab = tab,
      totalPublishedCount = publishedItems.size,
      totalDraftCount = draftItems.size,
      totalArchivedCount = archivedItems.size,
      activeCount = publishedItems.size,
      studentsReachedCount = publishedItems.size * 120,
      upcomingDeadlinesCount = publishedItems.count { it.deadlineEpochMs != null && it.deadlineEpochMs > System.currentTimeMillis() },
      isPublishing = publishing,
      actionFeedbackMessage = feedback
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = FacultyUiState()
  )

  fun setTab(tab: FacultyTab) {
    _selectedTab.value = tab
  }

  fun clearFeedbackMessage() {
    _actionFeedbackMessage.value = null
  }

  fun refreshFacultyAnnouncements() {
    viewModelScope.launch {
      repository.refreshFacultyAnnouncements()
    }
  }

  private fun formatEpochToIso(epochMs: Long?): String? {
    if (epochMs == null) return null
    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
      timeZone = TimeZone.getTimeZone("UTC")
    }
    return sdf.format(Date(epochMs))
  }

  fun saveDraftAnnouncement(
    title: String,
    rawContent: String,
    category: String,
    department: String,
    year: Int,
    section: String,
    deadlineEpochMs: Long?,
    externalLink: String?,
    onComplete: (Boolean, String) -> Unit
  ) {
    viewModelScope.launch {
      _isPublishing.value = true
      val depts = if (department == "ALL") listOf("ALL") else listOf(department)
      val years = listOf(year)
      val sections = if (section.isNotBlank()) listOf(section) else emptyList()
      val deadlineIso = formatEpochToIso(deadlineEpochMs)

      val result = repository.createAnnouncement(
        title = title,
        content = rawContent,
        category = category,
        departments = depts,
        years = years,
        sections = sections,
        deadline = deadlineIso,
        externalLink = externalLink,
        status = "DRAFT"
      )

      _isPublishing.value = false
      if (result.isSuccess) {
        val msg = "Draft saved successfully."
        _actionFeedbackMessage.value = msg
        onComplete(true, msg)
      } else {
        val err = result.exceptionOrNull()?.message ?: "Failed to save draft."
        _actionFeedbackMessage.value = err
        onComplete(false, err)
      }
    }
  }

  fun publishNewAnnouncement(
    title: String,
    rawContent: String,
    category: String,
    department: String,
    year: Int,
    section: String,
    deadlineEpochMs: Long?,
    externalLink: String?,
    onComplete: (Boolean, String) -> Unit
  ) {
    viewModelScope.launch {
      _isPublishing.value = true
      val depts = if (department == "ALL") listOf("ALL") else listOf(department)
      val years = listOf(year)
      val sections = if (section.isNotBlank()) listOf(section) else emptyList()
      val deadlineIso = formatEpochToIso(deadlineEpochMs)

      val result = repository.createAnnouncement(
        title = title,
        content = rawContent,
        category = category,
        departments = depts,
        years = years,
        sections = sections,
        deadline = deadlineIso,
        externalLink = externalLink,
        status = "PUBLISHED"
      )

      _isPublishing.value = false
      if (result.isSuccess) {
        val msg = "Official announcement published to students."
        _actionFeedbackMessage.value = msg
        onComplete(true, msg)
      } else {
        val err = result.exceptionOrNull()?.message ?: "Failed to publish announcement."
        _actionFeedbackMessage.value = err
        onComplete(false, err)
      }
    }
  }

  fun publishExistingDraft(id: String, onComplete: ((Boolean, String) -> Unit)? = null) {
    viewModelScope.launch {
      _isPublishing.value = true
      val result = repository.publishAnnouncement(id)
      _isPublishing.value = false
      if (result.isSuccess) {
        val msg = "Announcement published to student feed."
        _actionFeedbackMessage.value = msg
        onComplete?.invoke(true, msg)
      } else {
        val err = result.exceptionOrNull()?.message ?: "Failed to publish draft."
        _actionFeedbackMessage.value = err
        onComplete?.invoke(false, err)
      }
    }
  }

  fun archiveAnnouncement(id: String, onComplete: ((Boolean, String) -> Unit)? = null) {
    viewModelScope.launch {
      _isPublishing.value = true
      val result = repository.archiveAnnouncement(id)
      _isPublishing.value = false
      if (result.isSuccess) {
        val msg = "Announcement archived."
        _actionFeedbackMessage.value = msg
        onComplete?.invoke(true, msg)
      } else {
        val err = result.exceptionOrNull()?.message ?: "Failed to archive announcement."
        _actionFeedbackMessage.value = err
        onComplete?.invoke(false, err)
      }
    }
  }

  fun deleteAnnouncement(id: String, onComplete: ((Boolean, String) -> Unit)? = null) {
    viewModelScope.launch {
      _isPublishing.value = true
      val result = repository.deleteAnnouncement(id)
      _isPublishing.value = false
      if (result.isSuccess) {
        val msg = "Announcement deleted."
        _actionFeedbackMessage.value = msg
        onComplete?.invoke(true, msg)
      } else {
        val err = result.exceptionOrNull()?.message ?: "Failed to delete announcement."
        _actionFeedbackMessage.value = err
        onComplete?.invoke(false, err)
      }
    }
  }
}

