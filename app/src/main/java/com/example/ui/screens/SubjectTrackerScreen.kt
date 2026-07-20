package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SubjectProgress
import com.example.ui.StudyViewModel
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassSlider
import com.example.ui.components.GlowProgressBar
import com.example.ui.theme.*

@Composable
fun SubjectTrackerScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val subjects by viewModel.subjectProgress.collectAsState()
    var expandedSubjectName by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 90.dp, top = 8.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Syllabus Monitor",
                    color = CyberSlateText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "EE Subject Tracker",
                    color = CosmicWhiteText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
            }
        }

        items(subjects, key = { it.subjectName }) { subject ->
            SubjectProgressCard(
                subject = subject,
                isExpanded = expandedSubjectName == subject.subjectName,
                onToggleExpand = {
                    expandedSubjectName = if (expandedSubjectName == subject.subjectName) null else subject.subjectName
                },
                onSaveProgress = { newPercentage, notesReady, pyqs, mocks ->
                    viewModel.updateSubjectManualProgress(subject.subjectName, newPercentage)
                    // If we want to save full parameters, let's allow it
                    expandedSubjectName = null
                }
            )
        }
    }
}

@Composable
fun SubjectProgressCard(
    subject: SubjectProgress,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSaveProgress: (Int, Boolean, Int, Int) -> Unit
) {
    var editedPercentage by remember(subject) { mutableFloatStateOf(subject.completionPercentage.toFloat()) }
    var notesDone by remember(subject) { mutableStateOf(subject.notesCompleted) }
    var pyqsSolvedCount by remember(subject) { mutableStateOf(subject.pyqsDone.toString()) }
    var mockTestsCount by remember(subject) { mutableStateOf(subject.mockTestsDone.toString()) }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() }
            .animateContentSize(),
        backgroundColor = if (isExpanded) GlassBackground.copy(alpha = 0.12f) else GlassBackground.copy(alpha = 0.05f)
    ) {
        // Main Header view showing subject and current progress bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subject.subjectName,
                    color = CosmicWhiteText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${subject.completionPercentage}% complete",
                        color = if (subject.completionPercentage >= 80) BrightGreenGlow else if (subject.completionPercentage >= 40) ElectricBlue else CyberSlateText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (subject.notesCompleted) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Notes Done",
                                tint = BrightGreenGlow,
                                modifier = Modifier.size(12.dp)
                            )
                            Text("Notes", color = CyberSlateText, fontSize = 10.sp)
                        }
                    }
                }
            }

            // Dropdown indicators
            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Collapse" else "Expand",
                tint = CyanGlow
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Glassmorphic Progress Line
        GlowProgressBar(
            progress = subject.completionPercentage / 100f,
            activeColor = if (subject.completionPercentage >= 80) BrightGreenGlow else if (subject.completionPercentage >= 40) ElectricBlue else NebulaPurple
        )

        // Expanded editor drawer panel
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn() + expandVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                Divider(color = Color.White.copy(alpha = 0.1f), modifier = Modifier.padding(bottom = 12.dp))

                Text(
                    text = "EDIT PROGRESS",
                    color = CyanGlow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Completion percentage slider
                GlassSlider(
                    value = editedPercentage,
                    onValueChange = { editedPercentage = it },
                    valueRange = 0f..100f,
                    steps = 19,
                    label = "Syllabus Completion",
                    displayValue = "${editedPercentage.toInt()}%"
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Stats summaries read-only display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("PYQs SOLVED", color = CyberSlateText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("${subject.pyqsDone}", color = CosmicWhiteText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .padding(8.dp)
                    ) {
                        Column {
                            Text("MOCK TESTS", color = CyberSlateText, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            Text("${subject.mockTestsDone}", color = CosmicWhiteText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Save buttons inside list
                GlassButton(
                    text = "Save Progress",
                    onClick = {
                        onSaveProgress(editedPercentage.toInt(), notesDone, pyqsSolvedCount.toIntOrNull() ?: 0, mockTestsCount.toIntOrNull() ?: 0)
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
