package com.example.ui

import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Event
import com.example.data.Integration
import com.example.api.ExtensionDraftResponse
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import com.example.ui.theme.*
import com.example.util.CalendarHelper
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DeadSaverViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val events by viewModel.allEvents.collectAsState()
    val integrations by viewModel.allIntegrations.collectAsState()
    val logs by viewModel.terminalLogs.collectAsState()
    val isParsing by viewModel.isParsing.collectAsState()
    val isDrafting by viewModel.isDrafting.collectAsState()
    val selectedEvent by viewModel.selectedEvent.collectAsState()
    val draftResult by viewModel.draftResult.collectAsState()

    var showEmailParserDialog by remember { mutableStateOf(false) }
    var emailInputText by remember { mutableStateOf("") }
    var showDraftDialog by remember { mutableStateOf(false) }
    var bulkSelectedIds by remember { mutableStateOf(setOf<Int>()) }

    // Request permissions for notifications
    var hasNotificationPermission by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else {
            hasNotificationPermission = true
        }
    }

    // Determine layout columns based on width
    val configuration = LocalConfiguration.current
    val isExpanded = configuration.screenWidthDp >= 800

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = "Crisis Icon",
                            tint = CyberTeal,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "DEAD-SAVER",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            color = CyberTeal
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberRed.copy(alpha = 0.2f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ZERO-INPUT CORE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberRed,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                },
                actions = {
                    userProfile?.let { user ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = user.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyberTextPrimary
                                )
                                Text(
                                    text = user.email,
                                    fontSize = 10.sp,
                                    color = CyberTextSecondary
                                )
                            }
                            IconButton(
                                onClick = { viewModel.logout() },
                                modifier = Modifier.testTag("logout_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Logout,
                                    contentDescription = "Logout",
                                    tint = CyberTextSecondary
                                )
                            }
                        }
                    } ?: run {
                        Button(
                            onClick = {
                                viewModel.loginWithGoogle("likhilgowda89@gmail.com", "Likhil Gowda")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberTeal),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("login_button")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AccountCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text("Sign In with Google", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CyberDarkBg,
                    titleContentColor = CyberTextPrimary
                )
            )
        },
        containerColor = CyberDarkBg,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Row(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Main content side
            Column(
                modifier = Modifier
                    .weight(if (isExpanded) 1.2f else 1.0f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Section: Header status banner
                HeaderStatusBanner(events = events, onParseClick = {
                    showEmailParserDialog = true
                })

                // Section: Terminal Log monitor
                TerminalMonitor(logs = logs)

                // Master Panic Button Row (Panel D)
                PanicControlPanel(onPanicClick = {
                    viewModel.panicAction()
                })

                val onToggleSelectEvent = { id: Int ->
                    bulkSelectedIds = if (bulkSelectedIds.contains(id)) {
                        bulkSelectedIds - id
                    } else {
                        bulkSelectedIds + id
                    }
                }
                val onSelectAll = { list: List<Event> ->
                    bulkSelectedIds = list.map { it.id }.toSet()
                }
                val onClearSelection = {
                    bulkSelectedIds = emptySet()
                }
                val onBulkResolve = {
                    val selectedEvents = events.filter { bulkSelectedIds.contains(it.id) }
                    viewModel.bulkMarkAsResolved(selectedEvents)
                    bulkSelectedIds = emptySet()
                }
                val onBulkArchive = {
                    val selectedEvents = events.filter { bulkSelectedIds.contains(it.id) }
                    viewModel.bulkDeleteEvents(selectedEvents)
                    bulkSelectedIds = emptySet()
                }

                if (isExpanded) {
                    // Wide Screen: Render Triage Queue (Panel B) and Timeline Grid (Panel A) Side-by-Side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            SectionHeader(title = "Triage Queue (Panel B)", subtitle = "Sorted by Gemini Priority Score")
                            TriageQueueList(
                                events = events.sortedByDescending { it.priorityScore },
                                selectedEvent = selectedEvent,
                                onEventSelect = { viewModel.selectedEvent.value = it },
                                bulkSelectedIds = bulkSelectedIds,
                                onToggleSelectEvent = onToggleSelectEvent,
                                onSelectAll = onSelectAll,
                                onClearSelection = onClearSelection,
                                onBulkResolve = onBulkResolve,
                                onBulkArchive = onBulkArchive
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            SectionHeader(title = "Unified Timeline Grid (Panel A)", subtitle = "Chronological External Feed")
                            TimelineGridList(
                                events = events.sortedBy { it.startTime },
                                selectedEvent = selectedEvent,
                                onEventSelect = { viewModel.selectedEvent.value = it }
                            )
                        }
                    }
                } else {
                    // Portrait Mobile: Tabbed View for Panel A and Panel B
                    var selectedTab by remember { mutableIntStateOf(0) }
                    Column {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = CyberDarkBg,
                            contentColor = CyberTeal
                        ) {
                            Tab(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                text = { Text("Triage Queue (Panel B)", fontWeight = FontWeight.Bold) }
                            )
                            Tab(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                text = { Text("Timeline Grid (Panel A)", fontWeight = FontWeight.Bold) }
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        if (selectedTab == 0) {
                            TriageQueueList(
                                events = events.sortedByDescending { it.priorityScore },
                                selectedEvent = selectedEvent,
                                onEventSelect = { viewModel.selectedEvent.value = it },
                                bulkSelectedIds = bulkSelectedIds,
                                onToggleSelectEvent = onToggleSelectEvent,
                                onSelectAll = onSelectAll,
                                onClearSelection = onClearSelection,
                                onBulkResolve = onBulkResolve,
                                onBulkArchive = onBulkArchive
                            )
                        } else {
                            TimelineGridList(
                                events = events.sortedBy { it.startTime },
                                selectedEvent = selectedEvent,
                                onEventSelect = { viewModel.selectedEvent.value = it }
                            )
                        }
                    }
                }

                // Panel E: Integrations Dashboard
                IntegrationsCenter(
                    integrations = integrations,
                    onToggle = { id, email -> viewModel.toggleIntegration(id, email) }
                )
            }

            // Expanded Mode Side-Drawer or Dynamic Detail Drawer (Panel C: Contextual Toolbelt Tray)
            if (isExpanded || selectedEvent != null) {
                AnimatedVisibility(
                    visible = selectedEvent != null,
                    enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                    exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                    modifier = Modifier
                        .width(if (isExpanded) 360.dp else 300.dp)
                        .fillMaxHeight()
                        .background(CyberCardBg)
                        .border(1.dp, CyberCardBorder)
                ) {
                    selectedEvent?.let { event ->
                        ContextualToolbeltTray(
                            event = event,
                            draftResult = draftResult,
                            isDrafting = isDrafting,
                            onClose = { viewModel.selectedEvent.value = null },
                            onMarkResolved = { viewModel.markEventAsResolved(event) },
                            onDelete = { viewModel.deleteEvent(event) },
                            onDraftExtension = {
                                viewModel.draftExtensionEmail(event)
                                showDraftDialog = true
                            },
                            onSyncToCalendar = {
                                CalendarHelper.addToDeviceCalendar(context, event)
                            }
                        )
                    }
                }
            }
        }
    }

    // Modal Email Input Dialog (Zero-Input Parsing Gateway)
    if (showEmailParserDialog) {
        AlertDialog(
            onDismissRequest = { showEmailParserDialog = false },
            containerColor = CyberCardBg,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Mail, contentDescription = null, tint = CyberTeal)
                    Text("Parse Raw Email Inbox Blob", fontFamily = FontFamily.Monospace, color = CyberTeal, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Paste a raw confirmation email, deadline notice, or webhook text. Gemini AI will automatically extract and map metadata.",
                        color = CyberTextSecondary,
                        fontSize = 12.sp
                    )
                    OutlinedTextField(
                        value = emailInputText,
                        onValueChange = { emailInputText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .testTag("email_input_field"),
                        placeholder = { Text("Paste raw email here...", color = CyberTextSecondary.copy(alpha = 0.5f)) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberTeal,
                            unfocusedBorderColor = CyberCardBorder,
                            focusedTextColor = CyberTextPrimary,
                            unfocusedTextColor = CyberTextPrimary
                        ),
                        textStyle = TextStyle(fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    )
                    // Quick pre-filled prompt chips
                    Text("Select Sample Email Templates:", fontSize = 10.sp, color = CyberTeal, fontWeight = FontWeight.Bold)
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SamplePromptChip("GitHub CI Failure") {
                            emailInputText = "From: GitHub Notifications <noreply@github.com>\nSubject: [GitHub] Workflow run failed: main (build.yml)\n\nDear engineer,\nYour workflow build.yml failed at 10:14 PM UTC. It says a critical bug crashed the dev compilation bundle. Please review and deploy a hotfix to https://github.com/org/repo/issues/402. The deadline is before the next release cycle tomorrow at 12:00 PM."
                        }
                        SamplePromptChip("Unstop Challenge") {
                            emailInputText = "From: Unstop <support@unstop.com>\nSubject: National Coding Challenge: Coding Round Imminent\n\nHello Likhil!\nYour registration is confirmed. The competitive coding round starts on July 5, 2026, at 3:00 PM. This is a proctored test. Main hub will be remote. Do not miss it!"
                        }
                        SamplePromptChip("Executive Pitch Draft") {
                            emailInputText = "From: VP of Operations <vp@company.com>\nSubject: Urgent: Whitepaper Project Executive Pitch Draft\n\nHi team, we need a complete draft of the market expansion executive whitepaper. It must be finalized on Google Docs Folder #9 by June 27, 2026. This is essential for the board review."
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.parseEmailInbox(emailInputText)
                        showEmailParserDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberTeal),
                    modifier = Modifier.testTag("submit_parse_button")
                ) {
                    if (isParsing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.Black)
                    } else {
                        Text("Parse & Triage")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showEmailParserDialog = false }) {
                    Text("Cancel", color = CyberTextSecondary)
                }
            }
        )
    }

    // Negotiator Email Extension Proposal Draft View
    if (showDraftDialog && draftResult != null) {
        val draft = draftResult!!
        AlertDialog(
            onDismissRequest = { showDraftDialog = false },
            containerColor = CyberCardBg,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = null, tint = CyberGreen)
                    Text("AI Elite Negotiator Email Draft", fontFamily = FontFamily.Monospace, color = CyberGreen, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Formulated via Gemini 3.5 Flash tools based on deadline context:", fontSize = 11.sp, color = CyberTextSecondary)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CyberCardBorder, RoundedCornerShape(4.dp))
                            .background(CyberDarkBg)
                            .padding(12.dp)
                    ) {
                        Text("Subject: ${draft.subject}", fontWeight = FontWeight.Bold, color = CyberTeal, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = CyberCardBorder)
                        Box(modifier = Modifier.height(200.dp).verticalScroll(rememberScrollState())) {
                            Text(draft.body, color = CyberTextPrimary, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Action to copy body to clipboard
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Extension Draft", draft.body)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Copied draft to clipboard!", Toast.LENGTH_SHORT).show()
                        showDraftDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGreen)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Text("Copy Draft")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showDraftDialog = false }) {
                    Text("Close", color = CyberTextSecondary)
                }
            }
        )
    }
}

@Composable
fun SamplePromptChip(name: String, onClick: () -> Unit) {
    SuggestionChip(
        onClick = onClick,
        label = { Text(name, fontSize = 11.sp) },
        colors = SuggestionChipDefaults.suggestionChipColors(
            labelColor = CyberTeal,
            containerColor = CyberCardBorder.copy(alpha = 0.3f)
        )
    )
}

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = title.uppercase(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = CyberTeal
        )
        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = CyberTextSecondary
        )
    }
}

// Visual Polish component: Upper Header Status summary
@Composable
fun HeaderStatusBanner(
    events: List<Event>,
    onParseClick: () -> Unit
) {
    val pendingCount = events.count { it.status == "pending" }
    val urgentCount = events.count { it.status == "pending" && it.priorityScore >= 8 }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(CyberCardBg, Color(0xFF0F0F11))
                )
            )
            .border(1.dp, CyberCardBorder, RoundedCornerShape(24.dp))
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "CRISIS OPERATIONS BUFFER",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CyberTextSecondary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (pendingCount > 0) CyberOrange else CyberGreen, CircleShape)
                        )
                        Text(
                            text = "$pendingCount Active",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = CyberTextPrimary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(if (urgentCount > 0) CyberRed else CyberGreen, CircleShape)
                        )
                        Text(
                            text = "$urgentCount Extreme Priority",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextSecondary
                        )
                    }
                }
            }
            Button(
                onClick = onParseClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CyberTeal, contentColor = Color.Black),
                modifier = Modifier.testTag("open_parser_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Filled.Input, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Triage Email", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

// Visual Polish component: Terminal Diagnostic feed
@Composable
fun TerminalMonitor(logs: List<String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF030303)),
        border = BorderStroke(1.dp, CyberCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).background(CyberGreen, CircleShape))
                    Text(
                        text = "DEAD-SAVER AUTONOMOUS LOGS",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CyberGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = "STATUS: ENGAGED",
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    color = CyberGreen
                )
            }
            Divider(modifier = Modifier.padding(vertical = 6.dp), color = CyberCardBorder)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Column {
                    if (logs.isEmpty()) {
                        Text(
                            text = "> Monitoring crisis buffers... Standby.",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = CyberTextSecondary
                        )
                    } else {
                        logs.forEach { log ->
                            Text(
                                text = "> $log",
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = if (log.contains("[ERROR]")) CyberRed else if (log.contains("[SUCCESS]") || log.contains("[SYSTEM]")) CyberGreen else CyberTextPrimary,
                                modifier = Modifier.padding(bottom = 2.dp)
                              )
                        }
                    }
                }
            }
        }
    }
}

// Imminent Panic Sequence Dashboard (Panel D)
@Composable
fun PanicControlPanel(onPanicClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, CyberRed.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0x33EF4444), Color(0xFF050505))
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = "Panic Trigger",
                        tint = CyberRed,
                        modifier = Modifier.size(36.dp)
                    )
                    Column {
                        Text(
                            text = "ACTIVE CRISIS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = CyberRed
                        )
                        Text(
                            text = "Execute panic protocol to initiate negotiator assistants for 24h imminent deadlines.",
                            fontSize = 12.sp,
                            color = CyberTextSecondary,
                            lineHeight = 15.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onPanicClick,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberRed, contentColor = Color.White),
                    modifier = Modifier
                        .testTag("panic_button")
                        .height(48.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text("PANIC PROTOCOL", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// Panel B: Triage Queue list view
@Composable
fun TriageQueueList(
    events: List<Event>,
    selectedEvent: Event?,
    onEventSelect: (Event) -> Unit,
    bulkSelectedIds: Set<Int>,
    onToggleSelectEvent: (Int) -> Unit,
    onSelectAll: (List<Event>) -> Unit,
    onClearSelection: () -> Unit,
    onBulkResolve: () -> Unit,
    onBulkArchive: () -> Unit
) {
    if (events.isEmpty()) {
        EmptyQueuePlaceholder()
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            TriageSparklineDistributionCard(events = events)

            // Bulk Actions Bar (Shown when 1 or more are selected)
            AnimatedVisibility(
                visible = bulkSelectedIds.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberDarkBg),
                    border = BorderStroke(1.dp, CyberTeal.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${bulkSelectedIds.size} Selected",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = CyberTeal,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "|",
                                color = CyberCardBorder,
                                fontSize = 12.sp
                            )
                            val allTargetIds = events.map { it.id }.toSet()
                            val isAllSelected = bulkSelectedIds.containsAll(allTargetIds)
                            
                            Text(
                                text = if (isAllSelected) "Deselect All" else "Select All",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextSecondary,
                                modifier = Modifier
                                    .clickable {
                                        if (isAllSelected) {
                                            onClearSelection()
                                        } else {
                                            onSelectAll(events)
                                        }
                                    }
                                    .padding(vertical = 4.dp, horizontal = 6.dp)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = onBulkResolve,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberGreen.copy(alpha = 0.2f),
                                    contentColor = CyberGreen
                                ),
                                border = BorderStroke(1.dp, CyberGreen.copy(alpha = 0.6f)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.height(32.dp).testTag("bulk_resolve_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Text("Bulk Resolve", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Button(
                                onClick = onBulkArchive,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CyberRed.copy(alpha = 0.15f),
                                    contentColor = CyberRed
                                ),
                                border = BorderStroke(1.dp, CyberRed.copy(alpha = 0.5f)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier.height(32.dp).testTag("bulk_archive_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Text("Bulk Archive", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            events.forEach { event ->
                TriageItemRow(
                    event = event,
                    isSelected = selectedEvent?.id == event.id,
                    isChecked = bulkSelectedIds.contains(event.id),
                    onCheckedChange = { onToggleSelectEvent(event.id) },
                    onClick = { onEventSelect(event) }
                )
            }
        }
    }
}

// Panel A: Timeline Grid feed list
@Composable
fun TimelineGridList(
    events: List<Event>,
    selectedEvent: Event?,
    onEventSelect: (Event) -> Unit
) {
    if (events.isEmpty()) {
        EmptyQueuePlaceholder()
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            events.forEach { event ->
                TimelineItemRow(
                    event = event,
                    isSelected = selectedEvent?.id == event.id,
                    onClick = { onEventSelect(event) }
                )
            }
        }
    }
}

@Composable
fun EmptyQueuePlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = null,
            tint = CyberGreen,
            modifier = Modifier.size(48.dp)
        )
        Text("Crisis queue fully empty.", fontWeight = FontWeight.Bold, color = CyberTextPrimary, fontSize = 14.sp)
        Text("Use 'Triage Email' to parse incoming confirmation or deadline notification alerts.", color = CyberTextSecondary, fontSize = 11.sp)
    }
}

@Composable
fun TriageSparklineDistributionCard(events: List<Event>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
        border = BorderStroke(1.dp, CyberCardBorder)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PRIORITY DENSITY PROFILE",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = CyberTeal
                    )
                    Text(
                        text = "Real-time criticality distribution (Levels 1-10)",
                        fontSize = 10.sp,
                        color = CyberTextSecondary
                    )
                }
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberTeal.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    val avgPriority = if (events.isNotEmpty()) {
                        events.map { it.priorityScore }.average()
                    } else 0.0
                    Text(
                        text = "AVG: ${"%.1f".format(avgPriority)}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CyberTeal
                    )
                }
            }

            SparklineChart(
                events = events,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .padding(vertical = 4.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "L1 (LOW)",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CyberGreen.copy(alpha = 0.7f)
                )
                Text(
                    text = "L5 (MODERATE)",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CyberOrange.copy(alpha = 0.7f)
                )
                Text(
                    text = "L10 (CRITICAL)",
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CyberRed.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun SparklineChart(events: List<Event>, modifier: Modifier = Modifier) {
    val distribution = remember(events) {
        val counts = IntArray(10) { 0 }
        events.forEach { event ->
            val score = event.priorityScore.coerceIn(1, 10)
            counts[score - 1]++
        }
        counts.map { it.toFloat() }
    }

    val maxVal = distribution.maxOrNull()?.coerceAtLeast(1f) ?: 1f

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val stepX = width / (distribution.size - 1).coerceAtLeast(1)
        
        val path = Path()
        val fillPath = Path()
        
        distribution.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - (value / maxVal) * (height - 8.dp.toPx()) - 4.dp.toPx()
            if (index == 0) {
                path.moveTo(x, y)
                fillPath.moveTo(x, height)
                fillPath.lineTo(x, y)
            } else {
                val prevX = (index - 1) * stepX
                val prevY = height - (distribution[index - 1] / maxVal) * (height - 8.dp.toPx()) - 4.dp.toPx()
                val controlX1 = prevX + (x - prevX) / 3
                val controlY1 = prevY
                val controlX2 = prevX + 2 * (x - prevX) / 3
                val controlY2 = y
                
                path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
            }
        }
        fillPath.lineTo(width, height)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(CyberTeal.copy(alpha = 0.2f), Color.Transparent)
            )
        )

        drawPath(
            path = path,
            brush = Brush.horizontalGradient(
                colors = listOf(CyberGreen, CyberTeal, CyberOrange, CyberRed)
            ),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
        
        distribution.forEachIndexed { index, value ->
            if (value > 0) {
                val x = index * stepX
                val y = height - (value / maxVal) * (height - 8.dp.toPx()) - 4.dp.toPx()
                if (value == maxVal) {
                    drawCircle(
                        color = CyberRed,
                        radius = 3.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}

// Triage Row styling focusing on Priority Level (Panel B)
@Composable
fun TriageItemRow(
    event: Event,
    isSelected: Boolean,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit
) {
    val isOverdue = CalendarHelper.parseIso8601(event.startTime) < System.currentTimeMillis()
    val isResolved = event.status == "resolved"

    // Set colors based on priority score with smooth interpolation
    val priorityColor = if (isResolved) {
        CyberGreen
    } else {
        val fraction = ((event.priorityScore - 1).toFloat() / 9f).coerceIn(0f, 1f)
        if (fraction < 0.5f) {
            interpolateColor(CyberGreen, CyberOrange, fraction * 2f)
        } else {
            interpolateColor(CyberOrange, CyberRed, (fraction - 0.5f) * 2f)
        }
    }

    val startBorderColor = if (isResolved) {
        CyberGreen.copy(alpha = 0.2f)
    } else if (isSelected) {
        CyberTeal
    } else {
        priorityColor.copy(alpha = 0.25f)
    }

    val endBorderColor = if (isResolved) {
        CyberGreen.copy(alpha = 0.6f)
    } else if (isSelected) {
        priorityColor
    } else {
        priorityColor.copy(alpha = 0.75f)
    }

    val borderStrokeBrush = Brush.horizontalGradient(
        colors = listOf(startBorderColor, endBorderColor)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("triage_item_${event.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CyberCardBg.copy(alpha = 0.5f) else CyberCardBg
        ),
        border = BorderStroke(1.5.dp, borderStrokeBrush)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Multi-select Checkbox
                Checkbox(
                    checked = isChecked,
                    onCheckedChange = { onCheckedChange(it ?: false) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = CyberTeal,
                        uncheckedColor = CyberCardBorder.copy(alpha = 0.6f),
                        checkmarkColor = Color.Black
                    ),
                    modifier = Modifier.size(24.dp).testTag("triage_checkbox_${event.id}")
                )

                // Priority score badge
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(priorityColor.copy(alpha = 0.15f))
                        .border(1.dp, priorityColor, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = event.priorityScore.toString(),
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = priorityColor
                        )
                        Text(
                            text = "SCORE",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = priorityColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Title + source
                Column {
                    Text(
                        text = event.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isResolved) CyberTextSecondary else CyberTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = event.source,
                            fontSize = 11.sp,
                            color = CyberTeal
                        )
                        Text(
                            text = "•",
                            fontSize = 10.sp,
                            color = CyberTextSecondary
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyberCardBorder)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = event.taskCategory,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberTextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Deadline Status or Badges
            Column(horizontalAlignment = Alignment.End) {
                if (isResolved) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberGreen.copy(alpha = 0.15f))
                            .border(1.dp, CyberGreen, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("RESOLVED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyberGreen)
                    }
                } else if (isOverdue) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberRed.copy(alpha = 0.15f))
                            .border(1.dp, CyberRed, RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("OVERDUE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = CyberRed)
                    }
                } else {
                    val formattedTime = formatIsoToReadable(event.startTime)
                    Text(
                        text = formattedTime,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberTextPrimary
                    )
                    Text(
                        text = "Imminent",
                        fontSize = 9.sp,
                        color = CyberOrange
                    )
                }
            }
        }
    }
}

private fun interpolateColor(color1: Color, color2: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = color1.red + (color2.red - color1.red) * f,
        green = color1.green + (color2.green - color1.green) * f,
        blue = color1.blue + (color2.blue - color1.blue) * f,
        alpha = color1.alpha + (color2.alpha - color1.alpha) * f
    )
}

// @Composable - placeholder for matching the gap in edit_file target content
// Timeline Row focusing on date sequence and source platform branding (Panel A)
@Composable
fun TimelineItemRow(
    event: Event,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val formattedTime = formatIsoToReadable(event.startTime)
    val isResolved = event.status == "resolved"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("timeline_item_${event.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CyberCardBg.copy(alpha = 0.5f) else CyberCardBg
        ),
        border = BorderStroke(1.dp, if (isSelected) CyberTeal else CyberCardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Platform icon or badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(3.dp))
                            .background(CyberBlue.copy(alpha = 0.2f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = event.source.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace,
                            color = CyberBlue
                        )
                    }
                    Text(
                        text = event.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = if (isResolved) CyberTextSecondary else CyberTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = CyberTextSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Deadline: $formattedTime",
                        fontSize = 11.sp,
                        color = CyberTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Small details
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(CyberCardBorder)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = event.taskCategory,
                        fontSize = 10.sp,
                        color = CyberTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// Side-Drawer contextual actions toolbelt (Panel C)
@Composable
fun ContextualToolbeltTray(
    event: Event,
    draftResult: ExtensionDraftResponse?,
    isDrafting: Boolean,
    onClose: () -> Unit,
    onMarkResolved: () -> Unit,
    onDelete: () -> Unit,
    onDraftExtension: () -> Unit,
    onSyncToCalendar: () -> Unit
) {
    val context = LocalContext.current
    val formattedTime = formatIsoToReadable(event.startTime)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Header drawer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TOOLBELT TRAY (PANEL C)",
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = CyberTeal
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "Close Tray", tint = CyberTextSecondary)
                }
            }

            // Info Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(CyberDarkBg)
                    .border(1.dp, CyberCardBorder, RoundedCornerShape(16.dp))
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(event.title, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CyberTextPrimary)
                Divider(color = CyberCardBorder)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Outlined.Label, contentDescription = null, tint = CyberTeal, modifier = Modifier.size(14.dp))
                    Text("Category: ${event.taskCategory}", fontSize = 12.sp, color = CyberTextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Outlined.Platform, contentDescription = null, tint = CyberBlue, modifier = Modifier.size(14.dp))
                    Text("Platform: ${event.source}", fontSize = 12.sp, color = CyberTextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Outlined.Schedule, contentDescription = null, tint = CyberOrange, modifier = Modifier.size(14.dp))
                    Text("Expires: $formattedTime", fontSize = 12.sp, color = CyberTextSecondary)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = CyberRed, modifier = Modifier.size(14.dp))
                    Text(
                        text = "Location: ${event.location}",
                        fontSize = 12.sp,
                        color = CyberTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable {
                            // If it's a web link, open it!
                            if (event.location.startsWith("http")) {
                                try {
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(event.location)).apply {
                                        addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Cannot open browser link", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                }
            }

            // Contextual Utilities based on Category
            Text("CONTEXT UTILITIES", fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = CyberTextSecondary, fontWeight = FontWeight.Bold)

            if (event.taskCategory == "Coding") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Utility 1: StackOverflow intent helper
                    Button(
                        onClick = {
                            val query = "android compile ${event.title}"
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://stackoverflow.com/search?q=$query")).apply {
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberBlue),
                        modifier = Modifier.fillMaxWidth().testTag("stackoverflow_btn")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("StackOverflow Diagnostic Link", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Utility 2: Sandbox coding anchor
                    Button(
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://play.kotlinlang.org/")).apply {
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberTeal),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Open Kotlin Compiler Sandbox", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (event.taskCategory == "Writing") {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Utility 1: Text Copy action
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Title copy", event.title)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Copied title to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberTeal),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Copy Extracted Document Title", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Utility 2: Launch Google Docs
                    Button(
                        onClick = {
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://docs.google.com/")).apply {
                                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberBlue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Google Docs Cloud Drive", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else { // Admin
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Copy location details
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Location details", event.location)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Location copied!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CyberTeal),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Filled.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Copy Event Location", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action: Sync to Device Calendar (REAL ANDROID INTEGRATION!)
            Button(
                onClick = onSyncToCalendar,
                colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                modifier = Modifier.fillMaxWidth().testTag("sync_calendar_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Filled.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                    Text("Add to Device Google Calendar", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Action: Draft extension email using Gemini Negotiator
            Button(
                onClick = onDraftExtension,
                colors = ButtonDefaults.buttonColors(containerColor = CyberOrange),
                modifier = Modifier.fillMaxWidth().testTag("negotiator_draft_btn")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (isDrafting) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.Black)
                    } else {
                        Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Text("Draft Negotiation Extension", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Status Control Actions (Mark as resolved / Delete)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onMarkResolved,
                colors = ButtonDefaults.buttonColors(containerColor = CyberGreen.copy(alpha = 0.2f), contentColor = CyberGreen),
                border = BorderStroke(1.dp, CyberGreen),
                modifier = Modifier.fillMaxWidth().testTag("resolve_btn")
            ) {
                Text("Mark as Resolved", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = onDelete,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CyberRed),
                border = BorderStroke(1.dp, CyberRed.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().testTag("purge_btn")
            ) {
                Text("Purge Crisis Buffer", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Panel E: Integrations Dashboard configuration
@Composable
fun IntegrationsCenter(
    integrations: List<Integration>,
    onToggle: (String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CyberCardBg),
        border = BorderStroke(1.dp, CyberCardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(title = "Panel E: Integrations Dashboard", subtitle = "Authorize external tracking webhooks & profiles securely")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    Triple("google", "Google Workspace", "likhilgowda89@gmail.com"),
                    Triple("outlook", "Microsoft Outlook", "likhil@outlook.com"),
                    Triple("github", "GitHub Webhooks", "likhil_dev")
                ).forEach { (id, label, defaultEmail) ->
                    val integration = integrations.find { it.id == id }
                    val isConnected = integration?.isConnected ?: false

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(CyberDarkBg)
                            .border(1.dp, if (isConnected) CyberTeal.copy(alpha = 0.4f) else CyberCardBorder, RoundedCornerShape(16.dp))
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberTextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(3.dp))
                                .background(if (isConnected) CyberGreen.copy(alpha = 0.15f) else CyberCardBorder)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isConnected) "CONNECTED" else "STANDBY",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isConnected) CyberGreen else CyberTextSecondary
                            )
                        }
                        Text(
                            text = if (isConnected) (integration.accountEmail) else "Offline",
                            fontSize = 9.sp,
                            color = CyberTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Switch(
                            checked = isConnected,
                            onCheckedChange = { onToggle(id, defaultEmail) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyberTeal,
                                checkedTrackColor = CyberTeal.copy(alpha = 0.3f),
                                uncheckedThumbColor = CyberTextSecondary,
                                uncheckedTrackColor = CyberDarkBg
                            ),
                            modifier = Modifier.scale(0.8f).testTag("switch_$id")
                        )
                    }
                }
            }
        }
    }
}

// Helper: Custom implementation for missing material icons or placeholders
val Icons.Outlined.Platform: androidx.compose.ui.graphics.vector.ImageVector
    get() = Icons.Outlined.Layers

val Icons.Outlined.LocationOn: androidx.compose.ui.graphics.vector.ImageVector
    get() = Icons.Outlined.Place

// Simple ISO 8601 Parser to nice human date format
fun formatIsoToReadable(isoString: String): String {
    return try {
        val formatIn = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = formatIn.parse(isoString) ?: return isoString
        val formatOut = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
        formatOut.format(date)
    } catch (e: Exception) {
        // Fallback simple parsing
        try {
            val formatIn = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = formatIn.parse(isoString) ?: return isoString
            val formatOut = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            formatOut.format(date)
        } catch (e2: Exception) {
            isoString
        }
    }
}
