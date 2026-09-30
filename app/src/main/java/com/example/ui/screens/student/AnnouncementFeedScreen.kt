package com.example.ui.screens.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.core.constants.AppConstants
import com.example.core.data.AnnouncementRepository
import com.example.core.data.DemoRepository
import com.example.core.model.Announcement
import com.example.core.model.PriorityLevel
import com.example.core.model.Role
import com.example.ui.components.announcement.AnnouncementCard
import com.example.ui.components.app.OneCampusAppScaffold
import com.example.ui.components.app.OneCampusTopAppBar
import com.example.ui.components.category.CategoryChip
import kotlinx.coroutines.launch

@Composable
fun AnnouncementFeedScreen(
  onNavigateBack: () -> Unit,
  onNavigateToDetail: (String) -> Unit,
  onNavigateToSearch: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedTab by remember { mutableStateOf(0) }
  val tabs = listOf("All", "Critical", "High", "Medium", "Low")
  var selectedCategory by remember { mutableStateOf("All") }
  val coroutineScope = rememberCoroutineScope()

  val repo = AnnouncementRepository.getInstance()
  val feedAnnouncements by repo.feedAnnouncements.collectAsStateWithLifecycle()

  LaunchedEffect(Unit) {
    repo.refreshPersonalizedFeed()
  }

  val activeList = if (feedAnnouncements.isNotEmpty()) feedAnnouncements else DemoRepository.announcements.value

  val filteredAnnouncements = remember(activeList, selectedCategory, selectedTab) {
    activeList.filter { ann ->
      val matchesCategory = selectedCategory == "All" || ann.category.equals(selectedCategory, ignoreCase = true)
      val matchesPriority = when (selectedTab) {
        1 -> ann.priority == PriorityLevel.CRITICAL
        2 -> ann.priority == PriorityLevel.HIGH
        3 -> ann.priority == PriorityLevel.MEDIUM
        4 -> ann.priority == PriorityLevel.LOW
        else -> true
      }
      matchesCategory && matchesPriority
    }
  }

  val onBookmarkToggle = remember(repo, coroutineScope) {
    { id: String ->
      coroutineScope.launch {
        repo.toggleBookmark(id)
      }
    }
  }

  OneCampusAppScaffold(
    topBar = {
      OneCampusTopAppBar(
        title = "Personalized Feed",
        showBackButton = true,
        onBackClick = onNavigateBack,
        actions = {
          IconButton(onClick = onNavigateToSearch) {
            Icon(Icons.Filled.Search, contentDescription = "Search")
          }
        }
      )
    },
    modifier = modifier.testTag("announcement_feed_screen")
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      TabRow(
        selectedTabIndex = selectedTab,
        containerColor = MaterialTheme.colorScheme.surface
      ) {
        tabs.forEachIndexed { index, title ->
          Tab(
            selected = selectedTab == index,
            onClick = { selectedTab = index },
            text = {
              Text(
                text = title,
                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
              )
            }
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val categories = listOf("All") + AppConstants.CATEGORIES.take(8)
        items(categories, key = { it }) { cat ->
          CategoryChip(
            category = cat,
            isSelected = selectedCategory == cat,
            onSelected = { selectedCategory = it }
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxSize()
      ) {
        items(filteredAnnouncements, key = { it.id }) { ann ->
          AnnouncementCard(
            announcement = ann,
            onClick = { onNavigateToDetail(ann.id) },
            onBookmarkToggle = { onBookmarkToggle(ann.id) },
            onActionClick = { onNavigateToDetail(ann.id) }
          )
        }
      }
    }
  }
}

