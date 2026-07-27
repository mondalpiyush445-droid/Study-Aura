package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AcademicDatabase
import com.example.data.local.AppSettingsEntity
import com.example.data.local.AssignmentEntity
import com.example.data.local.CertificateEntity
import com.example.data.local.ClassScheduleEntity
import com.example.data.local.ExamEntity
import com.example.data.local.FocusSessionEntity
import com.example.data.local.StudyNoteEntity
import com.example.data.local.UserProfileEntity
import com.example.data.remote.GeminiStudyAssistant
import com.example.data.repository.AcademicRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class NavigationTab {
    DASHBOARD,
    COURSE_CURRICULUM,
    SCHEDULE,
    ASSIGNMENTS,
    EXAMS,
    CERTIFICATES,
    NOTES_AI,
    PROFILE_SETTINGS
}

data class ChatMessage(
    val id: Long = System.currentTimeMillis(),
    val sender: String, // "USER" or "AI"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

class AcademicViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AcademicRepository

    val userProfile: StateFlow<UserProfileEntity?>
    val appSettings: StateFlow<AppSettingsEntity?>
    val classSchedules: StateFlow<List<ClassScheduleEntity>>
    val assignments: StateFlow<List<AssignmentEntity>>
    val exams: StateFlow<List<ExamEntity>>
    val certificates: StateFlow<List<CertificateEntity>>
    val notes: StateFlow<List<StudyNoteEntity>>
    val focusSessions: StateFlow<List<FocusSessionEntity>>

    private val _currentTab = MutableStateFlow(NavigationTab.DASHBOARD)
    val currentTab: StateFlow<NavigationTab> = _currentTab.asStateFlow()

    private val _aiMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = "AI",
                text = "Hello Alex! I am your Academic AI Assistant. Ask me anything about your schedules, GPA targets, exam prep, or professional certificates."
            )
        )
    )
    val aiMessages: StateFlow<List<ChatMessage>> = _aiMessages.asStateFlow()

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    // Focus Study Timer
    private val _targetTimerMinutes = MutableStateFlow(25)
    val targetTimerMinutes: StateFlow<Int> = _targetTimerMinutes.asStateFlow()

    private val _focusTimerSeconds = MutableStateFlow(25 * 60)
    val focusTimerSeconds: StateFlow<Int> = _focusTimerSeconds.asStateFlow()

    private val _isFocusTimerRunning = MutableStateFlow(false)
    val isFocusTimerRunning: StateFlow<Boolean> = _isFocusTimerRunning.asStateFlow()

    private val _breaksCount = MutableStateFlow(0)
    val breaksCount: StateFlow<Int> = _breaksCount.asStateFlow()

    private var timerJob: Job? = null

    // App Lock State
    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    init {
        val dao = AcademicDatabase.getDatabase(application).academicDao()
        repository = AcademicRepository(dao)

        userProfile = repository.userProfile.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        appSettings = repository.appSettings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        classSchedules = repository.classSchedules.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        assignments = repository.assignments.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        exams = repository.exams.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        certificates = repository.certificates.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        notes = repository.notes.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        focusSessions = repository.focusSessions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Sync lock state from settings
        viewModelScope.launch {
            repository.appSettings.collect { settings ->
                if (settings != null) {
                    if (settings.isBiometricEnabled && settings.isLocked) {
                        _isAppLocked.value = true
                    }
                }
            }
        }
    }

    fun setNavigationTab(tab: NavigationTab) {
        _currentTab.value = tab
    }

    fun unlockApp(pinEntered: String): Boolean {
        val currentPin = appSettings.value?.pinCode ?: "1234"
        return if (pinEntered == currentPin || pinEntered == "BIOMETRIC_SUCCESS") {
            _isAppLocked.value = false
            true
        } else {
            false
        }
    }

    fun lockApp() {
        if (appSettings.value?.isBiometricEnabled == true) {
            _isAppLocked.value = true
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            val current = appSettings.value ?: AppSettingsEntity()
            repository.saveAppSettings(current.copy(isDarkMode = enabled))
        }
    }

    fun toggleBiometricLock(enabled: Boolean, pinCode: String = "1234") {
        viewModelScope.launch {
            val current = appSettings.value ?: AppSettingsEntity()
            repository.saveAppSettings(current.copy(isBiometricEnabled = enabled, pinCode = pinCode, isLocked = false))
            if (!enabled) {
                _isAppLocked.value = false
            }
        }
    }

    fun updateProfile(name: String, dept: String, uniId: String, currentGpa: Double, targetGpa: Double) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.saveUserProfile(
                current.copy(
                    fullName = name,
                    department = dept,
                    universityId = uniId,
                    currentGpa = currentGpa,
                    targetGpa = targetGpa
                )
            )
        }
    }

    fun updateProfileAvatar(avatarUri: String?) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.saveUserProfile(current.copy(avatarUri = avatarUri))
        }
    }

    fun updateProfileGpa(currentGpa: Double, targetGpa: Double? = null) {
        viewModelScope.launch {
            val current = userProfile.value ?: UserProfileEntity()
            repository.saveUserProfile(
                current.copy(
                    currentGpa = currentGpa,
                    targetGpa = targetGpa ?: current.targetGpa
                )
            )
        }
    }

    // Timer Controls
    fun setTargetTimerMinutes(minutes: Int) {
        timerJob?.cancel()
        _isFocusTimerRunning.value = false
        _targetTimerMinutes.value = minutes.coerceIn(1, 480)
        _focusTimerSeconds.value = minutes * 60
    }

    fun takeBreak() {
        if (_isFocusTimerRunning.value) {
            timerJob?.cancel()
            _isFocusTimerRunning.value = false
        }
        _breaksCount.value += 1
    }

    fun toggleFocusTimer() {
        if (_isFocusTimerRunning.value) {
            timerJob?.cancel()
            _isFocusTimerRunning.value = false
        } else {
            _isFocusTimerRunning.value = true
            timerJob = viewModelScope.launch {
                while (_focusTimerSeconds.value > 0) {
                    delay(1000)
                    _focusTimerSeconds.value -= 1
                }
                _isFocusTimerRunning.value = false
                val totalMins = _targetTimerMinutes.value
                val totalBreaks = _breaksCount.value
                // Log completed focus session
                repository.logFocusSession(
                    FocusSessionEntity(
                        durationMinutes = totalMins,
                        subjectTag = "Focused Study ($totalMins min, $totalBreaks breaks)",
                        notes = "Completed $totalMins min session with $totalBreaks breaks taken."
                    )
                )
                _focusTimerSeconds.value = totalMins * 60
            }
        }
    }

    fun resetFocusTimer() {
        timerJob?.cancel()
        _isFocusTimerRunning.value = false
        _breaksCount.value = 0
        _focusTimerSeconds.value = _targetTimerMinutes.value * 60
    }

    // Entity Mutators
    fun addClassSchedule(name: String, location: String, day: String, start: String, end: String, instructor: String, colorHex: String) {
        viewModelScope.launch {
            repository.addClassSchedule(
                ClassScheduleEntity(
                    courseName = name,
                    location = location,
                    dayOfWeek = day,
                    startTime = start,
                    endTime = end,
                    instructor = instructor,
                    colorHex = colorHex
                )
            )
        }
    }

    fun deleteClassSchedule(schedule: ClassScheduleEntity) {
        viewModelScope.launch { repository.deleteClassSchedule(schedule) }
    }

    fun addAssignment(title: String, course: String, dueDateMillis: Long, priority: String, notes: String) {
        viewModelScope.launch {
            repository.addAssignment(
                AssignmentEntity(
                    title = title,
                    course = course,
                    dueDateMillis = dueDateMillis,
                    priority = priority,
                    isCompleted = false,
                    notes = notes
                )
            )
        }
    }

    fun toggleAssignmentCompletion(assignment: AssignmentEntity) {
        viewModelScope.launch {
            repository.updateAssignment(assignment.copy(isCompleted = !assignment.isCompleted))
        }
    }

    fun deleteAssignment(assignment: AssignmentEntity) {
        viewModelScope.launch { repository.deleteAssignment(assignment) }
    }

    fun addExam(subject: String, dateMillis: Long, coveragePercent: Int, location: String, notes: String) {
        viewModelScope.launch {
            repository.addExam(
                ExamEntity(
                    subject = subject,
                    dateMillis = dateMillis,
                    syllabusCoveragePercent = coveragePercent,
                    location = location,
                    notes = notes
                )
            )
        }
    }

    fun updateExamCoverage(exam: ExamEntity, newPercent: Int) {
        viewModelScope.launch {
            repository.updateExam(exam.copy(syllabusCoveragePercent = newPercent.coerceIn(0, 100)))
        }
    }

    fun deleteExam(exam: ExamEntity) {
        viewModelScope.launch { repository.deleteExam(exam) }
    }

    fun addCertificate(title: String, issuer: String, dateMillis: Long, credId: String, category: String, skills: String, imageUri: String? = null) {
        viewModelScope.launch {
            val randomVerif = "VER-" + (1000..9999).random()
            repository.addCertificate(
                CertificateEntity(
                    title = title,
                    issuer = issuer,
                    issueDateMillis = dateMillis,
                    credentialId = credId,
                    category = category,
                    skills = skills,
                    isVerified = true,
                    verificationCode = randomVerif,
                    imageUri = imageUri
                )
            )
        }
    }

    fun updateCertificateImage(certificate: CertificateEntity, imageUri: String?) {
        viewModelScope.launch {
            repository.addCertificate(certificate.copy(imageUri = imageUri))
        }
    }

    fun deleteCertificate(certificate: CertificateEntity) {
        viewModelScope.launch { repository.deleteCertificate(certificate) }
    }

    fun addNote(title: String, course: String, content: String, tags: String) {
        viewModelScope.launch {
            repository.addNote(
                StudyNoteEntity(
                    title = title,
                    course = course,
                    content = content,
                    tags = tags
                )
            )
        }
    }

    fun updateNote(note: StudyNoteEntity) {
        viewModelScope.launch {
            repository.updateNote(note)
        }
    }

    fun deleteNote(note: StudyNoteEntity) {
        viewModelScope.launch { repository.deleteNote(note) }
    }

    // AI Study Assistant Query
    fun sendAiPrompt(promptText: String) {
        if (promptText.isBlank()) return

        val userMsg = ChatMessage(sender = "USER", text = promptText)
        _aiMessages.value = _aiMessages.value + userMsg
        _isAiLoading.value = true

        viewModelScope.launch {
            val currentNotes = notes.value
            val reply = GeminiStudyAssistant.queryAssistant(promptText, currentNotes)
            val aiMsg = ChatMessage(sender = "AI", text = reply)
            _aiMessages.value = _aiMessages.value + aiMsg
            _isAiLoading.value = false
        }
    }
}
