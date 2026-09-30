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
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.MaterialTheme
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
import com.example.core.data.DemoRepository
import com.example.ui.components.announcement.AnnouncementCard
import com.example.ui.components.app.OneCampusAppScaffold
import com.example.ui.components.app.OneCampusTopAppBar
import com.example.ui.components.category.CategoryChip
import com.example.ui.components.states.EmptyStateView

@Composable
fun BookmarksScreen(
  onNavigateBack: () -> Unit,
  onNavigateToDetail: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val announcements by DemoRepository.announcements.collectAsState()
  var selectedCategory by remember { mutableStateOf("All") }

  val bookmarkedAnnouncements = remember(announcements, selectedCategory) {
    val saved = announcements.filter { it.isBookmarked }
    if (selectedCategory == "All") saved
    else saved.filter { it.category.equals(selectedCategory, ignoreCase = true) }
  }

  OneCampusAppScaffold(
    topBar = {
      OneCampusTopAppBar(
        title = "Saved Bookmarks",
        showBackButton = true,
        onBackClick = onNavigateBack
      )
    },
    modifier = modifier.testTag("bookmarks_screen")
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Category filter chips
      LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val categories = listOf("All", "Placement", "Internship", "Assignment", "Workshop", "Competition")
        items(categories) { category ->
          CategoryChip(
            category = category,
            isSelected = selectedCategory == category,
            onSelected = { selectedCategory = it }
          )
        }
      }

      if (bookmarkedAnnouncements.isEmpty()) {
        EmptyStateView(
          title = "No Bookmarks Saved",
          message = "Save important circulars and deadlines by tapping the bookmark icon on any announcement card."
        )
      } else {
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(14.dp),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          item {
            Text(
              text = "Saved Circulars (${bookmarkedAnnouncements.size})",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold
            )
          }

          items(bookmarkedAnnouncements, key = { it.id }) { ann ->
            AnnouncementCard(
              announcement = ann,
              isBookmarked = true,
              onClick = { onNavigateToDetail(ann.id) },
              onBookmarkToggle = { DemoRepository.toggleBookmark(ann.id) },
              onActionClick = { onNavigateToDetail(ann.id) }
            )
          }
        }
      }
    }
  }
}
