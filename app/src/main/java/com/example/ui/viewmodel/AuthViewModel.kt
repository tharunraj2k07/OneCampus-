package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.core.data.DemoRepository
import com.example.core.model.Role
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AuthUiState(
  val email: String = "",
  val password: String = "",
  val fullName: String = "",
  val confirmPassword: String = "",
  val isPasswordVisible: Boolean = false,
  val selectedDemoRole: Role = Role.STUDENT,
  val errorMessage: String? = null,
  val isLoading: Boolean = false,
  val isOnboardingStep: Int = 1,
  val selectedDepartment: String = "CSE",
  val selectedYear: Int = 3,
  val selectedSection: String = "A",
  val selectedInterests: List<String> = listOf("Placement", "Competitive Programming", "Hackathons")
)

class AuthViewModel(
  private val repository: DemoRepository = DemoRepository
) : ViewModel() {

  private val _uiState = MutableStateFlow(AuthUiState())
  val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

  fun onEmailChange(email: String) {
    _uiState.value = _uiState.value.copy(email = email, errorMessage = null)
  }

  fun onPasswordChange(password: String) {
    _uiState.value = _uiState.value.copy(password = password, errorMessage = null)
  }

  fun onFullNameChange(name: String) {
    _uiState.value = _uiState.value.copy(fullName = name, errorMessage = null)
  }

  fun onConfirmPasswordChange(password: String) {
    _uiState.value = _uiState.value.copy(confirmPassword = password, errorMessage = null)
  }

  fun togglePasswordVisibility() {
    _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
  }

  fun selectDemoRole(role: Role) {
    _uiState.value = _uiState.value.copy(
      selectedDemoRole = role,
      email = if (role == Role.FACULTY) "ramanathan.cse@college.edu" else "tharun.cse@college.edu",
      password = "••••••••"
    )
    repository.switchUserRole(role)
  }

  fun updateAcademicInfo(department: String, year: Int, section: String) {
    _uiState.value = _uiState.value.copy(
      selectedDepartment = department,
      selectedYear = year,
      selectedSection = section
    )
  }

  fun toggleInterest(interest: String) {
    val current = _uiState.value.selectedInterests
    val updated = if (current.contains(interest)) {
      current - interest
    } else {
      current + interest
    }
    _uiState.value = _uiState.value.copy(selectedInterests = updated)
  }

  fun nextOnboardingStep() {
    _uiState.value = _uiState.value.copy(isOnboardingStep = _uiState.value.isOnboardingStep + 1)
  }

  fun prevOnboardingStep() {
    if (_uiState.value.isOnboardingStep > 1) {
      _uiState.value = _uiState.value.copy(isOnboardingStep = _uiState.value.isOnboardingStep - 1)
    }
  }

  fun completeOnboarding() {
    repository.updateStudentProfile(
      department = _uiState.value.selectedDepartment,
      year = _uiState.value.selectedYear,
      section = _uiState.value.selectedSection,
      interests = _uiState.value.selectedInterests
    )
  }
}
