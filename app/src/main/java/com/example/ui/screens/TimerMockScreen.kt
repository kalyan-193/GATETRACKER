package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.StudyViewModel
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.theme.*

@Composable
fun TimerMockScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTimerMode by remember { mutableStateOf("Pomodoro") } // "Pomodoro" or "Mock"

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
                    text = "Focus Lounge",
                    color = CyberSlateText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Timer & Mock Simulator",
                    color = CosmicWhiteText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
            }
        }

        // Timer mode tab selector
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.04f))
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTimerMode == "Pomodoro") ElectricBlue else Color.Transparent)
                        .clickable { selectedTimerMode = "Pomodoro" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Pomodoro Timer",
                        color = if (selectedTimerMode == "Pomodoro") CosmicWhiteText else CyberSlateText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTimerMode == "Mock") ElectricBlue else Color.Transparent)
                        .clickable { selectedTimerMode = "Mock" }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Timed Mock Simulator",
                        color = if (selectedTimerMode == "Mock") CosmicWhiteText else CyberSlateText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (selectedTimerMode == "Pomodoro") {
            item { PomodoroTimerLayout(viewModel) }
        } else {
            item { MockExamSimulatorLayout(viewModel) }
        }
    }
}

@Composable
fun PomodoroTimerLayout(viewModel: StudyViewModel) {
    val secondsRemaining by viewModel.pomodoroSecondsRemaining.collectAsState()
    val isRunning by viewModel.isPomodoroRunning.collectAsState()
    val mode by viewModel.pomodoroMode.collectAsState()

    val minutes = secondsRemaining / 60
    val seconds = secondsRemaining % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    val progress = secondsRemaining.toFloat() / (if (mode == "Work") 25f * 60f else 5f * 60f)

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = GlassBackground.copy(alpha = 0.08f)
        ) {
            Text(
                text = if (mode == "Work") "⚡ FOCUS WORK SESSION" else "☕ SHORT REFRESHING BREAK",
                color = if (mode == "Work") ElectricBlue else BrightGreenGlow,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Beautiful glowing circular countdown meter
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .align(Alignment.CenterHorizontally),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val radius = size.width / 2f
                    // Draw inactive track
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = radius - 8f,
                        style = Stroke(width = 12f)
                    )
                    // Draw animated active clock arc
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(ElectricBlue, CyanGlow, NebulaPurple, ElectricBlue)
                        ),
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = 12f)
                    )
                }

                // Centered readable timer digits
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = timeFormatted,
                        color = CosmicWhiteText,
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1).sp
                    )
                    Text(
                        text = if (isRunning) "Deep Focus" else "Paused",
                        color = CyberSlateText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Control Trigger Button cluster
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Play / Pause Button
                GlassButton(
                    text = if (isRunning) "Pause" else "Start focus",
                    onClick = { viewModel.togglePomodoro() },
                    modifier = Modifier.weight(1f),
                    accentColor = if (isRunning) NebulaPurple else ElectricBlue,
                    testTag = "btn_toggle_pomodoro"
                )

                // Reset Button
                GlassButton(
                    text = "Reset",
                    onClick = { viewModel.resetPomodoro() },
                    modifier = Modifier.weight(1f),
                    accentColor = Color.White.copy(alpha = 0.1f),
                    testTag = "btn_reset_pomodoro"
                )
            }
        }

        // Daily Motivational Quotes card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = DarkGlassBackground
        ) {
            Text(
                text = "DAILY RE-CHARGE",
                color = WarmAmberGold,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "\"The best way to predict your GATE score is to write it down in your habits every single day.\"",
                color = CosmicWhiteText.copy(alpha = 0.9f),
                fontSize = 13.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "— GATE EE Cockpit Mentor",
                color = CyberSlateText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun MockExamSimulatorLayout(viewModel: StudyViewModel) {
    val context = LocalContext.current
    val secondsRemaining by viewModel.mockExamSecondsRemaining.collectAsState()
    val isRunning by viewModel.isMockExamRunning.collectAsState()
    val examStatus by viewModel.mockExamStatus.collectAsState()

    val attempted by viewModel.mockQuestionsAttempted.collectAsState()
    val correct by viewModel.mockCorrectAnswers.collectAsState()
    val scoreStr by viewModel.mockScore.collectAsState()
    val remarks by viewModel.mockRemarks.collectAsState()

    val hours = secondsRemaining / 3600
    val minutes = (secondsRemaining % 3600) / 60
    val seconds = secondsRemaining % 60
    val formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = GlassBackground.copy(alpha = 0.08f)
        ) {
            Text(
                text = "GATE 3-HOUR MOCK EXAM",
                color = CyanGlow,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(18.dp))

            // Timer visualization
            Text(
                text = formattedTime,
                color = CosmicWhiteText,
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            when (examStatus) {
                "NotStarted" -> {
                    Text(
                        text = "Simulate a realistic 3-hour GATE environment. Set up your sheets, shut down notifications, and tap below to start.",
                        color = CyberSlateText,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    GlassButton(
                        text = "Begin 3-Hr Mock",
                        onClick = { viewModel.startMockExam() },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "btn_start_mock"
                    )
                }
                "Running" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlassButton(
                            text = "Finish Mock Early",
                            onClick = { viewModel.finishMockExam() },
                            modifier = Modifier.fillMaxWidth(),
                            accentColor = VividRedAlert,
                            testTag = "btn_stop_mock"
                        )
                    }
                }
                "Finished" -> {
                    Text(
                        text = "🎉 Mock Exam Completed! Please log your outcomes below.",
                        color = BrightGreenGlow,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Feedback form inputs
                    GlassTextField(
                        value = attempted,
                        onValueChange = { viewModel.mockQuestionsAttempted.value = it },
                        label = "Questions Attempted",
                        isNumberOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "e.g. 55"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    GlassTextField(
                        value = correct,
                        onValueChange = { viewModel.mockCorrectAnswers.value = it },
                        label = "Correct Answers",
                        isNumberOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "e.g. 42"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    GlassTextField(
                        value = scoreStr,
                        onValueChange = { viewModel.mockScore.value = it },
                        label = "Final Score (Out of 100)",
                        isNumberOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "e.g. 68.5"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    GlassTextField(
                        value = remarks,
                        onValueChange = { viewModel.mockRemarks.value = it },
                        label = "Remarks / Difficult Topics",
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = "Power Systems transients felt tricky..."
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GlassButton(
                            text = "Submit & Log",
                            onClick = {
                                val scoreVal = scoreStr.toDoubleOrNull() ?: 0.0
                                val attVal = attempted.toIntOrNull() ?: 0
                                val corVal = correct.toIntOrNull() ?: 0
                                viewModel.submitMockFeedback(attVal, corVal, scoreVal, remarks)
                                Toast.makeText(context, "Mock results successfully committed to logs and syllabus progress!", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.weight(1f),
                            accentColor = BrightGreenGlow,
                            testTag = "btn_submit_feedback_mock"
                        )

                        GlassButton(
                            text = "Discard",
                            onClick = { viewModel.resetMockExam() },
                            modifier = Modifier.weight(1f),
                            accentColor = Color.White.copy(alpha = 0.1f),
                            testTag = "btn_discard_mock"
                        )
                    }
                }
            }
        }
    }
}
