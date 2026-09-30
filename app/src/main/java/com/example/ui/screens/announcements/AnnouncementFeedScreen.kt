package com.example.ui.screens.announcements

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.constants.AppConstants
import com.example.ui.components.announcement.AnnouncementCard
import com.example.ui.components.app.OneCampusAppScaffold
import com.example.ui.components.app.OneCampusTopAppBar
import com.example.ui.components.category.CategoryChip
import com.example.ui.components.states.EmptyStateView
import com.example.ui.viewmodel.AnnouncementViewModel

@Composable
fun AnnouncementFeedScreen(
  onNavigateToAnnouncementDetail: (String) -> Unit,
  onNavigateToSearch: () -> Unit,
  onNavigateToBookmarks: () -> Unit,
  viewModel: AnnouncementViewModel = viewModel(),
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.feedUiState.collectAsState()
  var isSearchExpanded by remember { mutableStateOf(false) }

  val priorityTabs = listOf("All", "CRITICAL", "HIGH", "MEDIUM", "LOW")
  val selectedTabIndex = priorityTabs.indexOf(uiState.selectedPriorityTab).coerceAtLeast(0)

  OneCampusAppScaffold(
    topBar = {
      OneCampusTopAppBar(
        title = "Announcements Feed",
        actions = {
          IconButton(
            onClick = { isSearchExpanded = !isSearchExpanded },
            modifier = Modifier.testTag("feed_search_toggle")
          ) {
            Icon(Icons.Filled.Search, contentDescription = "Search feed")
          }
          IconButton(
            onClick = onNavigateToBookmarks,
            modifier = Modifier.testTag("feed_bookmarks_button")
          ) {
            Icon(Icons.Filled.Bookmark, contentDescription = "Saved bookmarks")
          }
        }
      )
    },
    modifier = modifier.testTag("announcement_feed_screen")
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // In-line Search Bar when activated
      if (isSearchExpanded) {
        OutlinedTextField(
          value = uiState.searchQuery,
          onValueChange = { viewModel.setSearchQuery(it) },
          placeholder = { Text("Filter announcements by keyword...") },
          leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
          trailingIcon = {
            if (uiState.searchQuery.isNotEmpty()) {
              IconButton(onClick = { viewModel.setSearchQuery("") }) {
                Icon(Icons.Filled.Close, contentDescription = "Clear search")
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
        )
      }

      // Priority Filter Tabs
      ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        indicator = { tabPositions ->
          if (selectedTabIndex < tabPositions.size) {
            TabRowDefaults.SecondaryIndicator(
              modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
              color = MaterialTheme.colorScheme.primary
            )
          }
        },
        divider = {}
      ) {
        priorityTabs.forEach { tab ->
          Tab(
            selected = uiState.selectedPriorityTab == tab,
            onClick = { viewModel.setPriorityTab(tab) },
            text = {
              Text(
                text = tab,
                fontWeight = if (uiState.selectedPriorityTab == tab) FontWeight.Bold else FontWeight.Normal
              )
            }
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Category Filter Chips
      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val categories = listOf("All") + AppConstants.CATEGORIES
        items(categories) { category ->
          CategoryChip(
            category = category,
            isSelected = uiState.selectedCategory == category,
            onSelected = { viewModel.setCategory(it) }
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Announcements List / Empty State
      if (uiState.announcements.isEmpty()) {
        EmptyStateView(
          title = "No Announcements Found",
          message = "Try changing your priority filter or category chip to see more notices."
        )
      } else {
        LazyColumn(
          contentPadding = PaddingValues(bottom = 24.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          item {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Showing ${uiState.announcements.size} circulars",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              if (uiState.selectedPriorityTab != "All" || uiState.selectedCategory != "All") {
                Text(
                  text = "Filtered by ${uiState.selectedPriorityTab} • ${uiState.selectedCategory}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.SemiBold
                )
              }
            }
          }

          items(uiState.announcements, key = { it.id }) { announcement ->
            AnnouncementCard(
              announcement = announcement,
              onClick = { onNavigateToAnnouncementDetail(announcement.id) },
              onBookmarkToggle = { viewModel.toggleBookmark(announcement.id) },
              onActionClick = { onNavigateToAnnouncementDetail(announcement.id) },
              modifier = Modifier.padding(horizontal = 16.dp)
            )
          }
        }
      }
    }
  }
}
