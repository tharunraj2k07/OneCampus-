package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.data.DemoRepository
import com.example.core.model.AIChatMessage
import com.example.core.model.MessageSender
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AIChatUiState(
  val messages: List<AIChatMessage> = emptyList(),
  val isThinking: Boolean = false,
  val suggestedPrompts: List<String> = listOf(
    "What should I complete today?",
    "What are my closest deadlines?",
    "Which placement opportunities are open?",
    "Did I miss anything important?",
    "Summarize the DAA assignment",
    "Am I eligible for Amazon Internship?"
  )
)

class AIChatViewModel(
  private val repository: DemoRepository = DemoRepository
) : ViewModel() {

  private val _isThinking = MutableStateFlow(false)
  val isThinking = _isThinking.asStateFlow()

  val uiState: StateFlow<AIChatUiState> = combine(
    repository.chatMessages,
    _isThinking
  ) { messages, thinking ->
    AIChatUiState(
      messages = messages,
      isThinking = thinking
    )
  }.stateIn(
    scope = viewModelScope,
    started = SharingStarted.WhileSubscribed(5000),
    initialValue = AIChatUiState()
  )

  fun sendMessage(userText: String) {
    if (userText.isBlank()) return

    // Add student message immediately
    repository.addChatMessage(userText, isStudent = true)

    // Trigger simulated AI thinking & intelligent response
    viewModelScope.launch {
      _isThinking.value = true
      delay(1200) // Realistic AI synthesis latency
      _isThinking.value = false

      val responseText = generateIntelligentResponse(userText)
      repository.addChatMessage(responseText, isStudent = false)
    }
  }

  private fun generateIntelligentResponse(query: String): String {
    val q = query.lowercase()
    return when {
      q.contains("today") || q.contains("due") -> {
        "Based on your 3rd Year CSE profile, you have **2 urgent action items** today:\n\n" +
          "1. 🔴 **Zoho Campus Drive Registration** — Closes in **5 hours** (5:00 PM). Eligible since your CGPA is 8.65.\n" +
          "2. 🔴 **Semester End Exam Fee Payment** — Closes tomorrow 4:00 PM on the ERP portal.\n\n" +
          "I recommend completing the Zoho registration immediately!"
      }
      q.contains("deadline") || q.contains("closest") -> {
        "Here are your upcoming deadlines sorted by urgency:\n\n" +
          "• **Zoho Campus Drive**: Today, 5:00 PM (Critical)\n" +
          "• **Semester Exam Fee**: Tomorrow, 4:00 PM (Critical)\n" +
          "• **DAA Assignment 2 (DP)**: Tomorrow, 11:59 PM (High)\n" +
          "• **Amazon Summer Internship**: In 3 days (High)\n" +
          "• **SIH Hackathon Pitch**: In 4 days (Medium)"
      }
      q.contains("placement") || q.contains("job") || q.contains("hiring") -> {
        "Currently active placement drives matching your profile:\n\n" +
          "• **Zoho Campus Drive 2026** (SDE & QA) — Open for CSE, CGPA ≥ 7.0. Deadline: Today 5 PM.\n" +
          "• **Amazon Summer Internship 2026** — 6-month SDE Intern (₹1.1L/mo). Deadline: In 3 days."
      }
      q.contains("daa") || q.contains("assignment") || q.contains("dynamic programming") -> {
        "**DAA Assignment 2 Summary:**\n" +
          "• **Topics**: Dynamic Programming (LCS, Matrix Chain Multiplication, Knapsack 0/1)\n" +
          "• **Platform**: HackerRank\n" +
          "• **Due**: Tomorrow at 11:59 PM\n" +
          "• **Faculty**: Dr. S. Ramanathan\n" +
          "• Note: Plagiarism checks are enabled."
      }
      q.contains("amazon") || q.contains("internship") -> {
        "**Amazon SDE Summer 2026 Internship:**\n" +
          "• **Eligibility**: 3rd Year CSE/IT with CGPA ≥ 8.0 (Your CGPA: 8.65 ✓ Eligible)\n" +
          "• **Stipend**: ₹1,10,000 / month\n" +
          "• **Action Required**: Fill CDC form and submit application on Amazon portal before Aug 31."
      }
      q.contains("miss") || q.contains("important") -> {
        "You haven't missed anything yet, but **Zoho Registration** and **Exam Fee Payment** are in their final 24 hours. Ensure those are completed!"
      }
      else -> {
        "I analyzed all recent circulars for **'$query'**. Details have been cross-checked with official department circulars and your academic profile (3rd Year CSE). Let me know if you would like me to set a calendar reminder or open the circular!"
      }
    }
  }
}
