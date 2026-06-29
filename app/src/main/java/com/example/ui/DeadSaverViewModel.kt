package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.api.*
import com.example.data.*
import com.example.util.CalendarHelper
import com.example.util.NotificationHelper
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

data class UserProfile(
    val email: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

class DeadSaverViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = EventRepository(database.eventDao(), database.integrationDao())

    val allEvents: StateFlow<List<Event>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allIntegrations: StateFlow<List<Integration>> = repository.allIntegrations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile = MutableStateFlow<UserProfile?>(null)
    val terminalLogs = MutableStateFlow<List<String>>(emptyList())
    val isParsing = MutableStateFlow(false)
    val selectedEvent = MutableStateFlow<Event?>(null)

    val draftResult = MutableStateFlow<ExtensionDraftResponse?>(null)
    val isDrafting = MutableStateFlow(false)

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val parsedEventAdapter = moshi.adapter(ParsedEvent::class.java)
    private val extensionDraftAdapter = moshi.adapter(ExtensionDraftResponse::class.java)

    init {
        // Prepopulate integrations and sample events if empty
        viewModelScope.launch(Dispatchers.IO) {
            addTerminalLog("[SYSTEM] Dead-Saver Autonomous Core Initialized.")
            addTerminalLog("[SYSTEM] Ready to intercept deadlines.")

            // Setup mock user by default to speed up demo
            userProfile.value = UserProfile("likhilgowda89@gmail.com", "Likhil Gowda")

            // Wait a moment for flow collection
            kotlinx.coroutines.delay(500)

            // Check integrations
            repository.allIntegrations.collect { list ->
                if (list.isEmpty()) {
                    repository.insertIntegration(Integration("google", true, "likhilgowda89@gmail.com"))
                    repository.insertIntegration(Integration("outlook", false, "Not Connected"))
                    repository.insertIntegration(Integration("github", true, "likhil_github"))
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            repository.allEvents.collect { list ->
                if (list.isEmpty()) {
                    // Populate initial sample events as structured in prompt
                    val now = System.currentTimeMillis()
                    val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                    val tomorrow = format.format(Date(now + 24 * 3600 * 1000))
                    val nextWeek = format.format(Date(now + 7 * 24 * 3600 * 1000))
                    val yesterday = format.format(Date(now - 12 * 3600 * 1000))

                    repository.insertEvent(Event(
                        userId = "likhilgowda89",
                        title = "Critical Production Bug Hotfix",
                        source = "GitHub",
                        startTime = tomorrow,
                        location = "https://github.com/org/repo/issues/402",
                        status = "pending",
                        priorityScore = 9,
                        taskCategory = "Coding"
                    ))

                    repository.insertEvent(Event(
                        userId = "likhilgowda89",
                        title = "Unstop National Coding Challenge",
                        source = "Unstop",
                        startTime = nextWeek,
                        location = "https://unstop.com/hackathons/national-coding",
                        status = "pending",
                        priorityScore = 6,
                        taskCategory = "Coding"
                    ))

                    repository.insertEvent(Event(
                        userId = "likhilgowda89",
                        title = "Executive Whitepaper Pitch Draft",
                        source = "Workspace Mail",
                        startTime = yesterday, // overdue task!
                        location = "Google Docs Folder #9",
                        status = "pending",
                        priorityScore = 8,
                        taskCategory = "Writing"
                    ))

                    repository.insertEvent(Event(
                        userId = "likhilgowda89",
                        title = "Quarterly Compliance Audit Submission",
                        source = "Eventbrite / Admin Inbox",
                        startTime = tomorrow,
                        location = "Main Conference Room / Remote",
                        status = "resolved",
                        priorityScore = 4,
                        taskCategory = "Admin"
                    ))
                }
            }
        }
    }

    fun addTerminalLog(message: String) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val formatted = "[$timestamp] $message"
        val current = terminalLogs.value.toMutableList()
        current.add(0, formatted) // newest logs first
        if (current.size > 100) {
            current.removeAt(current.size - 1)
        }
        terminalLogs.value = current
    }

    fun loginWithGoogle(email: String, name: String) {
        userProfile.value = UserProfile(email, name)
        addTerminalLog("[AUTH] Signed in successfully as $name.")
    }

    fun logout() {
        userProfile.value = null
        addTerminalLog("[AUTH] Signed out of Dead-Saver.")
    }

    fun toggleIntegration(id: String, email: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val isConnectedNow = !isIntegrationConnected(id)
            val accountName = if (isConnectedNow) email else "Not Connected"
            repository.insertIntegration(Integration(id, isConnectedNow, accountName))
            addTerminalLog("[INTEGRATION] Toggle: $id -> ${if (isConnectedNow) "CONNECTED ($accountName)" else "DISCONNECTED"}")
        }
    }

    private fun isIntegrationConnected(id: String): Boolean {
        return allIntegrations.value.find { it.id == id }?.isConnected ?: false
    }

    // Parse email using Gemini 3.5 Flash direct REST API
    fun parseEmailInbox(rawEmailText: String) {
        if (rawEmailText.isBlank()) return
        isParsing.value = true
        addTerminalLog("[AGENT] Intercepting raw email stream blob...")

        viewModelScope.launch(Dispatchers.IO) {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
                withContext(Dispatchers.Main) {
                    addTerminalLog("[ERROR] API Key is missing! Configure GEMINI_API_KEY in Secrets Panel.")
                    isParsing.value = false
                }
                // Fallback demo mode if no key is set
                simulateSuccessfulParsingFallback(rawEmailText)
                return@launch
            }

            val systemInstructionText = "You are an elite Operations Triage Agent. Strip all conversational filler. Extract explicit or strongly implied deadlines, event titles, time windows, and locations from raw email content."

            val prompt = """
                Parse the following email content and extract details of the event or deadline. 
                Format your response as a valid JSON object matching this exact schema:
                {
                  "title": "String (Clear, concise event/task title)",
                  "source_platform": "String (e.g., Unstop, Eventbrite, GitHub, Workspace Mail, Gmail)",
                  "start_time": "ISO 8601 String representing the exact or implied start time or deadline",
                  "location": "String (URL, physical address, or 'Remote' if unspecified)",
                  "priority_score": Number (1 to 10 based on urgency and consequences)",
                  "task_category": "Coding" | "Writing" | "Admin" (Pick the most matching category)
                }
                
                Email content:
                $rawEmailText
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                generationConfig = GeminiGenerationConfig(
                    responseMimeType = "application/json",
                    temperature = 0.2f
                ),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = systemInstructionText)))
            )

            try {
                addTerminalLog("[AGENT] Sending email payload to Gemini 3.5 Flash...")
                val response = GeminiClient.apiService.generateContent(apiKey, request)
                val jsonResponse = GeminiClient.getResponseText(response)

                if (jsonResponse != null) {
                    addTerminalLog("[AGENT] Received triage payload from Gemini.")
                    // Strip markdown wrapping if model returns any ```json ... ```
                    val cleanedJson = cleanJsonString(jsonResponse)
                    val parsed = parsedEventAdapter.fromJson(cleanedJson)

                    if (parsed != null) {
                        val newEvent = Event(
                            userId = userProfile.value?.email ?: "guest",
                            title = parsed.title,
                            source = parsed.source_platform,
                            startTime = parsed.start_time,
                            location = parsed.location,
                            status = "pending",
                            priorityScore = parsed.priority_score,
                            taskCategory = parsed.task_category
                        )
                        repository.insertEvent(newEvent)
                        addTerminalLog("[AGENT] Created new triage item: '${newEvent.title}' (Priority ${newEvent.priorityScore}/10)")

                        // Trigger notifications
                        withContext(Dispatchers.Main) {
                            NotificationHelper.triggerNotification(
                                getApplication(),
                                newEvent.hashCode(),
                                "⚠️ Dead-Saver Alert: ${newEvent.title}",
                                "New ${newEvent.taskCategory} task flagged from ${newEvent.source}. Priority score: ${newEvent.priorityScore}/10"
                            )
                        }

                        // Automatically sync to Calendar stub if connected
                        if (isIntegrationConnected("google")) {
                            CalendarHelper.syncGoogleCalendarSimulated(newEvent) { log ->
                                addTerminalLog(log)
                            }
                        }
                    } else {
                        addTerminalLog("[ERROR] Failed to map JSON schema into Event object.")
                    }
                } else {
                    addTerminalLog("[ERROR] Empty response from Gemini API.")
                }
            } catch (e: Exception) {
                addTerminalLog("[ERROR] Connection failure: ${e.message}")
                simulateSuccessfulParsingFallback(rawEmailText)
            } finally {
                isParsing.value = false
            }
        }
    }

    private fun cleanJsonString(json: String): String {
        return json.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }

    private suspend fun simulateSuccessfulParsingFallback(rawText: String) {
        addTerminalLog("[DEMO-MODE] Simulating local heuristic parsing parsing for email:")
        addTerminalLog("  - \"${rawText.take(40)}...\"")
        kotlinx.coroutines.delay(800)

        // Parse key attributes heuristically to make the mock smart
        val title = when {
            rawText.contains("hackathon", true) -> "Hackathon Challenge Registration"
            rawText.contains("bug", true) || rawText.contains("crash", true) -> "Emergency Server Fix"
            rawText.contains("meeting", true) -> "Operations Alignment Board"
            else -> "Immediate Action Item"
        }
        val category = when {
            rawText.contains("code", true) || rawText.contains("server", true) || rawText.contains("bug", true) -> "Coding"
            rawText.contains("report", true) || rawText.contains("write", true) || rawText.contains("pitch", true) -> "Writing"
            else -> "Admin"
        }
        val source = when {
            rawText.contains("github", true) -> "GitHub"
            rawText.contains("eventbrite", true) -> "Eventbrite"
            else -> "Workspace Mail"
        }

        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val tomorrow = format.format(Date(System.currentTimeMillis() + 18 * 3600 * 1000))

        val event = Event(
            userId = userProfile.value?.email ?: "guest",
            title = title,
            source = source,
            startTime = tomorrow,
            location = "https://office.workspace.com/task",
            status = "pending",
            priorityScore = 8,
            taskCategory = category
        )

        repository.insertEvent(event)
        addTerminalLog("[AGENT] Simulated creation of: '${event.title}' (Priority ${event.priorityScore})")

        withContext(Dispatchers.Main) {
            NotificationHelper.triggerNotification(
                getApplication(),
                event.hashCode(),
                "⚠️ Dead-Saver Alert: ${event.title}",
                "Automated Triage intercepted a deadline. Priority score: ${event.priorityScore}/10"
            )
        }

        if (isIntegrationConnected("google")) {
            CalendarHelper.syncGoogleCalendarSimulated(event) { log ->
                addTerminalLog(log)
            }
        }
        isParsing.value = false
    }

    // Call draft_extension_email via Gemini 3.5 Flash
    fun draftExtensionEmail(event: Event) {
        isDrafting.value = true
        addTerminalLog("[AGENT] Contacting elite negotiator proxy...")

        viewModelScope.launch(Dispatchers.IO) {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isBlank()) {
                simulateExtensionDraftFallback(event)
                return@launch
            }

            val prompt = """
                Draft a highly professional, respectful, but urgent extension request email for:
                - Event Title: ${event.title}
                - Platform: ${event.source}
                - Deadline: ${event.startTime}
                - Category: ${event.taskCategory} (Priority score: ${event.priorityScore}/10)
                
                The user has run into unexpected technical hurdles. Draft subject and body.
                Format your response as a valid JSON object matching this exact schema:
                {
                  "subject": "Subject of the email",
                  "body": "Body of the email"
                }
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                generationConfig = GeminiGenerationConfig(
                    responseMimeType = "application/json",
                    temperature = 0.7f
                )
            )

            try {
                val response = GeminiClient.apiService.generateContent(apiKey, request)
                val jsonResponse = GeminiClient.getResponseText(response)

                if (jsonResponse != null) {
                    val cleanedJson = cleanJsonString(jsonResponse)
                    val parsed = extensionDraftAdapter.fromJson(cleanedJson)
                    if (parsed != null) {
                        draftResult.value = parsed
                        addTerminalLog("[SUCCESS] Negotiator agent drafted extension email successfully.")
                    } else {
                        addTerminalLog("[ERROR] Failed to map negotiator JSON response.")
                    }
                } else {
                    addTerminalLog("[ERROR] Empty negotiator response.")
                }
            } catch (e: Exception) {
                addTerminalLog("[ERROR] Negotiator API call failed: ${e.message}")
                simulateExtensionDraftFallback(event)
            } finally {
                isDrafting.value = false
            }
        }
    }

    private suspend fun simulateExtensionDraftFallback(event: Event) {
        addTerminalLog("[DEMO-MODE] Simulating extension negotiator draft locally...")
        kotlinx.coroutines.delay(1000)
        draftResult.value = ExtensionDraftResponse(
            subject = "Extension Request: [URGENT] ${event.title}",
            body = "Dear Operations Team,\n\nI am writing to formally request a brief extension for the '${event.title}' task. " +
                    "Due to unforeseen technical constraints and pipeline alignment blockers, we require an additional 48 hours to ensure the delivery meets high production standards.\n\n" +
                    "We are actively coordinating to resolve the blocker. Thank you for your leadership and understanding.\n\n" +
                    "Best regards,\n${userProfile.value?.name ?: "Operations Lead"}"
        )
        addTerminalLog("[SUCCESS] Generated extension proposal draft locally.")
        isDrafting.value = false
    }

    fun markEventAsResolved(event: Event) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = event.copy(status = "resolved")
            repository.updateEvent(updated)
            addTerminalLog("[SYSTEM] Status changed: '${event.title}' is now RESOLVED.")
            if (selectedEvent.value?.id == event.id) {
                selectedEvent.value = updated
            }
        }
    }

    fun bulkMarkAsResolved(eventsToResolve: List<Event>) {
        viewModelScope.launch(Dispatchers.IO) {
            eventsToResolve.forEach { event ->
                val updated = event.copy(status = "resolved")
                repository.updateEvent(updated)
            }
            addTerminalLog("[SYSTEM] Bulk Status Update: Resolved ${eventsToResolve.size} tasks.")
            val currentSelected = selectedEvent.value
            if (currentSelected != null && eventsToResolve.any { it.id == currentSelected.id }) {
                selectedEvent.value = currentSelected.copy(status = "resolved")
            }
        }
    }

    fun deleteEvent(event: Event) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteEvent(event)
            addTerminalLog("[SYSTEM] Purged task: '${event.title}'.")
            if (selectedEvent.value?.id == event.id) {
                selectedEvent.value = null
            }
        }
    }

    fun bulkDeleteEvents(eventsToDelete: List<Event>) {
        viewModelScope.launch(Dispatchers.IO) {
            eventsToDelete.forEach { event ->
                repository.deleteEvent(event)
            }
            addTerminalLog("[SYSTEM] Bulk Purge: Deleted ${eventsToDelete.size} tasks.")
            val currentSelected = selectedEvent.value
            if (currentSelected != null && eventsToDelete.any { it.id == currentSelected.id }) {
                selectedEvent.value = null
            }
        }
    }

    fun clearAllTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteAllEvents()
            addTerminalLog("[SYSTEM] Purged entire local crisis buffer queue.")
            selectedEvent.value = null
        }
    }

    // Flag all tasks due in 24 hours and trigger bulk extension emails
    fun panicAction() {
        viewModelScope.launch(Dispatchers.IO) {
            addTerminalLog("[⚠️ PANIC ENGINE] Initiating 24H Deadline Lockout Sequence...")
            val now = System.currentTimeMillis()
            val eventsList = allEvents.value
            var flaggedCount = 0

            eventsList.forEach { event ->
                val timeRemaining = CalendarHelper.parseIso8601(event.startTime) - now
                if (event.status == "pending" && timeRemaining in 0..(24 * 3600 * 1000)) {
                    flaggedCount++
                    addTerminalLog("[PANIC] Flagged near-deadline task: '${event.title}'")
                    // Automatically trigger draft extension
                    draftExtensionEmail(event)
                    kotlinx.coroutines.delay(300)
                }
            }

            if (flaggedCount == 0) {
                addTerminalLog("[PANIC] System Scan Complete: No pending tasks found due within 24 hours.")
            } else {
                addTerminalLog("[PANIC] Engine triggered drafts for $flaggedCount critical tasks.")
                withContext(Dispatchers.Main) {
                    NotificationHelper.triggerNotification(
                        getApplication(),
                        9999,
                        "🚨 PANIC LOCKOUT ACTIVE",
                        "Dead-Saver flagged $flaggedCount imminent deadlines and prepared negotiation drafts!"
                    )
                }
            }
        }
    }
}
