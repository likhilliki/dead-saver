package com.example.util

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import android.widget.Toast
import com.example.data.Event
import java.text.SimpleDateFormat
import java.util.*

object CalendarHelper {

    // Simple parser for ISO 8601 string
    fun parseIso8601(isoString: String): Long {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val date = format.parse(isoString)
            date?.time ?: System.currentTimeMillis()
        } catch (e: Exception) {
            // fallback format: yyyy-MM-dd
            try {
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val date = format.parse(isoString)
                date?.time ?: System.currentTimeMillis()
            } catch (e2: Exception) {
                System.currentTimeMillis()
            }
        }
    }

    // 1. Asynchronous stub simulating backend `googleapis` calendar instance insertion
    suspend fun syncGoogleCalendarSimulated(event: Event, onLogAdded: (String) -> Unit): Boolean {
        onLogAdded("[CALENDAR] Initiating OAuth handshake for Calendar API...")
        kotlinx.coroutines.delay(600)
        onLogAdded("[CALENDAR] Fetching client credentials and token...")
        kotlinx.coroutines.delay(500)
        onLogAdded("[CALENDAR] Preparing Event Payload for Google Calendar:")
        onLogAdded("  - Summary: ${event.title}")
        onLogAdded("  - Category: ${event.taskCategory} (Priority: ${event.priorityScore})")
        onLogAdded("  - Start: ${event.startTime}")
        onLogAdded("  - Location: ${event.location}")
        kotlinx.coroutines.delay(700)
        onLogAdded("[CALENDAR] Calling POST v3/calendars/primary/events...")
        kotlinx.coroutines.delay(800)
        onLogAdded("[CALENDAR] Google API Response: 200 OK. Event inserted successfully! ID: gcal_${event.id}")
        return true
    }

    // 2. Real local system integration: Launch intent to add to real on-device Google Calendar
    fun addToDeviceCalendar(context: Context, event: Event) {
        val startTimeMs = parseIso8601(event.startTime)
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, event.title)
            putExtra(CalendarContract.Events.EVENT_LOCATION, event.location)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTimeMs)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, startTimeMs + 3600000) // 1 hour duration
            putExtra(CalendarContract.Events.DESCRIPTION, "Parsed & scheduled by Dead-Saver AI. Category: ${event.taskCategory}, Priority: ${event.priorityScore}/10")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No Calendar application found on device.", Toast.LENGTH_SHORT).show()
        }
    }
}
