package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.StudyViewModel
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassSlider
import com.example.ui.components.GlassTextField
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StudyEntryScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val allEntries by viewModel.allEntries.collectAsState()

    // Form states
    val todayDateStr = viewModel.getTodayDateStr()
    var date by remember { mutableStateOf(todayDateStr) }
    
    // Clock selector states
    var startHour by remember { mutableFloatStateOf(9f) }
    var startMinute by remember { mutableFloatStateOf(0f) }
    var startAmPm by remember { mutableStateOf("AM") }
    var endHour by remember { mutableFloatStateOf(11f) }
    var endMinute by remember { mutableFloatStateOf(0f) }
    var endAmPm by remember { mutableStateOf("AM") }
    
    // Session Type state: "Preparation", "Video Lecture", "Other"
    var sessionType by remember { mutableStateOf("Preparation") }

    var studyHours by remember { mutableFloatStateOf(2.0f) }
    var selectedSubjects = remember { mutableStateListOf<String>() }
    var topicsCompleted by remember { mutableStateOf("") }
    var questionsSolved by remember { mutableStateOf("") }
    var pyqsSolved by remember { mutableStateOf("") }
    var mockTestsAttempted by remember { mutableStateOf("") }
    var revisionHours by remember { mutableFloatStateOf(1.0f) }
    var videoLectureHours by remember { mutableFloatStateOf(0.0f) }
    var notesPrepared by remember { mutableStateOf(false) }
    var confidence by remember { mutableFloatStateOf(5.0f) }
    var productivity by remember { mutableFloatStateOf(5.0f) }
    var remarks by remember { mutableStateOf("") }

    val gateSubjects = listOf(
        "Engineering Mathematics", "Network Theory", "Signals and Systems",
        "Control Systems", "Electrical Machines", "Power Systems",
        "Power Electronics", "Analog Electronics", "Digital Electronics",
        "Measurements", "Electromagnetic Fields"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Daily Journal",
                    color = CyberSlateText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Log Study Session",
                    color = CosmicWhiteText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
            }
        }

        // Section A: Essential Timing Details
        item {
            val calculatedHours = remember(startHour, startMinute, startAmPm, endHour, endMinute, endAmPm) {
                val sH = startHour.toInt()
                val sM = startMinute.toInt()
                val eH = endHour.toInt()
                val eM = endMinute.toInt()
                
                var start24 = if (startAmPm == "PM" && sH < 12) sH + 12 else if (startAmPm == "AM" && sH == 12) 0 else sH
                var end24 = if (endAmPm == "PM" && eH < 12) eH + 12 else if (endAmPm == "AM" && eH == 12) 0 else eH
                
                val startMins = start24 * 60 + sM
                var endMins = end24 * 60 + eM
                
                if (endMins < startMins) {
                    endMins += 24 * 60 // next day crossing
                }
                
                (endMins - startMins).toFloat() / 60f
            }

            LaunchedEffect(calculatedHours) {
                studyHours = calculatedHours
            }

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = GlassBackground.copy(alpha = 0.08f)
            ) {
                Text(
                    text = "SESSION TIME & DATE",
                    color = CyanGlow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Date Input (Read-only or styled with calendar icon)
                GlassTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = "Date (YYYY-MM-DD)",
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "yyyy-mm-dd",
                    testTag = "input_date"
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Session Type Selector
                Text(
                    text = "Session Type",
                    color = CyberSlateText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color.White.copy(alpha = 0.04f))
                        .border(1.dp, GlassBorder.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(2.dp)
                ) {
                    listOf("Preparation", "Video Lecture", "Other").forEach { type ->
                        val isSelected = sessionType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ElectricBlue.copy(alpha = 0.4f) else Color.Transparent)
                                .border(
                                    width = if (isSelected) 1.dp else 0.dp,
                                    color = if (isSelected) ElectricBlue else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { sessionType = type }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val emoji = when (type) {
                                "Preparation" -> "📖"
                                "Video Lecture" -> "🎥"
                                else -> "🛠️"
                            }
                            Text(
                                text = "$emoji $type",
                                color = if (isSelected) CosmicWhiteText else CyberSlateText,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Clock Widgets container
                Text(
                    text = "Keep a Clock (Select Start & End)",
                    color = CosmicWhiteText.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Start Time Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "START TIME",
                                color = CyberSlateText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format("%02d:%02d %s", startHour.toInt(), startMinute.toInt(), startAmPm),
                                color = CyanGlow,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Hour Slider
                        GlassSlider(
                            value = startHour,
                            onValueChange = { startHour = it },
                            valueRange = 1f..12f,
                            steps = 11,
                            label = "Hour",
                            displayValue = "${startHour.toInt()}"
                        )

                        // Minute Slider
                        GlassSlider(
                            value = startMinute,
                            onValueChange = { startMinute = it },
                            valueRange = 0f..55f,
                            steps = 11,
                            label = "Minute",
                            displayValue = String.format("%02d", startMinute.toInt())
                        )

                        // AM/PM Pills
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("AM", "PM").forEach { ampm ->
                                val selected = startAmPm == ampm
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) ElectricBlue.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.03f))
                                        .border(1.dp, if (selected) ElectricBlue else Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                        .clickable { startAmPm = ampm }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ampm,
                                        color = if (selected) CosmicWhiteText else CyberSlateText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // End Time Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "END TIME",
                                color = CyberSlateText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = String.format("%02d:%02d %s", endHour.toInt(), endMinute.toInt(), endAmPm),
                                color = NebulaPurple,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Hour Slider
                        GlassSlider(
                            value = endHour,
                            onValueChange = { endHour = it },
                            valueRange = 1f..12f,
                            steps = 11,
                            label = "Hour",
                            displayValue = "${endHour.toInt()}"
                        )

                        // Minute Slider
                        GlassSlider(
                            value = endMinute,
                            onValueChange = { endMinute = it },
                            valueRange = 0f..55f,
                            steps = 11,
                            label = "Minute",
                            displayValue = String.format("%02d", endMinute.toInt())
                        )

                        // AM/PM Pills
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf("AM", "PM").forEach { ampm ->
                                val selected = endAmPm == ampm
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) NebulaPurple.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.03f))
                                        .border(1.dp, if (selected) NebulaPurple else Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                        .clickable { endAmPm = ampm }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ampm,
                                        color = if (selected) CosmicWhiteText else CyberSlateText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Calculated Duration Indicator Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(CyanGlow.copy(alpha = 0.12f), NebulaPurple.copy(alpha = 0.12f))
                            )
                        )
                        .border(1.dp, GlassBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "⏱️", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CALCULATED DURATION",
                                color = CosmicWhiteText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Text(
                            text = String.format("%.1f Hours", studyHours),
                            color = BrightGreenGlow,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Revision Hours Slider
                GlassSlider(
                    value = revisionHours,
                    onValueChange = { revisionHours = it },
                    valueRange = 0f..8f,
                    steps = 16,
                    label = "Revision Hours",
                    displayValue = "${"%.1f".format(revisionHours)} hrs"
                )
            }
        }

        // Section B: Subject & Content Coverage
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = GlassBackground.copy(alpha = 0.08f)
            ) {
                Text(
                    text = "SUBJECTS & TOPICS",
                    color = CyanGlow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Multi-select glowing subject chips
                Text(
                    text = "Select Subjects Studied:",
                    color = CosmicWhiteText.copy(alpha = 0.8f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    gateSubjects.forEach { subject ->
                        val isSelected = selectedSubjects.contains(subject)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) ElectricBlue.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.05f)
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) ElectricBlue else Color.White.copy(alpha = 0.1f),
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    if (isSelected) {
                                        selectedSubjects.remove(subject)
                                    } else {
                                        selectedSubjects.add(subject)
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = subject,
                                color = if (isSelected) CosmicWhiteText else CyberSlateText,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                GlassTextField(
                    value = topicsCompleted,
                    onValueChange = { topicsCompleted = it },
                    label = "Topics Completed (e.g. Transient Analysis)",
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "Specify subjects topics...",
                    testTag = "input_topics"
                )
            }
        }

        // Section C: Practice Metrics
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = GlassBackground.copy(alpha = 0.08f)
            ) {
                Text(
                    text = "PRACTICE METRICS",
                    color = CyanGlow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                GlassTextField(
                    value = questionsSolved,
                    onValueChange = { questionsSolved = it },
                    label = "Total Questions Solved",
                    modifier = Modifier.fillMaxWidth(),
                    isNumberOnly = true,
                    placeholder = "0",
                    testTag = "input_questions"
                )
                Spacer(modifier = Modifier.height(12.dp))

                GlassTextField(
                    value = pyqsSolved,
                    onValueChange = { pyqsSolved = it },
                    label = "Number of PYQs Solved",
                    modifier = Modifier.fillMaxWidth(),
                    isNumberOnly = true,
                    placeholder = "0",
                    testTag = "input_pyqs"
                )
                Spacer(modifier = Modifier.height(12.dp))

                GlassTextField(
                    value = mockTestsAttempted,
                    onValueChange = { mockTestsAttempted = it },
                    label = "Mock Tests Attempted",
                    modifier = Modifier.fillMaxWidth(),
                    isNumberOnly = true,
                    placeholder = "0",
                    testTag = "input_mocks"
                )
            }
        }

        // Section D: Quality & Reflections
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = GlassBackground.copy(alpha = 0.08f)
            ) {
                Text(
                    text = "REFLECTIONS & QUALITY",
                    color = CyanGlow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Notes Prepared Toggle Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.03f))
                        .border(1.dp, GlassBorder.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Notes Prepared?", color = CosmicWhiteText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text("Did you prepare short notes?", color = CyberSlateText, fontSize = 11.sp)
                    }
                    Switch(
                        checked = notesPrepared,
                        onCheckedChange = { notesPrepared = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = CosmicWhiteText,
                            checkedTrackColor = ElectricBlue,
                            uncheckedThumbColor = CyberSlateText,
                            uncheckedTrackColor = Color.White.copy(alpha = 0.1f)
                        ),
                        modifier = Modifier.testTag("switch_notes")
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Confidence Slider
                GlassSlider(
                    value = confidence,
                    onValueChange = { confidence = it },
                    valueRange = 1f..10f,
                    steps = 8,
                    label = "Confidence Level",
                    displayValue = "${confidence.toInt()}/10"
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Productivity Slider
                GlassSlider(
                    value = productivity,
                    onValueChange = { productivity = it },
                    valueRange = 1f..10f,
                    steps = 8,
                    label = "Productivity Rating",
                    displayValue = "${productivity.toInt()}/10"
                )
                Spacer(modifier = Modifier.height(16.dp))

                GlassTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = "Daily Reflections / Remarks",
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "Write a quick reflection...",
                    testTag = "input_remarks"
                )
            }
        }

        // Submit Button
        item {
            GlassButton(
                text = "Save Daily Log",
                onClick = {
                    if (selectedSubjects.isEmpty() && topicsCompleted.isEmpty()) {
                        Toast.makeText(context, "Please enter at least a subject or a topic.", Toast.LENGTH_SHORT).show()
                    } else {
                        val videoHrs = if (sessionType == "Video Lecture") studyHours.toDouble() else 0.0
                        viewModel.addDailyEntry(
                            date = date,
                            studyHours = studyHours.toDouble(),
                            subjects = selectedSubjects.toList(),
                            topics = topicsCompleted,
                            questions = questionsSolved.toIntOrNull() ?: 0,
                            pyqs = pyqsSolved.toIntOrNull() ?: 0,
                            mocks = mockTestsAttempted.toIntOrNull() ?: 0,
                            revision = revisionHours.toDouble(),
                            videoHours = videoHrs,
                            notesPrepared = notesPrepared,
                            confidence = confidence.toInt(),
                            productivity = productivity.toInt(),
                            remarks = remarks,
                            sessionType = sessionType
                        )
                        Toast.makeText(context, "Log saved successfully! GPS score updated.", Toast.LENGTH_LONG).show()
                        
                        // Clear form state
                        selectedSubjects.clear()
                        topicsCompleted = ""
                        questionsSolved = ""
                        pyqsSolved = ""
                        mockTestsAttempted = ""
                        remarks = ""
                        sessionType = "Preparation"
                        startHour = 9f
                        startMinute = 0f
                        startAmPm = "AM"
                        endHour = 11f
                        endMinute = 0f
                        endAmPm = "AM"
                        viewModel.setActiveTab("Dashboard")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "submit_log_button"
            )
        }

        // 5. Recent Logged Sessions list
        item {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "RECENT STUDY LOGS",
                color = CyanGlow,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }

        if (allEntries.isEmpty()) {
            item {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = GlassBackground.copy(alpha = 0.03f)
                ) {
                    Text(
                        text = "No study logs recorded yet. Use the form above to log hours.",
                        color = CyberSlateText,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    )
                }
            }
        } else {
            val sortedList = allEntries.sortedByDescending { it.date }
            items(sortedList, key = { it.id }) { entry ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = GlassBackground.copy(alpha = 0.05f)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = entry.date,
                                        color = CosmicWhiteText,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val badgeEmoji = when(entry.sessionType) {
                                        "Video Lecture" -> "🎥"
                                        "Other" -> "🛠️"
                                        else -> "📖"
                                    }
                                    Box(
                                        modifier = Modifier
                                            .background(
                                                color = if (entry.sessionType == "Video Lecture") ElectricBlue.copy(alpha = 0.15f)
                                                       else if (entry.sessionType == "Other") NebulaPurple.copy(alpha = 0.15f)
                                                       else CyanGlow.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "$badgeEmoji ${entry.sessionType.uppercase()}",
                                            color = if (entry.sessionType == "Video Lecture") ElectricBlue
                                                   else if (entry.sessionType == "Other") NebulaPurple
                                                   else CyanGlow,
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                                if (entry.subjectsStudied.isNotEmpty()) {
                                    Text(
                                        text = entry.subjectsStudied,
                                        color = CyanGlow,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            IconButton(
                                onClick = { viewModel.deleteEntry(entry) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete entry",
                                    tint = VividRedAlert.copy(alpha = 0.8f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("STUDY", color = CyberSlateText, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Text("${entry.studyHours} hrs", color = CosmicWhiteText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text("PRACTICE", color = CyberSlateText, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Text("${entry.questionsSolved} Qs / ${entry.pyqsSolved} PYQs", color = CosmicWhiteText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("GPS SCORED", color = CyberSlateText, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                Text("+${entry.gpsScore}", color = BrightGreenGlow, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (entry.topicsCompleted.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Topics: ${entry.topicsCompleted}",
                                color = CosmicWhiteText.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        }

                        if (entry.remarks.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "\"${entry.remarks}\"",
                                    color = CyberSlateText.copy(alpha = 0.9f),
                                    fontSize = 10.sp,
                                    fontStyle = FontStyle.Italic
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
