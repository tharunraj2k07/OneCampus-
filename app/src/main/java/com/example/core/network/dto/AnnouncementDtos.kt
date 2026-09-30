package com.example.core.network.dto

data class ApiResponseDto<T>(
  val success: Boolean = false,
  val message: String = "",
  val data: T? = null,
  val errors: List<ApiErrorDetailDto>? = null
)

data class ApiErrorDetailDto(
  val field: String? = null,
  val message: String = ""
)

data class PaginationDto(
  val total: Int = 0,
  val page: Int = 1,
  val limit: Int = 20,
  val totalPages: Int = 1
)

data class AnnouncementFeedDataDto(
  val announcements: List<AnnouncementResponseDto> = emptyList(),
  val pagination: PaginationDto? = null
)

data class PublisherInfoDto(
  val id: String = "",
  val name: String = "",
  val department: String? = null,
  val designation: String? = null,
  val verified: Boolean = true
)

data class TargetAudienceDto(
  val departments: List<String> = listOf("ALL"),
  val years: List<Int> = listOf(1, 2, 3, 4),
  val sections: List<String> = emptyList()
)

data class PriorityInfoDto(
  val score: Int = 50,
  val level: String = "MEDIUM",
  val explanation: List<String> = emptyList()
)

data class AiInfoDto(
  val status: String = "PENDING",
  val confidence: Double? = null,
  val eventType: String? = null,
  val keywords: List<String>? = null,
  val requiredDocuments: List<String>? = null,
  val locationOrPlatform: String? = null
)

data class RelevanceInfoDto(
  val score: Int = 50,
  val level: String = "MEDIUM",
  val explanation: List<String> = emptyList()
)

data class AnnouncementResponseDto(
  val id: String = "",
  val title: String = "",
  val originalContent: String = "",
  val summary: String? = null,
  val category: String = "General Announcement",
  val targetAudience: TargetAudienceDto? = null,
  val deadline: String? = null,
  val eligibility: String? = null,
  val externalLink: String? = null,
  val status: String = "DRAFT",
  val publisher: PublisherInfoDto? = null,
  val publishedAt: String? = null,
  val archivedAt: String? = null,
  val keyActions: List<String>? = null,
  val extractedActions: List<String>? = null,
  val extractedDeadline: String? = null,
  val extractedEligibility: String? = null,
  val keywords: List<String>? = null,
  val eventType: String? = null,
  val urgencyIndicators: List<String>? = null,
  val requiredDocuments: List<String>? = null,
  val locationOrPlatform: String? = null,
  val aiAnalysisStatus: String? = null,
  val aiConfidence: Double? = null,
  val deadlineConflict: Boolean? = null,
  val priorityScore: Int? = null,
  val priorityLevel: String? = null,
  val priorityExplanation: List<String>? = null,
  val priority: PriorityInfoDto? = null,
  val ai: AiInfoDto? = null,
  val aiPriorityScore: Double? = null,
  val aiExplanation: String? = null,
  val relevance: RelevanceInfoDto? = null,
  val createdAt: String? = null,
  val updatedAt: String? = null
)

data class CreateAnnouncementRequestDto(
  val title: String,
  val originalContent: String,
  val category: String,
  val targetAudience: TargetAudienceDto? = null,
  val deadline: String? = null,
  val eligibility: String? = null,
  val externalLink: String? = null,
  val status: String? = null
)

data class UpdateAnnouncementRequestDto(
  val title: String? = null,
  val originalContent: String? = null,
  val category: String? = null,
  val targetAudience: TargetAudienceDto? = null,
  val deadline: String? = null,
  val eligibility: String? = null,
  val externalLink: String? = null,
  val status: String? = null
)

data class DeleteAnnouncementResponseDto(
  val id: String = "",
  val deleted: Boolean = true
)
