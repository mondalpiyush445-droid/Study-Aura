package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.data.local.AssignmentEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AssignmentsScreen(
    assignments: List<AssignmentEntity>,
    onAddAssignment: (title: String, course: String, dueDateMillis: Long, priority: String, notes: String) -> Unit,
    onToggleCompletion: (AssignmentEntity) -> Unit,
    onDeleteAssignment: (AssignmentEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") } // ALL, PENDING, COMPLETED
    var showAddDialog by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    val filteredList = when (selectedFilter) {
        "PENDING" -> assignments.filter { !it.isCompleted }
        "COMPLETED" -> assignments.filter { it.isCompleted }
        else -> assignments
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Assignments & Task Hub",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Track homework, lab reports, and research papers with priority badges.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("All (${assignments.size})") },
                    modifier = Modifier.testTag("filter_all_tasks")
                )
                FilterChip(
                    selected = selectedFilter == "PENDING",
                    onClick = { selectedFilter = "PENDING" },
                    label = { Text("Pending (${assignments.count { !it.isCompleted }})") },
                    modifier = Modifier.testTag("filter_pending_tasks")
                )
                FilterChip(
                    selected = selectedFilter == "COMPLETED",
                    onClick = { selectedFilter = "COMPLETED" },
                    label = { Text("Completed (${assignments.count { it.isCompleted }})") },
                    modifier = Modifier.testTag("filter_completed_tasks")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No assignments found for this filter.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredList) { assignment ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("assignment_item_${assignment.id}"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Checkbox(
                                        checked = assignment.isCompleted,
                                        onCheckedChange = { onToggleCompletion(assignment) },
                                        modifier = Modifier.testTag("task_checkbox_${assignment.id}")
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Column {
                                        Text(
                                            text = assignment.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            textDecoration = if (assignment.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                                            color = if (assignment.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${assignment.course} • Due: ${dateFormat.format(Date(assignment.dueDateMillis))}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (assignment.notes.isNotBlank()) {
                                            Text(
                                                text = assignment.notes,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                when (assignment.priority) {
                                                    "HIGH" -> MaterialTheme.colorScheme.errorContainer
                                                    "MEDIUM" -> MaterialTheme.colorScheme.secondaryContainer
                                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                                }
                                            )
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = assignment.priority,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = when (assignment.priority) {
                                                "HIGH" -> MaterialTheme.colorScheme.onErrorContainer
                                                "MEDIUM" -> MaterialTheme.colorScheme.onSecondaryContainer
                                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                                            }
                                        )
                                    }

                                    IconButton(onClick = { onDeleteAssignment(assignment) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Task",
                                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .testTag("add_assignment_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Task")
        }
    }

    if (showAddDialog) {
        var title by remember { mutableStateOf("") }
        var course by remember { mutableStateOf("") }
        var priority by remember { mutableStateOf("MEDIUM") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Assignment Task") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title") },
                        modifier = Modifier.fillMaxWidth().testTag("add_assignment_title_input")
                    )
                    OutlinedTextField(
                        value = course,
                        onValueChange = { course = it },
                        label = { Text("Course Name") },
                        modifier = Modifier.fillMaxWidth().testTag("add_assignment_course_input")
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = priority == "HIGH",
                            onClick = { priority = "HIGH" },
                            label = { Text("High") }
                        )
                        FilterChip(
                            selected = priority == "MEDIUM",
                            onClick = { priority = "MEDIUM" },
                            label = { Text("Medium") }
                        )
                        FilterChip(
                            selected = priority == "LOW",
                            onClick = { priority = "LOW" },
                            label = { Text("Low") }
                        )
                    }
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Instructions") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (title.isNotBlank()) {
                            val dueInTwoDays = System.currentTimeMillis() + (86400000L * 2)
                            onAddAssignment(title, if (course.isBlank()) "General Study" else course, dueInTwoDays, priority, notes)
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("submit_add_assignment_btn")
                ) {
                    Text("Save Task")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
