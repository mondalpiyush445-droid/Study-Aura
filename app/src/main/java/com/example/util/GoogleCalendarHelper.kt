package com.example.util

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import com.example.data.local.ClassScheduleEntity
import com.example.data.local.ExamEntity
import java.util.Calendar

object GoogleCalendarHelper {

    fun addClassToGoogleCalendar(context: Context, schedule: ClassScheduleEntity) {
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, "Class: ${schedule.courseName}")
                putExtra(CalendarContract.Events.EVENT_LOCATION, schedule.location)
                putExtra(
                    CalendarContract.Events.DESCRIPTION,
                    "Instructor: ${schedule.instructor}\nWeekly Schedule: ${schedule.dayOfWeek} (${schedule.startTime} - ${schedule.endTime})"
                )

                // Calculate next occurrence timestamp
                val cal = Calendar.getInstance()
                val targetDay = when (schedule.dayOfWeek.lowercase()) {
                    "monday", "mon" -> Calendar.MONDAY
                    "tuesday", "tue" -> Calendar.TUESDAY
                    "wednesday", "wed" -> Calendar.WEDNESDAY
                    "thursday", "thu" -> Calendar.THURSDAY
                    "friday", "fri" -> Calendar.FRIDAY
                    "saturday", "sat" -> Calendar.SATURDAY
                    else -> Calendar.SUNDAY
                }

                while (cal.get(Calendar.DAY_OF_WEEK) != targetDay) {
                    cal.add(Calendar.DAY_OF_YEAR, 1)
                }

                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.timeInMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cal.timeInMillis + (90 * 60 * 1000))
                putExtra(CalendarContract.Events.RRULE, "FREQ=WEEKLY")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            context.startActivity(intent)
            Toast.makeText(context, "Opening Google Calendar...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Google Calendar app not installed or unavailable.", Toast.LENGTH_LONG).show()
        }
    }

    fun addExamToGoogleCalendar(context: Context, exam: ExamEntity) {
        try {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, "EXAM: ${exam.subject}")
                putExtra(CalendarContract.Events.EVENT_LOCATION, exam.location)
                putExtra(
                    CalendarContract.Events.DESCRIPTION,
                    "Exam Coverage: ${exam.syllabusCoveragePercent}% Prepared\nNotes: ${exam.notes}"
                )
                val startTime = if (exam.dateMillis > 0) exam.dateMillis else System.currentTimeMillis() + 86400000L
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startTime + (120 * 60 * 1000))
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            context.startActivity(intent)
            Toast.makeText(context, "Opening Google Calendar for ${exam.subject}...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Google Calendar app unavailable.", Toast.LENGTH_LONG).show()
        }
    }
}
