package com.example.ui.screens.auth

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.constants.AppConstants
import com.example.ui.components.app.ButtonVariant
import com.example.ui.components.app.OneCampusAppScaffold
import com.example.ui.components.app.OneCampusButton
import com.example.ui.components.app.OneCampusTopAppBar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudentOnboardingScreen(
  onCompleteOnboarding: () -> Unit,
  modifier: Modifier = Modifier
) {
  var selectedDepartment by remember { mutableStateOf("CSE") }
  var selectedYear by remember { mutableStateOf(3) }
  var selectedSection by remember { mutableStateOf("A") }
  val selectedInterests = remember {
    mutableStateListOf("Placement", "Hackathons", "Competitive Programming")
  }

  // Calculate dynamic completion percentage
  val completionPercentage = remember(selectedDepartment, selectedYear, selectedSection, selectedInterests.size) {
    var count = 3 // registered name, email, reg no
    if (selectedDepartment.isNotEmpty()) count++
    if (selectedYear > 0) count++
    if (selectedSection.isNotEmpty()) count++
    if (selectedInterests.isNotEmpty()) count++
    ((count / 7.0) * 100).toInt()
  }

  OneCampusAppScaffold(
    topBar = {
      OneCampusTopAppBar(
        title = "Personalize Profile",
        showBackButton = false
      )
    },
    modifier = modifier.testTag("onboarding_screen")
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(20.dp)
    ) {
      // Progress Bar Section
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(
          text = "Profile Completion",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "$completionPercentage%",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      LinearProgressIndicator(
        progress = { completionPercentage / 100f },
        modifier = Modifier
          .fillMaxWidth()
          .height(8.dp)
          .clip(RoundedCornerShape(4.dp))
      )

      Spacer(modifier = Modifier.height(24.dp))

      // Department Selection
      Text(
        text = "Select Department",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(8.dp))
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        AppConstants.DEPARTMENTS.forEach { dept ->
          val isSelected = selectedDepartment == dept
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .clickable { selectedDepartment = dept }
          ) {
            Text(
              text = dept,
              color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Year Selection
      Text(
        text = "Academic Year",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppConstants.YEARS.forEach { year ->
          val isSelected = selectedYear == year
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .clickable { selectedYear = year }
          ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
              Text(
                text = "Year $year",
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(20.dp))

      // Section Selection
      Text(
        text = "Section",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        AppConstants.SECTIONS.forEach { sec ->
          val isSelected = selectedSection == sec
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
            border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(10.dp))
              .clickable { selectedSection = sec }
          ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 10.dp)) {
              Text(
                text = "Sec $sec",
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      // Interests Selection
      Text(
        text = "Areas of Interest",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
      )
      Text(
        text = "AI uses these tags to prioritize announcements for you",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(10.dp))

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        AppConstants.INTEREST_OPTIONS.forEach { interest ->
          val isSelected = selectedInterests.contains(interest)
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            border = BorderStroke(
              1.dp,
              if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier
              .clip(RoundedCornerShape(20.dp))
              .clickable {
                if (isSelected) selectedInterests.remove(interest) else selectedInterests.add(interest)
              }
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
              if (isSelected) {
                Icon(
                  imageVector = Icons.Filled.Check,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
              }
              Text(
                text = interest,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 13.sp
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(32.dp))

      OneCampusButton(
        text = "Complete Profile & Start",
        onClick = onCompleteOnboarding,
        fullWidth = true,
        variant = ButtonVariant.PRIMARY,
        testTag = "onboarding_complete_button"
      )
    }
  }
}
