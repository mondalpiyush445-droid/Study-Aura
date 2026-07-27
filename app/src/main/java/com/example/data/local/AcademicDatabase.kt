package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfileEntity::class,
        ClassScheduleEntity::class,
        AssignmentEntity::class,
        ExamEntity::class,
        CertificateEntity::class,
        StudyNoteEntity::class,
        FocusSessionEntity::class,
        AppSettingsEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AcademicDatabase : RoomDatabase() {

    abstract fun academicDao(): AcademicDao

    companion object {
        @Volatile
        private var INSTANCE: AcademicDatabase? = null

        fun getDatabase(context: Context): AcademicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AcademicDatabase::class.java,
                    "academic_hub_db"
                )
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Prepopulate default academic data asynchronously
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(database.academicDao())
                            }
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(dao: AcademicDao) {
            dao.insertOrUpdateProfile(
                UserProfileEntity(
                    id = 1,
                    fullName = "Alex Scholar",
                    department = "Computer Science",
                    universityId = "CS-2024-8891",
                    currentGpa = 3.88,
                    targetGpa = 3.95,
                    attendanceRate = 94
                )
            )

            dao.insertOrUpdateSettings(
                AppSettingsEntity(
                    id = 1,
                    isDarkMode = false,
                    isBiometricEnabled = false,
                    isLocked = false,
                    pinCode = "1234"
                )
            )

            // Initial Class Schedule
            dao.insertClassSchedule(
                ClassScheduleEntity(
                    courseName = "Data Structures & Algorithms",
                    location = "Room 402, Science Wing",
                    dayOfWeek = "Wednesday",
                    startTime = "09:00 AM",
                    endTime = "10:30 AM",
                    instructor = "Dr. Aris Thorne",
                    colorHex = "#3B82F6"
                )
            )
            dao.insertClassSchedule(
                ClassScheduleEntity(
                    courseName = "Ethics in Technology",
                    location = "Online Seminar Hall",
                    dayOfWeek = "Wednesday",
                    startTime = "11:30 AM",
                    endTime = "01:00 PM",
                    instructor = "Prof. Sarah Chen",
                    colorHex = "#10B981"
                )
            )
            dao.insertClassSchedule(
                ClassScheduleEntity(
                    courseName = "Discrete Mathematics",
                    location = "Hall B, Main Campus",
                    dayOfWeek = "Wednesday",
                    startTime = "02:00 PM",
                    endTime = "03:30 PM",
                    instructor = "Dr. Alan Turing Jr.",
                    colorHex = "#F59E0B"
                )
            )
            dao.insertClassSchedule(
                ClassScheduleEntity(
                    courseName = "Database Systems Architecture",
                    location = "Lab 105, Tech Center",
                    dayOfWeek = "Thursday",
                    startTime = "10:00 AM",
                    endTime = "12:00 PM",
                    instructor = "Prof. Elena Vance",
                    colorHex = "#8B5CF6"
                )
            )

            // Initial Assignments
            val now = System.currentTimeMillis()
            val dayInMillis = 86400000L
            dao.insertAssignment(
                AssignmentEntity(
                    title = "Algorithm Research & Complexity Analysis Paper",
                    course = "Data Structures & Algorithms",
                    dueDateMillis = now + (dayInMillis * 1),
                    priority = "HIGH",
                    isCompleted = false,
                    notes = "Analyze Big-O bounds for Graph Shortest Path algorithms."
                )
            )
            dao.insertAssignment(
                AssignmentEntity(
                    title = "Ethical Framework Case Study on Autonomous AI",
                    course = "Ethics in Technology",
                    dueDateMillis = now + (dayInMillis * 3),
                    priority = "MEDIUM",
                    isCompleted = false,
                    notes = "Review IEEE standards and write 1500-word critical report."
                )
            )
            dao.insertAssignment(
                AssignmentEntity(
                    title = "Set Theory & Matrix Proof Problem Set",
                    course = "Discrete Mathematics",
                    dueDateMillis = now + (dayInMillis * 5),
                    priority = "LOW",
                    isCompleted = true,
                    notes = "Completed problems 1-15 in Chapter 4."
                )
            )

            // Initial Exams
            dao.insertExam(
                ExamEntity(
                    subject = "Database Systems Midterm Exam",
                    dateMillis = now + (dayInMillis * 12),
                    syllabusCoveragePercent = 65,
                    location = "Auditorium A",
                    notes = "Focus on B-Trees, Normalization (3NF/BCNF), and Relational Algebra."
                )
            )
            dao.insertExam(
                ExamEntity(
                    subject = "Network Security & Cryptography Final",
                    dateMillis = now + (dayInMillis * 20),
                    syllabusCoveragePercent = 35,
                    location = "Main Exam Hall",
                    notes = "Review Public Key Infrastructure, RSA, and TLS Handshake."
                )
            )

            // Initial Professional Certificates
            dao.insertCertificate(
                CertificateEntity(
                    title = "Certified Cloud Solutions Architect",
                    issuer = "Global Tech Institute",
                    issueDateMillis = now - (dayInMillis * 60),
                    credentialId = "GTI-ARCH-2024-9912",
                    category = "Professional",
                    skills = "Cloud Security, Distributed Systems, Microservices",
                    isVerified = true,
                    verificationCode = "VER-GTI-9912"
                )
            )
            dao.insertCertificate(
                CertificateEntity(
                    title = "Advanced Data Structures & Algorithms Mastery",
                    issuer = "Academic Council for Computer Science",
                    issueDateMillis = now - (dayInMillis * 120),
                    credentialId = "ACCS-DSA-8821",
                    category = "Academic",
                    skills = "Dynamic Programming, Graph Theory, Memory Optimization",
                    isVerified = true,
                    verificationCode = "VER-ACCS-8821"
                )
            )
            dao.insertCertificate(
                CertificateEntity(
                    title = "Cybersecurity Practitioner & Cryptography Specialist",
                    issuer = "National Cyber Security Association",
                    issueDateMillis = now - (dayInMillis * 180),
                    credentialId = "NCSA-CYBER-5541",
                    category = "Certification",
                    skills = "Biometric Security, Encryption, Network Auditing",
                    isVerified = true,
                    verificationCode = "VER-NCSA-5541"
                )
            )

            // Initial Study Notes
            dao.insertNote(
                StudyNoteEntity(
                    title = "B-Tree vs Hash Index Benchmarks",
                    course = "Database Systems Architecture",
                    content = "B-Trees excel at range queries and sorted scans (O(log N)). Hash indexes provide O(1) point lookups but cannot support range queries efficiently.",
                    tags = "Databases, Indexing, Performance"
                )
            )
            dao.insertNote(
                StudyNoteEntity(
                    title = "Zero Knowledge Proofs Summary",
                    course = "Network Security",
                    content = "ZKP enables a prover to demonstrate knowledge of a secret without revealing the secret itself. Core properties: Completeness, Soundness, Zero-Knowledge.",
                    tags = "Cryptography, Privacy, ZKP"
                )
            )

            // Initial Focus Session
            dao.insertFocusSession(
                FocusSessionEntity(
                    durationMinutes = 25,
                    subjectTag = "Data Structures",
                    notes = "Focused review of Red-Black Tree rotations and balance invariants."
                )
            )
        }
    }
}
