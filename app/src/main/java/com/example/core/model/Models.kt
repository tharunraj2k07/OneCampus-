package com.example.core.model

import androidx.compose.runtime.Immutable

enum class Role {
  STUDENT,
  FACULTY,
  PLACEMENT_CELL,
  CLUB_COORDINATOR,
  DEPARTMENT_ADMIN,
  SUPER_ADMIN
}

enum class PriorityLevel(val label: String, val weight: Int) {
  CRITICAL("CRITICAL", 4),
  HIGH("HIGH", 3),
  MEDIUM("MEDIUM", 2),
  LOW("LOW", 1)
}

@Immutable
data class User(
  val id: String,
  val email: String,
  val fullName: String,
  val role: Role,
  val avatarUrl: String? = null
)

@Immutable
data class StudentProfile(
  val userId: String,
  val fullName: String,
  val collegeEmail: String,
  val registerNumber: String,
  val department: String,
  val year: Int,
  val section: String,
  val cgpa: Double = 8.5,
  val interests: List<String> = emptyList(),
  val focusAreas: List<String> = emptyList(),
  val preferredCategories: List<String> = emptyList(),
  val profileCompletionPercentage: Int = 0
) {
  fun calculateCompletion(): Int {
    var filledFields = 0
    val totalFields = 7
    if (fullName.isNotBlank()) filledFields++
    if (collegeEmail.isNotBlank()) filledFields++
    if (registerNumber.isNotBlank()) filledFields++
    if (department.isNotBlank()) filledFields++
    if (year > 0) filledFields++
    if (section.isNotBlank()) filledFields++
    if (interests.isNotEmpty()) filledFields++
    return ((filledFields.toDouble() / totalFields) * 100).toInt()
  }
}

@Immutable
data class Announcement(
  val id: String,
  val title: String,
  val rawContent: String,
  val aiSummary: String,
  val category: String,
  val publisherName: String,
  val publisherRole: Role = Role.FACULTY,
  val publisherDepartment: String? = null,
  val publisherDesignation: String? = null,
  val isVerifiedPublisher: Boolean = true,
  val targetDepartments: List<String> = listOf("ALL"),
  val targetYears: List<Int> = listOf(1, 2, 3, 4),
  val targetSections: List<String> = emptyList(),
  val minCgpa: Double = 0.0,
  val interestTags: List<String> = emptyList(),
  val deadlineEpochMs: Long? = null,
  val requiredAction: String? = null,
  val externalLink: String? = null,
  val attachmentName: String? = null,
  val priority: PriorityLevel = PriorityLevel.MEDIUM,
  val priorityReason: String = "",
  val status: String = "PUBLISHED",
  val publishedAtEpochMs: Long? = null,
  val createdAtEpochMs: Long = System.currentTimeMillis(),
  val isBookmarked: Boolean = false,

  // Phase 7 AI Structured Intelligence & Hybrid Priority
  val priorityScore: Int = 50,
  val priorityExplanation: List<String> = emptyList(),
  val keyActions: List<String> = emptyList(),
  val aiAnalysisStatus: String = "COMPLETED", // PENDING, PROCESSING, COMPLETED, FAILED
  val aiConfidence: Float = 0.9f,
  val keywords: List<String> = emptyList(),
  val requiredDocuments: List<String> = emptyList(),
  val locationOrPlatform: String? = null,
  val extractedDeadlineEpochMs: Long? = null,
  val deadlineConflict: Boolean = false,

  // Phase 8 Personalization
  val relevance: RelevanceInfo? = null
)

@Immutable
data class RelevanceInfo(
  val score: Int = 50,
  val level: String = "MEDIUM", // VERY_HIGH, HIGH, MEDIUM, LOW
  val explanation: List<String> = emptyList()
)

@Immutable
data class TaskItem(
  val id: String,
  val announcementId: String? = null,
  val title: String,
  val description: String? = null,
  val category: String = "ACADEMICS",
  val deadlineEpochMs: Long? = null,
  val priority: PriorityLevel = PriorityLevel.MEDIUM,
  val isCompleted: Boolean = false,
  val status: String = if (isCompleted) "COMPLETED" else "PENDING", // PENDING, IN_PROGRESS, COMPLETED, DISMISSED
  val actionIdentifier: String? = null,
  val requiredAction: String = "Action Required"
)

@Immutable
data class NotificationItem(
  val id: String,
  val title: String,
  val message: String,
  val type: String, // DEADLINE, ANNOUNCEMENT, AI_SUMMARY, PLACEMENT, EVENT, CRITICAL_ALERT
  val timestampEpochMs: Long = System.currentTimeMillis(),
  val isRead: Boolean = false,
  val announcementId: String? = null,
  val taskId: String? = null,
  val priority: PriorityLevel = PriorityLevel.MEDIUM
)

@Immutable
data class AIChatMessage(
  val id: String,
  val sender: MessageSender,
  val text: String,
  val timestampEpochMs: Long = System.currentTimeMillis(),
  val referencedAnnouncementIds: List<String> = emptyList()
)

enum class MessageSender {
  STUDENT,
  AI_ASSISTANT
}
