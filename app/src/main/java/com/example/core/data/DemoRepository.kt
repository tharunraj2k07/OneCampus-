package com.example.core.data

import com.example.core.model.AIChatMessage
import com.example.core.model.Announcement
import com.example.core.model.MessageSender
import com.example.core.model.NotificationItem
import com.example.core.model.PriorityLevel
import com.example.core.model.Role
import com.example.core.model.StudentProfile
import com.example.core.model.TaskItem
import com.example.core.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Centralized Repository providing realistic demo data and reactive state management
 * for OneCampus AI during Phase 3. Structured to be easily swapped with Room / Network Repositories in Phase 4.
 */
object DemoRepository {

  private val now = System.currentTimeMillis()
  private val hour = 3600_000L
  private val day = 86400_000L

  // Current authenticated user (Student or Faculty)
  private val _currentUser = MutableStateFlow(
    User(
      id = "user_student_1",
      email = "tharun.cse@college.edu",
      fullName = "Tharun Kumar",
      role = Role.STUDENT
    )
  )
  val currentUser: StateFlow<User> = _currentUser.asStateFlow()

  // Student Profile
  private val _studentProfile = MutableStateFlow(
    StudentProfile(
      userId = "user_student_1",
      fullName = "Tharun Kumar",
      collegeEmail = "tharun.cse@college.edu",
      registerNumber = "917621104052",
      department = "CSE",
      year = 3,
      section = "A",
      cgpa = 8.65,
      interests = listOf("Placement", "Competitive Programming", "Hackathons", "Internship", "Workshops"),
      profileCompletionPercentage = 100
    )
  )
  val studentProfile: StateFlow<StudentProfile> = _studentProfile.asStateFlow()

  // Central Announcements List
  private val initialAnnouncements = listOf(
    // 1. Zoho Campus Drive (Placement - CRITICAL - Today)
    Announcement(
      id = "ann-zoho-2026",
      title = "Zoho Campus Recruitment Drive 2026",
      rawContent = "Attention all Final & Pre-final year students: Zoho Corporation has officially announced the on-campus recruitment drive for Software Development Engineer (SDE) and Quality Assurance roles. Eligibility criteria: B.E/B.Tech (CSE, IT, ECE) with minimum CGPA of 7.0 and zero standing arrears. All eligible candidates must complete the Zoho hiring portal registration before Friday 5:00 PM. Late submissions will strictly not be entertained. Online aptitude round is scheduled for this Saturday at 10:00 AM.",
      aiSummary = "Zoho hiring SDE & QA for CSE/IT/ECE (CGPA ≥ 7.0, no active arrears). Mandatory portal registration closes today at 5:00 PM.",
      category = "Placement",
      publisherName = "Dr. C. Venkatesh (Placement Director)",
      publisherRole = Role.PLACEMENT_CELL,
      targetDepartments = listOf("CSE", "IT", "ECE"),
      targetYears = listOf(3, 4),
      minCgpa = 7.0,
      interestTags = listOf("Placement", "Competitive Programming", "Software Engineering"),
      deadlineEpochMs = now + (5 * hour), // 5 hours remaining today
      requiredAction = "Register on Zoho Portal",
      externalLink = "https://careers.zoho.com/campus/drive-2026",
      attachmentName = "Zoho_Drive_Eligibility_Criteria.pdf",
      priority = PriorityLevel.CRITICAL,
      priorityReason = "Deadline within 5 hours & critical graduation placement milestone for 3rd/4th year CSE",
      createdAtEpochMs = now - (2 * day),
      isBookmarked = true
    ),

    // 2. DAA Assignment 2 (Assignment - HIGH - Tomorrow)
    Announcement(
      id = "ann-daa-assign2",
      title = "Design & Analysis of Algorithms — Assignment 2 (DP)",
      rawContent = "Students of 3rd Year CSE Section A and B are instructed to complete Assignment 2 covering Dynamic Programming (Longest Common Subsequence, Matrix Chain Multiplication, and Knapsack 0/1). Solutions must be written cleanly with time complexity analysis and submitted on HackerRank by tomorrow at 11:59 PM. Automated code similarity checking will be executed.",
      aiSummary = "Assignment 2 on Dynamic Programming (LCS, Knapsack) must be submitted via HackerRank before tomorrow 11:59 PM.",
      category = "Assignment",
      publisherName = "Dr. S. Ramanathan",
      publisherRole = Role.FACULTY,
      targetDepartments = listOf("CSE"),
      targetYears = listOf(3),
      targetSections = listOf("A", "B"),
      minCgpa = 0.0,
      interestTags = listOf("Competitive Programming", "Algorithms"),
      deadlineEpochMs = now + (28 * hour), // Tomorrow night
      requiredAction = "Submit on HackerRank",
      externalLink = "https://hackerrank.com/college-daa-assign-2",
      attachmentName = "DAA_Assignment2_ProblemStatements.pdf",
      priority = PriorityLevel.HIGH,
      priorityReason = "Graded academic coursework due within 28 hours for CSE Year 3 Sec A",
      createdAtEpochMs = now - (1 * day),
      isBookmarked = false
    ),

    // 3. Amazon Summer Internship 2026 (Internship - HIGH - 3 days)
    Announcement(
      id = "ann-amazon-intern",
      title = "Amazon Summer Internship 2026 (SDE Intern)",
      rawContent = "Amazon India University Relations has opened applications for 6-month Summer SDE Internships for 3rd-year engineering students. Stipend is ₹1,10,000/month. Eligible streams: CSE, IT with CGPA >= 8.0. Interested students must fill the CDC verification Google Form and apply on the Amazon job portal by 31st August.",
      aiSummary = "Amazon SDE Summer 2026 Internship for 3rd Year CSE/IT (CGPA ≥ 8.0). Stipend ₹1.1L/mo. Deadline in 3 days.",
      category = "Internship",
      publisherName = "Campus Career Development Center",
      publisherRole = Role.PLACEMENT_CELL,
      targetDepartments = listOf("CSE", "IT"),
      targetYears = listOf(3),
      minCgpa = 8.0,
      interestTags = listOf("Internship", "Placement", "Competitive Programming"),
      deadlineEpochMs = now + (3 * day),
      requiredAction = "Fill CDC Form & Apply",
      externalLink = "https://amazon.jobs/en/jobs/2849102",
      attachmentName = "Amazon_University_JD.pdf",
      priority = PriorityLevel.HIGH,
      priorityReason = "High prestige internship opportunity matching CGPA 8.65 and CSE Year 3 profile",
      createdAtEpochMs = now - (3 * day),
      isBookmarked = true
    ),

    // 4. CodeChef Campus Chapter Contest (Coding Contest - MEDIUM - Saturday)
    Announcement(
      id = "ann-codechef-contest",
      title = "Campus CodeClash 2026 — Division 1 & 2 Contest",
      rawContent = "The CodeChef Campus Chapter is organizing an intra-college rated coding competition this Saturday from 7:00 PM to 9:30 PM. The contest will feature 6 algorithmic challenges ranging from basic math to graphs and dynamic programming. Top 5 rankers will receive cash rewards and fast-track interview passes for the upcoming hackathon.",
      aiSummary = "Intra-college 2.5 hour rated algorithmic contest on CodeChef this Saturday 7 PM. Cash prizes for top 5.",
      category = "Coding Contest",
      publisherName = "CodeChef Campus Club",
      publisherRole = Role.CLUB_COORDINATOR,
      targetDepartments = listOf("ALL"),
      targetYears = listOf(1, 2, 3, 4),
      minCgpa = 0.0,
      interestTags = listOf("Competitive Programming", "Clubs", "Hackathons"),
      deadlineEpochMs = now + (4 * day),
      requiredAction = "Register on CodeChef",
      externalLink = "https://codechef.com/CAMPUS2026",
      priority = PriorityLevel.MEDIUM,
      priorityReason = "Matches student interest in Competitive Programming",
      createdAtEpochMs = now - (1 * day),
      isBookmarked = false
    ),

    // 5. GenAI on Cloud Workshop (Workshop - MEDIUM - Next Tuesday)
    Announcement(
      id = "ann-genai-workshop",
      title = "Hands-on Workshop: Building Agentic Apps with Gemini & Vertex AI",
      rawContent = "Google Developer Student Club (GDSC) in collaboration with the CSE Department presents a hands-on technical workshop on building multi-agent AI systems with Gemini and Google Cloud. Hands-on labs, free Cloud Skill Boost vouchers, and project certificates provided. Limited to 120 seats on first-come-first-serve basis.",
      aiSummary = "GDSC & CSE Dept hands-on workshop on Gemini AI & Vertex Cloud. Includes free cloud vouchers and certificates. Limited seats.",
      category = "Workshop",
      publisherName = "Google Developer Student Club",
      publisherRole = Role.CLUB_COORDINATOR,
      targetDepartments = listOf("CSE", "IT", "AI&DS"),
      targetYears = listOf(2, 3, 4),
      minCgpa = 0.0,
      interestTags = listOf("Workshops", "Research", "Hackathons"),
      deadlineEpochMs = now + (6 * day),
      requiredAction = "RSVP on GDSC Portal",
      externalLink = "https://gdsc.community.dev/events/college-genai",
      priority = PriorityLevel.MEDIUM,
      priorityReason = "Matches student interest in AI Workshops and CSE curriculum",
      createdAtEpochMs = now - (12 * hour),
      isBookmarked = false
    ),

    // 6. Continuous Internal Assessment II (Examination - HIGH - Next Week)
    Announcement(
      id = "ann-cia2-schedule",
      title = "CIA-II Theory Examination Timetable — Odd Semester",
      rawContent = "The Controller of Examinations has published the finalized timetable for Continuous Internal Assessment II (CIA-2) for 3rd year B.E students. Examinations will be conducted in the FN session (9:30 AM to 11:30 AM) starting next Monday. Hall tickets will be issued by the respective faculty advisors after attendance verification (minimum 75% mandatory).",
      aiSummary = "CIA-2 Theory Exam timetable released for 3rd Year B.E. FN sessions commence next Monday. Minimum 75% attendance required.",
      category = "Examination",
      publisherName = "Office of Controller of Examinations",
      publisherRole = Role.DEPARTMENT_ADMIN,
      targetDepartments = listOf("ALL"),
      targetYears = listOf(3),
      minCgpa = 0.0,
      interestTags = listOf("Higher Studies"),
      deadlineEpochMs = now + (7 * day),
      requiredAction = "Download Exam Timetable",
      attachmentName = "CIA2_Timetable_Final_2026.pdf",
      priority = PriorityLevel.HIGH,
      priorityReason = "Mandatory internal examination milestone for Year 3",
      createdAtEpochMs = now - (4 * day),
      isBookmarked = false
    ),

    // 7. Smart India Hackathon 2026 Internal Qualifier (Competition - MEDIUM)
    Announcement(
      id = "ann-sih-2026",
      title = "Smart India Hackathon (SIH 2026) — College Internal Qualifier",
      rawContent = "Institution's Innovation Council (IIC) invites teams of 6 students (with at least 1 female team member) to submit innovative solution pitch decks for SIH 2026 hardware and software problem statements. Shortlisted teams will represent the college at the national level. Submit team details and 3-slide PPT by Sunday.",
      aiSummary = "Internal screening for Smart India Hackathon 2026. Teams of 6 can submit pitch decks by Sunday.",
      category = "Competition",
      publisherName = "Institution's Innovation Council",
      publisherRole = Role.CLUB_COORDINATOR,
      targetDepartments = listOf("ALL"),
      targetYears = listOf(1, 2, 3, 4),
      minCgpa = 0.0,
      interestTags = listOf("Hackathons", "Competitive Programming"),
      deadlineEpochMs = now + (4 * day),
      requiredAction = "Submit Team Pitch Deck",
      externalLink = "https://iic.college.edu/sih-pitch-2026",
      priority = PriorityLevel.MEDIUM,
      priorityReason = "Directly aligns with student's interest in Hackathons",
      createdAtEpochMs = now - (2 * day),
      isBookmarked = false
    ),

    // 8. Semester End Exam Fee Registration (Registration - CRITICAL)
    Announcement(
      id = "ann-exam-fee",
      title = "Anna University / Autonomous Semester End Exam Fee Payment",
      rawContent = "All 3rd Year engineering students are hereby notified that the semester end examination fee portal will close strictly tomorrow at 4:00 PM. Students must pay via the ERP student portal and verify that the payment transaction receipt is generated with successful status. Hall tickets will not be generated for unpaid records.",
      aiSummary = "Semester Exam Fee portal closes tomorrow at 4:00 PM. Mandatory payment via ERP portal to generate hall tickets.",
      category = "Registration",
      publisherName = "Academic Accounts Section",
      publisherRole = Role.DEPARTMENT_ADMIN,
      targetDepartments = listOf("ALL"),
      targetYears = listOf(3),
      deadlineEpochMs = now + (22 * hour), // Tomorrow 4 PM
      requiredAction = "Pay via ERP Portal",
      externalLink = "https://erp.college.edu/fee-portal",
      priority = PriorityLevel.CRITICAL,
      priorityReason = "Urgent official registration closing in 22 hours; failure prevents exam appearance",
      createdAtEpochMs = now - (1 * day),
      isBookmarked = false
    )
  )

  private val _announcements = MutableStateFlow(initialAnnouncements)
  val announcements: StateFlow<List<Announcement>> = _announcements.asStateFlow()

  // Central Tasks / Deadlines List for Calendar & Today's Progress
  private val initialTasks = listOf(
    TaskItem(
      id = "task-zoho",
      announcementId = "ann-zoho-2026",
      title = "Zoho Campus Drive Registration",
      category = "Placement",
      deadlineEpochMs = now + (5 * hour),
      priority = PriorityLevel.CRITICAL,
      isCompleted = false,
      requiredAction = "Register on Portal"
    ),
    TaskItem(
      id = "task-exam-fee",
      announcementId = "ann-exam-fee",
      title = "Semester Exam Fee Payment",
      category = "Registration",
      deadlineEpochMs = now + (22 * hour),
      priority = PriorityLevel.CRITICAL,
      isCompleted = false,
      requiredAction = "Pay on ERP"
    ),
    TaskItem(
      id = "task-daa",
      announcementId = "ann-daa-assign2",
      title = "DAA Assignment 2 (DP Problem Set)",
      category = "Assignment",
      deadlineEpochMs = now + (28 * hour),
      priority = PriorityLevel.HIGH,
      isCompleted = false,
      requiredAction = "Submit on HackerRank"
    ),
    TaskItem(
      id = "task-amazon",
      announcementId = "ann-amazon-intern",
      title = "Amazon SDE Summer Intern Application",
      category = "Internship",
      deadlineEpochMs = now + (3 * day),
      priority = PriorityLevel.HIGH,
      isCompleted = true,
      requiredAction = "Submitted Application"
    ),
    TaskItem(
      id = "task-codechef",
      announcementId = "ann-codechef-contest",
      title = "Campus CodeClash 2026 Contest",
      category = "Coding Contest",
      deadlineEpochMs = now + (4 * day),
      priority = PriorityLevel.MEDIUM,
      isCompleted = true,
      requiredAction = "Registered"
    ),
    TaskItem(
      id = "task-workshop",
      announcementId = "ann-genai-workshop",
      title = "GenAI on Cloud Workshop RSVP",
      category = "Workshop",
      deadlineEpochMs = now + (6 * day),
      priority = PriorityLevel.MEDIUM,
      isCompleted = true,
      requiredAction = "Seat Reserved"
    )
  )

  private val _tasks = MutableStateFlow(initialTasks)
  val tasks: StateFlow<List<TaskItem>> = _tasks.asStateFlow()

  // Central Notifications List
  private val initialNotifications = listOf(
    NotificationItem(
      id = "notif-1",
      title = "🔴 Critical Deadline: Zoho Campus Drive",
      message = "Zoho recruitment portal registration closes in 5 hours! Complete your form now to participate.",
      type = "DEADLINE",
      timestampEpochMs = now - (15 * 60_000),
      isRead = false,
      announcementId = "ann-zoho-2026"
    ),
    NotificationItem(
      id = "notif-2",
      title = "Exam Fee Payment Closing Soon",
      message = "Semester exam fee payment window closes tomorrow at 4:00 PM. Avoid late penalty fees.",
      type = "DEADLINE",
      timestampEpochMs = now - (2 * hour),
      isRead = false,
      announcementId = "ann-exam-fee"
    ),
    NotificationItem(
      id = "notif-3",
      title = "New Graded Assignment: DAA (DP)",
      message = "Dr. S. Ramanathan posted Assignment 2 due tomorrow night on HackerRank.",
      type = "ANNOUNCEMENT",
      timestampEpochMs = now - (6 * hour),
      isRead = true,
      announcementId = "ann-daa-assign2"
    ),
    NotificationItem(
      id = "notif-4",
      title = "Amazon Summer Internship 2026 Open",
      message = "CDC announced Amazon 6-month SDE Internships with ₹1.1L stipend for CSE students.",
      type = "PLACEMENT",
      timestampEpochMs = now - (1 * day),
      isRead = true,
      announcementId = "ann-amazon-intern"
    ),
    NotificationItem(
      id = "notif-5",
      title = "AI Daily Briefing Ready",
      message = "You have 2 critical deadlines due within 24 hours. Tap to review your personalized schedule.",
      type = "AI_SUMMARY",
      timestampEpochMs = now - (10 * hour),
      isRead = true,
      announcementId = null
    )
  )

  private val _notifications = MutableStateFlow(initialNotifications)
  val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

  // Central AI Chat History
  private val initialChatMessages = listOf(
    AIChatMessage(
      id = "chat-1",
      sender = MessageSender.AI_ASSISTANT,
      text = "Hello Tharun! 👋 I am your OneCampus AI Copilot. I constantly analyze official college circulars, deadlines, and department notices personalized for 3rd Year CSE. How can I help you today?"
    ),
    AIChatMessage(
      id = "chat-2",
      sender = MessageSender.STUDENT,
      text = "What should I complete today?"
    ),
    AIChatMessage(
      id = "chat-3",
      sender = MessageSender.AI_ASSISTANT,
      text = "You have **1 urgent placement task** and **1 critical administrative fee** today:\n\n1. **Zoho Campus Recruitment Registration**: Closes at **5:00 PM (in 5 hours)**. Mandatory for 3rd/4th Year CSE students with CGPA ≥ 7.0.\n2. **Semester Exam Fee Payment**: Closes tomorrow at 4:00 PM via ERP portal.\n\nWould you like me to open the direct Zoho registration link for you?",
      referencedAnnouncementIds = listOf("ann-zoho-2026", "ann-exam-fee")
    )
  )

  private val _chatMessages = MutableStateFlow(initialChatMessages)
  val chatMessages: StateFlow<List<AIChatMessage>> = _chatMessages.asStateFlow()

  // Faculty Published Announcements List
  private val initialFacultyAnnouncements = listOf(
    Announcement(
      id = "ann-daa-assign2",
      title = "Design & Analysis of Algorithms — Assignment 2 (DP)",
      rawContent = "Students of 3rd Year CSE Section A and B are instructed to complete Assignment 2 covering Dynamic Programming. Submit on HackerRank.",
      aiSummary = "Assignment 2 on Dynamic Programming must be submitted via HackerRank before tomorrow 11:59 PM.",
      category = "Assignment",
      publisherName = "Dr. S. Ramanathan",
      publisherRole = Role.FACULTY,
      targetDepartments = listOf("CSE"),
      targetYears = listOf(3),
      targetSections = listOf("A", "B"),
      deadlineEpochMs = now + (28 * hour),
      requiredAction = "Submit on HackerRank",
      priority = PriorityLevel.HIGH,
      priorityReason = "Graded coursework due within 28 hours"
    ),
    Announcement(
      id = "ann-faculty-demo-2",
      title = "Mini Project Phase 1 Evaluation Review Schedule",
      rawContent = "Review committee meets next Monday at 1:30 PM in CSE Lab 3. Teams must present system architecture diagrams and working database schemas.",
      aiSummary = "CSE Year 3 Mini Project Phase 1 evaluation scheduled for Monday 1:30 PM in Lab 3.",
      category = "Examination",
      publisherName = "Dr. S. Ramanathan",
      publisherRole = Role.FACULTY,
      targetDepartments = listOf("CSE"),
      targetYears = listOf(3),
      priority = PriorityLevel.MEDIUM,
      priorityReason = "Internal evaluation milestone"
    ),
    Announcement(
      id = "ann-faculty-demo-3",
      title = "Guest Lecture on Distributed Systems by Google SDE",
      rawContent = "Distinguished alumni guest lecture on Google Spanner and high-scale RPC architectures on Friday at 3:00 PM in Aud 2.",
      aiSummary = "Guest lecture on Distributed Systems & Cloud Architecture this Friday 3 PM.",
      category = "Event",
      publisherName = "Dr. S. Ramanathan",
      publisherRole = Role.FACULTY,
      targetDepartments = listOf("CSE", "IT"),
      targetYears = listOf(3, 4),
      priority = PriorityLevel.LOW,
      priorityReason = "Department technical enrichment event"
    )
  )

  private val _facultyAnnouncements = MutableStateFlow(initialFacultyAnnouncements)
  val facultyAnnouncements: StateFlow<List<Announcement>> = _facultyAnnouncements.asStateFlow()

  // ----------------------------------------------------
  // Repository Mutations & Actions
  // ----------------------------------------------------

  fun toggleBookmark(announcementId: String) {
    _announcements.update { list ->
      list.map { item ->
        if (item.id == announcementId) {
          item.copy(isBookmarked = !item.isBookmarked)
        } else {
          item
        }
      }
    }
  }

  fun toggleTaskCompletion(taskId: String) {
    _tasks.update { list ->
      list.map { task ->
        if (task.id == taskId) {
          task.copy(isCompleted = !task.isCompleted)
        } else {
          task
        }
      }
    }
  }

  fun markAllNotificationsRead() {
    _notifications.update { list ->
      list.map { it.copy(isRead = true) }
    }
  }

  fun markNotificationRead(notifId: String) {
    _notifications.update { list ->
      list.map { if (it.id == notifId) it.copy(isRead = true) else it }
    }
  }

  fun addChatMessage(messageText: String, isStudent: Boolean = true) {
    val newMessage = AIChatMessage(
      id = "chat-${System.currentTimeMillis()}",
      sender = if (isStudent) MessageSender.STUDENT else MessageSender.AI_ASSISTANT,
      text = messageText
    )
    _chatMessages.update { it + newMessage }
  }

  fun updateStudentProfile(
    department: String,
    year: Int,
    section: String,
    interests: List<String>
  ) {
    _studentProfile.update { current ->
      current.copy(
        department = department,
        year = year,
        section = section,
        interests = interests,
        profileCompletionPercentage = 100
      )
    }
  }

  fun switchUserRole(role: Role) {
    if (role == Role.FACULTY) {
      _currentUser.value = User(
        id = "user_faculty_1",
        email = "ramanathan.cse@college.edu",
        fullName = "Dr. S. Ramanathan",
        role = Role.FACULTY
      )
    } else {
      _currentUser.value = User(
        id = "user_student_1",
        email = "tharun.cse@college.edu",
        fullName = "Tharun Kumar",
        role = Role.STUDENT
      )
    }
  }

  fun publishFacultyAnnouncement(announcement: Announcement) {
    _facultyAnnouncements.update { listOf(announcement) + it }
    _announcements.update { listOf(announcement) + it }
  }

  fun deleteFacultyAnnouncement(announcementId: String) {
    _facultyAnnouncements.update { list -> list.filterNot { it.id == announcementId } }
    _announcements.update { list -> list.filterNot { it.id == announcementId } }
  }
}
