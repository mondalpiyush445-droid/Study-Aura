package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AcademicNavigationRail
import com.example.ui.components.AcademicSideDrawerSheet
import com.example.ui.components.AcademicTopAppBar
import com.example.ui.components.BiometricLockOverlay
import com.example.ui.screens.AssignmentsScreen
import com.example.ui.screens.CertificatesScreen
import com.example.ui.screens.CourseScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExamsScreen
import com.example.ui.screens.NotesScreen
import com.example.ui.screens.ProfileSettingsScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.theme.AcademicHubTheme
import com.example.ui.viewmodel.AcademicViewModel
import com.example.ui.viewmodel.NavigationTab
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: AcademicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
            val isDarkMode = appSettings?.isDarkMode ?: false

            AcademicHubTheme(darkTheme = isDarkMode) {
                AcademicHubApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AcademicHubApp(viewModel: AcademicViewModel) {
    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
    val appSettings by viewModel.appSettings.collectAsStateWithLifecycle()
    val classSchedules by viewModel.classSchedules.collectAsStateWithLifecycle()
    val assignments by viewModel.assignments.collectAsStateWithLifecycle()
    val exams by viewModel.exams.collectAsStateWithLifecycle()
    val certificates by viewModel.certificates.collectAsStateWithLifecycle()
    val notes by viewModel.notes.collectAsStateWithLifecycle()
    val aiMessages by viewModel.aiMessages.collectAsStateWithLifecycle()
    val isAiLoading by viewModel.isAiLoading.collectAsStateWithLifecycle()

    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val timerSeconds by viewModel.focusTimerSeconds.collectAsStateWithLifecycle()
    val targetTimerMinutes by viewModel.targetTimerMinutes.collectAsStateWithLifecycle()
    val breaksCount by viewModel.breaksCount.collectAsStateWithLifecycle()
    val isTimerRunning by viewModel.isFocusTimerRunning.collectAsStateWithLifecycle()
    val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 600.dp

        if (isAppLocked) {
            BiometricLockOverlay(
                onUnlockAttempt = { pin -> viewModel.unlockApp(pin) }
            )
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    AcademicSideDrawerSheet(
                        selectedTab = currentTab,
                        userName = userProfile?.fullName ?: "Alex Scholar",
                        department = userProfile?.department ?: "Computer Science",
                        currentGpa = userProfile?.currentGpa ?: 3.82,
                        isDarkMode = appSettings?.isDarkMode ?: false,
                        isBiometricEnabled = appSettings?.isBiometricEnabled ?: false,
                        onTabSelected = { tab ->
                            viewModel.setNavigationTab(tab)
                            coroutineScope.launch { drawerState.close() }
                        },
                        onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                        onLockAppNow = {
                            viewModel.lockApp()
                            coroutineScope.launch { drawerState.close() }
                        }
                    )
                }
            ) {
                Scaffold(
                    topBar = {
                        AcademicTopAppBar(
                            title = when (currentTab) {
                                NavigationTab.DASHBOARD -> "Study Aura"
                                NavigationTab.COURSE_CURRICULUM -> "My Course & Syllabus"
                                NavigationTab.SCHEDULE -> "Schedule & 75% Attendance Tracker"
                                NavigationTab.ASSIGNMENTS -> "Assignments"
                                NavigationTab.EXAMS -> "Exams & Midterms"
                                NavigationTab.CERTIFICATES -> "Credentials"
                                NavigationTab.NOTES_AI -> "Notes, AI & Wikipedia"
                                NavigationTab.PROFILE_SETTINGS -> "Profile & Security"
                            },
                            userName = userProfile?.fullName ?: "Alex Scholar",
                            isDarkMode = appSettings?.isDarkMode ?: false,
                            isBiometricEnabled = appSettings?.isBiometricEnabled ?: false,
                            onMenuClicked = {
                                coroutineScope.launch {
                                    if (drawerState.isClosed) drawerState.open() else drawerState.close()
                                }
                            },
                            onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                            onLockClicked = { viewModel.lockApp() },
                            onAiClicked = { viewModel.setNavigationTab(NavigationTab.NOTES_AI) },
                            onProfileClicked = { viewModel.setNavigationTab(NavigationTab.PROFILE_SETTINGS) }
                        )
                    }
                ) { innerPadding ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        if (isWideScreen) {
                            AcademicNavigationRail(
                                selectedTab = currentTab,
                                onTabSelected = { viewModel.setNavigationTab(it) }
                            )
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            when (currentTab) {
                                NavigationTab.DASHBOARD -> {
                                    DashboardScreen(
                                        userProfile = userProfile,
                                        classSchedules = classSchedules,
                                        assignments = assignments,
                                        exams = exams,
                                        timerSeconds = timerSeconds,
                                        targetTimerMinutes = targetTimerMinutes,
                                        breaksCount = breaksCount,
                                        isTimerRunning = isTimerRunning,
                                        onToggleTimer = { viewModel.toggleFocusTimer() },
                                        onTakeBreak = { viewModel.takeBreak() },
                                        onResetTimer = { viewModel.resetFocusTimer() },
                                        onSetTimerDuration = { mins -> viewModel.setTargetTimerMinutes(mins) },
                                        onNavigateTab = { viewModel.setNavigationTab(it) },
                                        onOpenAddDialog = { viewModel.setNavigationTab(NavigationTab.SCHEDULE) },
                                        onUpdateGpa = { newGpa ->
                                            viewModel.updateProfile(
                                                userProfile?.fullName ?: "Alex Scholar",
                                                userProfile?.department ?: "Computer Science",
                                                userProfile?.universityId ?: "CS-2024-8891",
                                                newGpa,
                                                userProfile?.targetGpa ?: 3.95
                                            )
                                        }
                                    )
                                }
                                NavigationTab.COURSE_CURRICULUM -> {
                                    CourseScreen()
                                }
                                NavigationTab.SCHEDULE -> {
                                    ScheduleScreen(
                                        classSchedules = classSchedules,
                                        onAddClassSchedule = { name, loc, day, start, end, instructor ->
                                            viewModel.addClassSchedule(name, loc, day, start, end, instructor, "#3B82F6")
                                        },
                                        onDeleteClassSchedule = { viewModel.deleteClassSchedule(it) }
                                    )
                                }
                                NavigationTab.ASSIGNMENTS -> {
                                    AssignmentsScreen(
                                        assignments = assignments,
                                        onAddAssignment = { title, course, due, prio, notes ->
                                            viewModel.addAssignment(title, course, due, prio, notes)
                                        },
                                        onToggleCompletion = { viewModel.toggleAssignmentCompletion(it) },
                                        onDeleteAssignment = { viewModel.deleteAssignment(it) }
                                    )
                                }
                                NavigationTab.EXAMS -> {
                                    ExamsScreen(
                                        exams = exams,
                                        onAddExam = { subject, date, coverage, loc, notes ->
                                            viewModel.addExam(subject, date, coverage, loc, notes)
                                        },
                                        onUpdateCoverage = { exam, coverage ->
                                            viewModel.updateExamCoverage(exam, coverage)
                                        },
                                        onDeleteExam = { viewModel.deleteExam(it) }
                                    )
                                }
                                NavigationTab.CERTIFICATES -> {
                                    CertificatesScreen(
                                        certificates = certificates,
                                        onAddCertificate = { title, issuer, date, credId, category, skills ->
                                            viewModel.addCertificate(title, issuer, date, credId, category, skills)
                                        },
                                        onDeleteCertificate = { viewModel.deleteCertificate(it) },
                                        onAttachImage = { cert, uri -> viewModel.updateCertificateImage(cert, uri) }
                                    )
                                }
                                NavigationTab.NOTES_AI -> {
                                    NotesScreen(
                                        notes = notes,
                                        aiMessages = aiMessages,
                                        isAiLoading = isAiLoading,
                                        onSendAiPrompt = { viewModel.sendAiPrompt(it) },
                                        onAddNote = { title, course, content, tags ->
                                            viewModel.addNote(title, course, content, tags)
                                        },
                                        onUpdateNote = { viewModel.updateNote(it) },
                                        onDeleteNote = { viewModel.deleteNote(it) }
                                    )
                                }
                                NavigationTab.PROFILE_SETTINGS -> {
                                    ProfileSettingsScreen(
                                        userProfile = userProfile,
                                        appSettings = appSettings,
                                        onUpdateProfile = { name, dept, uniId, cGpa, tGpa ->
                                            viewModel.updateProfile(name, dept, uniId, cGpa, tGpa)
                                        },
                                        onUpdateAvatar = { uri -> viewModel.updateProfileAvatar(uri) },
                                        onToggleDarkMode = { viewModel.toggleDarkMode(it) },
                                        onToggleBiometricLock = { enabled, pin ->
                                            viewModel.toggleBiometricLock(enabled, pin)
                                        },
                                        onLockAppNow = { viewModel.lockApp() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
