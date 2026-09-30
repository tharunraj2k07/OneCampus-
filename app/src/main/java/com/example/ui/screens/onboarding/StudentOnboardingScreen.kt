package com.example.ui.screens.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.constants.AppConstants
import com.example.core.data.DemoRepository
import com.example.ui.components.app.ButtonVariant
import com.example.ui.components.app.OneCampusAppScaffold
import com.example.ui.components.app.OneCampusButton
import com.example.ui.components.app.OneCampusCard
import com.example.ui.theme.AiGlowCyan

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudentOnboardingScreen(
  onOnboardingComplete: () -> Unit,
  modifier: Modifier = Modifier
) {
  var currentStep by remember { mutableIntStateOf(1) } // 1: Academic, 2: Interests, 3: Personalization
  var selectedDepartment by remember { mutableStateOf("CSE") }
  var selectedYear by remember { mutableIntStateOf(3) }
  var selectedSection by remember { mutableStateOf("A") }
  var selectedInterests by remember {
    mutableStateOf(listOf("Placement", "Competitive Programming", "Hackathons", "Internship", "Workshops"))
  }

  OneCampusAppScaffold(
    modifier = modifier.testTag("onboarding_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 24.dp, vertical = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Step Indicator Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "Step $currentStep of 3",
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
        Text(
          text = when (currentStep) {
            1 -> "Academic Profile"
            2 -> "Career & Interests"
            else -> "Personalization"
          },
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      LinearProgressIndicator(
        progress = { (currentStep.toFloat() / 3f) },
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp)),
        strokeCap = StrokeCap.Round,
      )

      Spacer(modifier = Modifier.height(24.dp))

      AnimatedContent(
        targetState = currentStep,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "onboarding_step"
      ) { step ->
        when (step) {
          1 -> {
            // STEP 1: Academic Information
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
              Text(
                text = "Select Your Academic Details",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "OneCampus AI tailors department notices, assignments, and exam schedules to your exact class.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
              )

              // Department Selection
              Text(
                text = "Department",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(8.dp))
              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                AppConstants.DEPARTMENTS.forEach { dept ->
                  FilterChip(
                    selected = selectedDepartment == dept,
                    onClick = { selectedDepartment = dept },
                    label = { Text(dept) },
                    leadingIcon = if (selectedDepartment == dept) {
                      { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                      selectedLabelColor = MaterialTheme.colorScheme.primary
                    )
                  )
                }
              }

              Spacer(modifier = Modifier.height(20.dp))

              // Year Selection
              Text(
                text = "Year of Study",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                AppConstants.YEARS.forEach { yr ->
                  FilterChip(
                    selected = selectedYear == yr,
                    onClick = { selectedYear = yr },
                    label = { Text("Year $yr") },
                    leadingIcon = if (selectedYear == yr) {
                      { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f)
                  )
                }
              }

              Spacer(modifier = Modifier.height(20.dp))

              // Section Selection
              Text(
                text = "Section",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
              )
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                AppConstants.SECTIONS.forEach { sec ->
                  FilterChip(
                    selected = selectedSection == sec,
                    onClick = { selectedSection = sec },
                    label = { Text("Sec $sec") },
                    leadingIcon = if (selectedSection == sec) {
                      { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f)
                  )
                }
              }
            }
          }

          2 -> {
            // STEP 2: Interests & Goals
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
              Text(
                text = "What Are You Interested In?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
              )
              Text(
                text = "Select all topics you care about. The AI will prioritize matching opportunities in your feed and daily summary.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
              )

              FlowRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
              ) {
                AppConstants.INTEREST_OPTIONS.forEach { interest ->
                  val isSelected = selectedInterests.contains(interest)
                  FilterChip(
                    selected = isSelected,
                    onClick = {
                      selectedInterests = if (isSelected) {
                        selectedInterests - interest
                      } else {
                        selectedInterests + interest
                      }
                    },
                    label = {
                      Text(
                        text = interest,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                      )
                    },
                    leadingIcon = if (isSelected) {
                      { Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null,
                    colors = FilterChipDefaults.filterChipColors(
                      selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                      selectedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.padding(vertical = 2.dp)
                  )
                }
              }
            }
          }

          3 -> {
            // STEP 3: Personalization Summary & Activation
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.fillMaxWidth()
            ) {
              Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(80.dp)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = AiGlowCyan,
                    modifier = Modifier.size(44.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              Text(
                text = "Personalizing OneCampus AI for You",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
              )

              Text(
                text = "We configured your intelligent assistant with your academic profile and preferences.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
              )

              OneCampusCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = "Profile Summary",
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold
                    )
                    Surface(
                      shape = RoundedCornerShape(8.dp),
                      color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                      Text(
                        text = "100% Ready",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                      )
                    }
                  }

                  Spacer(modifier = Modifier.height(12.dp))

                  Text(
                    text = "• Academic Class: $selectedDepartment | Year $selectedYear - Sec $selectedSection",
                    style = MaterialTheme.typography.bodyMedium
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "• Active Interests: ${selectedInterests.joinToString(", ")}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "• AI Daily Briefing: Enabled",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                  )
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.weight(1f))
      Spacer(modifier = Modifier.height(24.dp))

      // Navigation Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        if (currentStep > 1) {
          OneCampusButton(
            text = "Back",
            onClick = { currentStep-- },
            variant = ButtonVariant.OUTLINE,
            modifier = Modifier.weight(1f)
          )
        }

        OneCampusButton(
          text = if (currentStep == 3) "Go to Command Center →" else "Continue →",
          onClick = {
            if (currentStep < 3) {
              currentStep++
            } else {
              DemoRepository.updateStudentProfile(
                department = selectedDepartment,
                year = selectedYear,
                section = selectedSection,
                interests = selectedInterests
              )
              onOnboardingComplete()
            }
          },
          variant = ButtonVariant.PRIMARY,
          modifier = Modifier.weight(if (currentStep > 1) 1.5f else 1f)
        )
      }
    }
  }
}
