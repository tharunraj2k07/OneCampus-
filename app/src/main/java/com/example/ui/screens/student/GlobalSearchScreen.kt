package com.example.ui.screens.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
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
import com.example.ui.components.states.EmptyStateView

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GlobalSearchScreen(
  onNavigateBack: () -> Unit,
  onNavigateToDetail: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val announcements by DemoRepository.announcements.collectAsState()
  var searchQuery by remember { mutableStateOf("") }

  val trendingSuggestions = listOf(
    "Zoho Campus Drive",
    "DAA Assignment",
    "Amazon Internship",
    "Exam Fee",
    "Gemini Workshop",
    "Smart India Hackathon"
  )

  val filtered = remember(searchQuery, announcements) {
    if (searchQuery.isBlank()) {
      emptyList()
    } else {
      announcements.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
          it.category.contains(searchQuery, ignoreCase = true) ||
          it.aiSummary.contains(searchQuery, ignoreCase = true) ||
          it.rawContent.contains(searchQuery, ignoreCase = true) ||
          it.publisherName.contains(searchQuery, ignoreCase = true)
      }
    }
  }

  OneCampusAppScaffold(
    topBar = {
      OneCampusTopAppBar(
        title = "Global Campus Search",
        showBackButton = true,
        onBackClick = onNavigateBack
      )
    },
    modifier = modifier.testTag("global_search_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        placeholder = { Text("Search by topic, company, subject, faculty...") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        trailingIcon = {
          if (searchQuery.isNotEmpty()) {
            IconButton(onClick = { searchQuery = "" }) {
              Icon(Icons.Filled.Clear, contentDescription = "Clear")
            }
          }
        },
        shape = RoundedCornerShape(14.dp),
        singleLine = true,
        modifier = Modifier
          .fillMaxWidth()
          .testTag("search_query_input")
      )

      Spacer(modifier = Modifier.height(14.dp))

      if (searchQuery.isBlank()) {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.TrendingUp,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary
            )
            Text(
              text = "  Trending Searches",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            trendingSuggestions.forEach { suggestion ->
              SuggestionChip(
                onClick = { searchQuery = suggestion },
                label = { Text(suggestion) }
              )
            }
          }
        }
      } else {
        if (filtered.isEmpty()) {
          EmptyStateView(
            title = "No Matching Circulars",
            message = "We could not find any circulars matching '$searchQuery'. Try checking other keywords."
          )
        } else {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Found ${filtered.size} results for '$searchQuery'",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            modifier = Modifier.fillMaxSize()
          ) {
            items(filtered, key = { it.id }) { ann ->
              AnnouncementCard(
                announcement = ann,
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
}
