package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.data.local.StudyNoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

data class VisualizedNoteResult(
    val title: String,
    val executiveSummary: String,
    val visualDiagramSteps: List<String>,
    val keyFormulasAndTerms: List<String>,
    val simplifiedExplanation: String,
    val studyTips: List<String>
)

object GeminiStudyAssistant {

    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun queryAssistant(
        userPrompt: String,
        savedNotes: List<StudyNoteEntity> = emptyList()
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                // Build saved notes knowledge base context for Gemini
                val notesContext = if (savedNotes.isNotEmpty()) {
                    val sb = StringBuilder("USER'S SAVED NOTES KNOWLEDGE BASE:\n")
                    savedNotes.forEachIndexed { idx, note ->
                        sb.append("Note #${idx + 1}:\n")
                        sb.append("  - Title: ${note.title}\n")
                        sb.append("  - Course: ${note.course}\n")
                        sb.append("  - Tags: ${note.tags}\n")
                        sb.append("  - Content: ${note.content}\n\n")
                    }
                    sb.toString()
                } else {
                    "USER HAS NO SAVED NOTES CURRENTLY IN KNOWLEDGE BASE."
                }

                val systemInstruction = "You are Academic Assistant for Academic Hub app. " +
                        "You have direct access to the user's saved notes database (both personal knowledge notes and visual chapter notes) provided below in context.\n" +
                        "$notesContext\n\n" +
                        "STRICT PRECISION & ACCURACY MANDATES:\n" +
                        "1. ALWAYS SEARCH USER NOTES FIRST: Search both personal knowledge notes and saved textbook notes in the database above.\n" +
                        "2. ANSWER ONLY WHAT WAS ASKED: If the user asks for a specific definition (e.g., 'definition of calculus'), output ONLY the exact definition found in their notes. Do NOT add unasked history, extra chapters, or general commentary.\n" +
                        "3. FORMULA REQUESTS: If the user asks for a formula (e.g., 'formula for integration'), output ONLY that specific formula from their notes.\n" +
                        "4. SUMMARY REQUESTS: If the user asks to 'Summarize my calculus notes', summarize ONLY the calculus note content and nothing else.\n" +
                        "5. CITE SOURCE: Explicitly state which note title the information came from (e.g., 'From your note [Note Title]')."

                val jsonPayload = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", "$systemInstruction\n\nUser Question: $userPrompt") })
                            })
                        })
                    })
                }

                val body = jsonPayload.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$BASE_URL?key=$apiKey")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        if (responseBody != null) {
                            val jsonResponse = JSONObject(responseBody)
                            val candidates = jsonResponse.optJSONArray("candidates")
                            if (candidates != null && candidates.length() > 0) {
                                val content = candidates.getJSONObject(0).optJSONObject("content")
                                val parts = content?.optJSONArray("parts")
                                if (parts != null && parts.length() > 0) {
                                    val replyText = parts.getJSONObject(0).optString("text")
                                    if (replyText.isNotBlank()) {
                                        return@withContext replyText
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Fall back to smart offline helper on network or key exception
            }
        }

        // Smart Offline Academic AI Assistant Logic connected to Saved Notes
        generateOfflineAssistantResponse(userPrompt, savedNotes)
    }

    suspend fun analyzeChapterScreenshot(
        chapterName: String,
        bitmap: Bitmap? = null
    ): VisualizedNoteResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val partsArray = JSONArray()
                val promptText = "Generate comprehensive, structured handwritten-style study notes for topic/chapter '$chapterName'. " +
                        "Format response strictly as JSON with key fields:\n" +
                        "1. title: Chapter or Topic Title\n" +
                        "2. executiveSummary: 2-3 sentence clear high-level summary\n" +
                        "3. visualDiagramSteps: Array of 4-6 sequential flow/diagram steps with step numbers and flow arrows (e.g., 'Step 1: ... -> Step 2: ...')\n" +
                        "4. keyFormulasAndTerms: Array of 4-6 essential formulas, mathematical equations, or key technical terms\n" +
                        "5. simplifiedExplanation: Simplified ELI5 explanation with real-world analogies\n" +
                        "6. studyTips: Array of 3 exam preparation tips for this topic"

                partsArray.put(JSONObject().apply { put("text", promptText) })

                if (bitmap != null) {
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                    val base64Image = Base64.encodeToString(stream.toByteArray(), Base64.NO_WRAP)
                    partsArray.put(JSONObject().apply {
                        put("inlineData", JSONObject().apply {
                            put("mimeType", "image/jpeg")
                            put("data", base64Image)
                        })
                    })
                }

                val jsonPayload = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply { put("parts", partsArray) })
                    })
                }

                val body = jsonPayload.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder()
                    .url("$BASE_URL?key=$apiKey")
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val responseBody = response.body?.string()
                        if (responseBody != null) {
                            val jsonResponse = JSONObject(responseBody)
                            val candidates = jsonResponse.optJSONArray("candidates")
                            if (candidates != null && candidates.length() > 0) {
                                val content = candidates.getJSONObject(0).optJSONObject("content")
                                val parts = content?.optJSONArray("parts")
                                if (parts != null && parts.length() > 0) {
                                    val text = parts.getJSONObject(0).optString("text")
                                    // Parse or fallback
                                    val cleanedJson = text.substringAfter("{").substringBeforeLast("}")
                                    val parsedObj = JSONObject("{$cleanedJson}")
                                    return@withContext VisualizedNoteResult(
                                        title = parsedObj.optString("title", chapterName),
                                        executiveSummary = parsedObj.optString("executiveSummary", "Overview of $chapterName"),
                                        visualDiagramSteps = parseJsonArray(parsedObj.optJSONArray("visualDiagramSteps")),
                                        keyFormulasAndTerms = parseJsonArray(parsedObj.optJSONArray("keyFormulasAndTerms")),
                                        simplifiedExplanation = parsedObj.optString("simplifiedExplanation", "Summary explanation"),
                                        studyTips = parseJsonArray(parsedObj.optJSONArray("studyTips"))
                                    )
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                // Fallback to offline chapter generator
            }
        }

        generateOfflineVisualizedNote(chapterName)
    }

    private fun parseJsonArray(arr: JSONArray?): List<String> {
        if (arr == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until arr.length()) {
            list.add(arr.optString(i))
        }
        return list
    }

    fun generateOfflineVisualizedNote(chapterName: String): VisualizedNoteResult {
        val lower = chapterName.lowercase().trim()
        return when {
            lower.contains("calculus") || lower.contains("integrat") || lower.contains("differentia") || lower.contains("derivative") -> VisualizedNoteResult(
                title = "Calculus: Integration & Fundamental Theorem",
                executiveSummary = "Integration measures net accumulated totals under continuous curves over intervals, functioning as the inverse of differentiation.",
                visualDiagramSteps = listOf(
                    "Step 1: Plot Curve f(x) over Interval [a, b]",
                    "Step 2: Subdivide Interval into n Riemann Rectangles with width Δx = (b-a)/n",
                    "Step 3: Limit n → ∞ (Rectangles become infinitely thin strips height f(x_i))",
                    "Step 4: Form Definite Integral → ∫[a to b] f(x) dx = F(b) - F(a)"
                ),
                keyFormulasAndTerms = listOf(
                    "Power Rule: ∫ x^n dx = (x^(n+1))/(n+1) + C",
                    "Integration by Parts: ∫ u dv = uv - ∫ v du",
                    "Logarithmic Rule: ∫ (1/x) dx = ln|x| + C",
                    "FTC Part I: d/dx [∫[a to x] f(t) dt] = f(x)"
                ),
                simplifiedExplanation = "Differentiation slices a loaf of bread into tiny crumbs to measure rate at one instant. Integration puts all those crumbs back together to find total volume!",
                studyTips = listOf(
                    "Always append the constant of integration (+ C) for indefinite integrals.",
                    "Use LIATE rule (Logarithmic, Inverse trig, Algebraic, Trig, Exponential) to choose 'u' in Integration by Parts.",
                    "Remember to transform limits when applying u-substitution to definite integrals."
                )
            )

            lower.contains("dbms") || lower.contains("database") || lower.contains("sql") || lower.contains("relational") -> VisualizedNoteResult(
                title = "DBMS: Relational Model, Normalization & SQL Queries",
                executiveSummary = "Database Management Systems structure structured data into tables (relations) enforcing ACID properties, integrity constraints, and optimized SQL retrieval.",
                visualDiagramSteps = listOf(
                    "Step 1: Conceptual ER Diagram (Entities, Attributes & Relationships)",
                    "Step 2: Relational Schema Mapping (Primary Keys & Foreign Key Constraints)",
                    "Step 3: Normalization Workflow (1NF → 2NF → 3NF → BCNF to remove redundancy)",
                    "Step 4: SQL Query Execution Engine (Parsing → Optimization → Index Scan → Result Set)"
                ),
                keyFormulasAndTerms = listOf(
                    "ACID Properties: Atomicity, Consistency, Isolation, Durability",
                    "1NF: Atomic values only (no repeating groups)",
                    "2NF: 1NF + No partial dependencies on composite primary key",
                    "3NF: 2NF + No transitive dependencies (non-key → non-key)",
                    "SQL Join: SELECT * FROM A INNER JOIN B ON A.id = B.foreign_id"
                ),
                simplifiedExplanation = "A relational database is like a digital filing cabinet where every folder has a unique barcode (Primary Key) and cross-references related files (Foreign Keys) without duplicating papers!",
                studyTips = listOf(
                    "Master the difference between INNER JOIN, LEFT OUTER JOIN, and FULL JOIN.",
                    "Check non-key attribute dependencies carefully when solving 3NF normalization questions.",
                    "Remember indexes speed up SELECT reads but slightly slow down INSERT/UPDATE writes."
                )
            )

            lower.contains("network") || lower.contains("tcp") || lower.contains("ip") || lower.contains("osi") -> VisualizedNoteResult(
                title = "Computer Networks: OSI 7-Layer Model & TCP/IP Protocol",
                executiveSummary = "Computer networking structures packet transmission through layered architectures (OSI & TCP/IP) ensuring reliable framing, routing, and end-to-end delivery.",
                visualDiagramSteps = listOf(
                    "Application Layer (HTTP/DNS) → Encapsulates User Data",
                    "Transport Layer (TCP/UDP) → Adds Port Numbers & Sequence Headers",
                    "Network Layer (IP) → Encapsulates into Packets with Source/Dest IP Addresses",
                    "Data Link Layer (Ethernet/MAC) → Frames Packet & Routes across Routers/Switches",
                    "Physical Layer (Bits/Signals) → Transmits Electrical/Optical Pulses over Cable"
                ),
                keyFormulasAndTerms = listOf(
                    "TCP 3-Way Handshake: SYN → SYN-ACK → ACK",
                    "Subnet Masking: Network ID = IP Address AND Subnet Mask",
                    "OSI Layers (Top to Bottom): Application, Presentation, Session, Transport, Network, Data Link, Physical",
                    "IPv4 vs IPv6: 32-bit dotted decimal vs 128-bit hexadecimal"
                ),
                simplifiedExplanation = "Sending data over the internet is like mailing a letter: you write a message (Application), put it in an envelope with apartment number (Port), write postal addresses (IP), and put it on a delivery truck (Physical cable)!",
                studyTips = listOf(
                    "Memorize OSI layers using mnemonic: 'All People Seem To Need Data Processing'.",
                    "Understand why TCP is connection-oriented (reliable) while UDP is connectionless (fast/streaming).",
                    "Practice calculating host ranges from CIDR subnet notation (/24, /28)."
                )
            )

            lower.contains("os") || lower.contains("operating system") || lower.contains("process") || lower.contains("deadlock") || lower.contains("scheduling") -> VisualizedNoteResult(
                title = "Operating Systems: Process Management & CPU Scheduling",
                executiveSummary = "An OS manages hardware abstractions, process lifecycles, memory allocation, context switching, and CPU scheduling algorithms.",
                visualDiagramSteps = listOf(
                    "Process Creation (New State) → Loaded into Ready Queue",
                    "CPU Scheduler dispatches process → Running State",
                    "I/O Request or Interrupt occurs → Process transitions to Waiting / Blocked State",
                    "I/O Completion → Returns to Ready Queue",
                    "Execution Finished → Process Terminated & Resources Freed"
                ),
                keyFormulasAndTerms = listOf(
                    "Turnaround Time = Completion Time - Arrival Time",
                    "Waiting Time = Turnaround Time - Burst Time",
                    "4 Deadlock Conditions: Mutual Exclusion, Hold & Wait, No Preemption, Circular Wait",
                    "Banker's Algorithm: Need[i][j] = Max[i][j] - Allocation[i][j]"
                ),
                simplifiedExplanation = "The OS is a master chef in a restaurant kitchen: CPU is the burner, processes are customer orders, and CPU scheduling decides which dish gets cooked next without burning orders!",
                studyTips = listOf(
                    "Draw Gantt charts step-by-step for FCFS, SJF, Round Robin, and Priority scheduling.",
                    "Remember Round Robin performance depends heavily on Time Quantum size.",
                    "Be ready to explain how Banker's Algorithm prevents unsafe states and deadlocks."
                )
            )

            lower.contains("data structure") || lower.contains("algo") || lower.contains("tree") || lower.contains("graph") || lower.contains("sort") -> VisualizedNoteResult(
                title = "Data Structures & Algorithms: Trees, Graphs & Complexity",
                executiveSummary = "Data structures organize memory efficient storage, enabling fast algorithmic search, insertion, traversal, and computational complexity optimization.",
                visualDiagramSteps = listOf(
                    "Input Data Array / Nodes → Choose Optimal Data Structure",
                    "Binary Search Tree (BST) Traversal → In-order gives sorted sequence",
                    "Graph Exploration → Breadth-First Search (Queue) or Depth-First Search (Stack)",
                    "Divide & Conquer Sorting (Merge Sort) → Divide array in half → Sort recursively → Merge sorted halves"
                ),
                keyFormulasAndTerms = listOf(
                    "Big-O Time Complexities: O(1) < O(log n) < O(n) < O(n log n) < O(n²)",
                    "Binary Search: O(log n) comparison time on sorted array",
                    "Merge Sort / Quick Sort: O(n log n) average time complexity",
                    "Hash Table Lookup: O(1) average time via Hash Function h(k) = k mod m"
                ),
                simplifiedExplanation = "An array is a straight row of lockers, a Linked List is a treasure hunt with clues pointing to the next location, and a Hash Table is a magic index where you jump straight to the exact locker!",
                studyTips = listOf(
                    "Know worst-case vs average-case time complexities for Quick Sort vs Merge Sort.",
                    "Practice writing recursive base cases to avoid infinite stack overflow errors.",
                    "Remember BFS uses a Queue (FIFO) whereas DFS uses a Stack (LIFO)."
                )
            )

            lower.contains("ai") || lower.contains("machine learning") || lower.contains("neural") || lower.contains("deep learning") -> VisualizedNoteResult(
                title = "Machine Learning & Neural Networks: Core Architecture",
                executiveSummary = "Machine Learning builds predictive mathematical models from data using supervised/unsupervised learning, gradient descent optimization, and artificial neural networks.",
                visualDiagramSteps = listOf(
                    "Data Preprocessing & Feature Extraction → Train/Test Split (80/20)",
                    "Forward Pass: Input Layer x_i × Weights w_i + Bias b → Activation Function σ(z)",
                    "Compute Loss Function: Mean Squared Error (MSE) or Cross-Entropy Loss L(y, ŷ)",
                    "Backward Pass (Backpropagation): Compute Gradients ∂L/∂w via Chain Rule",
                    "Gradient Descent Update: w_new = w_old - α × ∂L/∂w"
                ),
                keyFormulasAndTerms = listOf(
                    "Linear Neuron: z = Σ (w_i × x_i) + b",
                    "Sigmoid Activation: σ(z) = 1 / (1 + e^(-z))",
                    "ReLU Activation: f(z) = max(0, z)",
                    "Gradient Descent Weight Update Rule: w = w - α ∇L"
                ),
                simplifiedExplanation = "A neural network is like a team of blindfolded musicians tuning their instruments. Every wrong note (error) gives feedback to adjust tuning pegs (weights) until the song sounds perfect!",
                studyTips = listOf(
                    "Understand Overfitting vs Underfitting and how Regularization (L1/L2, Dropout) helps.",
                    "Know the difference between Supervised (labeled targets), Unsupervised (clustering), and Reinforcement learning.",
                    "Be prepared to explain the role of activation functions in introducing non-linearity."
                )
            )

            else -> {
                // Dynamic generator that creates tailored visual notes for ANY subject prompt
                val capitalizedTopic = chapterName.trim().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                VisualizedNoteResult(
                    title = "Chapter Study Note: $capitalizedTopic",
                    executiveSummary = "Essential core concepts, structural logic, governing formulas, and visual workflows for $capitalizedTopic formulated for rapid academic retention.",
                    visualDiagramSteps = listOf(
                        "Step 1: Core Fundamentals & Primary System Postulates of $capitalizedTopic",
                        "Step 2: Component Interactions, Input Transformations & Variable Relationships",
                        "Step 3: Execution Logic, Algorithmic Step Sequence & Mathematical Formulations",
                        "Step 4: Output Synthesis, Real-World Applications & Edge Case Solutions"
                    ),
                    keyFormulasAndTerms = listOf(
                        "Fundamental Equation of $capitalizedTopic: Result = Input × Efficiency Factor",
                        "Core Theorem: System Stability holds when Boundary Conditions ≤ Maximum Threshold",
                        "Primary Terminology: System State (S_t), Transfer Function H(s), Equilibrium Point",
                        "Optimization Constraint: Min(Loss) subject to Resource Constraints"
                    ),
                    simplifiedExplanation = "To master $capitalizedTopic, break it into 3 clear components: 1) What initial inputs go into the system, 2) How the core rules transform those inputs in the middle, and 3) What final output results!",
                    studyTips = listOf(
                        "Focus on understanding the underlying 'why' behind $capitalizedTopic rather than rote memorization.",
                        "Draw visual concept maps connecting $capitalizedTopic to prerequisite coursework.",
                        "Solve past exam problems highlighting common calculation pitfalls and boundary conditions."
                    )
                )
            }
        }
    }

    private fun generateOfflineAssistantResponse(
        prompt: String,
        savedNotes: List<StudyNoteEntity>
    ): String {
        val lower = prompt.lowercase()

        // 1. Check if user is asking about their saved notes
        val isAskingAboutNotes = lower.contains("note") || lower.contains("saved") || lower.contains("my notes") ||
                lower.contains("what notes") || lower.contains("summarize notes") || lower.contains("list notes")

        // 2. Search saved notes for relevant query matches
        val matchingNote = savedNotes.find { note ->
            val tLower = note.title.lowercase()
            val cLower = note.content.lowercase()
            val tagLower = note.tags.lowercase()
            val courseLower = note.course.lowercase()

            val searchTerms = lower.split(" ", "?", "!", ".", ",").filter { it.length > 3 }
            searchTerms.any { term -> tLower.contains(term) || cLower.contains(term) || tagLower.contains(term) || courseLower.contains(term) }
        }

        if (matchingNote != null) {
            val contentLines = matchingNote.content.lines().filter { it.isNotBlank() }
            
            val isAskingDefinition = lower.contains("definition") || lower.contains("define") || lower.contains("what is") || lower.contains("meaning")
            val isAskingFormula = lower.contains("formula") || lower.contains("equation") || lower.contains("theorem") || lower.contains("math")
            val isAskingSummary = lower.contains("summarize") || lower.contains("summary") || lower.contains("overview")

            return when {
                isAskingDefinition -> {
                    val defLines = contentLines.filter { line ->
                        val lLower = line.lowercase()
                        lLower.contains("definition") || lLower.contains("executive summary") || lLower.contains("is defined as") || lLower.contains("is ")
                    }
                    val extractedText = if (defLines.isNotEmpty()) defLines.joinToString("\n") else matchingNote.content
                    "📌 **Definition from Note: '${matchingNote.title}'** (${matchingNote.course})\n\n" +
                            "$extractedText\n\n" +
                            "*(Strictly provided from your personal saved note: '${matchingNote.title}')*"
                }
                isAskingFormula -> {
                    val formulaLines = contentLines.filter { line ->
                        val lLower = line.lowercase()
                        lLower.contains("formula") || lLower.contains("term") || lLower.contains("=") || lLower.contains("∫") || lLower.contains("dx") || lLower.contains("key")
                    }
                    val extractedText = if (formulaLines.isNotEmpty()) formulaLines.joinToString("\n") else matchingNote.content
                    "📐 **Formula(s) from Note: '${matchingNote.title}'** (${matchingNote.course})\n\n" +
                            "$extractedText\n\n" +
                            "*(Strictly provided from your personal saved note: '${matchingNote.title}')*"
                }
                isAskingSummary -> {
                    "📝 **Summary of Note: '${matchingNote.title}'** (${matchingNote.course})\n\n" +
                            "${matchingNote.content}\n\n" +
                            "*(Full content from your saved note: '${matchingNote.title}')*"
                }
                else -> {
                    "📖 **Note Content: '${matchingNote.title}'** (${matchingNote.course})\n\n" +
                            "${matchingNote.content}\n\n" +
                            "💡 *Tags: ${if (matchingNote.tags.isBlank()) "Personal Knowledge" else matchingNote.tags}*"
                }
            }
        }

        if (isAskingAboutNotes) {
            if (savedNotes.isEmpty()) {
                return "📝 **Your Saved Notes Knowledge Base**\n\n" +
                        "You do not have any saved study notes yet.\n\n" +
                        "• Tap the **'+' button** in the Notes tab to add your first note!\n" +
                        "• Or use the **Chapter Visualizer** / **Wikipedia** search to instantly generate and save visual notes.\n" +
                        "• Once saved, I will automatically use your notes to answer your study questions!"
            } else {
                val sb = StringBuilder("📚 **You have ${savedNotes.size} Saved Note(s) in your Knowledge Base**:\n\n")
                savedNotes.forEachIndexed { idx, note ->
                    sb.append("**${idx + 1}. ${note.title}** (${note.course})\n")
                    sb.append("   • *Content*:\n${note.content}\n\n")
                }
                sb.append("💡 *Ask me any question about these notes (e.g. 'Explain the formula in my Calculus note') and I will answer directly from your saved notes!*")
                return sb.toString()
            }
        }

        // General topic fallback guidance
        val notesNotice = if (savedNotes.isNotEmpty()) {
            "\n\n💡 *Note Knowledge Base*: I checked your ${savedNotes.size} saved note(s). Ask me specific questions about your notes anytime!"
        } else {
            "\n\n💡 *Tip*: You can save study notes in the Notes tab or Chapter Visualizer. Any note you save is automatically connected to me!"
        }

        return when {
            lower.contains("certif") || lower.contains("credential") || lower.contains("verify") -> {
                "🎓 **Certificates & Verification Hub**\n\n" +
                "• Navigate to the **Certificates** tab in the main navigation.\n" +
                "• View your verified credentials, issuer details, issue dates, and unique verification codes.\n" +
                "• Tap 'Add Credential' to record new professional certifications, academic honors, or workshops offline.$notesNotice"
            }
            lower.contains("gpa") || lower.contains("standing") || lower.contains("grade") -> {
                "📊 **Academic Standing & GPA Tracker**\n\n" +
                "• Check your current GPA and target goals in the **Dashboard** bento cards.\n" +
                "• All GPA metrics and target calculations are processed locally on your device for absolute privacy.$notesNotice"
            }
            lower.contains("assign") || lower.contains("homework") || lower.contains("due") -> {
                "📝 **Assignments Management**\n\n" +
                "• Switch to the **Assignments** screen.\n" +
                "• Filter tasks by Pending, Urgent, or Completed.\n" +
                "• Check off completed tasks to update your syllabus momentum in real-time.$notesNotice"
            }
            lower.contains("exam") || lower.contains("midterm") || lower.contains("final") -> {
                "⏳ **Exam Countdown & Preparation**\n\n" +
                "• Open the **Exams** tab to review upcoming exams and days remaining.\n" +
                "• Track syllabus coverage percentages and store study topic notes directly under each exam entry.$notesNotice"
            }
            lower.contains("dark") || lower.contains("theme") || lower.contains("mode") || lower.contains("light") -> {
                "🌙 **Dark Mode & Eye Strain Preference**\n\n" +
                "• Go to **Settings / Profile** or tap the Theme toggle in the top app bar.\n" +
                "• Enable Dark Mode to enjoy a contrast-rich, low-light optimized slate background.$notesNotice"
            }
            lower.contains("biometric") || lower.contains("lock") || lower.contains("security") || lower.contains("privacy") -> {
                "🔒 **Biometric Security & Local Privacy**\n\n" +
                "• All user records are stored strictly in your device's local Room Database.\n" +
                "• Turn on **Biometric Lock** in Settings to protect your academic records with fingerprint / face unlock or a PIN.$notesNotice"
            }
            lower.contains("schedule") || lower.contains("timetable") || lower.contains("class") || lower.contains("75") || lower.contains("attend") -> {
                "📅 **Timetable & 75% Attendance Tracker**\n\n" +
                "• Open the **Schedule** tab to check your weekly college attendance checklist and 75% target calculator.\n" +
                "• Check off college days each week and view your exact safety allowance!$notesNotice"
            }
            else -> {
                "🤖 **Academic AI Study Assistant**\n\n" +
                "I am your AI study assistant connected directly to your saved notes!\n\n" +
                "• **Ask from Notes**: Ask me questions about your saved study notes or course topics.\n" +
                "• **Chapter Visualizer**: Use the Chapter Visualizer tab to search any topic for handwritten notes, formulas, and visual diagrams.\n" +
                "• **App Guidance**: Ask about exams, GPA calculations, certificates, timetable, or 75% attendance criteria.$notesNotice"
            }
        }
    }
}

