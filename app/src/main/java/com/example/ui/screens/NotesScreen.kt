package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.StudyNoteEntity
import com.example.data.remote.GeminiStudyAssistant
import com.example.data.remote.VisualizedNoteResult
import com.example.data.remote.WikiSearchResult
import com.example.data.remote.WikipediaService
import com.example.ui.viewmodel.ChatMessage
import kotlinx.coroutines.launch

@Composable
fun NotesScreen(
    notes: List<StudyNoteEntity>,
    aiMessages: List<ChatMessage>,
    isAiLoading: Boolean,
    onSendAiPrompt: (String) -> Unit,
    onAddNote: (title: String, course: String, content: String, tags: String) -> Unit,
    onDeleteNote: (StudyNoteEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current

    var selectedTab by remember { mutableStateOf(0) } // 0 = Notes, 1 = AI Chat, 2 = Wikipedia Search, 3 = Chapter Visualizer
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var promptInput by remember { mutableStateOf("") }

    // Wikipedia state
    var wikiQuery by remember { mutableStateOf("Calculus") }
    var wikiResult by remember { mutableStateOf<WikiSearchResult?>(null) }
    var isWikiLoading by remember { mutableStateOf(false) }

    // Chapter Screenshot Visualizer state
    var chapterNameInput by remember { mutableStateOf("C Programming") }
    var visualizedNote by remember { mutableStateOf<VisualizedNoteResult?>(null) }
    var isVisualizerLoading by remember { mutableStateOf(false) }
    var savedNoteBannerMessage by remember { mutableStateOf<String?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Study Notes, AI & Wikipedia",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 0.dp) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Local Notes (${notes.size})") },
                    modifier = Modifier.testTag("notes_tab_btn")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("AI Assistant") },
                    modifier = Modifier.testTag("ai_assistant_tab_btn")
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = {
                        selectedTab = 2
                        if (wikiResult == null) {
                            coroutineScope.launch {
                                isWikiLoading = true
                                wikiResult = WikipediaService.searchWikipedia(wikiQuery)
                                isWikiLoading = false
                            }
                        }
                    },
                    text = { Text("Wikipedia") },
                    modifier = Modifier.testTag("wikipedia_tab_btn")
                )
                Tab(
                    selected = selectedTab == 3,
                    onClick = {
                        selectedTab = 3
                        if (visualizedNote == null) {
                            coroutineScope.launch {
                                isVisualizerLoading = true
                                visualizedNote = GeminiStudyAssistant.analyzeChapterScreenshot(chapterNameInput)
                                isVisualizerLoading = false
                            }
                        }
                    },
                    text = { Text("Chapter Visualizer") },
                    modifier = Modifier.testTag("chapter_visualizer_tab_btn")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> {
                    // Local Study Notes List
                    if (notes.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No study notes saved. Tap '+' to create a note.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(notes) { note ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("note_item_${note.id}"),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                                Icon(
                                                    imageVector = Icons.Default.Description,
                                                    contentDescription = "Note",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(end = 8.dp)
                                                )
                                                Column {
                                                    Text(
                                                        text = note.title,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = note.course,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }

                                            IconButton(onClick = { onDeleteNote(note) }) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete Note",
                                                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = note.content,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        if (note.tags.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Tags: ${note.tags}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // AI Study Assistant Interface
                    Column(modifier = Modifier.weight(1f)) {
                        // AI & Notes Connectivity Status Banner
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Psychology,
                                    contentDescription = "AI Notes Context",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "🧠 Connected to ${notes.size} Personal Knowledge & Saved Note(s)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = if (notes.isEmpty()) "Save personal notes or Chapter Visualizer notes to query definitions & formulas directly!"
                                        else "Personal notes & textbook notes are indexed for instant formula & definition lookups.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Search Guidance Box for Notes, Formulas & Definitions
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search Guide",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "💡 How to Search Formulas & Definitions in AI Assistant:",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "• Search Formula: Type 'Formula for integration in my calculus note'\n" +
                                            "• Search Definition: Type 'Definition of calculus in my notes'\n" +
                                            "• Summarize Note: Type 'Summarize my calculus notes'\n" +
                                            "*(AI will strictly extract only what you ask for without extra fluff!)*",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Quick Prompt Chips from Saved Notes Context
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                            item {
                                FilterChip(
                                    selected = false,
                                    onClick = { onSendAiPrompt("Definition of calculus in my notes") },
                                    label = { Text("📌 Definition of Calculus", style = MaterialTheme.typography.labelSmall) },
                                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp)) }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = false,
                                    onClick = { onSendAiPrompt("Formula for integration in my calculus notes") },
                                    label = { Text("📐 Formula for Integration", style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                            item {
                                FilterChip(
                                    selected = false,
                                    onClick = { onSendAiPrompt("Summarize my calculus notes") },
                                    label = { Text("📝 Summarize Calculus Note", style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                            items(notes.take(5)) { note ->
                                FilterChip(
                                    selected = false,
                                    onClick = { onSendAiPrompt("Search formulas and definitions in my note '${note.title}'") },
                                    label = { Text("Search '${note.title}'", style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }

                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(aiMessages) { msg ->
                                val isUser = msg.sender == "USER"
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth(0.85f)
                                            .testTag("chat_msg_${msg.id}")
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Text(
                                                text = if (isUser) "You" else "Academic AI Assistant",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer
                                                else MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = msg.text,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer
                                                else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }

                            if (isAiLoading) {
                                item {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(8.dp)
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Academic AI is thinking...", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = promptInput,
                                onValueChange = { promptInput = it },
                                placeholder = { Text("Search definition, formula, or summary in notes...", style = MaterialTheme.typography.bodyMedium) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ai_prompt_input")
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    if (promptInput.isNotBlank()) {
                                        onSendAiPrompt(promptInput)
                                        promptInput = ""
                                    }
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .testTag("send_ai_prompt_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "Send",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }

                2 -> {
                    // Wikipedia Discovery Section
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = wikiQuery,
                                onValueChange = { wikiQuery = it },
                                placeholder = { Text("Search Wikipedia topic...") },
                                leadingIcon = {
                                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search Wiki")
                                },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("wiki_search_input")
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        isWikiLoading = true
                                        wikiResult = WikipediaService.searchWikipedia(wikiQuery)
                                        isWikiLoading = false
                                    }
                                },
                                modifier = Modifier.testTag("submit_wiki_search_btn")
                            ) {
                                Text("Search")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Trending Academic Topics
                        Text(
                            text = "Explore Popular Topics:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            val topics = listOf("Calculus", "Quantum Mechanics", "Cell Biology", "Algorithms", "Macroeconomics", "Linear Algebra")
                            items(topics) { topic ->
                                FilterChip(
                                    selected = wikiQuery.equals(topic, ignoreCase = true),
                                    onClick = {
                                        wikiQuery = topic
                                        coroutineScope.launch {
                                            isWikiLoading = true
                                            wikiResult = WikipediaService.searchWikipedia(topic)
                                            isWikiLoading = false
                                        }
                                    },
                                    label = { Text(topic) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isWikiLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else if (wikiResult != null) {
                            val res = wikiResult!!
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Icon(
                                                imageVector = Icons.Default.Book,
                                                contentDescription = "Wikipedia",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(end = 8.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = res.title,
                                                    style = MaterialTheme.typography.titleLarge,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = res.description,
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        if (res.articleUrl != null) {
                                            IconButton(
                                                onClick = { uriHandler.openUri(res.articleUrl) },
                                                modifier = Modifier.testTag("open_wiki_web_btn")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.OpenInNew,
                                                    contentDescription = "Open in Browser",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }

                                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        if (!res.thumbnailUrl.isNullOrBlank()) {
                                            item {
                                                AsyncImage(
                                                    model = res.thumbnailUrl,
                                                    contentDescription = res.title,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(180.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                )
                                            }
                                        }

                                        item {
                                            Text(
                                                text = res.extract,
                                                style = MaterialTheme.typography.bodyLarge,
                                                lineHeight = 24.sp
                                            )
                                        }

                                        if (res.relatedSnippets.isNotEmpty()) {
                                            item {
                                                Text(
                                                    text = "Related Topics & Information:",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(top = 8.dp)
                                                )
                                            }

                                            items(res.relatedSnippets) { snippetObj ->
                                                Card(
                                                    shape = RoundedCornerShape(10.dp),
                                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Column(modifier = Modifier.padding(10.dp)) {
                                                        Text(
                                                            text = snippetObj.title,
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary
                                                        )
                                                        if (snippetObj.snippet.isNotBlank()) {
                                                            Spacer(modifier = Modifier.height(2.dp))
                                                            Text(
                                                                text = snippetObj.snippet,
                                                                style = MaterialTheme.typography.bodySmall
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                onAddNote(
                                                    "Wiki: ${res.title}",
                                                    "Wikipedia Reference",
                                                    "${res.extract}\n\nReference: ${res.articleUrl ?: "Wikipedia"}",
                                                    "Wikipedia, Research"
                                                )
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("save_wiki_to_notes_btn")
                                        ) {
                                            Icon(imageVector = Icons.Default.BookmarkAdd, contentDescription = null)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Save as Note")
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                selectedTab = 1
                                                onSendAiPrompt("Explain this Wikipedia concept in detail with bullet points: ${res.title}. ${res.extract}")
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("ask_ai_about_wiki_btn")
                                        ) {
                                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Explain with AI")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // Textbook Chapter Screenshot Visualizer & Handwritten Notes
                    Column(modifier = Modifier.weight(1f)) {
                        // Saved Note Notification Banner
                        if (savedNoteBannerMessage != null) {
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Saved",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = savedNoteBannerMessage!!,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                    TextButton(onClick = { savedNoteBannerMessage = null }) {
                                        Text("Dismiss")
                                    }
                                }
                            }
                        }

                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Chapter Visualizer",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "AI Textbook Chapter Visualizer & Notes",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Text(
                                    text = "Search any topic or chapter. The AI assistant generates handwritten notes, visual diagrams, formulas, and flowcharts for you.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = chapterNameInput,
                                        onValueChange = { chapterNameInput = it },
                                        label = { Text("Topic or Chapter Name") },
                                        placeholder = { Text("e.g. C Programming, DBMS, Calculus...") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("chapter_input_field")
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Button(
                                        onClick = {
                                            coroutineScope.launch {
                                                isVisualizerLoading = true
                                                visualizedNote = GeminiStudyAssistant.analyzeChapterScreenshot(chapterNameInput)
                                                isVisualizerLoading = false
                                            }
                                        },
                                        modifier = Modifier.testTag("analyze_chapter_btn")
                                    ) {
                                        Text("Visualize")
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    val presetChapters = listOf(
                                        "C Programming",
                                        "Calculus Integration",
                                        "Data Structures",
                                        "DBMS & SQL",
                                        "Computer Networks",
                                        "Operating Systems",
                                        "Machine Learning",
                                        "Cellular Respiration",
                                        "Electromagnetism",
                                        "Organic Chemistry"
                                    )
                                    items(presetChapters) { name ->
                                        FilterChip(
                                            selected = chapterNameInput == name,
                                            onClick = {
                                                chapterNameInput = name
                                                coroutineScope.launch {
                                                    isVisualizerLoading = true
                                                    visualizedNote = GeminiStudyAssistant.analyzeChapterScreenshot(name)
                                                    isVisualizerLoading = false
                                                }
                                            },
                                            label = { Text(name, style = MaterialTheme.typography.labelSmall) }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (isVisualizerLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator()
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Generating Handwritten Notes, Formulas & Diagrams...", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        } else if (visualizedNote != null) {
                            val noteRes = visualizedNote!!

                            // Lined Notebook Styled Canvas Card
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(20.dp)
                                    )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    // Notebook Header
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = "Handwritten",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(end = 6.dp)
                                            )
                                            Text(
                                                text = "✍️ HANDWRITTEN VISUAL NOTEBOOK",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                val contentBuilder = StringBuilder()
                                                contentBuilder.append("SUMMARY:\n${noteRes.executiveSummary}\n\n")
                                                contentBuilder.append("VISUAL DIAGRAM STEPS:\n")
                                                noteRes.visualDiagramSteps.forEach { step -> contentBuilder.append("• $step\n") }
                                                contentBuilder.append("\nFORMULAS & TERMS:\n")
                                                noteRes.keyFormulasAndTerms.forEach { form -> contentBuilder.append("• $form\n") }
                                                contentBuilder.append("\nSIMPLIFIED EXPLANATION:\n${noteRes.simplifiedExplanation}\n")

                                                onAddNote(
                                                    noteRes.title,
                                                    "Chapter Visualizer",
                                                    contentBuilder.toString(),
                                                    "Visualized, Handwritten, AI"
                                                )
                                                savedNoteBannerMessage = "✅ Saved '${noteRes.title}' to your notes! Connected to AI Assistant now."
                                            },
                                            modifier = Modifier.testTag("save_visual_note_btn")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.BookmarkAdd,
                                                contentDescription = "Save Visual Note",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = noteRes.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                                    LazyColumn(
                                        verticalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // 1. Executive Summary
                                        item {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                                                    .padding(12.dp)
                                            ) {
                                                Column {
                                                    Text(
                                                        text = "📌 Executive Summary",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = FontWeight.Bold,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = noteRes.executiveSummary,
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                }
                                            }
                                        }

                                        // 2. Visual Diagram Steps
                                        item {
                                            Text(
                                                text = "✏️ Visual Concept Diagram & Workflow:",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        items(noteRes.visualDiagramSteps) { step ->
                                            Card(
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Psychology,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = step,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                }
                                            }
                                        }

                                        // 3. Key Formulas & Terms
                                        item {
                                            Text(
                                                text = "⚡ Essential Formulas & Key Definitions:",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }

                                        items(noteRes.keyFormulasAndTerms) { formula ->
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                                    .padding(10.dp)
                                            ) {
                                                Text(
                                                    text = formula,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }

                                        // 4. Simplified ELI5 Explanation
                                        item {
                                            Card(
                                                shape = RoundedCornerShape(12.dp),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(12.dp)) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.Lightbulb,
                                                            contentDescription = "ELI5",
                                                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Text(
                                                            text = "💡 Simplified ELI5 Explanation",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onTertiaryContainer
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = noteRes.simplifiedExplanation,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onTertiaryContainer
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
            }
        }

        if (selectedTab == 0) {
            FloatingActionButton(
                onClick = { showAddNoteDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp)
                    .testTag("add_note_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    }

    if (showAddNoteDialog) {
        var title by remember { mutableStateOf("") }
        var course by remember { mutableStateOf("") }
        var content by remember { mutableStateOf("") }
        var tags by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddNoteDialog = false },
            title = { Text("Create Local Study Note") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Note Title") },
                        modifier = Modifier.fillMaxWidth().testTag("add_note_title_input")
                    )
                    OutlinedTextField(
                        value = course,
                        onValueChange = { course = it },
                        label = { Text("Course Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text("Content & Key Formulas") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (comma separated)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (title.isNotBlank() && content.isNotBlank()) {
                            onAddNote(title, if (course.isBlank()) "General" else course, content, tags)
                            showAddNoteDialog = false
                        }
                    },
                    modifier = Modifier.testTag("submit_add_note_btn")
                ) {
                    Text("Save Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddNoteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
