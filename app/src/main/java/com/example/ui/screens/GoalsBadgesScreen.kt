package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.StudyViewModel
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassTextField
import com.example.ui.theme.*
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GoalsBadgesScreen(
    viewModel: StudyViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val goals by viewModel.goalSettings.collectAsState()
    val entries by viewModel.allEntries.collectAsState()
    val streak by viewModel.streak.collectAsState()

    // Form states
    var rank by remember(goals) { mutableStateOf(goals.targetAirRank.toString()) }
    var score by remember(goals) { mutableStateOf(goals.targetScore.toString()) }
    var hours by remember(goals) { mutableStateOf(goals.targetDailyHours.toString()) }
    var monthly by remember(goals) { mutableStateOf(goals.monthlyGoals) }

    var importText by remember { mutableStateOf("") }
    var showImportBox by remember { mutableStateOf(false) }

    // Badges Calculations
    val totalHours = entries.sumOf { it.studyHours }
    val totalPyqs = entries.sumOf { it.pyqsSolved }
    val totalMocks = entries.sumOf { it.mockTestsAttempted }

    val badges = listOf(
        BadgeInfo(
            name = "Bronze Scholar",
            requirement = "Reach 50+ total hours studied",
            icon = "🥉",
            isEarned = totalHours >= 50.0,
            activeBrush = Brush.horizontalGradient(listOf(Color(0xFFCD7F32), Color(0xFF8B4513)))
        ),
        BadgeInfo(
            name = "Silver Scholar",
            requirement = "Reach 150+ total hours studied",
            icon = "🥈",
            isEarned = totalHours >= 150.0,
            activeBrush = Brush.horizontalGradient(listOf(Color(0xFFC0C0C0), Color(0xFF708090)))
        ),
        BadgeInfo(
            name = "Gold Scholar",
            requirement = "Reach 300+ total hours studied",
            icon = "🥇",
            isEarned = totalHours >= 300.0,
            activeBrush = Brush.horizontalGradient(listOf(Color(0xFFFFD700), Color(0xFFDAA520)))
        ),
        BadgeInfo(
            name = "Consistency Warrior",
            requirement = "Maintain a 7-day study streak",
            icon = "⚔️",
            isEarned = streak >= 7,
            activeBrush = Brush.horizontalGradient(listOf(ElectricBlue, CyanGlow))
        ),
        BadgeInfo(
            name = "100-Day Streak",
            requirement = "Reach a legendary 100-day streak",
            icon = "👑",
            isEarned = streak >= 100,
            activeBrush = Brush.horizontalGradient(listOf(NebulaPurple, Color(0xFFFF4500)))
        ),
        BadgeInfo(
            name = "PYQ Master",
            requirement = "Solve 500+ total practice PYQs",
            icon = "🎯",
            isEarned = totalPyqs >= 500,
            activeBrush = Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF059669)))
        ),
        BadgeInfo(
            name = "Mock Champion",
            requirement = "Complete 10+ timed mock exams",
            icon = "🏆",
            isEarned = totalMocks >= 10,
            activeBrush = Brush.horizontalGradient(listOf(Color(0xFFFF8C00), Color(0xFFFF4500)))
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 100.dp, top = 8.dp)
    ) {
        item {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                Text(
                    text = "Aspirations Drawer",
                    color = CyberSlateText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Goals & Badges Panel",
                    color = CosmicWhiteText,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
            }
        }

        // 1. Goal Settings Card
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backgroundColor = GlassBackground.copy(alpha = 0.08f)
            ) {
                Text(
                    text = "SET YOUR TARGETS",
                    color = CyanGlow,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassTextField(
                        value = rank,
                        onValueChange = { rank = it },
                        label = "Target AIR Rank",
                        isNumberOnly = true,
                        modifier = Modifier.weight(1f)
                    )
                    GlassTextField(
                        value = score,
                        onValueChange = { score = it },
                        label = "Target Score",
                        isNumberOnly = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                GlassTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = "Target Daily Study Hours",
                    isNumberOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))

                GlassTextField(
                    value = monthly,
                    onValueChange = { monthly = it },
                    label = "Active Monthly Syllabus Goals",
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = "e.g. Complete machines..."
                )

                Spacer(modifier = Modifier.height(18.dp))

                GlassButton(
                    text = "Save Target Parameters",
                    onClick = {
                        val rVal = rank.toIntOrNull() ?: 100
                        val sVal = score.toIntOrNull() ?: 80
                        val hVal = hours.toDoubleOrNull() ?: 6.0
                        viewModel.updateGoalSettings(rVal, sVal, hVal, monthly)
                        Toast.makeText(context, "Targets updated successfully!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "save_targets_button"
                )
            }
        }

        // 2. Gamified Badges Shelf
        item {
            Text(
                text = "GAMIFIED BADGE SHELF",
                color = CyanGlow,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        items(badges) { badge ->
            BadgeCard(badge)
        }

        // 3. Backup, Excel Export, Restore Options
        item {
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = Color.White.copy(alpha = 0.15f),
                backgroundColor = DarkGlassBackground
            ) {
                Text(
                    text = "DATA MANAGEMENT & EXCEL SYNC",
                    color = CyanGlow,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Export Excel Button
                    Button(
                        onClick = {
                            val csvContent = viewModel.exportToCsvContent()
                            shareCsvSpreadsheet(context, csvContent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, "Share", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Export Excel", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    // Import Button
                    Button(
                        onClick = { showImportBox = !showImportBox },
                        colors = ButtonDefaults.buttonColors(containerColor = NebulaPurple),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Download, "Import", tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Import CSV", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Expanding import field drawer
                if (showImportBox) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Paste Excel-compatible CSV text raw rows below:",
                        color = CyberSlateText,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    GlassTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        label = "CSV Data Block",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GlassButton(
                            text = "Commit Import",
                            onClick = {
                                viewModel.importFromCsvContent(
                                    content = importText,
                                    onSuccess = {
                                        Toast.makeText(context, "Excel sheet data imported successfully!", Toast.LENGTH_LONG).show()
                                        showImportBox = false
                                        importText = ""
                                    },
                                    onFailure = { error ->
                                        Toast.makeText(context, "Parse failure: $error", Toast.LENGTH_LONG).show()
                                    }
                                )
                            },
                            modifier = Modifier.weight(1.1f),
                            accentColor = BrightGreenGlow
                        )
                        GlassButton(
                            text = "Cancel",
                            onClick = {
                                showImportBox = false
                                importText = ""
                            },
                            modifier = Modifier.weight(1f),
                            accentColor = Color.White.copy(alpha = 0.1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider(color = Color.White.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(10.dp))

                // Wipe DB Clean trigger
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.clearAllData()
                            Toast
                                .makeText(context, "All logs cleared successfully.", Toast.LENGTH_SHORT)
                                .show()
                        }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DeleteForever, "Clear", tint = VividRedAlert, modifier = Modifier.size(20.dp))
                    Text("Clear All Data Logs", color = VividRedAlert, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BadgeCard(badge: BadgeInfo) {
    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = if (badge.isEarned) GlassBackground.copy(alpha = 0.12f) else GlassBackground.copy(alpha = 0.03f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left: Glowing custom icon circle
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(
                        if (badge.isEarned) badge.activeBrush else Brush.radialGradient(
                            listOf(
                                Color.White.copy(alpha = 0.08f),
                                Color.White.copy(alpha = 0.04f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        if (badge.isEarned) Color.White.copy(alpha = 0.3f) else Color.Transparent,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badge.icon,
                    fontSize = 26.sp
                )
            }

            // Middle: Description details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = badge.name,
                    color = if (badge.isEarned) CosmicWhiteText else CyberSlateText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = badge.requirement,
                    color = CyberSlateText,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }

            // Right: Status indicator
            if (badge.isEarned) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Earned",
                    tint = BrightGreenGlow,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    tint = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// Data class representation of a Badge
data class BadgeInfo(
    val name: String,
    val requirement: String,
    val icon: String,
    val isEarned: Boolean,
    val activeBrush: Brush
)

// Helper to share spreadsheet file directly
private fun shareCsvSpreadsheet(context: Context, csvContent: String) {
    try {
        val file = File(context.cacheDir, "GATE_Prep_Report.csv")
        val stream = FileOutputStream(file)
        stream.write(csvContent.toByteArray())
        stream.close()

        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/comma-separated-values"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "GATE EE Preparation Report")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Export Excel (CSV)"))
    } catch (e: Exception) {
        // Fallback to text copy
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("GATE Prep Data", csvContent)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Could not build temp file. Exported CSV text copied to clipboard instead!", Toast.LENGTH_LONG).show()
    }
}
