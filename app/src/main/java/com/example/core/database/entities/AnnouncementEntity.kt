package com.example.core.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.core.model.Announcement
import com.example.core.model.PriorityLevel
import com.example.core.model.RelevanceInfo
import com.example.core.model.Role

@Entity(
  tableName = "announcements",
  indices = [
    Index(value = ["createdAtEpochMs"]),
    Index(value = ["isBookmarked", "createdAtEpochMs"]),
    Index(value = ["cachedAtEpochMs", "isBookmarked"]),
    Index(value = ["category"])
  ]
)
data class AnnouncementEntity(
  @PrimaryKey val id: String,
  val title: String,
  val rawContent: String,
  val aiSummary: String,
  val category: String,
  val publisherName: String,
  val publisherRole: String,
  val publisherDepartment: String?,
  val publisherDesignation: String?,
  val isVerifiedPublisher: Boolean,
  val targetDepartmentsCsv: String,
  val targetYearsCsv: String,
  val targetSectionsCsv: String,
  val minCgpa: Double,
  val interestTagsCsv: String,
  val deadlineEpochMs: Long?,
  val requiredAction: String?,
  val externalLink: String?,
  val attachmentName: String?,
  val priority: String,
  val priorityReason: String,
  val status: String,
  val publishedAtEpochMs: Long?,
  val createdAtEpochMs: Long,
  val isBookmarked: Boolean,
  val priorityScore: Int,
  val keyActionsCsv: String,
  val aiAnalysisStatus: String,
  val relevanceScore: Int,
  val relevanceLevel: String,
  val relevanceExplanationCsv: String,
  val cachedAtEpochMs: Long = System.currentTimeMillis()
) {
  fun toDomain(): Announcement {
    val prio = try {
      PriorityLevel.valueOf(priority)
    } catch (_: Exception) {
      PriorityLevel.MEDIUM
    }

    val role = try {
      Role.valueOf(publisherRole)
    } catch (_: Exception) {
      Role.FACULTY
    }

    val depts = if (targetDepartmentsCsv.isBlank()) listOf("ALL") else targetDepartmentsCsv.split(",")
    val years = if (targetYearsCsv.isBlank()) listOf(1, 2, 3, 4) else targetYearsCsv.split(",").mapNotNull { it.toIntOrNull() }
    val sections = if (targetSectionsCsv.isBlank()) emptyList() else targetSectionsCsv.split(",")
    val tags = if (interestTagsCsv.isBlank()) emptyList() else interestTagsCsv.split(",")
    val actions = if (keyActionsCsv.isBlank()) emptyList() else keyActionsCsv.split("|||")
    val relExplanation = if (relevanceExplanationCsv.isBlank()) emptyList() else relevanceExplanationCsv.split("|||")

    val relInfo = if (relevanceScore > 0) {
      RelevanceInfo(
        score = relevanceScore,
        level = relevanceLevel,
        explanation = relExplanation
      )
    } else null

    return Announcement(
      id = id,
      title = title,
      rawContent = rawContent,
      aiSummary = aiSummary,
      category = category,
      publisherName = publisherName,
      publisherRole = role,
      publisherDepartment = publisherDepartment,
      publisherDesignation = publisherDesignation,
      isVerifiedPublisher = isVerifiedPublisher,
      targetDepartments = depts,
      targetYears = years,
      targetSections = sections,
      minCgpa = minCgpa,
      interestTags = tags,
      deadlineEpochMs = deadlineEpochMs,
      requiredAction = requiredAction,
      externalLink = externalLink,
      attachmentName = attachmentName,
      priority = prio,
      priorityReason = priorityReason,
      status = status,
      publishedAtEpochMs = publishedAtEpochMs,
      createdAtEpochMs = createdAtEpochMs,
      isBookmarked = isBookmarked,
      priorityScore = priorityScore,
      keyActions = actions,
      aiAnalysisStatus = aiAnalysisStatus,
      relevance = relInfo
    )
  }

  companion object {
    fun fromDomain(ann: Announcement): AnnouncementEntity {
      return AnnouncementEntity(
        id = ann.id,
        title = ann.title,
        rawContent = ann.rawContent,
        aiSummary = ann.aiSummary,
        category = ann.category,
        publisherName = ann.publisherName,
        publisherRole = ann.publisherRole.name,
        publisherDepartment = ann.publisherDepartment,
        publisherDesignation = ann.publisherDesignation,
        isVerifiedPublisher = ann.isVerifiedPublisher,
        targetDepartmentsCsv = ann.targetDepartments.joinToString(","),
        targetYearsCsv = ann.targetYears.joinToString(","),
        targetSectionsCsv = ann.targetSections.joinToString(","),
        minCgpa = ann.minCgpa,
        interestTagsCsv = ann.interestTags.joinToString(","),
        deadlineEpochMs = ann.deadlineEpochMs,
        requiredAction = ann.requiredAction,
        externalLink = ann.externalLink,
        attachmentName = ann.attachmentName,
        priority = ann.priority.name,
        priorityReason = ann.priorityReason,
        status = ann.status,
        publishedAtEpochMs = ann.publishedAtEpochMs,
        createdAtEpochMs = ann.createdAtEpochMs,
        isBookmarked = ann.isBookmarked,
        priorityScore = ann.priorityScore,
        keyActionsCsv = ann.keyActions.joinToString("|||"),
        aiAnalysisStatus = ann.aiAnalysisStatus,
        relevanceScore = ann.relevance?.score ?: 0,
        relevanceLevel = ann.relevance?.level ?: "MEDIUM",
        relevanceExplanationCsv = ann.relevance?.explanation?.joinToString("|||") ?: "",
        cachedAtEpochMs = System.currentTimeMillis()
      )
    }
  }
}
