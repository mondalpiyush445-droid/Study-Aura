package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import java.util.Locale

data class CourseGradeEntry(
    var name: String,
    var credits: String,
    var gradePoint: Double,
    var gradeLetter: String
)

data class BcaSubjectMark(
    var code: String,
    var name: String,
    var credits: Int,
    var marksObtained: Int,
    var maxMarks: Int = 100,
    var gradePoint: Double
) {
    val gradeLetter: String
        get() = when {
            marksObtained >= 85 -> "A+ (4.0)"
            marksObtained >= 75 -> "A (3.7)"
            marksObtained >= 65 -> "B+ (3.3)"
            marksObtained >= 55 -> "B (3.0)"
            marksObtained >= 50 -> "C (2.0)"
            else -> "F (0.0)"
        }
}

data class BcaSemester(
    val semesterNumber: Int,
    val yearName: String,
    val subjects: MutableList<BcaSubjectMark>
) {
    val totalMarksObtained: Int get() = subjects.sumOf { it.marksObtained }
    val totalMaxMarks: Int get() = subjects.sumOf { it.maxMarks }
    val totalCredits: Int get() = subjects.sumOf { it.credits }
    val totalWeightedPoints: Double get() = subjects.sumOf { it.credits * it.gradePoint }
    val sgpa: Double get() = if (totalCredits > 0) totalWeightedPoints / totalCredits else 0.0
    val percentage: Double get() = if (totalMaxMarks > 0) (totalMarksObtained.toDouble() / totalMaxMarks) * 100 else 0.0
}

val GRADE_OPTIONS = listOf(
    "A (4.0)" to 4.0,
    "A- (3.7)" to 3.7,
    "B+ (3.3)" to 3.3,
    "B (3.0)" to 3.0,
    "B- (2.7)" to 2.7,
    "C+ (2.3)" to 2.3,
    "C (2.0)" to 2.0,
    "D (1.0)" to 1.0,
    "F (0.0)" to 0.0
)

fun getInitialBcaSemesters(): List<BcaSemester> {
    return listOf(
        BcaSemester(
            semesterNumber = 1,
            yearName = "Year 1 - Semester 1",
            subjects = mutableStateListOf(
                BcaSubjectMark("BCA101", "Problem Solving using C", 4, 88, 100, 4.0),
                BcaSubjectMark("BCA102", "Computer Fundamentals & Office Tools", 3, 82, 100, 3.7),
                BcaSubjectMark("BCA103", "Mathematical Foundations for CS", 4, 78, 100, 3.3),
                BcaSubjectMark("BCA104", "Technical Communication Skills", 3, 85, 100, 4.0),
                BcaSubjectMark("BCA105", "Digital Logic & Architecture", 4, 80, 100, 3.7)
            )
        ),
        BcaSemester(
            semesterNumber = 2,
            yearName = "Year 1 - Semester 2",
            subjects = mutableStateListOf(
                BcaSubjectMark("BCA201", "Data Structures in C", 4, 90, 100, 4.0),
                BcaSubjectMark("BCA202", "Discrete Mathematics", 4, 84, 100, 3.7),
                BcaSubjectMark("BCA203", "Environmental Studies & Ethics", 2, 88, 100, 4.0),
                BcaSubjectMark("BCA204", "Financial Accounting & Management", 3, 76, 100, 3.3),
                BcaSubjectMark("BCA205", "OOP Concepts using C++", 4, 86, 100, 4.0)
            )
        ),
        BcaSemester(
            semesterNumber = 3,
            yearName = "Year 2 - Semester 3",
            subjects = mutableStateListOf(
                BcaSubjectMark("BCA301", "Database Management Systems", 4, 92, 100, 4.0),
                BcaSubjectMark("BCA302", "Operating Systems Architecture", 4, 85, 100, 4.0),
                BcaSubjectMark("BCA303", "Software Engineering Methods", 3, 88, 100, 4.0),
                BcaSubjectMark("BCA304", "Computer Networks & TCP/IP", 4, 82, 100, 3.7),
                BcaSubjectMark("BCA305", "Probability & Numerical Methods", 3, 80, 100, 3.7)
            )
        ),
        BcaSemester(
            semesterNumber = 4,
            yearName = "Year 2 - Semester 4",
            subjects = mutableStateListOf(
                BcaSubjectMark("BCA401", "Java Programming & Application Dev", 4, 94, 100, 4.0),
                BcaSubjectMark("BCA402", "Design & Analysis of Algorithms", 4, 88, 100, 4.0),
                BcaSubjectMark("BCA403", "Web Technologies (HTML5/CSS/JS)", 4, 90, 100, 4.0),
                BcaSubjectMark("BCA404", "E-Commerce & Management Systems", 3, 84, 100, 3.7),
                BcaSubjectMark("BCA405", "System Software & Unix/Linux", 3, 86, 100, 4.0)
            )
        ),
        BcaSemester(
            semesterNumber = 5,
            yearName = "Year 3 - Semester 5",
            subjects = mutableStateListOf(
                BcaSubjectMark("BCA501", "Python Programming & Data Analytics", 4, 95, 100, 4.0),
                BcaSubjectMark("BCA502", "Android App Development", 4, 91, 100, 4.0),
                BcaSubjectMark("BCA503", "Software Testing & QA", 3, 87, 100, 4.0),
                BcaSubjectMark("BCA504", "Artificial Intelligence Fundamentals", 4, 89, 100, 4.0),
                BcaSubjectMark("BCA505", "Cloud Computing & DevOps", 3, 85, 100, 4.0)
            )
        ),
        BcaSemester(
            semesterNumber = 6,
            yearName = "Year 3 - Semester 6",
            subjects = mutableStateListOf(
                BcaSubjectMark("BCA601", "Full Stack Web Engineering", 4, 93, 100, 4.0),
                BcaSubjectMark("BCA602", "Machine Learning & AI Tools", 4, 90, 100, 4.0),
                BcaSubjectMark("BCA603", "Information & Cyber Security", 3, 88, 100, 4.0),
                BcaSubjectMark("BCA604", "Elective I: Big Data Systems", 3, 86, 100, 4.0),
                BcaSubjectMark("BCA605", "Minor Software Project & Seminar", 4, 96, 100, 4.0)
            )
        ),
        BcaSemester(
            semesterNumber = 7,
            yearName = "Year 4 - Semester 7",
            subjects = mutableStateListOf(
                BcaSubjectMark("BCA701", "Deep Learning & Neural Nets", 4, 92, 100, 4.0),
                BcaSubjectMark("BCA702", "Internet of Things & Sensors", 4, 88, 100, 4.0),
                BcaSubjectMark("BCA703", "Cyber Law, Ethics & Patents", 3, 85, 100, 4.0),
                BcaSubjectMark("BCA704", "Elective II: Cloud Microservices", 3, 90, 100, 4.0),
                BcaSubjectMark("BCA705", "Major Capstone Project Phase 1", 5, 95, 100, 4.0)
            )
        ),
        BcaSemester(
            semesterNumber = 8,
            yearName = "Year 4 - Semester 8",
            subjects = mutableStateListOf(
                BcaSubjectMark("BCA801", "Advanced AI & LLM Systems", 4, 94, 100, 4.0),
                BcaSubjectMark("BCA802", "Enterprise System Architecture", 4, 91, 100, 4.0),
                BcaSubjectMark("BCA803", "Industrial Internship & Defense", 6, 98, 100, 4.0),
                BcaSubjectMark("BCA804", "Major Capstone Project Phase 2", 6, 97, 100, 4.0)
            )
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CgpaCalculatorDialog(
    initialCgpa: Double,
    onDismiss: () -> Unit,
    onApplyToProfile: (newCgpa: Double) -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) } // 0: 4-Year BCA (8 Semesters), 1: Quick Sem Calculator

    // 8 Semesters State
    val bcaSemesters = remember { getInitialBcaSemesters() }
    val expandedSemesters = remember { mutableStateListOf<Int>(1) } // Sem 1 expanded by default

    // Computations for 4-Year BCA
    val totalBcaSubjects = bcaSemesters.sumOf { it.subjects.size }
    val totalBcaMarksObtained = bcaSemesters.sumOf { it.totalMarksObtained }
    val totalBcaMaxMarks = bcaSemesters.sumOf { it.totalMaxMarks }
    val totalBcaCredits = bcaSemesters.sumOf { it.totalCredits }
    val totalBcaPoints = bcaSemesters.sumOf { it.totalWeightedPoints }
    val bcaCumulativeCgpa = if (totalBcaCredits > 0) totalBcaPoints / totalBcaCredits else 0.0
    val bcaOverallPercentage = if (totalBcaMaxMarks > 0) (totalBcaMarksObtained.toDouble() / totalBcaMaxMarks) * 100 else 0.0

    // Quick Calculator State
    val customCourses = remember {
        mutableStateListOf(
            CourseGradeEntry("Data Structures", "3.0", 4.0, "A (4.0)"),
            CourseGradeEntry("Algorithms & Logic", "3.0", 3.7, "A- (3.7)"),
            CourseGradeEntry("Database Systems", "4.0", 3.3, "B+ (3.3)")
        )
    }
    var prevCreditsStr by remember { mutableStateOf("30.0") }
    var prevCgpaStr by remember { mutableStateOf(initialCgpa.toString()) }

    // Quick Calculator Computations
    val quickSemCredits = customCourses.sumOf { it.credits.toDoubleOrNull() ?: 0.0 }
    val quickSemPoints = customCourses.sumOf { (it.credits.toDoubleOrNull() ?: 0.0) * it.gradePoint }
    val quickSemesterGpa = if (quickSemCredits > 0) quickSemPoints / quickSemCredits else 0.0

    val prevCredits = prevCreditsStr.toDoubleOrNull() ?: 0.0
    val prevCgpa = prevCgpaStr.toDoubleOrNull() ?: 0.0
    val prevPoints = prevCredits * prevCgpa

    val cumulativeCredits = prevCredits + quickSemCredits
    val cumulativePoints = prevPoints + quickSemPoints
    val quickCumulativeCgpa = if (cumulativeCredits > 0) cumulativePoints / cumulativeCredits else quickSemesterGpa

    val finalCalculatedCgpa = if (selectedTabIndex == 0) bcaCumulativeCgpa else quickCumulativeCgpa

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = "Academic Degrees",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("BCA 4-Year (8 Sem) & CGPA Calculator", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                }

                Spacer(modifier = Modifier.height(12.dp))

                TabRow(selectedTabIndex = selectedTabIndex) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("4-Year BCA (8 Sems)", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Quick Calculator", fontWeight = FontWeight.Bold) }
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Computed Result Banner
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            if (selectedTabIndex == 0) {
                                Text(
                                    text = "4-Year BCA Degree Completed",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Total Subjects: $totalBcaSubjects • Marks: $totalBcaMarksObtained / $totalBcaMaxMarks",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                                )
                                Text(
                                    text = "Overall Percentage: ${String.format(Locale.getDefault(), "%.2f", bcaOverallPercentage)}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Text(
                                    text = "Semester SGPA: ${String.format(Locale.getDefault(), "%.2f", quickSemesterGpa)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "Semester Credits: $quickSemCredits",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary)
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (selectedTabIndex == 0) "OVERALL CGPA" else "CUMULATIVE CGPA",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                    text = String.format(Locale.getDefault(), "%.2f", finalCalculatedCgpa),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTabIndex == 0) {
                    // TAB 0: 4-Year BCA (8 Semesters Breakdown)
                    Text(
                        text = "8 Semesters Marks & SGPA Breakdown (Tap to expand/edit subjects)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                    ) {
                        itemsIndexed(bcaSemesters) { semIndex, sem ->
                            val isExpanded = expandedSemesters.contains(sem.semesterNumber)

                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    // Header Row for Semester
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                if (isExpanded) expandedSemesters.remove(sem.semesterNumber)
                                                else expandedSemesters.add(sem.semesterNumber)
                                            }
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                                                contentDescription = "Expand",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(
                                                    text = sem.yearName,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "${sem.subjects.size} Subjects • ${sem.totalMarksObtained}/${sem.totalMaxMarks} Marks (${String.format(Locale.getDefault(), "%.1f", sem.percentage)}%)",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "SGPA: ${String.format(Locale.getDefault(), "%.2f", sem.sgpa)}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }

                                    // Subjects List
                                    AnimatedVisibility(
                                        visible = isExpanded,
                                        enter = expandVertically(),
                                        exit = shrinkVertically()
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            sem.subjects.forEachIndexed { subjectIndex, subject ->
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(MaterialTheme.colorScheme.surface)
                                                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                                        .padding(8.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1.8f)) {
                                                            Text(
                                                                text = "${subject.code}: ${subject.name}",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                            Text(
                                                                text = "Credits: ${subject.credits} • Grade: ${subject.gradeLetter}",
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        }

                                                        OutlinedTextField(
                                                            value = subject.marksObtained.toString(),
                                                            onValueChange = { inputVal ->
                                                                val newMark = inputVal.toIntOrNull() ?: 0
                                                                val newGp = when {
                                                                    newMark >= 85 -> 4.0
                                                                    newMark >= 75 -> 3.7
                                                                    newMark >= 65 -> 3.3
                                                                    newMark >= 55 -> 3.0
                                                                    newMark >= 50 -> 2.0
                                                                    else -> 0.0
                                                                }
                                                                sem.subjects[subjectIndex] = subject.copy(
                                                                    marksObtained = newMark.coerceIn(0, 100),
                                                                    gradePoint = newGp
                                                                )
                                                            },
                                                            label = { Text("Marks") },
                                                            singleLine = true,
                                                            modifier = Modifier.weight(1f)
                                                        )
                                                    }
                                                }
                                            }

                                            TextButton(
                                                onClick = {
                                                    val nextNum = sem.subjects.size + 1
                                                    sem.subjects.add(
                                                        BcaSubjectMark("BCA${sem.semesterNumber}0$nextNum", "Elective Subject $nextNum", 3, 85, 100, 4.0)
                                                    )
                                                },
                                                modifier = Modifier.align(Alignment.End)
                                            ) {
                                                Icon(Icons.Default.Add, contentDescription = "Add Subject", modifier = Modifier.padding(end = 4.dp))
                                                Text("Add Subject to Sem ${sem.semesterNumber}")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // TAB 1: Quick Custom Calculator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = prevCreditsStr,
                            onValueChange = { prevCreditsStr = it },
                            label = { Text("Prior Credits") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = prevCgpaStr,
                            onValueChange = { prevCgpaStr = it },
                            label = { Text("Prior CGPA") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Course Grade Entries",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(onClick = {
                            customCourses.add(CourseGradeEntry("New Course", "3.0", 4.0, "A (4.0)"))
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "Add Course", modifier = Modifier.padding(end = 4.dp))
                            Text("Add Course")
                        }
                    }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        itemsIndexed(customCourses) { index, course ->
                            var expanded by remember { mutableStateOf(false) }

                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = course.name,
                                            onValueChange = { newName ->
                                                customCourses[index] = course.copy(name = newName)
                                            },
                                            label = { Text("Course Title") },
                                            modifier = Modifier.weight(1.5f)
                                        )

                                        OutlinedTextField(
                                            value = course.credits,
                                            onValueChange = { newCred ->
                                                customCourses[index] = course.copy(credits = newCred)
                                            },
                                            label = { Text("Credits") },
                                            modifier = Modifier.weight(1f)
                                        )

                                        ExposedDropdownMenuBox(
                                            expanded = expanded,
                                            onExpandedChange = { expanded = !expanded },
                                            modifier = Modifier.weight(1.2f)
                                        ) {
                                            OutlinedTextField(
                                                value = course.gradeLetter,
                                                onValueChange = {},
                                                readOnly = true,
                                                label = { Text("Grade") },
                                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                                                modifier = Modifier.menuAnchor()
                                            )
                                            ExposedDropdownMenu(
                                                expanded = expanded,
                                                onDismissRequest = { expanded = false }
                                            ) {
                                                GRADE_OPTIONS.forEach { (label, point) ->
                                                    DropdownMenuItem(
                                                        text = { Text(label) },
                                                        onClick = {
                                                            customCourses[index] = course.copy(gradeLetter = label, gradePoint = point)
                                                            expanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        IconButton(onClick = {
                                            if (customCourses.size > 1) {
                                                customCourses.removeAt(index)
                                            }
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Remove",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rounded = (finalCalculatedCgpa * 100).toInt() / 100.0
                    onApplyToProfile(rounded)
                    onDismiss()
                },
                modifier = Modifier.testTag("save_cgpa_btn")
            ) {
                Icon(Icons.Default.Grade, contentDescription = "Save", modifier = Modifier.padding(end = 4.dp))
                Text("Save CGPA (${String.format(Locale.getDefault(), "%.2f", finalCalculatedCgpa)}) to Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
