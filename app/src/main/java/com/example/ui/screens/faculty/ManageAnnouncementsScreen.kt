package com.example.ui.screens.faculty

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.core.model.Announcement
import com.example.ui.components.announcement.AnnouncementCard
import com.example.ui.components.app.ButtonVariant
import com.example.ui.components.app.OneCampusAppScaffold
import com.example.ui.components.app.OneCampusButton
import com.example.ui.components.app.OneCampusTopAppBar
import com.example.ui.components.category.CategoryBadge
import com.example.ui.components.states.EmptyStateView
import com.example.ui.viewmodel.FacultyAnnouncementViewModel
import com.example.ui.viewmodel.FacultyTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAnnouncementsScreen(
  onNavigateBack: () -> Unit,
  onNavigateToPreview: (String) -> Unit,
  viewModel: FacultyAnnouncementViewModel = viewModel(),
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val uiState by viewModel.uiState.collectAsState()

  OneCampusAppScaffold(
    topBar = {
      OneCampusTopAppBar(
        title = "Manage Circulars",
        showBackButton = true,
        onBackClick = onNavigateBack
      )
    },
    modifier = modifier.testTag("manage_announcements_screen")
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Tab Row
      PrimaryTabRow(
        selectedTabIndex = uiState.selectedTab.ordinal,
        modifier = Modifier.fillMaxWidth()
      ) {
        Tab(
          selected = uiState.selectedTab == FacultyTab.PUBLISHED,
          onClick = { viewModel.setTab(FacultyTab.PUBLISHED) },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text("Published")
              if (uiState.totalPublishedCount > 0) {
                Badge(modifier = Modifier.padding(start = 6.dp)) {
                  Text("${uiState.totalPublishedCount}")
                }
              }
            }
          }
        )
        Tab(
          selected = uiState.selectedTab == FacultyTab.DRAFT,
          onClick = { viewModel.setTab(FacultyTab.DRAFT) },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text("Drafts")
              if (uiState.totalDraftCount > 0) {
                Badge(modifier = Modifier.padding(start = 6.dp)) {
                  Text("${uiState.totalDraftCount}")
                }
              }
            }
          }
        )
        Tab(
          selected = uiState.selectedTab == FacultyTab.ARCHIVED,
          onClick = { viewModel.setTab(FacultyTab.ARCHIVED) },
          text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text("Archived")
              if (uiState.totalArchivedCount > 0) {
                Badge(modifier = Modifier.padding(start = 6.dp)) {
                  Text("${uiState.totalArchivedCount}")
                }
              }
            }
          }
        )
      }

      if (uiState.filteredAnnouncements.isEmpty()) {
        val (emptyTitle, emptyMsg) = when (uiState.selectedTab) {
          FacultyTab.PUBLISHED -> "No Published Circulars" to "You have not published any announcements to students yet."
          FacultyTab.DRAFT -> "No Draft Announcements" to "You don't have any saved drafts."
          FacultyTab.ARCHIVED -> "No Archived Circulars" to "Old or expired announcements you archive will appear here."
        }
        EmptyStateView(
          title = emptyTitle,
          message = emptyMsg
        )
      } else {
        LazyColumn(
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp),
          modifier = Modifier.fillMaxSize()
        ) {
          items(uiState.filteredAnnouncements, key = { it.id }) { ann ->
            FacultyManagedAnnouncementCard(
              announcement = ann,
              tab = uiState.selectedTab,
              onPreview = { onNavigateToPreview(ann.id) },
              onPublishDraft = {
                viewModel.publishExistingDraft(ann.id) { _, msg ->
                  Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
              },
              onArchive = {
                viewModel.archiveAnnouncement(ann.id) { _, msg ->
                  Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
              },
              onDelete = {
                viewModel.deleteAnnouncement(ann.id) { _, msg ->
                  Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
              }
            )
          }
        }
      }
    }
  }
}

@Composable
private fun FacultyManagedAnnouncementCard(
  announcement: Announcement,
  tab: FacultyTab,
  onPreview: () -> Unit,
  onPublishDraft: () -> Unit,
  onArchive: () -> Unit,
  onDelete: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        CategoryBadge(category = announcement.category)

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = when (announcement.status) {
            "PUBLISHED" -> MaterialTheme.colorScheme.primaryContainer
            "DRAFT" -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
          }
        ) {
          Text(
            text = announcement.status,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = when (announcement.status) {
              "PUBLISHED" -> MaterialTheme.colorScheme.primary
              "DRAFT" -> MaterialTheme.colorScheme.secondary
              else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Text(
        text = announcement.title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = announcement.rawContent,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2
      )

      Spacer(modifier = Modifier.height(12.dp))

      // Action row depending on Tab/Status
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onPreview) {
          Icon(
            imageVector = Icons.Filled.Visibility,
            contentDescription = "Preview announcement",
            tint = MaterialTheme.colorScheme.primary
          )
        }

        if (tab == FacultyTab.DRAFT) {
          IconButton(onClick = onDelete) {
            Icon(
              imageVector = Icons.Filled.Delete,
              contentDescription = "Delete draft",
              tint = MaterialTheme.colorScheme.error
            )
          }

          Spacer(modifier = Modifier.width(6.dp))

          OneCampusButton(
            text = "Publish",
            icon = Icons.Filled.Send,
            onClick = onPublishDraft,
            variant = ButtonVariant.PRIMARY
          )
        } else if (tab == FacultyTab.PUBLISHED) {
          Spacer(modifier = Modifier.width(6.dp))
          OneCampusButton(
            text = "Archive",
            icon = Icons.Filled.Archive,
            onClick = onArchive,
            variant = ButtonVariant.OUTLINE
          )
        }
      }
    }
  }
}

