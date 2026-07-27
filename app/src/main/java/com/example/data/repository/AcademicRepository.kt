package com.example.data.repository

import com.example.data.local.AcademicDao
import com.example.data.local.AppSettingsEntity
import com.example.data.local.AssignmentEntity
import com.example.data.local.CertificateEntity
import com.example.data.local.ClassScheduleEntity
import com.example.data.local.ExamEntity
import com.example.data.local.FocusSessionEntity
import com.example.data.local.StudyNoteEntity
import com.example.data.local.UserProfileEntity
import kotlinx.coroutines.flow.Flow

class AcademicRepository(private val dao: AcademicDao) {

    val userProfile: Flow<UserProfileEntity?> = dao.getUserProfile()
    val appSettings: Flow<AppSettingsEntity?> = dao.getAppSettings()
    val classSchedules: Flow<List<ClassScheduleEntity>> = dao.getAllClassSchedules()
    val assignments: Flow<List<AssignmentEntity>> = dao.getAllAssignments()
    val exams: Flow<List<ExamEntity>> = dao.getAllExams()
    val certificates: Flow<List<CertificateEntity>> = dao.getAllCertificates()
    val notes: Flow<List<StudyNoteEntity>> = dao.getAllNotes()
    val focusSessions: Flow<List<FocusSessionEntity>> = dao.getAllFocusSessions()

    suspend fun saveUserProfile(profile: UserProfileEntity) {
        dao.insertOrUpdateProfile(profile)
    }

    suspend fun saveAppSettings(settings: AppSettingsEntity) {
        dao.insertOrUpdateSettings(settings)
    }

    suspend fun addClassSchedule(schedule: ClassScheduleEntity) {
        dao.insertClassSchedule(schedule)
    }

    suspend fun deleteClassSchedule(schedule: ClassScheduleEntity) {
        dao.deleteClassSchedule(schedule)
    }

    suspend fun addAssignment(assignment: AssignmentEntity) {
        dao.insertAssignment(assignment)
    }

    suspend fun updateAssignment(assignment: AssignmentEntity) {
        dao.updateAssignment(assignment)
    }

    suspend fun deleteAssignment(assignment: AssignmentEntity) {
        dao.deleteAssignment(assignment)
    }

    suspend fun addExam(exam: ExamEntity) {
        dao.insertExam(exam)
    }

    suspend fun updateExam(exam: ExamEntity) {
        dao.updateExam(exam)
    }

    suspend fun deleteExam(exam: ExamEntity) {
        dao.deleteExam(exam)
    }

    suspend fun addCertificate(certificate: CertificateEntity) {
        dao.insertCertificate(certificate)
    }

    suspend fun deleteCertificate(certificate: CertificateEntity) {
        dao.deleteCertificate(certificate)
    }

    suspend fun addNote(note: StudyNoteEntity) {
        dao.insertNote(note)
    }

    suspend fun updateNote(note: StudyNoteEntity) {
        dao.insertNote(note)
    }

    suspend fun deleteNote(note: StudyNoteEntity) {
        dao.deleteNote(note)
    }

    suspend fun logFocusSession(session: FocusSessionEntity) {
        dao.insertFocusSession(session)
    }
}
