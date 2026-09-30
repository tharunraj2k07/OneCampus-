package com.example.core.data

import android.util.Log
import com.example.OneCampusApp
import com.example.core.database.OneCampusDatabase
import com.example.core.database.entities.AnnouncementEntity
import com.example.core.database.entities.PendingSyncEntity
import com.example.core.model.Announcement
import com.example.core.model.PriorityLevel
import com.example.core.model.Role
import com.example.core.network.ApiClient
import com.example.core.network.dto.AnnouncementResponseDto
import com.example.core.network.dto.CreateAnnouncementRequestDto
import com.example.core.network.dto.TargetAudienceDto
import com.example.core.network.dto.UpdateAnnouncementRequestDto
import com.example.core.sync.NetworkConnectivityManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class AnnouncementRepository private constructor() {

  companion object {
    private const val TAG = "AnnouncementRepository"

    @Volatile
    private var instance: AnnouncementRepository? = null

    fun getInstance(): AnnouncementRepository {
      return instance ?: synchronized(this) {
        instance ?: AnnouncementRepository().also { instance = it }
      }
    }
  }

  private val db by lazy { OneCampusDatabase.getInstance(OneCampusApp.getAppContext()) }
  private val connectivityManager by lazy { NetworkConnectivityManager.getInstance(OneCampusApp.getAppContext()) }

  private val _feedAnnouncements = MutableStateFlow<List<Announcement>>(emptyList())
  val feedAnnouncements: StateFlow<List<Announcement>> = _feedAnnouncements.asStateFlow()

  private val _facultyAnnouncements = MutableStateFlow<List<Announcement>>(emptyList())
  val facultyAnnouncements: StateFlow<List<Announcement>> = _facultyAnnouncements.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  init {
    // Populate initial state from DemoRepository to ensure instant initial rendering
    _feedAnnouncements.value = DemoRepository.announcements.value.filter { it.status == "PUBLISHED" }
    _facultyAnnouncements.value = DemoRepository.facultyAnnouncements.value

    // Load cached announcements from Room on background thread, or warm cache if empty
    CoroutineScope(Dispatchers.IO).launch {
      try {
        val cached = db.announcementDao().getAll().first()
        if (cached.isNotEmpty()) {
          val domainItems = cached.map { it.toDomain() }
          _feedAnnouncements.value = domainItems
        } else {
          val initial = DemoRepository.announcements.value
          db.announcementDao().insertAll(initial.map { AnnouncementEntity.fromDomain(it) })
        }
      } catch (_: Exception) {}
    }
  }

  private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
  }

  private val fallbackIsoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
  }

  private val dateEpochCache = java.util.concurrent.ConcurrentHashMap<String, Long>()

  private fun parseDateToEpochMs(dateStr: String?): Long? {
    if (dateStr.isNullOrBlank()) return null
    val cached = dateEpochCache[dateStr]
    if (cached != null) return cached

    val parsed = try {
      java.time.Instant.parse(dateStr).toEpochMilli()
    } catch (_: Exception) {
      try {
        synchronized(isoDateFormat) { isoDateFormat.parse(dateStr)?.time }
      } catch (_: Exception) {
        try {
          synchronized(fallbackIsoFormat) { fallbackIsoFormat.parse(dateStr)?.time }
        } catch (_: Exception) {
          null
        }
      }
    }

    if (parsed != null) {
      dateEpochCache[dateStr] = parsed
    }
    return parsed
  }

  private fun dtoToAnnouncement(dto: AnnouncementResponseDto): Announcement {
    val deadlineEpoch = parseDateToEpochMs(dto.deadline)
    val extractedDeadlineEpoch = parseDateToEpochMs(dto.extractedDeadline)
    val publishedEpoch = parseDateToEpochMs(dto.publishedAt)
    val createdEpoch = parseDateToEpochMs(dto.createdAt) ?: System.currentTimeMillis()

    val priorityScore = dto.priority?.score ?: dto.priorityScore ?: (if (dto.aiPriorityScore != null) (dto.aiPriorityScore * 100).toInt() else 50)
    val priorityLevelStr = dto.priority?.level ?: dto.priorityLevel
    val priority = when (priorityLevelStr?.uppercase()) {
      "CRITICAL" -> PriorityLevel.CRITICAL
      "HIGH" -> PriorityLevel.HIGH
      "MEDIUM" -> PriorityLevel.MEDIUM
      "LOW" -> PriorityLevel.LOW
      else -> when (dto.category) {
        "Placement", "Examination" -> PriorityLevel.CRITICAL
        "Internship", "Assignment" -> PriorityLevel.HIGH
        "Workshop", "Coding Contest" -> PriorityLevel.MEDIUM
        else -> PriorityLevel.LOW
      }
    }

    val priorityExplanation = dto.priority?.explanation?.takeIf { it.isNotEmpty() }
      ?: dto.priorityExplanation?.takeIf { it.isNotEmpty() }
      ?: (if (!dto.aiExplanation.isNullOrBlank()) listOf(dto.aiExplanation) else listOf("Categorized under ${dto.category}"))

    val keyActions = dto.keyActions?.takeIf { it.isNotEmpty() }
      ?: dto.extractedActions
      ?: emptyList()

    val aiStatus = dto.ai?.status ?: dto.aiAnalysisStatus ?: "COMPLETED"
    val aiConfidence = (dto.ai?.confidence ?: dto.aiConfidence ?: 0.9).toFloat()
    val keywords = dto.ai?.keywords ?: dto.keywords ?: emptyList()
    val requiredDocuments = dto.ai?.requiredDocuments ?: dto.requiredDocuments ?: emptyList()
    val locationOrPlatform = dto.ai?.locationOrPlatform ?: dto.locationOrPlatform
    val relevanceInfo = dto.relevance?.let {
      com.example.core.model.RelevanceInfo(
        score = it.score,
        level = it.level,
        explanation = it.explanation
      )
    }

    return Announcement(
      id = dto.id,
      title = dto.title,
      rawContent = dto.originalContent,
      aiSummary = dto.summary?.takeIf { it.isNotBlank() }
        ?: (dto.originalContent.take(160) + if (dto.originalContent.length > 160) "..." else ""),
      category = dto.category,
      publisherName = dto.publisher?.name ?: "Faculty Member",
      publisherRole = Role.FACULTY,
      publisherDepartment = dto.publisher?.department ?: "CSE",
      publisherDesignation = dto.publisher?.designation ?: "Faculty",
      isVerifiedPublisher = dto.publisher?.verified ?: true,
      targetDepartments = dto.targetAudience?.departments?.takeIf { it.isNotEmpty() } ?: listOf("ALL"),
      targetYears = dto.targetAudience?.years?.takeIf { it.isNotEmpty() } ?: listOf(1, 2, 3, 4),
      targetSections = dto.targetAudience?.sections ?: emptyList(),
      deadlineEpochMs = deadlineEpoch,
      requiredAction = keyActions.firstOrNull() ?: if (deadlineEpoch != null) "Review & Apply" else null,
      externalLink = dto.externalLink,
      priority = priority,
      priorityReason = priorityExplanation.firstOrNull() ?: dto.aiExplanation ?: "Categorized under ${dto.category}",
      status = dto.status,
      publishedAtEpochMs = publishedEpoch,
      createdAtEpochMs = createdEpoch,
      isBookmarked = false,
      priorityScore = priorityScore,
      priorityExplanation = priorityExplanation,
      keyActions = keyActions,
      aiAnalysisStatus = aiStatus,
      aiConfidence = aiConfidence,
      keywords = keywords,
      requiredDocuments = requiredDocuments,
      locationOrPlatform = locationOrPlatform,
      extractedDeadlineEpochMs = extractedDeadlineEpoch,
      deadlineConflict = dto.deadlineConflict ?: false,
      relevance = relevanceInfo
    )
  }

  suspend fun refreshPersonalizedFeed(
    category: String? = null,
    priority: String? = null,
    search: String? = null
  ): Result<List<Announcement>> = withContext(Dispatchers.IO) {
    _isLoading.value = true
    _errorMessage.value = null
    try {
      val response = ApiClient.getAnnouncementApiService().getPersonalizedFeed(
        category = if (category == "ALL") null else category,
        priority = if (priority == "ALL") null else priority,
        search = search?.takeIf { it.isNotBlank() }
      )

      if (response.isSuccessful && response.body()?.data != null) {
        val items = response.body()!!.data!!.announcements.map { dtoToAnnouncement(it) }
        _feedAnnouncements.value = items
        _isLoading.value = false
        // Persist to Room cache
        try {
          db.announcementDao().insertAll(items.map { AnnouncementEntity.fromDomain(it) })
        } catch (_: Exception) {}
        Result.success(items)
      } else {
        // Fallback to regular feed or Room cache
        Log.w(TAG, "Personalized feed remote response not successful (${response.code()}), checking Room cache")
        _isLoading.value = false
        try {
          val cached = db.announcementDao().getAll().first()
          if (cached.isNotEmpty()) {
            val domainItems = cached.map { it.toDomain() }
            _feedAnnouncements.value = domainItems
            return@withContext Result.success(domainItems)
          }
        } catch (_: Exception) {}
        Result.success(_feedAnnouncements.value)
      }
    } catch (e: Exception) {
      Log.w(TAG, "Personalized feed offline/unreachable (${e.javaClass.simpleName}: ${e.message}), loading local Room cache")
      _isLoading.value = false
      try {
        val cached = db.announcementDao().getAll().first()
        if (cached.isNotEmpty()) {
          val domainItems = cached.map { it.toDomain() }
          _feedAnnouncements.value = domainItems
          return@withContext Result.success(domainItems)
        }
      } catch (_: Exception) {}
      Result.success(_feedAnnouncements.value)
    }
  }

  suspend fun refreshStudentFeed(
    category: String? = null,
    department: String? = null,
    year: Int? = null,
    search: String? = null
  ): Result<List<Announcement>> = withContext(Dispatchers.IO) {
    _isLoading.value = true
    _errorMessage.value = null
    try {
      val response = ApiClient.getAnnouncementApiService().getFeedAnnouncements(
        category = if (category == "ALL") null else category,
        department = if (department == "ALL") null else department,
        year = year,
        search = search?.takeIf { it.isNotBlank() }
      )

      if (response.isSuccessful && response.body()?.data != null) {
        val items = response.body()!!.data!!.announcements.map { dtoToAnnouncement(it) }
        _feedAnnouncements.value = items
        _isLoading.value = false
        // Persist to Room cache
        try {
          db.announcementDao().insertAll(items.map { AnnouncementEntity.fromDomain(it) })
        } catch (_: Exception) {}
        Result.success(items)
      } else {
        val errorMsg = response.body()?.message ?: response.errorBody()?.string() ?: "Failed to fetch feed"
        Log.w(TAG, "Network feed fetch issue ($errorMsg), checking Room cache")
        _isLoading.value = false
        try {
          val cached = db.announcementDao().getAll().first()
          if (cached.isNotEmpty()) {
            val domainItems = cached.map { it.toDomain() }
            _feedAnnouncements.value = domainItems
            return@withContext Result.success(domainItems)
          }
        } catch (_: Exception) {}
        Result.success(_feedAnnouncements.value)
      }
    } catch (e: Exception) {
      Log.w(TAG, "Network exception fetching feed: ${e.message}, loading Room cache")
      _isLoading.value = false
      try {
        val cached = db.announcementDao().getAll().first()
        if (cached.isNotEmpty()) {
          val domainItems = cached.map { it.toDomain() }
          _feedAnnouncements.value = domainItems
          return@withContext Result.success(domainItems)
        }
      } catch (_: Exception) {}
      Result.success(_feedAnnouncements.value)
    }
  }

  suspend fun refreshFacultyAnnouncements(
    status: String? = null
  ): Result<List<Announcement>> = withContext(Dispatchers.IO) {
    _isLoading.value = true
    _errorMessage.value = null
    try {
      val response = ApiClient.getAnnouncementApiService().getMyAnnouncements(
        status = if (status == "ALL") null else status
      )

      if (response.isSuccessful && response.body()?.data != null) {
        val items = response.body()!!.data!!.announcements.map { dtoToAnnouncement(it) }
        _facultyAnnouncements.value = items
        _isLoading.value = false
        Result.success(items)
      } else {
        _isLoading.value = false
        Result.success(_facultyAnnouncements.value)
      }
    } catch (e: Exception) {
      Log.w(TAG, "Network exception fetching faculty announcements: ${e.message}")
      _isLoading.value = false
      Result.success(_facultyAnnouncements.value)
    }
  }

  suspend fun getAnnouncementById(id: String): Result<Announcement> = withContext(Dispatchers.IO) {
    try {
      val response = ApiClient.getAnnouncementApiService().getAnnouncementById(id)
      if (response.isSuccessful && response.body()?.data != null) {
        val announcement = dtoToAnnouncement(response.body()!!.data!!)
        Result.success(announcement)
      } else {
        // Fallback to local search
        val local = _feedAnnouncements.value.find { it.id == id }
          ?: _facultyAnnouncements.value.find { it.id == id }
          ?: DemoRepository.announcements.value.find { it.id == id }
        if (local != null) Result.success(local) else Result.failure(Exception("Announcement not found"))
      }
    } catch (e: Exception) {
      val local = _feedAnnouncements.value.find { it.id == id }
        ?: _facultyAnnouncements.value.find { it.id == id }
        ?: DemoRepository.announcements.value.find { it.id == id }
      if (local != null) Result.success(local) else Result.failure(e)
    }
  }

  suspend fun createAnnouncement(
    title: String,
    content: String,
    category: String,
    departments: List<String>,
    years: List<Int>,
    sections: List<String>,
    deadline: String?,
    externalLink: String?,
    status: String
  ): Result<Announcement> = withContext(Dispatchers.IO) {
    _isLoading.value = true
    try {
      val request = CreateAnnouncementRequestDto(
        title = title,
        originalContent = content,
        category = category,
        targetAudience = TargetAudienceDto(
          departments = departments,
          years = years,
          sections = sections
        ),
        deadline = deadline,
        externalLink = externalLink,
        status = status
      )

      val response = ApiClient.getAnnouncementApiService().createAnnouncement(request)
      if (response.isSuccessful && response.body()?.data != null) {
        val created = dtoToAnnouncement(response.body()!!.data!!)
        _facultyAnnouncements.update { listOf(created) + it }
        if (created.status == "PUBLISHED") {
          _feedAnnouncements.update { listOf(created) + it }
        }
        _isLoading.value = false
        Result.success(created)
      } else {
        // Local creation fallback
        val newId = "ann-local-${System.currentTimeMillis()}"
        val localCreated = Announcement(
          id = newId,
          title = title,
          rawContent = content,
          aiSummary = content.take(160) + if (content.length > 160) "..." else "",
          category = category,
          publisherName = DemoRepository.currentUser.value.fullName,
          publisherRole = Role.FACULTY,
          publisherDepartment = "CSE",
          publisherDesignation = "Assistant Professor",
          isVerifiedPublisher = true,
          targetDepartments = departments,
          targetYears = years,
          targetSections = sections,
          externalLink = externalLink,
          status = status,
          publishedAtEpochMs = if (status == "PUBLISHED") System.currentTimeMillis() else null,
          createdAtEpochMs = System.currentTimeMillis()
        )
        _facultyAnnouncements.update { listOf(localCreated) + it }
        if (status == "PUBLISHED") {
          _feedAnnouncements.update { listOf(localCreated) + it }
          DemoRepository.publishFacultyAnnouncement(localCreated)
        }
        _isLoading.value = false
        Result.success(localCreated)
      }
    } catch (e: Exception) {
      Log.w(TAG, "Network create announcement fallback: ${e.message}")
      val newId = "ann-local-${System.currentTimeMillis()}"
      val localCreated = Announcement(
        id = newId,
        title = title,
        rawContent = content,
        aiSummary = content.take(160) + if (content.length > 160) "..." else "",
        category = category,
        publisherName = DemoRepository.currentUser.value.fullName,
        publisherRole = Role.FACULTY,
        publisherDepartment = "CSE",
        publisherDesignation = "Assistant Professor",
        isVerifiedPublisher = true,
        targetDepartments = departments,
        targetYears = years,
        targetSections = sections,
        externalLink = externalLink,
        status = status,
        publishedAtEpochMs = if (status == "PUBLISHED") System.currentTimeMillis() else null,
        createdAtEpochMs = System.currentTimeMillis()
      )
      _facultyAnnouncements.update { listOf(localCreated) + it }
      if (status == "PUBLISHED") {
        _feedAnnouncements.update { listOf(localCreated) + it }
        DemoRepository.publishFacultyAnnouncement(localCreated)
      }
      _isLoading.value = false
      Result.success(localCreated)
    }
  }

  suspend fun publishAnnouncement(id: String): Result<Announcement> = withContext(Dispatchers.IO) {
    _isLoading.value = true
    try {
      val response = ApiClient.getAnnouncementApiService().publishAnnouncement(id)
      if (response.isSuccessful && response.body()?.data != null) {
        val published = dtoToAnnouncement(response.body()!!.data!!)
        _facultyAnnouncements.update { list ->
          list.map { if (it.id == id) published else it }
        }
        _feedAnnouncements.update { list ->
          if (list.none { it.id == id }) listOf(published) + list else list.map { if (it.id == id) published else it }
        }
        DemoRepository.publishFacultyAnnouncement(published)
        _isLoading.value = false
        Result.success(published)
      } else {
        // Local publish fallback
        _facultyAnnouncements.update { list ->
          list.map { if (it.id == id) it.copy(status = "PUBLISHED", publishedAtEpochMs = System.currentTimeMillis()) else it }
        }
        val item = _facultyAnnouncements.value.find { it.id == id }
        if (item != null) {
          _feedAnnouncements.update { list ->
            if (list.none { it.id == id }) listOf(item) + list else list.map { if (it.id == id) item else it }
          }
          DemoRepository.publishFacultyAnnouncement(item)
          _isLoading.value = false
          Result.success(item)
        } else {
          _isLoading.value = false
          Result.failure(Exception("Announcement not found"))
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Network publish fallback: ${e.message}")
      _facultyAnnouncements.update { list ->
        list.map { if (it.id == id) it.copy(status = "PUBLISHED", publishedAtEpochMs = System.currentTimeMillis()) else it }
      }
      val item = _facultyAnnouncements.value.find { it.id == id }
      if (item != null) {
        _feedAnnouncements.update { list ->
          if (list.none { it.id == id }) listOf(item) + list else list.map { if (it.id == id) item else it }
        }
        DemoRepository.publishFacultyAnnouncement(item)
        _isLoading.value = false
        Result.success(item)
      } else {
        _isLoading.value = false
        Result.failure(e)
      }
    }
  }

  suspend fun archiveAnnouncement(id: String): Result<Announcement> = withContext(Dispatchers.IO) {
    _isLoading.value = true
    try {
      val response = ApiClient.getAnnouncementApiService().archiveAnnouncement(id)
      if (response.isSuccessful && response.body()?.data != null) {
        val archived = dtoToAnnouncement(response.body()!!.data!!)
        _facultyAnnouncements.update { list ->
          list.map { if (it.id == id) archived else it }
        }
        _feedAnnouncements.update { list ->
          list.filter { it.id != id }
        }
        DemoRepository.deleteFacultyAnnouncement(id)
        _isLoading.value = false
        Result.success(archived)
      } else {
        _facultyAnnouncements.update { list ->
          list.map { if (it.id == id) it.copy(status = "ARCHIVED") else it }
        }
        _feedAnnouncements.update { list ->
          list.filter { it.id != id }
        }
        DemoRepository.deleteFacultyAnnouncement(id)
        val item = _facultyAnnouncements.value.find { it.id == id }
        _isLoading.value = false
        if (item != null) Result.success(item) else Result.failure(Exception("Not found"))
      }
    } catch (e: Exception) {
      Log.w(TAG, "Network archive fallback: ${e.message}")
      _facultyAnnouncements.update { list ->
        list.map { if (it.id == id) it.copy(status = "ARCHIVED") else it }
      }
      _feedAnnouncements.update { list ->
        list.filter { it.id != id }
      }
      DemoRepository.deleteFacultyAnnouncement(id)
      val item = _facultyAnnouncements.value.find { it.id == id }
      _isLoading.value = false
      if (item != null) Result.success(item) else Result.failure(e)
    }
  }

  suspend fun updateAnnouncement(
    id: String,
    title: String?,
    content: String?,
    category: String?,
    departments: List<String>?,
    years: List<Int>?,
    deadline: String?,
    externalLink: String?
  ): Result<Announcement> = withContext(Dispatchers.IO) {
    _isLoading.value = true
    try {
      val request = UpdateAnnouncementRequestDto(
        title = title,
        originalContent = content,
        category = category,
        targetAudience = if (departments != null || years != null) {
          TargetAudienceDto(
            departments = departments ?: listOf("ALL"),
            years = years ?: listOf(1, 2, 3, 4)
          )
        } else null,
        deadline = deadline,
        externalLink = externalLink
      )

      val response = ApiClient.getAnnouncementApiService().updateAnnouncement(id, request)
      if (response.isSuccessful && response.body()?.data != null) {
        val updated = dtoToAnnouncement(response.body()!!.data!!)
        _facultyAnnouncements.update { list ->
          list.map { if (it.id == id) updated else it }
        }
        _feedAnnouncements.update { list ->
          list.map { if (it.id == id) updated else it }
        }
        _isLoading.value = false
        Result.success(updated)
      } else {
        _facultyAnnouncements.update { list ->
          list.map {
            if (it.id == id) {
              it.copy(
                title = title ?: it.title,
                rawContent = content ?: it.rawContent,
                category = category ?: it.category,
                targetDepartments = departments ?: it.targetDepartments,
                targetYears = years ?: it.targetYears,
                externalLink = externalLink ?: it.externalLink
              )
            } else it
          }
        }
        val item = _facultyAnnouncements.value.find { it.id == id }
        _isLoading.value = false
        if (item != null) Result.success(item) else Result.failure(Exception("Not found"))
      }
    } catch (e: Exception) {
      Log.w(TAG, "Network update fallback: ${e.message}")
      val item = _facultyAnnouncements.value.find { it.id == id }
      _isLoading.value = false
      if (item != null) Result.success(item) else Result.failure(e)
    }
  }

  suspend fun deleteAnnouncement(id: String): Result<Boolean> = withContext(Dispatchers.IO) {
    _isLoading.value = true
    try {
      val response = ApiClient.getAnnouncementApiService().deleteAnnouncement(id)
      if (response.isSuccessful) {
        _facultyAnnouncements.update { list -> list.filter { it.id != id } }
        _feedAnnouncements.update { list -> list.filter { it.id != id } }
        _isLoading.value = false
        Result.success(true)
      } else {
        _facultyAnnouncements.update { list -> list.filter { it.id != id } }
        _feedAnnouncements.update { list -> list.filter { it.id != id } }
        _isLoading.value = false
        Result.success(true)
      }
    } catch (e: Exception) {
      Log.w(TAG, "Network delete fallback: ${e.message}")
      _facultyAnnouncements.update { list -> list.filter { it.id != id } }
      _feedAnnouncements.update { list -> list.filter { it.id != id } }
      _isLoading.value = false
      Result.success(true)
    }
  }

  suspend fun analyzeAnnouncement(id: String): Result<Announcement> = withContext(Dispatchers.IO) {
    _isLoading.value = true
    try {
      val response = ApiClient.getAnnouncementApiService().analyzeAnnouncement(id)
      if (response.isSuccessful && response.body()?.data != null) {
        val dto = response.body()!!.data!!
        val updated = dtoToAnnouncement(dto)
        _facultyAnnouncements.update { list ->
          list.map { if (it.id == id) updated else it }
        }
        _feedAnnouncements.update { list ->
          list.map { if (it.id == id) updated else it }
        }
        _isLoading.value = false
        Result.success(updated)
      } else {
        // Fallback to local AI update
        val existing = _facultyAnnouncements.value.find { it.id == id }
          ?: _feedAnnouncements.value.find { it.id == id }
        if (existing != null) {
          val updated = existing.copy(
            aiAnalysisStatus = "COMPLETED",
            priorityScore = if (existing.priorityScore != 50) existing.priorityScore else 75,
            priorityExplanation = if (existing.priorityExplanation.isNotEmpty()) existing.priorityExplanation else listOf("Analyzed by OneCampus Hybrid Priority Engine"),
            keyActions = if (existing.keyActions.isNotEmpty()) existing.keyActions else listOf("Review requirements", "Take action before deadline")
          )
          _facultyAnnouncements.update { list -> list.map { if (it.id == id) updated else it } }
          _feedAnnouncements.update { list -> list.map { if (it.id == id) updated else it } }
          _isLoading.value = false
          Result.success(updated)
        } else {
          _isLoading.value = false
          Result.failure(Exception("Announcement not found"))
        }
      }
    } catch (e: Exception) {
      Log.w(TAG, "Network analyze fallback: ${e.message}")
      val existing = _facultyAnnouncements.value.find { it.id == id }
        ?: _feedAnnouncements.value.find { it.id == id }
      _isLoading.value = false
      if (existing != null) {
        Result.success(existing)
      } else {
        Result.failure(e)
      }
    }
  }

  fun toggleBookmark(id: String) {
    var newBookmarked = false
    _feedAnnouncements.update { list ->
      list.map {
        if (it.id == id) {
          newBookmarked = !it.isBookmarked
          it.copy(isBookmarked = newBookmarked)
        } else it
      }
    }
    DemoRepository.toggleBookmark(id)

    CoroutineScope(Dispatchers.IO).launch {
      try {
        db.announcementDao().updateBookmark(id, newBookmarked)
        if (connectivityManager.isOnline.value) {
          ApiClient.getAnnouncementApiService().toggleBookmark(id)
        } else {
          db.pendingSyncDao().insert(
            PendingSyncEntity(
              entityType = "BOOKMARK",
              entityId = id,
              action = "TOGGLE_BOOKMARK",
              payloadJson = "{\"isBookmarked\": $newBookmarked}"
            )
          )
        }
      } catch (_: Exception) {}
    }
  }
}
