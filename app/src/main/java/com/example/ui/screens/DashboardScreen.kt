package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SubjectProgress
import com.example.ui.StudyViewModel
import com.example.ui.components.GlassCard
import com.example.ui.components.LiquidGlassGauge
import com.example.ui.components.WeeklyTrendChart
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val entries by viewModel.allEntries.collectAsState()
    val gpsScore by viewModel.currentGps.collectAsState()
    val streak by viewModel.streak.collectAsState()
    val insights by viewModel.insights.collectAsState()
    val isInsightsLoading by viewModel.isInsightsLoading.collectAsState()
    val goals by viewModel.goalSettings.collectAsState()
    val subjectProgress by viewModel.subjectProgress.collectAsState()

    // Process countdown
    val targetExamDate = "2027-02-06" // GATE 2027 first Saturday
    val daysRemaining = calculateDaysRemaining(targetExamDate)

    // Process dynamic greeting based on current local time
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (currentHour) {
        in 0..11 -> "Morning"
        in 12..16 -> "Afternoon"
        else -> "Evening"
    }

    val totalStudiedHours = entries.sumOf { it.studyHours }
    val totalTargetHours = 800.0 // EE standard target hours
    val remainingTargetHours = (totalTargetHours - totalStudiedHours).coerceAtLeast(0.0)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp)
    ) {
        // 1. Welcome & High Density Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "GATE 2027 • EE",
                        color = CyanGlow.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$greeting, ",
                            color = CosmicWhiteText.copy(alpha = 0.85f),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Light,
                            fontStyle = FontStyle.Italic
                        )
                        Text(
                            text = "Kalyan",
                            color = CosmicWhiteText,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }

                // High Density Goal Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "GOAL",
                            color = CyberSlateText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "AIR ${goals.targetAirRank}",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // 2. High Density Stats Row (GPS Circular Progress + Countdown/Streak Column)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Card: GPS circular score
                GlassCard(
                    modifier = Modifier
                        .weight(1.2f)
                        .height(180.dp),
                    backgroundColor = GlassBackground.copy(alpha = 0.06f),
                    cornerRadius = 24.dp
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            LiquidGlassGauge(score = gpsScore, size = 110.dp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(BrightGreenGlow)
                                )
                                Text(
                                    text = "Silver Scholar",
                                    color = BrightGreenGlow,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }

                // Right Card Column: Countdown & Streak
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(180.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Countdown card
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        backgroundColor = GlassBackground.copy(alpha = 0.05f),
                        cornerRadius = 16.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "COUNTDOWN",
                                color = CyberSlateText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Column {
                                Text(
                                    text = "$daysRemaining",
                                    color = CosmicWhiteText,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Days Left",
                                    color = CyberSlateText,
                                    fontSize = 9.sp
                                )
                            }
                        }
                    }

                    // Streak card
                    GlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        backgroundColor = GlassBackground.copy(alpha = 0.05f),
                        cornerRadius = 16.dp
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "STREAK",
                                color = CyberSlateText,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Row(
                                verticalAlignment = Alignment.Bottom,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "$streak",
                                    color = CosmicWhiteText,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "🔥",
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(bottom = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Grid Row (2 Columns of styled KPI Progress indicators)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Column 1: Study Hours
                val averageHours = if (entries.isNotEmpty()) entries.sumOf { it.studyHours } / entries.size else 0.0
                val targetHours = if (goals.targetDailyHours > 0) goals.targetDailyHours else 6.0
                val studyRatio = (averageHours / targetHours).toFloat().coerceIn(0f, 1f)
                val studyPercent = (studyRatio * 100).toInt()
                
                GlassCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = GlassBackground.copy(alpha = 0.05f),
                    cornerRadius = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STUDY HOURS",
                            color = CyberSlateText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "+$studyPercent%",
                            color = BrightGreenGlow,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "%.1f".format(averageHours),
                            color = CosmicWhiteText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = " hrs",
                            color = CyberSlateText,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    // Sleek horizontal progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(studyRatio)
                                .background(CyanGlow, RoundedCornerShape(2.dp))
                        )
                    }
                }

                // Column 2: Questions/PYQs
                val totalPyqs = entries.sumOf { it.pyqsSolved }
                val pyqsTarget = 200 // standard milestone target
                val pyqsRatio = (totalPyqs.toFloat() / pyqsTarget).coerceIn(0f, 1f)
                val pyqsPercent = (pyqsRatio * 100).toInt()

                GlassCard(
                    modifier = Modifier.weight(1f),
                    backgroundColor = GlassBackground.copy(alpha = 0.05f),
                    cornerRadius = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "QUESTIONS",
                            color = CyberSlateText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "$pyqsPercent%",
                            color = NebulaPurple,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "$totalPyqs",
                            color = CosmicWhiteText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = " / $pyqsTarget",
                            color = CyberSlateText,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    // Sleek horizontal progress bar in purple
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(2.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(pyqsRatio)
                                .background(NebulaPurple, RoundedCornerShape(2.dp))
                        )
                    }
                }
            }
        }

        // 4. Subject Pulse Section
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = GlassBackground.copy(alpha = 0.05f),
                cornerRadius = 24.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SUBJECT PULSE",
                        color = CyberSlateText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "VIEW ALL",
                        color = CyanGlow,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { viewModel.setActiveTab("Subjects") }
                            .padding(4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))

                // Render first 3 subjects from progress or fallback defaults if database is fresh
                val displaySubjects = if (subjectProgress.isNotEmpty()) {
                    subjectProgress.take(3)
                } else {
                    listOf(
                        SubjectProgress("Electrical Machines", 88),
                        SubjectProgress("Power Systems", 42),
                        SubjectProgress("Control Systems", 65)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    displaySubjects.forEachIndexed { index, sub ->
                        val initials = getSubjectInitials(sub.subjectName)
                        val barColor = when (index % 3) {
                            0 -> Color(0xFF3B82F6) // Blue
                            1 -> Color(0xFF8B5CF6) // Purple
                            else -> Color(0xFF06B6D4) // Cyan
                        }
                        val bgGlowColor = barColor.copy(alpha = 0.15f)
                        val borderColor = barColor.copy(alpha = 0.3f)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Subject initials icon box
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(bgGlowColor, RoundedCornerShape(8.dp))
                                    .border(1.dp, borderColor, RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    color = barColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic
                                )
                            }

                            // Progress bar column
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sub.subjectName,
                                        color = CosmicWhiteText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${sub.completionPercentage}%",
                                        color = CyberSlateText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                // Custom progress bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(2.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(sub.completionPercentage / 100f)
                                            .background(
                                                brush = Brush.horizontalGradient(
                                                    colors = listOf(barColor, barColor.copy(alpha = 0.7f))
                                                ),
                                                shape = RoundedCornerShape(2.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Highlight Box (dynamic AI suggestion styled like design HTML)
                val displaySuggestion = if (insights.isNotEmpty()) {
                    "\"${insights.first()}\""
                } else {
                    "\"Your revision hours dropped by 30% this week. Focus on Power Electronics PQYs today.\""
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = displaySuggestion,
                        color = CyanGlow.copy(alpha = 0.9f),
                        fontSize = 10.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Medium,
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }

        // 5. Smart AI Suggestions Card (Full Insights view)
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = NebulaPurple.copy(alpha = 0.25f),
                backgroundColor = DarkGlassBackground,
                cornerRadius = 24.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI Suggestions",
                            tint = NebulaPurple
                        )
                        Text(
                            text = "Smart AI Suggestions",
                            color = CosmicWhiteText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Refresh Button
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Insights",
                        tint = CyanGlow,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { viewModel.generateInsights() }
                            .padding(4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                if (isInsightsLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = NebulaPurple,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    AnimatedVisibility(
                        visible = insights.isNotEmpty(),
                        enter = fadeIn() + expandVertically()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            insights.forEach { insight ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("•", color = NebulaPurple, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = insight,
                                        color = CosmicWhiteText.copy(alpha = 0.9f),
                                        fontSize = 13.sp,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                    if (insights.isEmpty()) {
                        Text(
                            text = "No insights loaded. Log a few study sessions or tap Refresh to analyze your data.",
                            color = CyberSlateText,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // 6. Weekly Study Trend Chart
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = GlassBackground.copy(alpha = 0.05f),
                cornerRadius = 24.dp
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
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Trend",
                            tint = ElectricBlue
                        )
                        Text(
                            text = "Weekly Study Trend",
                            color = CosmicWhiteText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Last 7 entries",
                        color = CyberSlateText,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))

                // Compile trend data
                val trendEntries = entries.take(7).reversed()
                val hoursList = if (trendEntries.isNotEmpty()) trendEntries.map { it.studyHours } else listOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0)
                val daysList = if (trendEntries.isNotEmpty()) trendEntries.map { it.date.substringAfterLast("-") } else listOf("", "", "", "", "", "", "")

                WeeklyTrendChart(
                    hoursList = hoursList,
                    daysList = daysList,
                    modifier = Modifier.fillMaxWidth()
                )

                if (entries.isEmpty()) {
                    Text(
                        text = "Your chart will plot once you log study entries.",
                        color = CyberSlateText,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    )
                }
            }
        }
    }
}

// Helper to extract subject name initials (e.g. Electrical Machines -> EM, Power Systems -> PS)
private fun getSubjectInitials(name: String): String {
    return name.split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()
}

// Countdown mathematics
private fun calculateDaysRemaining(targetDateStr: String): Int {
    return try {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val targetDate = sdf.parse(targetDateStr) ?: return 210
        val diff = targetDate.time - System.currentTimeMillis()
        if (diff <= 0) return 0
        (diff / (1000 * 60 * 60 * 24)).toInt()
    } catch (e: Exception) {
        210
    }
}
