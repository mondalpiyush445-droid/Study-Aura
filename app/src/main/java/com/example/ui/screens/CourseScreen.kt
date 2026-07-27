package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Locale

// --- Data Models for Dynamic User Courses ---

data class UserExamMark(
    val examName: String,
    var marksObtained: Int,
    var maxMarks: Int = 50
)

data class UserSubject(
    var code: String,
    var name: String,
    var credits: Int,
    val exams: MutableList<UserExamMark> = mutableStateListOf()
) {
    val totalMarksObtained: Int get() = exams.sumOf { it.marksObtained }
    val totalMaxMarks: Int get() = if (exams.isNotEmpty()) exams.sumOf { it.maxMarks } else 100
    val percentage: Double get() = if (totalMaxMarks > 0) (totalMarksObtained.toDouble() / totalMaxMarks) * 100 else 0.0
    val gradePoint: Double
        get() = when {
            percentage >= 85 -> 4.0
            percentage >= 75 -> 3.7
            percentage >= 65 -> 3.3
            percentage >= 55 -> 3.0
            percentage >= 50 -> 2.0
            else -> 0.0
        }
}

data class UserSemester(
    val semesterNumber: Int,
    var semesterTitle: String,
    val subjects: MutableList<UserSubject> = mutableStateListOf()
) {
    val totalCredits: Int get() = subjects.sumOf { it.credits }
    val totalExamsCount: Int get() = subjects.sumOf { it.exams.size }
    val totalMarksObtained: Int get() = subjects.sumOf { it.totalMarksObtained }
    val totalMaxMarks: Int get() = subjects.sumOf { it.totalMaxMarks }
    val totalWeightedPoints: Double get() = subjects.sumOf { it.credits * it.gradePoint }
    val sgpa: Double get() = if (totalCredits > 0) totalWeightedPoints / totalCredits else 0.0
    val percentage: Double get() = if (totalMaxMarks > 0) (totalMarksObtained.toDouble() / totalMaxMarks) * 100 else 0.0
}

data class UserCourse(
    var degreeName: String, // e.g. "Bachelor of Computer Applications"
    var degreeCode: String, // e.g. "BCA", "B.Tech", "BBA", "B.Sc CS"
    var durationYears: Int, // e.g. 3, 4, 2
    val semesters: MutableList<UserSemester> = mutableStateListOf()
) {
    val totalSubjects: Int get() = semesters.sumOf { it.subjects.size }
    val totalExams: Int get() = semesters.sumOf { it.totalExamsCount }
    val totalCredits: Int get() = semesters.sumOf { it.totalCredits }
    val totalMarksObtained: Int get() = semesters.sumOf { it.totalMarksObtained }
    val totalMaxMarks: Int get() = semesters.sumOf { it.totalMaxMarks }
    val overallPercentage: Double get() = if (totalMaxMarks > 0) (totalMarksObtained.toDouble() / totalMaxMarks) * 100 else 0.0
    val overallCgpa: Double get() {
        val totalWeighted = semesters.sumOf { it.totalWeightedPoints }
        return if (totalCredits > 0) totalWeighted / totalCredits else 0.0
    }
}

// --- AI Quiz Question Models ---

data class QuizQuestion(
    val id: Int,
    val subject: String,
    val questionText: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val explanation: String
)

// --- Sample Preset Generators ---

fun createSampleBcaCourse(): UserCourse {
    val course = UserCourse(
        degreeName = "Bachelor of Computer Applications",
        degreeCode = "BCA",
        durationYears = 3
    )

    val sem1 = UserSemester(1, "Year 1 - Semester 1")
    sem1.subjects.addAll(
        listOf(
            UserSubject("BCA101", "Problem Solving using C", 4, mutableStateListOf(
                UserExamMark("Midterm Exam", 25, 30),
                UserExamMark("Practical / Lab", 18, 20),
                UserExamMark("Final Theory Exam", 45, 50)
            )),
            UserSubject("BCA102", "Computer Fundamentals & Office Tools", 3, mutableStateListOf(
                UserExamMark("Internal Test", 32, 40),
                UserExamMark("End Term Exam", 50, 60)
            )),
            UserSubject("BCA103", "Mathematical Foundations for CS", 4, mutableStateListOf(
                UserExamMark("Unit Test", 18, 20),
                UserExamMark("Midterm Exam", 25, 30)
            )),
            UserSubject("BCA104", "Technical Communication Skills", 3, mutableStateListOf(
                UserExamMark("Viva & Presentation", 35, 40)
            )),
            UserSubject("BCA105", "Digital Logic & Architecture", 4, mutableStateListOf(
                UserExamMark("Lab Test", 25, 30),
                UserExamMark("Final Theory", 40, 50)
            ))
        )
    )

    val sem2 = UserSemester(2, "Year 1 - Semester 2")
    sem2.subjects.addAll(
        listOf(
            UserSubject("BCA201", "Data Structures in C", 4, mutableStateListOf(
                UserExamMark("Midterm Exam", 26, 30),
                UserExamMark("Lab Practical", 19, 20),
                UserExamMark("Final Theory Exam", 45, 50)
            )),
            UserSubject("BCA202", "Discrete Mathematics", 4, mutableStateListOf(
                UserExamMark("Test 1", 22, 25),
                UserExamMark("Final Exam", 40, 50)
            )),
            UserSubject("BCA203", "Environmental Studies & Ethics", 2, mutableStateListOf(
                UserExamMark("Internal Test", 38, 40)
            )),
            UserSubject("BCA204", "Financial Accounting & Management", 3, mutableStateListOf(
                UserExamMark("Midterm", 26, 30)
            )),
            UserSubject("BCA205", "OOP Concepts using C++", 4, mutableStateListOf(
                UserExamMark("Lab Exam", 28, 30),
                UserExamMark("Final Theory", 40, 50)
            ))
        )
    )

    val sem3 = UserSemester(3, "Year 2 - Semester 3")
    sem3.subjects.addAll(
        listOf(
            UserSubject("BCA301", "Database Management Systems", 4, mutableStateListOf(
                UserExamMark("SQL Lab Test", 28, 30)
            )),
            UserSubject("BCA302", "Operating Systems Architecture", 4, mutableStateListOf(
                UserExamMark("Quiz", 35, 40)
            )),
            UserSubject("BCA303", "Software Engineering Methods", 3, mutableStateListOf(
                UserExamMark("Project Presentation", 38, 40)
            ))
        )
    )

    course.semesters.addAll(listOf(sem1, sem2, sem3))
    return course
}

fun createSampleBtechCourse(): UserCourse {
    val course = UserCourse(
        degreeName = "Bachelor of Technology in Computer Science",
        degreeCode = "B.Tech CS",
        durationYears = 4
    )
    val sem1 = UserSemester(1, "Year 1 - Semester 1")
    sem1.subjects.add(UserSubject("CS101", "Programming for Problem Solving in C", 4, mutableStateListOf(UserExamMark("Midterm", 22, 30))))
    sem1.subjects.add(UserSubject("MA101", "Engineering Mathematics I", 4, mutableStateListOf(UserExamMark("Midterm", 25, 30))))
    sem1.subjects.add(UserSubject("PH101", "Engineering Physics & Optics", 3, mutableStateListOf(UserExamMark("Internal", 18, 20))))

    val sem2 = UserSemester(2, "Year 1 - Semester 2")
    sem2.subjects.add(UserSubject("CS201", "Data Structures & Algorithms", 4, mutableStateListOf(UserExamMark("Lab Exam", 28, 30))))
    sem2.subjects.add(UserSubject("MA201", "Engineering Mathematics II", 4, mutableStateListOf(UserExamMark("Quiz", 19, 20))))

    course.semesters.addAll(listOf(sem1, sem2))
    return course
}

fun createSampleBbaCourse(): UserCourse {
    val course = UserCourse(
        degreeName = "Bachelor of Business Administration",
        degreeCode = "BBA",
        durationYears = 3
    )
    val sem1 = UserSemester(1, "Year 1 - Semester 1")
    sem1.subjects.add(UserSubject("BBA101", "Principles of Management", 3, mutableStateListOf(UserExamMark("Internal", 35, 40))))
    sem1.subjects.add(UserSubject("BBA102", "Business Microeconomics", 3, mutableStateListOf(UserExamMark("Midterm", 24, 30))))
    sem1.subjects.add(UserSubject("BBA103", "Financial Accounting", 4, mutableStateListOf(UserExamMark("Final", 42, 50))))

    val sem2 = UserSemester(2, "Year 1 - Semester 2")
    sem2.subjects.add(UserSubject("BBA201", "Organizational Behavior", 3, mutableStateListOf(UserExamMark("Midterm", 27, 30))))
    sem2.subjects.add(UserSubject("BBA202", "Marketing Management", 3, mutableStateListOf(UserExamMark("Presentation", 38, 40))))

    course.semesters.addAll(listOf(sem1, sem2))
    return course
}

// Built-in Practical AI Questions Repository
fun getQuestionsForSubject(subjectName: String): List<QuizQuestion> {
    val normalized = subjectName.lowercase(Locale.getDefault())

    return when {
        normalized.contains("c programming") || normalized.contains("problem solving in c") || normalized.endsWith(" c") -> listOf(
            QuizQuestion(
                id = 101,
                subject = subjectName,
                questionText = "What will be the output of this C code snippet?\n\nint x = 5;\nprintf(\"%d %d %d\", x++, x, ++x);",
                options = listOf(
                    "5 6 7",
                    "Undefined Behavior (sequence point constraint)",
                    "5 5 7",
                    "7 7 7"
                ),
                correctOptionIndex = 1,
                explanation = "In C, modifying a variable multiple times or reading and modifying it without an intervening sequence point results in Undefined Behavior."
            ),
            QuizQuestion(
                id = 102,
                subject = subjectName,
                questionText = "Consider the array declaration: int arr[3][4]; What is the exact pointer type of 'arr' when used in an expression?",
                options = listOf(
                    "int**",
                    "int*",
                    "int (*)[4] (Pointer to array of 4 integers)",
                    "int* [3]"
                ),
                correctOptionIndex = 2,
                explanation = "When a 2D array 'arr[3][4]' decays into a pointer, its type is pointer to an array of 4 integers, written as int (*)[4]."
            ),
            QuizQuestion(
                id = 103,
                subject = subjectName,
                questionText = "Which pointer arithmetic operation is strictly invalid in standard C?",
                options = listOf(
                    "Adding an integer to a pointer (ptr + n)",
                    "Subtracting one pointer from another pointer of the same type (ptr1 - ptr2)",
                    "Adding two pointers together (ptr1 + ptr2)",
                    "Comparing two pointers with relational operators (ptr1 > ptr2)"
                ),
                correctOptionIndex = 2,
                explanation = "Adding two memory addresses (ptr1 + ptr2) is meaningless in computer architecture and produces a C compilation error."
            ),
            QuizQuestion(
                id = 104,
                subject = subjectName,
                questionText = "What does malloc(0) return according to the standard C library specification?",
                options = listOf(
                    "Always causes a Segmentation Fault runtime crash",
                    "Either NULL or a unique non-null pointer that cannot be dereferenced",
                    "A pointer to 1 byte of allocated memory",
                    "Triggers a compile-time assertion error"
                ),
                correctOptionIndex = 1,
                explanation = "If the size requested is zero, malloc returns either NULL or a non-null pointer that must still be freed."
            ),
            QuizQuestion(
                id = 105,
                subject = subjectName,
                questionText = "In C dynamic memory management, what defines a 'Dangling Pointer'?",
                options = listOf(
                    "A pointer that points to memory that has been deallocated using free()",
                    "A pointer initialized explicitly to NULL",
                    "A pointer pointing to constant string literal memory",
                    "A void pointer declared without a concrete data type"
                ),
                correctOptionIndex = 0,
                explanation = "A dangling pointer arises when memory allocated dynamically is freed via free(), but the pointer variable still holds the freed memory address."
            )
        )

        normalized.contains("data structure") || normalized.contains("dsa") || normalized.contains("algorithm") -> listOf(
            QuizQuestion(
                id = 201,
                subject = subjectName,
                questionText = "When implementing a Circular Queue of size N using an array, what is the exact index expression for 'Queue Full'?",
                options = listOf(
                    "(rear + 1) % N == front",
                    "rear == front",
                    "rear == N - 1",
                    "front == (rear + 1)"
                ),
                correctOptionIndex = 0,
                explanation = "In a circular array queue, reserving one empty slot to differentiate 'Queue Full' from 'Queue Empty' gives: (rear + 1) % N == front."
            ),
            QuizQuestion(
                id = 202,
                subject = subjectName,
                questionText = "Which scenario causes QuickSort to degrade to its worst-case time complexity of O(N²)?",
                options = listOf(
                    "When the pivot is picked randomly on every partition",
                    "When the input array is already sorted and the last element is always chosen as pivot",
                    "When all elements in the array are completely distinct",
                    "When using median-of-three pivot selection"
                ),
                correctOptionIndex = 1,
                explanation = "Picking the extremum (first or last element) as pivot on already sorted data leads to unbalanced subproblems of size 1 and N-1, yielding O(N²)."
            ),
            QuizQuestion(
                id = 203,
                subject = subjectName,
                questionText = "What is the fast algorithm used to detect a cycle in a Singly Linked List in O(N) time and O(1) space?",
                options = listOf(
                    "Floyd's Cycle-Finding Algorithm (Tortoise and Hare)",
                    "Dijkstra's Shortest Path Algorithm",
                    "Kruskal's Minimum Spanning Tree",
                    "Breadth First Search with Hash Set"
                ),
                correctOptionIndex = 0,
                explanation = "Floyd's algorithm uses two pointers moving at speeds 1 and 2. If a cycle exists, the fast pointer eventually meets the slow pointer inside the loop."
            )
        )

        normalized.contains("database") || normalized.contains("dbms") || normalized.contains("sql") -> listOf(
            QuizQuestion(
                id = 301,
                subject = subjectName,
                questionText = "Which Normal Form (NF) eliminates transitive functional dependencies (X -> Y and Y -> Z where X is primary key)?",
                options = listOf(
                    "First Normal Form (1NF)",
                    "Second Normal Form (2NF)",
                    "Third Normal Form (3NF)",
                    "Boyce-Codd Normal Form (BCNF)"
                ),
                correctOptionIndex = 2,
                explanation = "3NF mandates that a relation is in 2NF and no non-prime attribute is transitively dependent on the primary key."
            ),
            QuizQuestion(
                id = 302,
                subject = subjectName,
                questionText = "In ACID transaction properties, which property ensures that if a database crash occurs during transaction execution, incomplete changes are rolled back?",
                options = listOf(
                    "Atomicity",
                    "Consistency",
                    "Isolation",
                    "Durability"
                ),
                correctOptionIndex = 0,
                explanation = "Atomicity ensures 'All or Nothing' execution. If any operation fails or crashes, the entire transaction is rolled back."
            )
        )

        normalized.contains("java") || normalized.contains("oop") || normalized.contains("c++") -> listOf(
            QuizQuestion(
                id = 401,
                subject = subjectName,
                questionText = "In Java, what is the key difference between 'str1 == str2' and 'str1.equals(str2)'?",
                options = listOf(
                    "'==' compares object memory references; '.equals()' compares string content character by character",
                    "They are identical and perform the exact same operation",
                    "'.equals()' compares memory address while '==' compares length",
                    "'==' is only for primitive integers, '.equals()' causes a syntax error"
                ),
                correctOptionIndex = 0,
                explanation = "'==' checks reference equality (do both variables point to the exact same heap memory address), whereas '.equals()' evaluates structural content equality."
            ),
            QuizQuestion(
                id = 402,
                subject = subjectName,
                questionText = "What happens if an exception is thrown inside a try block that has a corresponding finally block containing a 'return 10;' statement?",
                options = listOf(
                    "The exception is propagated and the application crashes immediately",
                    "The finally block executes and returns 10, suppressing the thrown exception",
                    "The catch block executes but finally is skipped",
                    "Triggers a Java NullPointerException"
                ),
                correctOptionIndex = 1,
                explanation = "A return statement inside a finally block always executes and overrides any exception or return value produced in try/catch blocks."
            )
        )

        normalized.contains("python") -> listOf(
            QuizQuestion(
                id = 501,
                subject = subjectName,
                questionText = "What is the output of the Python expression: bool([0])?",
                options = listOf(
                    "False",
                    "True",
                    "TypeError",
                    "0"
                ),
                correctOptionIndex = 1,
                explanation = "In Python, non-empty containers (like a list containing [0]) evaluate to True in a boolean context, regardless of the elements inside."
            ),
            QuizQuestion(
                id = 502,
                subject = subjectName,
                questionText = "Why can a Python dictionary key NOT be a standard mutable List [1, 2, 3]?",
                options = listOf(
                    "Lists in Python are mutable and thus unhashable (do not have a fixed __hash__ value)",
                    "Dictionary keys must be strings only",
                    "Lists take up too much RAM memory",
                    "Python automatically converts lists to integers"
                ),
                correctOptionIndex = 0,
                explanation = "Dictionary keys require hashable objects whose hash values never change during lifetime. Mutable objects like lists are unhashable."
            )
        )

        normalized.contains("operating system") || normalized.contains("os") -> listOf(
            QuizQuestion(
                id = 601,
                subject = subjectName,
                questionText = "Which condition is NOT one of the 4 necessary conditions required for a Deadlock to occur?",
                options = listOf(
                    "Mutual Exclusion",
                    "Hold and Wait",
                    "Preemption Allowed (Resources can be forcibly taken)",
                    "Circular Wait"
                ),
                correctOptionIndex = 2,
                explanation = "No Preemption is required for deadlock. If preemption is allowed (resources CAN be taken away), deadlocks cannot occur."
            ),
            QuizQuestion(
                id = 602,
                subject = subjectName,
                questionText = "What is 'Thrashing' in Virtual Memory Operating Systems?",
                options = listOf(
                    "When the CPU spends more time swapping pages in/out of secondary disk storage than executing actual instructions",
                    "When a process uses 100% CPU thread time",
                    "When an application crashes due to a null pointer",
                    "When corrupt RAM blocks are formatted"
                ),
                correctOptionIndex = 0,
                explanation = "Thrashing occurs when main RAM is insufficient, forcing constant high-overhead page faults and disk swapping."
            )
        )

        else -> listOf(
            QuizQuestion(
                id = 701,
                subject = subjectName,
                questionText = "Practical Core Question 1 for $subjectName: Which foundational concept is most critical when designing scalable solutions in $subjectName?",
                options = listOf(
                    "Modular decoupling and clear interface boundaries",
                    "Hardcoding static global variables without validation",
                    "Ignoring edge-case error conditions during runtime",
                    "Bypassing memory and thread safety constraints"
                ),
                correctOptionIndex = 0,
                explanation = "Modular decoupling allows components in $subjectName to be tested, scaled, and maintained independently without side effects."
            ),
            QuizQuestion(
                id = 702,
                subject = subjectName,
                questionText = "Practical Question 2 for $subjectName: When optimizing performance and accuracy in $subjectName, what is the best practice?",
                options = listOf(
                    "Measure real-world metrics first using profiling before applying targeted optimizations",
                    "Prematurely optimize code before verifying correctness",
                    "Disable logging and error handling completely",
                    "Rely solely on luck without systematic unit tests"
                ),
                correctOptionIndex = 0,
                explanation = "Donald Knuth's principle: 'Premature optimization is the root of all evil.' Always profile $subjectName under realistic workloads first!"
            ),
            QuizQuestion(
                id = 703,
                subject = subjectName,
                questionText = "Practical Question 3 for $subjectName: How should unexpected exceptions or invalid inputs be handled in $subjectName?",
                options = listOf(
                    "Fail fast with descriptive error states and clean cleanup routines",
                    "Silently suppress all errors without user notification",
                    "Infinite retry loops without backoff strategy",
                    "Crashing the host system process immediately"
                ),
                correctOptionIndex = 0,
                explanation = "Robust software architecture in $subjectName requires explicit exception handling, resource cleanup, and actionable error messages."
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseScreen(
    modifier: Modifier = Modifier
) {
    // Current Active Course State
    var activeCourse by remember { mutableStateOf(createSampleBcaCourse()) }

    // Screen Tabs: 0 = "Curriculum & Exam Marks", 1 = "AI Practical Subject Quizzes"
    var topTabSelected by remember { mutableIntStateOf(0) }

    // Dialog States for Course Customization
    var showCreateCourseDialog by remember { mutableStateOf(false) }
    var showAddSemesterDialog by remember { mutableStateOf(false) }
    var showAddSubjectDialogForSem by remember { mutableStateOf<UserSemester?>(null) }
    var activeExamEditingSubject by remember { mutableStateOf<UserSubject?>(null) }

    // Quiz Tab State
    var subjectSearchQuery by remember { mutableStateOf("") }
    var selectedQuizSubject by remember { mutableStateOf("C Programming") }
    var activeQuestionIndex by remember { mutableIntStateOf(0) }
    var userAnswers by remember { mutableStateOf(mutableMapOf<Int, Int>()) } // questionId -> selectedOptionIndex

    // All available subjects list for quick picking & filtering
    val presetQuizSubjects = listOf(
        "C Programming",
        "Data Structures in C",
        "Database Management Systems",
        "Java Development",
        "Python Programming",
        "Operating Systems",
        "Digital Logic & Architecture",
        "Discrete Mathematics",
        "Financial Accounting",
        "Software Engineering",
        "Web Technologies",
        "Computer Networks"
    )

    // User's own added subjects from their current degree
    val userDegreeSubjects = remember(activeCourse) {
        activeCourse.semesters.flatMap { sem -> sem.subjects.map { it.name } }.distinct()
    }

    // Combined unique list of subjects
    val allCombinedSubjects = (userDegreeSubjects + presetQuizSubjects).distinct()

    // Filtered subjects based on search query
    val filteredSubjects = if (subjectSearchQuery.isBlank()) {
        allCombinedSubjects
    } else {
        allCombinedSubjects.filter { it.contains(subjectSearchQuery, ignoreCase = true) }
    }

    // Questions for the currently selected quiz subject
    val currentQuizQuestions = remember(selectedQuizSubject) {
        getQuestionsForSubject(selectedQuizSubject)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Screen Main Navigation Tabs
            TabRow(selectedTabIndex = topTabSelected) {
                Tab(
                    selected = topTabSelected == 0,
                    onClick = { topTabSelected = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.School, contentDescription = "Curriculum", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("My Curriculum & Marks", fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("curriculum_tab")
                )
                Tab(
                    selected = topTabSelected == 1,
                    onClick = { topTabSelected = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = "AI Quizzes", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("AI Subject Quizzes", fontWeight = FontWeight.Bold)
                        }
                    },
                    modifier = Modifier.testTag("ai_quizzes_tab")
                )
            }

            if (topTabSelected == 0) {
                // TAB 0: Dynamic User Course Curriculum & Exam Marks
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Course Header Card with Change/Add Course Options
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.primary)
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = activeCourse.degreeCode,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onPrimary
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "${activeCourse.durationYears} Years (${activeCourse.semesters.size} Sems)",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = activeCourse.degreeName,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }

                                    Button(
                                        onClick = { showCreateCourseDialog = true },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = "Switch", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Switch / Create Course")
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                Divider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))

                                Spacer(modifier = Modifier.height(16.dp))

                                // Overall Stats Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    CourseStatMetric(
                                        icon = Icons.Default.Grade,
                                        title = "Overall CGPA",
                                        value = String.format(Locale.getDefault(), "%.2f", activeCourse.overallCgpa)
                                    )
                                    CourseStatMetric(
                                        icon = Icons.Default.Book,
                                        title = "Total Subjects",
                                        value = "${activeCourse.totalSubjects}"
                                    )
                                    CourseStatMetric(
                                        icon = Icons.Default.EventNote,
                                        title = "Total Exams",
                                        value = "${activeCourse.totalExams}"
                                    )
                                    CourseStatMetric(
                                        icon = Icons.Default.Timer,
                                        title = "Total Marks",
                                        value = "${activeCourse.totalMarksObtained} / ${activeCourse.totalMaxMarks}"
                                    )
                                }
                            }
                        }
                    }

                    // Action Bar: Add Semester & Course Presets
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Custom Course Builder",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Add your degree, semesters, subjects, and exam marks!",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Button(onClick = { showAddSemesterDialog = true }) {
                                        Icon(Icons.Default.Add, contentDescription = "Add Sem", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add Semester")
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "QUICK PRESET DEGREES:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    item {
                                        OutlinedButton(onClick = { activeCourse = createSampleBcaCourse() }) {
                                            Text("BCA (3 Years)")
                                        }
                                    }
                                    item {
                                        OutlinedButton(onClick = { activeCourse = createSampleBtechCourse() }) {
                                            Text("B.Tech CS (4 Years)")
                                        }
                                    }
                                    item {
                                        OutlinedButton(onClick = { activeCourse = createSampleBbaCourse() }) {
                                            Text("BBA (3 Years)")
                                        }
                                    }
                                    item {
                                        OutlinedButton(onClick = {
                                            activeCourse = UserCourse("My Custom Degree", "CUSTOM", 3)
                                            activeCourse.semesters.add(UserSemester(1, "Semester 1"))
                                        }) {
                                            Text("Blank Custom Degree")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Semesters List
                    if (activeCourse.semesters.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.School, contentDescription = "No Sems", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("No Semesters Added Yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("Tap 'Add Semester' above to begin building your curriculum!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    itemsIndexed(activeCourse.semesters) { semIndex, sem ->
                        var isExpanded by remember { mutableStateOf(true) }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Semester Row Header
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { isExpanded = !isExpanded }
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "S${sem.semesterNumber}",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(12.dp))

                                        Column {
                                            Text(
                                                text = sem.semesterTitle,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${sem.subjects.size} Subjects • ${sem.totalCredits} Credits • ${sem.totalMarksObtained}/${sem.totalMaxMarks} Marks",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
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

                                        IconButton(onClick = { activeCourse.semesters.removeAt(semIndex) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Sem", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                                        }

                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.ExpandMore else Icons.Default.ChevronRight,
                                            contentDescription = "Expand",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                // Expanded Subjects list
                                AnimatedVisibility(
                                    visible = isExpanded,
                                    enter = expandVertically(),
                                    exit = shrinkVertically()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                                        if (sem.subjects.isEmpty()) {
                                            Text(
                                                text = "No subjects in this semester yet. Tap 'Add Subject' below to add one!",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }

                                        sem.subjects.forEachIndexed { subjectIndex, subject ->
                                            Card(
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Text(
                                                                    text = subject.code,
                                                                    style = MaterialTheme.typography.labelMedium,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = MaterialTheme.colorScheme.primary
                                                                )
                                                                Spacer(modifier = Modifier.width(6.dp))
                                                                Text(
                                                                    text = "(${subject.credits} Credits)",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                )
                                                            }

                                                            Text(
                                                                text = subject.name,
                                                                style = MaterialTheme.typography.titleSmall,
                                                                fontWeight = FontWeight.Bold
                                                            )
                                                        }

                                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(MaterialTheme.colorScheme.surface)
                                                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
                                                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                                            ) {
                                                                Column(horizontalAlignment = Alignment.End) {
                                                                    Text(
                                                                        text = "${subject.totalMarksObtained} / ${subject.totalMaxMarks}",
                                                                        style = MaterialTheme.typography.labelLarge,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = MaterialTheme.colorScheme.primary
                                                                    )
                                                                    Text(
                                                                        text = "${String.format(Locale.getDefault(), "%.1f", subject.percentage)}% • GP: ${subject.gradePoint}",
                                                                        style = MaterialTheme.typography.labelSmall,
                                                                        color = MaterialTheme.colorScheme.onSurface
                                                                    )
                                                                }
                                                            }

                                                            IconButton(onClick = { sem.subjects.removeAt(subjectIndex) }) {
                                                                Icon(Icons.Default.Delete, contentDescription = "Delete Subject", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                                                            }
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(8.dp))

                                                    Text(
                                                        text = "Exams & Marks (${subject.exams.size} Exams):",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )

                                                    Spacer(modifier = Modifier.height(4.dp))

                                                    // Exams List under subject
                                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                        subject.exams.forEachIndexed { examIndex, exam ->
                                                            Row(
                                                                modifier = Modifier
                                                                    .fillMaxWidth()
                                                                    .clip(RoundedCornerShape(8.dp))
                                                                    .background(MaterialTheme.colorScheme.surface)
                                                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1.5f)) {
                                                                    Icon(
                                                                        imageVector = Icons.Default.AssignmentTurnedIn,
                                                                        contentDescription = "Exam",
                                                                        tint = MaterialTheme.colorScheme.primary,
                                                                        modifier = Modifier.size(16.dp)
                                                                    )
                                                                    Spacer(modifier = Modifier.width(6.dp))
                                                                    Text(
                                                                        text = exam.examName,
                                                                        style = MaterialTheme.typography.bodySmall,
                                                                        fontWeight = FontWeight.Medium
                                                                    )
                                                                }

                                                                Row(
                                                                    verticalAlignment = Alignment.CenterVertically,
                                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                                ) {
                                                                    OutlinedTextField(
                                                                        value = exam.marksObtained.toString(),
                                                                        onValueChange = { input ->
                                                                            val num = input.toIntOrNull() ?: 0
                                                                            subject.exams[examIndex] = exam.copy(
                                                                                marksObtained = num.coerceIn(0, exam.maxMarks)
                                                                            )
                                                                        },
                                                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                                        singleLine = true,
                                                                        modifier = Modifier.width(68.dp),
                                                                        textStyle = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                                                    )

                                                                    Text(
                                                                        text = "/ ${exam.maxMarks}",
                                                                        style = MaterialTheme.typography.bodySmall,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                                    )

                                                                    IconButton(
                                                                        onClick = {
                                                                            subject.exams.removeAt(examIndex)
                                                                        },
                                                                        modifier = Modifier.size(28.dp)
                                                                    ) {
                                                                        Icon(
                                                                            imageVector = Icons.Default.Delete,
                                                                            contentDescription = "Delete Exam",
                                                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                                                            modifier = Modifier.size(16.dp)
                                                                        )
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }

                                                    Spacer(modifier = Modifier.height(6.dp))

                                                    TextButton(
                                                        onClick = { activeExamEditingSubject = subject },
                                                        modifier = Modifier.align(Alignment.End)
                                                    ) {
                                                        Icon(Icons.Default.Add, contentDescription = "Add Exam", modifier = Modifier.size(16.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text("Add Exam / Test Mark", style = MaterialTheme.typography.labelSmall)
                                                    }
                                                }
                                            }
                                        }

                                        Button(
                                            onClick = { showAddSubjectDialogForSem = sem },
                                            modifier = Modifier.align(Alignment.End)
                                        ) {
                                            Icon(Icons.Default.Add, contentDescription = "Add Subject", modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Add Subject to ${sem.semesterTitle}")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // TAB 1: AI Subject Practical Quizzes
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // AI Quiz Top Welcome Header
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = "AI Quiz",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "AI Practical Subject Quizzes",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "Select or search any subject to take hands-on practical quizzes! When you click an answer, you will immediately see whether it's Correct or Incorrect with a detailed clear explanation.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }

                    // SEARCH & SUBJECT SELECTION CARD
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "SEARCH & SELECT QUIZ SUBJECT",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Subject Search Bar
                                OutlinedTextField(
                                    value = subjectSearchQuery,
                                    onValueChange = { subjectSearchQuery = it },
                                    placeholder = { Text("Type subject name (e.g., C Programming, Python, DBMS, Marketing...)") },
                                    leadingIcon = {
                                        Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary)
                                    },
                                    trailingIcon = {
                                        if (subjectSearchQuery.isNotEmpty()) {
                                            IconButton(onClick = { subjectSearchQuery = "" }) {
                                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // If user typed a custom query not in standard list, offer "Take Quiz for Custom Subject"
                                if (subjectSearchQuery.isNotBlank() && filteredSubjects.none { it.equals(subjectSearchQuery, ignoreCase = true) }) {
                                    Button(
                                        onClick = {
                                            selectedQuizSubject = subjectSearchQuery.trim()
                                            activeQuestionIndex = 0
                                            userAnswers = mutableMapOf()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Start Quiz on Custom Subject: '${subjectSearchQuery.trim()}'")
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))
                                }

                                // User Course Subjects Quick Selection
                                if (userDegreeSubjects.isNotEmpty()) {
                                    Text(
                                        text = "YOUR DEGREE SUBJECTS:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.secondary
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(userDegreeSubjects) { userSubj ->
                                            FilterChip(
                                                selected = selectedQuizSubject == userSubj,
                                                onClick = {
                                                    selectedQuizSubject = userSubj
                                                    activeQuestionIndex = 0
                                                    userAnswers = mutableMapOf()
                                                },
                                                label = { Text(userSubj, fontWeight = FontWeight.Bold) },
                                                leadingIcon = {
                                                    Icon(Icons.Default.Book, contentDescription = null, modifier = Modifier.size(14.dp))
                                                },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                                )
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                // Popular Preset Subjects Chips
                                Text(
                                    text = "POPULAR SUBJECTS:",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(filteredSubjects) { subj ->
                                        FilterChip(
                                            selected = selectedQuizSubject == subj,
                                            onClick = {
                                                selectedQuizSubject = subj
                                                activeQuestionIndex = 0
                                                userAnswers = mutableMapOf()
                                            },
                                            label = { Text(subj) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // QUIZ QUESTIONS CARD & INTERACTIVE FEEDBACK
                    item {
                        if (currentQuizQuestions.isNotEmpty()) {
                            val currentQuestion = currentQuizQuestions[activeQuestionIndex.coerceIn(0, currentQuizQuestions.size - 1)]
                            val selectedOption = userAnswers[currentQuestion.id]
                            val isAnswered = selectedOption != null

                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(20.dp))
                            ) {
                                Column(modifier = Modifier.padding(20.dp)) {
                                    // Question Header Row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Question ${activeQuestionIndex + 1} of ${currentQuizQuestions.size}",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = selectedQuizSubject,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    LinearProgressIndicator(
                                        progress = (activeQuestionIndex + 1).toFloat() / currentQuizQuestions.size,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(4.dp))
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Question Text
                                    Text(
                                        text = currentQuestion.questionText,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Interactive Options List
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        currentQuestion.options.forEachIndexed { optIndex, optionText ->
                                            val isThisSelected = selectedOption == optIndex
                                            val isCorrectAnswer = optIndex == currentQuestion.correctOptionIndex

                                            // Determine option styling based on answered status
                                            val backgroundColor = when {
                                                isAnswered && isCorrectAnswer -> Color(0xFFE8F5E9) // Light Green
                                                isAnswered && isThisSelected && !isCorrectAnswer -> Color(0xFFFFEBEE) // Light Red
                                                isThisSelected -> MaterialTheme.colorScheme.primaryContainer
                                                else -> MaterialTheme.colorScheme.surface
                                            }

                                            val borderColor = when {
                                                isAnswered && isCorrectAnswer -> Color(0xFF2E7D32) // Dark Green
                                                isAnswered && isThisSelected && !isCorrectAnswer -> Color(0xFFC62828) // Dark Red
                                                isThisSelected -> MaterialTheme.colorScheme.primary
                                                else -> MaterialTheme.colorScheme.outlineVariant
                                            }

                                            Card(
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(containerColor = backgroundColor),
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .border(
                                                        width = if (isThisSelected || (isAnswered && isCorrectAnswer)) 2.dp else 1.dp,
                                                        color = borderColor,
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable {
                                                        val updated = userAnswers.toMutableMap()
                                                        updated[currentQuestion.id] = optIndex
                                                        userAnswers = updated
                                                    }
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(14.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        RadioButton(
                                                            selected = isThisSelected,
                                                            onClick = {
                                                                val updated = userAnswers.toMutableMap()
                                                                updated[currentQuestion.id] = optIndex
                                                                userAnswers = updated
                                                            }
                                                        )

                                                        Spacer(modifier = Modifier.width(8.dp))

                                                        Text(
                                                            text = optionText,
                                                            style = MaterialTheme.typography.bodyMedium,
                                                            fontWeight = if (isThisSelected || (isAnswered && isCorrectAnswer)) FontWeight.Bold else FontWeight.Normal,
                                                            color = when {
                                                                isAnswered && isCorrectAnswer -> Color(0xFF1B5E20)
                                                                isAnswered && isThisSelected && !isCorrectAnswer -> Color(0xFFB71C1C)
                                                                else -> MaterialTheme.colorScheme.onSurface
                                                            }
                                                        )
                                                    }

                                                    // Status Badges on the right of option
                                                    if (isAnswered) {
                                                        if (isCorrectAnswer) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(
                                                                    imageVector = Icons.Default.CheckCircle,
                                                                    contentDescription = "Correct",
                                                                    tint = Color(0xFF2E7D32),
                                                                    modifier = Modifier.size(20.dp)
                                                                )
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text(
                                                                    text = "Correct",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color(0xFF2E7D32)
                                                                )
                                                            }
                                                        } else if (isThisSelected) {
                                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                                Icon(
                                                                    imageVector = Icons.Default.Close,
                                                                    contentDescription = "Incorrect",
                                                                    tint = Color(0xFFC62828),
                                                                    modifier = Modifier.size(20.dp)
                                                                )
                                                                Spacer(modifier = Modifier.width(4.dp))
                                                                Text(
                                                                    text = "Incorrect",
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    fontWeight = FontWeight.Bold,
                                                                    color = Color(0xFFC62828)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // IMMEDIATE EXPLANATION BOX WHEN ANSWERED
                                    if (isAnswered) {
                                        val isUserCorrect = selectedOption == currentQuestion.correctOptionIndex

                                        Card(
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (isUserCorrect) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(
                                                    1.dp,
                                                    if (isUserCorrect) Color(0xFF81C784) else Color(0xFFFFB74D),
                                                    RoundedCornerShape(12.dp)
                                                )
                                        ) {
                                            Column(modifier = Modifier.padding(14.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = if (isUserCorrect) Icons.Default.CheckCircle else Icons.Default.Info,
                                                        contentDescription = null,
                                                        tint = if (isUserCorrect) Color(0xFF2E7D32) else Color(0xFFE65100),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = if (isUserCorrect) "✓ Correct Answer!" else "✗ Incorrect - Explanation & Concept:",
                                                        style = MaterialTheme.typography.labelLarge,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isUserCorrect) Color(0xFF1B5E20) else Color(0xFFE65100)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(6.dp))

                                                Text(
                                                    text = currentQuestion.explanation,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(16.dp))
                                    }

                                    // Quiz Navigation Buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Button(
                                            enabled = activeQuestionIndex > 0,
                                            onClick = { activeQuestionIndex-- }
                                        ) {
                                            Text("Previous")
                                        }

                                        TextButton(
                                            onClick = {
                                                userAnswers = userAnswers.toMutableMap().apply { remove(currentQuestion.id) }
                                            }
                                        ) {
                                            Text("Reset Question")
                                        }

                                        if (activeQuestionIndex < currentQuizQuestions.size - 1) {
                                            Button(
                                                onClick = { activeQuestionIndex++ }
                                            ) {
                                                Text("Next Question")
                                            }
                                        } else {
                                            Button(
                                                onClick = {
                                                    // Reset quiz to practice again
                                                    activeQuestionIndex = 0
                                                    userAnswers = mutableMapOf()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Restart Quiz")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Overall Quiz Score Summary
                    item {
                        val totalAnswered = currentQuizQuestions.count { q -> userAnswers.containsKey(q.id) }
                        val correctCount = currentQuizQuestions.count { q -> userAnswers[q.id] == q.correctOptionIndex }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                                    Text(
                                        text = "Quiz Progress: $totalAnswered / ${currentQuizQuestions.size} Answered",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Score: $correctCount Correct",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(onClick = {
                                    userAnswers = mutableMapOf()
                                    activeQuestionIndex = 0
                                }) {
                                    Text("Clear All Answers")
                                }
                            }
                        }
                    }
                }
            }
        }

        // --- DIALOG 1: Switch / Create Course Dialog ---
        if (showCreateCourseDialog) {
            CreateCourseDialog(
                currentDegreeName = activeCourse.degreeName,
                currentDegreeCode = activeCourse.degreeCode,
                currentDurationYears = activeCourse.durationYears,
                onDismiss = { showCreateCourseDialog = false },
                onCreateCourse = { name, code, duration ->
                    activeCourse = UserCourse(
                        degreeName = name,
                        degreeCode = code,
                        durationYears = duration
                    )
                    // Auto-generate empty semesters according to duration
                    for (i in 1..(duration * 2)) {
                        activeCourse.semesters.add(UserSemester(i, "Semester $i"))
                    }
                    showCreateCourseDialog = false
                }
            )
        }

        // --- DIALOG 2: Add Semester Dialog ---
        if (showAddSemesterDialog) {
            AddSemesterDialog(
                nextSemNumber = activeCourse.semesters.size + 1,
                onDismiss = { showAddSemesterDialog = false },
                onAddSemester = { semTitle ->
                    val newNum = activeCourse.semesters.size + 1
                    activeCourse.semesters.add(UserSemester(newNum, semTitle))
                    showAddSemesterDialog = false
                }
            )
        }

        // --- DIALOG 3: Add Subject to Semester Dialog ---
        showAddSubjectDialogForSem?.let { targetSem ->
            AddSubjectDialog(
                semesterTitle = targetSem.semesterTitle,
                onDismiss = { showAddSubjectDialogForSem = null },
                onAddSubject = { code, name, credits ->
                    targetSem.subjects.add(UserSubject(code, name, credits))
                    showAddSubjectDialogForSem = null
                }
            )
        }

        // --- DIALOG 4: Add Exam Mark Dialog ---
        activeExamEditingSubject?.let { targetSubj ->
            AddExamMarkDialog(
                subjectTitle = targetSubj.name,
                onDismiss = { activeExamEditingSubject = null },
                onAddExam = { examTitle, marks, max ->
                    targetSubj.exams.add(UserExamMark(examTitle, marks, max))
                    activeExamEditingSubject = null
                }
            )
        }
    }
}

@Composable
fun CreateCourseDialog(
    currentDegreeName: String,
    currentDegreeCode: String,
    currentDurationYears: Int,
    onDismiss: () -> Unit,
    onCreateCourse: (name: String, code: String, durationYears: Int) -> Unit
) {
    var degreeName by remember { mutableStateOf(currentDegreeName) }
    var degreeCode by remember { mutableStateOf(currentDegreeCode) }
    var durationStr by remember { mutableStateOf(currentDurationYears.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create / Switch My Degree Course", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Specify your custom degree title (e.g. BBA, B.Tech, BCA, B.Sc CS, M.Tech) and duration:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = degreeName,
                    onValueChange = { degreeName = it },
                    label = { Text("Degree Title (e.g. Bachelor of Technology)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = degreeCode,
                    onValueChange = { degreeCode = it },
                    label = { Text("Short Code (e.g. B.Tech, BBA, BCA)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = durationStr,
                    onValueChange = { durationStr = it },
                    label = { Text("Duration in Years (e.g. 3 or 4)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val name = if (degreeName.isNotBlank()) degreeName else "My Custom Degree"
                    val code = if (degreeCode.isNotBlank()) degreeCode else "DEGREE"
                    val duration = durationStr.toIntOrNull()?.coerceIn(1, 6) ?: 3
                    onCreateCourse(name, code, duration)
                },
                modifier = Modifier.testTag("confirm_create_course_btn")
            ) {
                Text("Set Course & Generate Semesters")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun AddSemesterDialog(
    nextSemNumber: Int,
    onDismiss: () -> Unit,
    onAddSemester: (title: String) -> Unit
) {
    var semTitle by remember { mutableStateOf("Semester $nextSemNumber") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Semester $nextSemNumber", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column {
                OutlinedTextField(
                    value = semTitle,
                    onValueChange = { semTitle = it },
                    label = { Text("Semester Title (e.g. Year 1 - Semester $nextSemNumber)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val title = if (semTitle.isNotBlank()) semTitle else "Semester $nextSemNumber"
                onAddSemester(title)
            }) {
                Text("Add Semester")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddSubjectDialog(
    semesterTitle: String,
    onDismiss: () -> Unit,
    onAddSubject: (code: String, name: String, credits: Int) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var creditsStr by remember { mutableStateOf("4") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Subject to $semesterTitle", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Subject Code (e.g. CS101, BCA201, BBA102)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject Title (e.g. C Programming, Accounting)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = creditsStr,
                    onValueChange = { creditsStr = it },
                    label = { Text("Subject Credits (e.g. 3, 4)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val subCode = if (code.isNotBlank()) code else "SUB101"
                val subName = if (name.isNotBlank()) name else "New Subject"
                val creds = creditsStr.toIntOrNull()?.coerceIn(1, 10) ?: 3
                onAddSubject(subCode, subName, creds)
            }) {
                Text("Add Subject")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun CourseStatMetric(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun AddExamMarkDialog(
    subjectTitle: String,
    onDismiss: () -> Unit,
    onAddExam: (examTitle: String, marksObtained: Int, maxMarks: Int) -> Unit
) {
    var examTitle by remember { mutableStateOf("") }
    var marksObtainedStr by remember { mutableStateOf("40") }
    var maxMarksStr by remember { mutableStateOf("50") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Exam / Test for $subjectTitle", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = examTitle,
                    onValueChange = { examTitle = it },
                    label = { Text("Exam Name (e.g. Midterm 2, Practical, Final)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = marksObtainedStr,
                        onValueChange = { marksObtainedStr = it },
                        label = { Text("Marks Received") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = maxMarksStr,
                        onValueChange = { maxMarksStr = it },
                        label = { Text("Max Marks") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val name = if (examTitle.isNotBlank()) examTitle else "Internal Exam"
                    val marks = marksObtainedStr.toIntOrNull() ?: 0
                    val max = maxMarksStr.toIntOrNull() ?: 50
                    onAddExam(name, marks, max)
                },
                modifier = Modifier.testTag("confirm_add_exam_btn")
            ) {
                Text("Add Exam")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
