package com.example.ui.components.category

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryMeta(
  val label: String,
  val icon: ImageVector,
  val accentColor: Color
)

object CategoryConstants {
  fun getMeta(category: String): CategoryMeta {
    return when (category.trim().lowercase()) {
      "placement" -> CategoryMeta("Placement", Icons.Filled.BusinessCenter, Color(0xFF2563EB))
      "assignment" -> CategoryMeta("Assignment", Icons.Filled.Assignment, Color(0xFFF59E0B))
      "internship" -> CategoryMeta("Internship", Icons.Filled.Work, Color(0xFF0D9488))
      "examination", "exam" -> CategoryMeta("Examination", Icons.Filled.School, Color(0xFFDC2626))
      "workshop" -> CategoryMeta("Workshop", Icons.Filled.Psychology, Color(0xFF8B5CF6))
      "event" -> CategoryMeta("Event", Icons.Filled.Event, Color(0xFFEC4899))
      "coding contest", "coding" -> CategoryMeta("Coding Contest", Icons.Filled.Code, Color(0xFF059669))
      "club activity", "club" -> CategoryMeta("Club Activity", Icons.Filled.Groups, Color(0xFFD97706))
      "registration" -> CategoryMeta("Registration", Icons.Filled.HowToReg, Color(0xFF0284C7))
      "scholarship" -> CategoryMeta("Scholarship", Icons.Filled.CardMembership, Color(0xFF10B981))
      "competition", "hackathon" -> CategoryMeta("Competition", Icons.Filled.EmojiEvents, Color(0xFFEA580C))
      else -> CategoryMeta(category.ifBlank { "General" }, Icons.Filled.Info, Color(0xFF64748B))
    }
  }
}
