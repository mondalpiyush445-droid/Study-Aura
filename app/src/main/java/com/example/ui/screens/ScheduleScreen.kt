package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ClassScheduleEntity
import com.example.util.GoogleCalendarHelper
import java.util.Calendar
import java.util.Locale
import kotlin.math.max

enum class AttendanceStatus {
    PRESENT, // Attended College - Locked (Green dot)
    ABSENT,  // Unattended Working Day - Automatic Red dot
    OFF,     // Holiday / Weekend (Gray dot)
    UPCOMING // Future Working Day (Outline)
}

@Composable
fun ScheduleScreen(
    classSchedules: List<ClassScheduleEntity>,
    onAddClassSchedule: (name: String, location: String, day: String, start: String, end: String, instructor: String) -> Unit,
    onDeleteClassSchedule: (ClassScheduleEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Full Calendar Attendance, 1 = Class Timetable

    // Current Date Context
    val todayCal = remember { Calendar.getInstance() }
    val todayYear = todayCal.get(Calendar.YEAR)
    val todayMonth = todayCal.get(Calendar.MONTH) // 0-indexed
    val todayDay = todayCal.get(Calendar.DAY_OF_MONTH)

    // Calendar Navigation State
    var displayedYear by remember { mutableIntStateOf(todayYear) }
    var displayedMonth by remember { mutableIntStateOf(todayMonth) }

    // Locked Marked Present Days Set: string key format "YEAR_MONTH_DAY" (e.g. "2026_6_27")
    val markedPresentSet = remember {
        mutableStateListOf<String>().apply {
            // Pre-fill July 2026 sample attended days
            addAll(listOf(
                "2026_6_1", "2026_6_2", "2026_6_3", "2026_6_6", "2026_6_7", "2026_6_8", "2026_6_9",
                "2026_6_10", "2026_6_13", "2026_6_14", "2026_6_15", "2026_6_16", "2026_6_17",
                "2026_6_20", "2026_6_21", "2026_6_22", "2026_6_23", "2026_6_24", "2026_6_27"
            ))
        }
    }

    // Custom Official Holidays Set
    val markedOffSet = remember {
        mutableStateListOf<String>()
    }

    // Info Toast/Banner Message when attempting to remove locked attendance
    var calendarMessageBanner by remember { mutableStateOf<String?>(null) }

    // Dynamic Attendance Metrics Calculation for Current Month up to Today
    val daysInDisplayedMonth = remember(displayedYear, displayedMonth) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, displayedYear)
        cal.set(Calendar.MONTH, displayedMonth)
        cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    val firstDayOfWeekOffset = remember(displayedYear, displayedMonth) {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, displayedYear)
        cal.set(Calendar.MONTH, displayedMonth)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) // 1 = Sun, 2 = Mon, ...
        (dayOfWeek + 5) % 7 // Monday = 0, Tuesday = 1, ..., Sunday = 6
    }

    // Stats Math across current month working days up to today
    var totalWorkingDaysSoFar = 0
    var totalAttendedDays = 0
    var totalAbsentDays = 0

    (1..daysInDisplayedMonth).forEach { day ->
        val dateKey = "${displayedYear}_${displayedMonth}_$day"
        val cal = Calendar.getInstance()
        cal.set(displayedYear, displayedMonth, day)
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val isWeekend = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY)
        val isPastOrToday = if (displayedYear < todayYear) true
        else if (displayedYear > todayYear) false
        else if (displayedMonth < todayMonth) true
        else if (displayedMonth > todayMonth) false
        else day <= todayDay

        val isOff = isWeekend || markedOffSet.contains(dateKey)
        val isPresent = markedPresentSet.contains(dateKey)

        if (isPastOrToday && !isOff) {
            totalWorkingDaysSoFar++
            if (isPresent) {
                totalAttendedDays++
            } else {
                totalAbsentDays++ // Automatically Absent!
            }
        }
    }

    val overallAttendanceRate = if (totalWorkingDaysSoFar > 0) {
        (totalAttendedDays.toDouble() / totalWorkingDaysSoFar) * 100.0
    } else 100.0

    val isAboveTarget = overallAttendanceRate >= 75.0

    // Math for 75% Criteria
    val neededToReach75 = max(0, 3 * totalWorkingDaysSoFar - 4 * totalAttendedDays)
    val allowedToMiss = max(0, (4 * totalAttendedDays - 3 * totalWorkingDaysSoFar) / 3)

    var showAddDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        // Tab Bar
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Calendar", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Full Attendance Calendar", fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("attendance_tab")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Timetable", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Class Timetable", fontWeight = FontWeight.Bold)
                    }
                },
                modifier = Modifier.testTag("timetable_tab")
            )
        }

        if (selectedTab == 0) {
            // TAB 0: Full Month Calendar Attendance & 75% Tracker
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Attendance Metrics & 75% Goal Banner Card
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isAboveTarget) Color(0xFFECFDF5) else Color(0xFFFEF2F2)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 1.5.dp,
                                color = if (isAboveTarget) Color(0xFF10B981) else Color(0xFFEF4444),
                                shape = RoundedCornerShape(20.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isAboveTarget) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = "Status",
                                        tint = if (isAboveTarget) Color(0xFF059669) else Color(0xFFDC2626),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (isAboveTarget) "Attendance: SAFE (≥75%)" else "Attendance Alert! (<75%)",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAboveTarget) Color(0xFF065F46) else Color(0xFF991B1B)
                                        )
                                        Text(
                                            text = "75.0% Mandatory College Criteria",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (isAboveTarget) Color(0xFF047857) else Color(0xFFB91C1C)
                                        )
                                    }
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isAboveTarget) Color(0xFF10B981) else Color(0xFFEF4444))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.1f%%", overallAttendanceRate),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Attendance Progress Bar
                            LinearProgressIndicator(
                                progress = { (overallAttendanceRate / 100.0).toFloat().coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(RoundedCornerShape(5.dp)),
                                color = if (isAboveTarget) Color(0xFF10B981) else Color(0xFFEF4444),
                                trackColor = Color.White.copy(alpha = 0.7f)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Smart Guidance Text
                            Text(
                                text = if (isAboveTarget) {
                                    if (allowedToMiss > 0) {
                                        "You attended $totalAttendedDays out of $totalWorkingDaysSoFar working days so far. You are above 75% and can safely miss up to $allowedToMiss college day(s)!"
                                    } else {
                                        "You are at $totalAttendedDays/$totalWorkingDaysSoFar working days (exactly 75%). Do not miss upcoming classes!"
                                    }
                                } else {
                                    "Warning! Attended $totalAttendedDays of $totalWorkingDaysSoFar working days ($totalAbsentDays absent). You MUST attend the next $neededToReach75 consecutive working day(s) to hit 75%!"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isAboveTarget) Color(0xFF065F46) else Color(0xFF991B1B)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Divider(color = (if (isAboveTarget) Color(0xFF10B981) else Color(0xFFEF4444)).copy(alpha = 0.3f))

                            Spacer(modifier = Modifier.height(12.dp))

                            // Detailed Breakdown Metrics
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                MetricColumn(
                                    label = "Working Days",
                                    value = "$totalWorkingDaysSoFar",
                                    textColor = Color.DarkGray
                                )
                                MetricColumn(
                                    label = "Attended (Present)",
                                    value = "$totalAttendedDays",
                                    textColor = Color(0xFF059669)
                                )
                                MetricColumn(
                                    label = "Auto Absent (Red)",
                                    value = "$totalAbsentDays",
                                    textColor = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }

                // 2. Full Month Calendar View
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            // Calendar Title & Navigation
                            val calHeaderFormat = Calendar.getInstance().apply {
                                set(displayedYear, displayedMonth, 1)
                            }
                            val monthName = calHeaderFormat.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) ?: "Month"

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = "Full Calendar",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "$monthName $displayedYear",
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = {
                                        if (displayedMonth == 0) {
                                            displayedMonth = 11
                                            displayedYear--
                                        } else {
                                            displayedMonth--
                                        }
                                    }) {
                                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
                                    }

                                    TextButton(onClick = {
                                        displayedYear = todayYear
                                        displayedMonth = todayMonth
                                    }) {
                                        Text("Today", fontWeight = FontWeight.Bold)
                                    }

                                    IconButton(onClick = {
                                        if (displayedMonth == 11) {
                                            displayedMonth = 0
                                            displayedYear++
                                        } else {
                                            displayedMonth++
                                        }
                                    }) {
                                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
                                    }
                                }
                            }

                            // Banner Message for Locked Present Notification
                            if (calendarMessageBanner != null) {
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Icon(Icons.Default.Info, contentDescription = "Lock Info", tint = Color(0xFFD97706), modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = calendarMessageBanner!!,
                                                style = MaterialTheme.typography.labelMedium,
                                                color = Color(0xFF92400E),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        TextButton(onClick = { calendarMessageBanner = null }) {
                                            Text("OK", color = Color(0xFF92400E), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Rules Banner
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.EventAvailable, contentDescription = "Rules", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "• Tap any working day to mark Present (Green).\n" +
                                                "• Once marked Present, it CANNOT be removed.\n" +
                                                "• Any unattended past/today working day AUTOMATICALLY shows a Red dot (Absent)!",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Day of Week Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { dayHeader ->
                                    Text(
                                        text = dayHeader,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (dayHeader == "Sat" || dayHeader == "Sun") Color.Gray else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.width(38.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Full Month Grid Calculation
                            val totalGridSlots = firstDayOfWeekOffset + daysInDisplayedMonth
                            val totalWeeks = (totalGridSlots + 6) / 7

                            (0 until totalWeeks).forEach { weekIndex ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    (0..6).forEach { dayOfWeekIndex ->
                                        val slotIndex = weekIndex * 7 + dayOfWeekIndex
                                        val dayNumber = slotIndex - firstDayOfWeekOffset + 1

                                        if (dayNumber in 1..daysInDisplayedMonth) {
                                            val dateKey = "${displayedYear}_${displayedMonth}_$dayNumber"
                                            val cal = Calendar.getInstance()
                                            cal.set(displayedYear, displayedMonth, dayNumber)
                                            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
                                            val isWeekend = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY)

                                            val isPastOrToday = if (displayedYear < todayYear) true
                                            else if (displayedYear > todayYear) false
                                            else if (displayedMonth < todayMonth) true
                                            else if (displayedMonth > todayMonth) false
                                            else dayNumber <= todayDay

                                            val isToday = (displayedYear == todayYear && displayedMonth == todayMonth && dayNumber == todayDay)

                                            val isMarkedPresent = markedPresentSet.contains(dateKey)
                                            val isMarkedOff = isWeekend || markedOffSet.contains(dateKey)

                                            // Status Determination strictly following user constraints:
                                            // 1. Marked Present -> Green dot (PRESENT) - Locked!
                                            // 2. Off / Weekend -> Gray (OFF)
                                            // 3. Unmarked past/today working day -> Red dot (ABSENT) automatically!
                                            // 4. Future day -> Upcoming (Outlined)
                                            val status = when {
                                                isMarkedPresent -> AttendanceStatus.PRESENT
                                                isMarkedOff -> AttendanceStatus.OFF
                                                isPastOrToday -> AttendanceStatus.ABSENT // Automatic Red Dot!
                                                else -> AttendanceStatus.UPCOMING
                                            }

                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(
                                                        when (status) {
                                                            AttendanceStatus.PRESENT -> Color(0xFF10B981) // Green
                                                            AttendanceStatus.ABSENT -> Color(0xFFEF4444)  // Automatic Red Dot
                                                            AttendanceStatus.OFF -> MaterialTheme.colorScheme.surfaceVariant
                                                            AttendanceStatus.UPCOMING -> MaterialTheme.colorScheme.surface
                                                        }
                                                    )
                                                    .border(
                                                        width = if (isToday) 2.5.dp else if (status == AttendanceStatus.UPCOMING) 1.dp else 0.dp,
                                                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                                                        shape = RoundedCornerShape(12.dp)
                                                    )
                                                    .clickable {
                                                        if (isMarkedPresent) {
                                                            // Locked Present Rule: Once marked, it cannot be removed!
                                                            calendarMessageBanner = "🔒 Attendance for $dayNumber ${monthName.take(3)} is marked Present and cannot be removed!"
                                                        } else if (status == AttendanceStatus.OFF) {
                                                            // User can convert an Off day to working day Present
                                                            markedOffSet.remove(dateKey)
                                                            markedPresentSet.add(dateKey)
                                                            calendarMessageBanner = "✅ Attendance for $dayNumber ${monthName.take(3)} marked Present (Locked)."
                                                        } else {
                                                            // Mark Present and lock it!
                                                            markedPresentSet.add(dateKey)
                                                            calendarMessageBanner = "✅ Attendance for $dayNumber ${monthName.take(3)} marked Present (Locked)."
                                                        }
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Text(
                                                        text = "$dayNumber",
                                                        style = MaterialTheme.typography.labelMedium,
                                                        fontWeight = if (isToday || isMarkedPresent) FontWeight.Bold else FontWeight.Medium,
                                                        color = when (status) {
                                                            AttendanceStatus.PRESENT, AttendanceStatus.ABSENT -> Color.White
                                                            AttendanceStatus.OFF -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                            AttendanceStatus.UPCOMING -> MaterialTheme.colorScheme.onSurface
                                                        }
                                                    )

                                                    // Visual Dot Indicator
                                                    Box(
                                                        modifier = Modifier
                                                            .size(6.dp)
                                                            .clip(CircleShape)
                                                            .background(
                                                                when (status) {
                                                                    AttendanceStatus.PRESENT -> Color.White
                                                                    AttendanceStatus.ABSENT -> Color.White // Clear white dot inside red box
                                                                    AttendanceStatus.OFF -> Color.Transparent
                                                                    AttendanceStatus.UPCOMING -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                                                }
                                                            )
                                                    )
                                                }
                                            }
                                        } else {
                                            // Empty padding slot
                                            Spacer(modifier = Modifier.size(42.dp))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Calendar Legend Bar
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                LegendItem(color = Color(0xFF10B981), label = "Present (Locked)")
                                LegendItem(color = Color(0xFFEF4444), label = "Auto Absent (Red Dot)")
                                LegendItem(color = MaterialTheme.colorScheme.surfaceVariant, label = "Off / Holiday")
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Quick Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                val todayKey = "${todayYear}_${todayMonth}_$todayDay"
                                val isTodayPresent = markedPresentSet.contains(todayKey)

                                Button(
                                    onClick = {
                                        if (!isTodayPresent) {
                                            markedPresentSet.add(todayKey)
                                            calendarMessageBanner = "✅ Marked today's college attendance Present! (Locked)"
                                        } else {
                                            calendarMessageBanner = "🔒 Today's attendance is already marked Present and locked."
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isTodayPresent) Color(0xFF10B981) else MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(
                                        imageVector = if (isTodayPresent) Icons.Default.CheckCircle else Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (isTodayPresent) "Today Marked Present ✓" else "Mark Today Present")
                                }
                            }
                        }
                    }
                }

                // 3. Explanation Card on College 75% Rules
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Target Math",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "How the Full Calendar Attendance Works",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "• **Auto Absent Tracking**: Any past or current working day (Mon-Fri) you don't mark Present automatically displays as a Red Dot (Absent).\n\n" +
                                        "• **Permanent Lock**: Once you tap a day to mark Present (Green), it locks in permanently so your record cannot accidentally be cleared or removed.\n\n" +
                                        "• **75% Mandatory Criteria**: Your attendance percentage updates automatically in real-time. If it dips below 75%, the app tells you the exact number of consecutive classes needed to get back in the safe zone!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        } else {
            // TAB 1: Class Timetable & Google Calendar
            Box(modifier = Modifier.fillMaxSize()) {
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Course Schedule & Timetable",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Manage your weekly classes, room locations, and professors offline.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Google Calendar Sync Bar
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Google Calendar Sync",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Google Calendar Sync",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "Export your weekly classes to Google Calendar app",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }

                            if (classSchedules.isNotEmpty()) {
                                OutlinedButton(
                                    onClick = {
                                        classSchedules.firstOrNull()?.let {
                                            GoogleCalendarHelper.addClassToGoogleCalendar(context, it)
                                        }
                                    },
                                    modifier = Modifier.testTag("sync_google_cal_btn")
                                ) {
                                    Text("Sync To Cal")
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (classSchedules.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No course schedules added yet. Tap '+' to create your timetable.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(classSchedules) { schedule ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("schedule_card_${schedule.id}"),
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
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CalendarToday,
                                                    contentDescription = "Calendar",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }

                                            Spacer(modifier = Modifier.width(16.dp))

                                            Column {
                                                Text(
                                                    text = schedule.dayOfWeek,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = schedule.courseName,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text(
                                                    text = "${schedule.startTime} - ${schedule.endTime}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )

                                                Spacer(modifier = Modifier.height(4.dp))

                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.LocationOn,
                                                        contentDescription = "Location",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = schedule.location,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = "Instructor",
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = schedule.instructor,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }

                                        Row {
                                            IconButton(
                                                onClick = { GoogleCalendarHelper.addClassToGoogleCalendar(context, schedule) },
                                                modifier = Modifier.testTag("add_item_to_gcal_btn_${schedule.id}")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CalendarMonth,
                                                    contentDescription = "Add to Google Calendar",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }

                                            IconButton(onClick = { onDeleteClassSchedule(schedule) }) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete Schedule",
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
                        .testTag("add_schedule_fab")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Schedule")
                }
            }
        }
    }

    if (showAddDialog) {
        var name by remember { mutableStateOf("") }
        var location by remember { mutableStateOf("") }
        var day by remember { mutableStateOf("Wednesday") }
        var startTime by remember { mutableStateOf("09:00 AM") }
        var endTime by remember { mutableStateOf("10:30 AM") }
        var instructor by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add New Class Schedule") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Course Name") },
                        modifier = Modifier.fillMaxWidth().testTag("add_class_name_input")
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location / Room") },
                        modifier = Modifier.fillMaxWidth().testTag("add_class_location_input")
                    )
                    OutlinedTextField(
                        value = day,
                        onValueChange = { day = it },
                        label = { Text("Day of Week") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Start Time") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { endTime = it },
                            label = { Text("End Time") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    OutlinedTextField(
                        value = instructor,
                        onValueChange = { instructor = it },
                        label = { Text("Instructor / Professor") },
                        modifier = Modifier.fillMaxWidth().testTag("add_class_instructor_input")
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (name.isNotBlank()) {
                            onAddClassSchedule(
                                name,
                                if (location.isBlank()) "Main Campus" else location,
                                day,
                                startTime,
                                endTime,
                                if (instructor.isBlank()) "Faculty Staff" else instructor
                            )
                            showAddDialog = false
                        }
                    },
                    modifier = Modifier.testTag("submit_add_class_btn")
                ) {
                    Text("Save Schedule")
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

@Composable
private fun MetricColumn(
    label: String,
    value: String,
    textColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
