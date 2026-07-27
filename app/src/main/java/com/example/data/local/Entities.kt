package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val fullName: String = "Alex Scholar",
    val department: String = "Computer Science",
    val universityId: String = "CS-2024-8891",
    val currentGpa: Double = 3.88,
    val targetGpa: Double = 3.95,
    val attendanceRate: Int = 94,
    val avatarUri: String? = null
)

@Entity(tableName = "class_schedule")
data class ClassScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val courseName: String,
    val location: String,
    val dayOfWeek: String, // e.g. "Monday", "Wednesday"
    val startTime: String, // e.g. "09:00 AM"
    val endTime: String,   // e.g. "10:30 AM"
    val instructor: String,
    val colorHex: String = "#3B82F6"
)

@Entity(tableName = "assignments")
data class AssignmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val course: String,
    val dueDateMillis: Long,
    val priority: String = "MEDIUM", // HIGH, MEDIUM, LOW
    val isCompleted: Boolean = false,
    val notes: String = ""
)

@Entity(tableName = "exams")
data class ExamEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val subject: String,
    val dateMillis: Long,
    val syllabusCoveragePercent: Int = 50,
    val location: String = "Main Exam Hall",
    val notes: String = ""
)

@Entity(tableName = "certificates")
data class CertificateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val issuer: String,
    val issueDateMillis: Long,
    val credentialId: String,
    val category: String = "Professional", // Academic, Professional, Workshop, Certification
    val skills: String = "",
    val isVerified: Boolean = true,
    val verificationCode: String = "",
    val imageUri: String? = null
)

@Entity(tableName = "study_notes")
data class StudyNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val course: String,
    val content: String,
    val tags: String = "",
    val createdDateMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val durationMinutes: Int,
    val dateMillis: Long = System.currentTimeMillis(),
    val subjectTag: String = "General Study",
    val notes: String = ""
)

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val isDarkMode: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val isLocked: Boolean = false,
    val pinCode: String = "1234"
)
