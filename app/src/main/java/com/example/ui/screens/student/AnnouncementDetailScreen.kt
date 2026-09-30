package com.example.ui.screens.student

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Attachment
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.data.AnnouncementRepository
import com.example.core.data.DemoRepository
import com.example.core.data.TaskRepository
import com.example.core.model.Announcement
import com.example.core.model.PriorityLevel
import com.example.core.model.Role
import com.example.ui.components.announcement.DeadlineIndicator
import com.example.ui.components.announcement.TimeRemainingIndicator
import com.example.ui.components.app.ButtonVariant
import com.example.ui.components.app.OneCampusAppScaffold
import com.example.ui.components.app.OneCampusButton
import com.example.ui.components.app.OneCampusCard
import com.example.ui.components.app.OneCampusTopAppBar
import com.example.ui.components.category.CategoryBadge
import com.example.ui.components.personalization.PersonalizedExplanationCard
import com.example.ui.components.priority.PriorityBadge
import com.example.ui.components.priority.PriorityScoreDisplay
import com.example.ui.theme.AiGlowCyan
import com.example.ui.theme.AiGlowPurple
import com.example.ui.theme.BrandBlueDark
import com.example.ui.theme.BrandBluePrimary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnnouncementDetailScreen(
  announcementId: String,
  onNavigateBack: () -> Unit,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val feedAnnouncements by AnnouncementRepository.getInstance().feedAnnouncements.collectAsState()
  val facultyAnnouncements by AnnouncementRepository.getInstance().facultyAnnouncements.collectAsState()
  val studentProfile by DemoRepository.studentProfile.collectAsState()

  val allItems = feedAnnouncements + facultyAnnouncements
  val announcement = allItems.find { it.id == announcementId }
    ?: DemoRepository.announcements.value.find { it.id == announcementId }
    ?: allItems.firstOrNull()
    ?: Announcement(
      id = announcementId,
      title = "Official Circular Notice",
      rawContent = "Circular content",
      aiSummary = "Summary",
      category = "General Announcement",
      publisherName = "Administration",
      publisherRole = Role.FACULTY,
      targetDepartments = listOf("ALL"),
      targetYears = listOf(1, 2, 3, 4)
    )

  var isCompleted by remember { mutableStateOf(false) }
  val checkedActions = remember { mutableStateMapOf<Int, Boolean>() }
  val coroutineScope = rememberCoroutineScope()
  var isAnalyzing by remember { mutableStateOf(false) }

  OneCampusAppScaffold(
    topBar = {
      OneCampusTopAppBar(
        title = "Announcement Details",
        showBackButton = true,
        onBackClick = onNavigateBack,
        actions = {
          IconButton(onClick = { AnnouncementRepository.getInstance().toggleBookmark(announcement.id) }) {
            Icon(
              imageVector = if (announcement.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
              contentDescription = "Bookmark",
              tint = if (announcement.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
          }
          IconButton(onClick = {
            val sendIntent: Intent = Intent().apply {
              action = Intent.ACTION_SEND
              putExtra(Intent.EXTRA_TEXT, "${announcement.title}\n\n${announcement.aiSummary}\n\nShared via OneCampus AI")
              type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share Announcement"))
          }) {
            Icon(Icons.Filled.Share, contentDescription = "Share")
          }
        }
      )
    },
    modifier = modifier.testTag("announcement_detail_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 0. Deadline Conflict Alert Banner if detected
      if (announcement.deadlineConflict) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Filled.Warning,
              contentDescription = "Deadline Conflict",
              tint = MaterialTheme.colorScheme.error,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Schedule Conflict Warning",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
              )
              Text(
                text = "Multiple critical deadlines overlap on this date. Review your timeline carefully.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f)
              )
            }
          }
        }
      }

      // 1. Badges Row & AI Verification Tag
      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
          CategoryBadge(category = announcement.category)
          PriorityBadge(priority = announcement.priority)
        }

        Surface(
          shape = RoundedCornerShape(8.dp),
          color = when (announcement.aiAnalysisStatus) {
            "COMPLETED" -> MaterialTheme.colorScheme.primaryContainer
            "PROCESSING" -> MaterialTheme.colorScheme.tertiaryContainer
            "FAILED" -> MaterialTheme.colorScheme.errorContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
          }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Filled.AutoAwesome,
              contentDescription = null,
              tint = when (announcement.aiAnalysisStatus) {
                "COMPLETED" -> MaterialTheme.colorScheme.primary
                "FAILED" -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
              },
              modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (announcement.aiAnalysisStatus == "COMPLETED") {
                "AI Verified (${(announcement.aiConfidence * 100).toInt()}%)"
              } else {
                "AI ${announcement.aiAnalysisStatus}"
              },
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Bold,
              color = when (announcement.aiAnalysisStatus) {
                "COMPLETED" -> MaterialTheme.colorScheme.onPrimaryContainer
                "FAILED" -> MaterialTheme.colorScheme.onErrorContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant
              }
            )
          }
        }
      }

      // 2. Title & Publisher
      Column {
        Text(
          text = announcement.title,
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.ExtraBold,
          color = MaterialTheme.colorScheme.onSurface,
          lineHeight = 28.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(28.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
            }
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = announcement.publisherName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
              )
              if (announcement.isVerifiedPublisher) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                  imageVector = Icons.Filled.Verified,
                  contentDescription = "Verified Publisher",
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(15.dp)
                )
              }
            }
            Text(
              text = "${announcement.publisherDepartment} • Official ${announcement.publisherRole.name.replace("_", " ")} Notice",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      // Phase 8 Personalization Match Card
      announcement.relevance?.let { rel ->
        PersonalizedExplanationCard(relevance = rel)
      }

      // 3. ✨ Phase 7 Gemini AI Summary & Action Checklist Card
      OneCampusCard(
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
        accentColor = AiGlowCyan
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Filled.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "✨ ONE CAMPUS AI INTELLIGENCE",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 0.5.sp
              )
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
            ) {
              Text(
                text = "Gemini 3.6 Flash",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = announcement.aiSummary,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Medium
          )

          // Key Actions Checklist
          if (announcement.keyActions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = "Action Checklist (${checkedActions.count { it.value }}/${announcement.keyActions.size})",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
              announcement.keyActions.forEachIndexed { index, actionText ->
                val isChecked = checkedActions[index] == true
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (isChecked) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surface,
                  border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isChecked) MaterialTheme.colorScheme.primary.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant
                  ),
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      val newState = !isChecked
                      checkedActions[index] = newState
                      coroutineScope.launch {
                        TaskRepository.getInstance().tasks.value
                          .find { it.announcementId == announcement.id && it.title.contains(actionText.take(20)) }
                          ?.let { TaskRepository.getInstance().toggleTask(it.id) }
                      }
                    }
                ) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(
                      imageVector = if (isChecked) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                      contentDescription = if (isChecked) "Checked" else "Unchecked",
                      tint = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                      modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = actionText,
                      style = MaterialTheme.typography.bodySmall,
                      fontWeight = if (isChecked) FontWeight.SemiBold else FontWeight.Normal,
                      color = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                  }
                }
              }
            }
          }

          // Venue & Required Documents
          if (!announcement.locationOrPlatform.isNullOrBlank() || announcement.requiredDocuments.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              if (!announcement.locationOrPlatform.isNullOrBlank()) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                  modifier = Modifier.weight(1f)
                ) {
                  Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                      Text("Platform/Venue", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                      Text(announcement.locationOrPlatform!!, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                    }
                  }
                }
              }

              if (announcement.requiredDocuments.isNotEmpty()) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                  modifier = Modifier.weight(1f)
                ) {
                  Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                      Text("Required Docs", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                      Text(announcement.requiredDocuments.joinToString(", "), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 1)
                    }
                  }
                }
              }
            }
          }

          // Keywords FlowRow
          if (announcement.keywords.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              announcement.keywords.forEach { kw ->
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                  border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                  Text(
                    text = "#$kw",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              }
            }
          }
        }
      }

      // 4. ⭐ "Why is this important to me?" Personalized Insight Card
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Filled.CheckCircle,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Why is this important to you?",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          Spacer(modifier = Modifier.height(10.dp))

          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Targeted to your department: ${studentProfile.department}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Targeted for Year ${studentProfile.year} students",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
            if (announcement.minCgpa > 0.0) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "CGPA Requirement: ${announcement.minCgpa} (Your CGPA: ${studentProfile.cgpa} ✓)",
                  style = MaterialTheme.typography.bodySmall,
                  fontWeight = FontWeight.SemiBold,
                  color = MaterialTheme.colorScheme.primary
                )
              }
            }
            if (announcement.interestTags.any { studentProfile.interests.contains(it) }) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Matches your interests in: ${announcement.interestTags.filter { studentProfile.interests.contains(it) }.joinToString(", ")}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          }
        }
      }

      // 5. Phase 7 Hybrid Priority Score & Deterministic Breakdown
      PriorityScoreDisplay(
        priority = announcement.priority,
        score = announcement.priorityScore.toDouble(),
        reason = announcement.priorityReason,
        explanations = announcement.priorityExplanation
      )

      // On-demand AI Re-analyze button
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
          .fillMaxWidth()
          .clickable(enabled = !isAnalyzing) {
            coroutineScope.launch {
              isAnalyzing = true
              AnnouncementRepository.getInstance().analyzeAnnouncement(announcement.id)
              isAnalyzing = false
            }
          }
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center
        ) {
          if (isAnalyzing) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Analyzing with Gemini AI...", style = MaterialTheme.typography.labelMedium)
          } else {
            Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              "Re-run AI Analysis & Recalculate Priority",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }

      // 6. Deadline & Time Remaining Countdown
      if (announcement.deadlineEpochMs != null) {
        OneCampusCard {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
              .fillMaxWidth()
              .padding(14.dp)
          ) {
            DeadlineIndicator(deadlineEpochMs = announcement.deadlineEpochMs)
            TimeRemainingIndicator(deadlineEpochMs = announcement.deadlineEpochMs)
          }
        }
      }

      // 7. Official Attachment if available
      if (announcement.attachmentName != null) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Simulated attachment download */ }
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(14.dp)
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Outlined.Attachment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text(
                  text = announcement.attachmentName,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = "Official PDF Document (2.4 MB)",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
            Icon(
              imageVector = Icons.Filled.Download,
              contentDescription = "Download attachment",
              tint = MaterialTheme.colorScheme.primary
            )
          }
        }
      }

      // 8. Official Circular Full Text
      Column {
        Text(
          text = "Official Circular Content",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surface,
          border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = announcement.rawContent,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            lineHeight = 22.sp,
            modifier = Modifier.padding(14.dp)
          )
        }
      }

      // 9. External Action Link / Application Button
      if (announcement.externalLink != null) {
        OneCampusButton(
          text = announcement.requiredAction ?: "Open External Portal",
          onClick = {
            try {
              val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(announcement.externalLink))
              context.startActivity(browserIntent)
            } catch (e: Exception) {
              // Fallback
            }
          },
          fullWidth = true,
          variant = ButtonVariant.PRIMARY,
          icon = Icons.Filled.OpenInNew,
          testTag = "detail_external_link_button"
        )
      }

      // 10. Mark Complete / Task Status
      OneCampusButton(
        text = if (isCompleted) "Completed ✓" else "Mark as Finished in Checklist",
        onClick = { isCompleted = !isCompleted },
        fullWidth = true,
        variant = if (isCompleted) ButtonVariant.SECONDARY else ButtonVariant.OUTLINE,
        testTag = "detail_mark_completed_button"
      )

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
