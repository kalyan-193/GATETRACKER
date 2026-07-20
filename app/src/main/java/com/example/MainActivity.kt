package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.StudyViewModel
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val studyViewModel: StudyViewModel = viewModel()
                val activeTab by studyViewModel.activeTab.collectAsState()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        FloatingCustomBottomNav(
                            activeTab = activeTab,
                            onTabSelected = { studyViewModel.setActiveTab(it) }
                        )
                    },
                    containerColor = Color.Transparent // Allow custom background canvas to shine
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .drawBehind {
                                // Draw deep dark space base
                                drawRect(DeepSpaceDark)
                                
                                // 1. Blue top-left radiant ambient glow
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(ElectricBlue.copy(alpha = 0.25f), Color.Transparent),
                                        center = Offset(-0.1f * size.width, -0.1f * size.height),
                                        radius = size.width * 1.1f
                                    ),
                                    radius = size.width * 1.1f,
                                    center = Offset(-0.1f * size.width, -0.1f * size.height)
                                )
                                
                                // 2. Purple bottom-right radiant ambient glow
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(NebulaPurple.copy(alpha = 0.22f), Color.Transparent),
                                        center = Offset(size.width * 1.1f, size.height * 0.9f),
                                        radius = size.height * 0.9f
                                    ),
                                    radius = size.height * 0.9f,
                                    center = Offset(size.width * 1.1f, size.height * 0.9f)
                                )

                                // 3. Cyan top-right/middle radiant ambient glow
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(CyanGlow.copy(alpha = 0.2f), Color.Transparent),
                                        center = Offset(size.width * 0.9f, size.height * 0.4f),
                                        radius = size.width * 0.8f
                                    ),
                                    radius = size.width * 0.8f,
                                    center = Offset(size.width * 0.9f, size.height * 0.4f)
                                )
                            }
                            .windowInsetsPadding(WindowInsets.statusBars)
                    ) {
                        Crossfade(
                            targetState = activeTab,
                            animationSpec = tween(durationMillis = 250),
                            label = "tab_transition"
                        ) { tab ->
                            when (tab) {
                                "Dashboard" -> DashboardScreen(
                                    viewModel = studyViewModel,
                                    modifier = Modifier.padding(top = 10.dp)
                                )
                                "Log" -> StudyEntryScreen(
                                    viewModel = studyViewModel,
                                    modifier = Modifier.padding(top = 10.dp)
                                )
                                "Subjects" -> SubjectTrackerScreen(
                                    viewModel = studyViewModel,
                                    modifier = Modifier.padding(top = 10.dp)
                                )
                                "Timer/Mock" -> TimerMockScreen(
                                    viewModel = studyViewModel,
                                    modifier = Modifier.padding(top = 10.dp)
                                )
                                "Goals" -> GoalsBadgesScreen(
                                    viewModel = studyViewModel,
                                    modifier = Modifier.padding(top = 10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Custom Bottom Navigation Bar styled to match High Density aesthetic
@Composable
fun FloatingCustomBottomNav(
    activeTab: String,
    onTabSelected: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Nav background bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .background(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(GlassBorder.copy(alpha = 0.5f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Tab 1: Home (Dashboard)
            val isTab1Selected = activeTab == "Dashboard"
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onTabSelected("Dashboard") }
                    .testTag("nav_tab_dashboard"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🏠",
                    fontSize = 18.sp,
                    color = if (isTab1Selected) CyanGlow else CyberSlateText
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "HOME",
                    color = if (isTab1Selected) CyanGlow else CyberSlateText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Tab 2: Stats (Subjects)
            val isTab2Selected = activeTab == "Subjects"
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onTabSelected("Subjects") }
                    .testTag("nav_tab_subjects"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "📊",
                    fontSize = 18.sp,
                    color = if (isTab2Selected) CyanGlow else CyberSlateText
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "STATS",
                    color = if (isTab2Selected) CyanGlow else CyberSlateText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Placeholder for middle button
            Box(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight()
            )

            // Tab 4: Plan (Timer/Mock)
            val isTab4Selected = activeTab == "Timer/Mock"
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onTabSelected("Timer/Mock") }
                    .testTag("nav_tab_timer/mock"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "📅",
                    fontSize = 18.sp,
                    color = if (isTab4Selected) CyanGlow else CyberSlateText
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "PLAN",
                    color = if (isTab4Selected) CyanGlow else CyberSlateText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Tab 5: Profile (Goals)
            val isTab5Selected = activeTab == "Goals"
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onTabSelected("Goals") }
                    .testTag("nav_tab_goals"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "👤",
                    fontSize = 18.sp,
                    color = if (isTab5Selected) CyanGlow else CyberSlateText
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "PROFILE",
                    color = if (isTab5Selected) CyanGlow else CyberSlateText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Floating Action Button (+) centered and raised
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-18).dp)
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = CircleShape,
                        spotColor = CyanGlow.copy(alpha = 0.4f)
                    )
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(CyanGlow, NebulaPurple)
                        ),
                        shape = CircleShape
                    )
                    .border(
                        width = 4.dp,
                        color = DeepSpaceDark,
                        shape = CircleShape
                    )
                    .clickable { onTabSelected("Log") }
                    .testTag("nav_tab_log"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

data class NavigationTab(
    val route: String,
    val icon: ImageVector,
    val label: String
)
