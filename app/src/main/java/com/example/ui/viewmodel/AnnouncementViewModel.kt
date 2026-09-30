package com.example.ui.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.AnnouncementRepository
import com.example.core.model.Announcement
import com.example.core.model.PriorityLevel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class AnnouncementFeedUiState(
  val announcements: List<Announcement> = emptyList(),
  val selectedPriorityTab: String = "All",
  val selectedCategory: String = "All",
  val searchQuery: String = "",
  val bookmarkedAnnouncements: List<Announcement> = emptyList(),
  val isLoading: Boolean = false,
  val errorMessage: String? = null
)

class AnnouncementViewModel(
  private val repository: AnnouncementRepository = AnnouncementRepository.getInstance()
) : ViewModel() {

  private val _selectedPriorityTab = MutableStateFlow("All")
  val selectedPriorityTab = _selectedPriorityTab.asStateFlow()

  private val _selectedCategory = MutableStateFlow("All")
  val selectedCategory = _selectedCategory.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery = _searchQuery.asStateFlow()

  init {
    refreshFeed()
  }

  val feedUiState: StateFlow<AnnouncementFeedUiState> = combine(
    repository.feedAnnouncements,
    _selectedPriorityTab,
    _selectedCategory,
    _searchQuery
  ) { list, priorityTab, category, query ->
    var filtered = list.filter { it.status == "PUBLISHED" }

    if (priorityTab != "All") {
      filtered = filtered.filter { it.priority.name.equals(priorityTab, ignoreCase = true) }
    }

    if (category != "All") {
      filtered = filtered.filter { it.category.equals(category, ignoreCase = true) }
    }

    if (query.isNotBlank()) {
      filtered = filtered.filter {
        it.title.contains(query, ignoreCase = true) ||
          it.aiSummary.contains(query, ignoreCase = true) ||
          it.category.contains(query, ignoreCase = true) ||
          it.rawContent.contains(query, ignoreCase = true) ||
          it.publisherName.contains(query, ignoreCase = true)
      }
    }

    val bookmarked = list.filter { it.isBookmarked }

    AnnouncementFeedUiState(
      announcements = filtered,
      selectedPriorityTab = priorityTab,
      selectedCategory = category,
      searchQuery = query,
      bookmarkedAnnouncements = bookmarked,
      isLoading = false,
      errorMessage = null
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = AnnouncementFeedUiState(isLoading = true)
  )

  fun refreshFeed() {
    viewModelScope.launch {
      repository.refreshStudentFeed(
        category = _selectedCategory.value,
        search = _searchQuery.value
      )
    }
  }

  fun setPriorityTab(tab: String) {
    _selectedPriorityTab.value = tab
  }

  fun setCategory(category: String) {
    _selectedCategory.value = category
    refreshFeed()
  }

  fun setSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun toggleBookmark(announcementId: String) {
    repository.toggleBookmark(announcementId)
  }

  fun getAnnouncementById(id: String): Announcement? {
    return repository.feedAnnouncements.value.find { it.id == id }
      ?: repository.facultyAnnouncements.value.find { it.id == id }
  }
}

