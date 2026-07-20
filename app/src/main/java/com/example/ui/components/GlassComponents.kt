package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.sin

// Reusable Apple-Style Glassmorphic Container
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderWidth: Dp = 1.dp,
    borderColor: Color = GlassBorder,
    backgroundColor: Color = GlassBackground,
    testTag: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val tagModifier = if (testTag != null) Modifier.testTag(testTag) else Modifier
    Column(
        modifier = modifier
            .then(tagModifier)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(cornerRadius),
                ambientColor = Color.Black.copy(alpha = 0.5f),
                spotColor = Color.Black.copy(alpha = 0.5f)
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        backgroundColor.copy(alpha = 0.05f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .border(
                width = borderWidth,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        borderColor,
                        borderColor.copy(alpha = 0.1f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(16.dp),
        content = content
    )
}

// Reusable Frosted Glass Glow Button
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    accentColor: Color = ElectricBlue,
    testTag: String? = null
) {
    val tagModifier = if (testTag != null) Modifier.testTag(testTag) else Modifier
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = CosmicWhiteText,
            disabledContainerColor = Color.White.copy(alpha = 0.05f),
            disabledContentColor = CyberSlateText
        ),
        contentPadding = PaddingValues(0.dp), // handle padding manually
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .then(tagModifier)
            .height(50.dp)
            .shadow(
                elevation = if (enabled) 6.dp else 0.dp,
                shape = RoundedCornerShape(12.dp),
                spotColor = accentColor.copy(alpha = 0.5f)
            )
            .background(
                brush = if (enabled) {
                    Brush.horizontalGradient(
                        colors = listOf(accentColor, NebulaPurple)
                    )
                } else {
                    Brush.horizontalGradient(
                        colors = listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.08f))
                    )
                },
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = if (enabled) GlassBorder else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Reusable Frosted Glass Text Input
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    isNumberOnly: Boolean = false,
    testTag: String? = null
) {
    val tagModifier = if (testTag != null) Modifier.testTag(testTag) else Modifier
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = CosmicWhiteText.copy(alpha = 0.7f), fontSize = 13.sp) },
        placeholder = { Text(placeholder, color = CyberSlateText) },
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isNumberOnly) KeyboardType.Number else KeyboardType.Text
        ),
        colors = TextFieldDefaults.colors(
            focusedTextColor = CosmicWhiteText,
            unfocusedTextColor = CosmicWhiteText,
            focusedContainerColor = Color.White.copy(alpha = 0.06f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.03f),
            focusedIndicatorColor = ElectricBlue,
            unfocusedIndicatorColor = Color.White.copy(alpha = 0.2f),
            disabledIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .then(tagModifier)
            .border(
                width = 1.dp,
                color = GlassBorder.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            )
    )
}

// Live Wave Animating Liquid Glass Circular Progress Gauge
@Composable
fun LiquidGlassGauge(
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = 160.dp
) {
    // Wave animation triggers
    val infiniteTransition = rememberInfiniteTransition(label = "wave_anim")
    val phaseShift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * java.lang.Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    // Animated score transition for smooth gauge level scaling
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "score_anim"
    )

    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(50),
                spotColor = ElectricBlue.copy(alpha = 0.3f)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(50))
        ) {
            val width = size.toPx()
            val height = size.toPx()
            val radius = width / 2f
            val center = Offset(radius, radius)

            // 1. Draw outer frosted-glowing boundary ring
            drawCircle(
                color = GlassBorder,
                radius = radius - 4f,
                style = Stroke(width = 4f)
            )

            // 2. Draw subtle background glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        ElectricBlue.copy(alpha = 0.15f),
                        NebulaPurple.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius
                ),
                radius = radius - 8f
            )

            // 3. Draw internal liquid wave based on the score percentage
            val liquidHeightFactor = (100f - animatedScore) / 100f // 0f (full) to 1f (empty)
            val liquidY = height * liquidHeightFactor

            val wavePath = Path()
            wavePath.moveTo(0f, height)

            // Generate sine-wave coordinate path
            val waveHeight = 12f
            val waveLength = width
            for (x in 0..width.toInt() step 4) {
                val waveX = x.toFloat()
                // wave formula: y = waveHeight * sin( (2pi/waveLength) * x + phase ) + liquidY
                val angle = (2f * java.lang.Math.PI.toFloat() / waveLength) * waveX + phaseShift
                val waveY = waveHeight * sin(angle) + liquidY
                wavePath.lineTo(waveX, waveY)
            }
            wavePath.lineTo(width, height)
            wavePath.close()

            // Fill wave path with a premium glass-liquid glowing gradient
            drawPath(
                path = wavePath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CyanGlow.copy(alpha = 0.8f),
                        ElectricBlue.copy(alpha = 0.6f),
                        NebulaPurple.copy(alpha = 0.4f)
                    ),
                    startY = liquidY - waveHeight,
                    endY = height
                )
            )

            // Draw a second sub-wave layer for double-depth liquid reflection effect
            val reflectionPath = Path()
            reflectionPath.moveTo(0f, height)
            for (x in 0..width.toInt() step 4) {
                val waveX = x.toFloat()
                val angle = (2f * java.lang.Math.PI.toFloat() / waveLength) * waveX - phaseShift + 1.5f
                val waveY = (waveHeight * 0.7f) * sin(angle) + liquidY + 4f
                reflectionPath.lineTo(waveX, waveY)
            }
            reflectionPath.lineTo(width, height)
            reflectionPath.close()

            drawPath(
                path = reflectionPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        WhiteGlassTransparency.copy(alpha = 0.3f),
                        CyanGlow.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    startY = liquidY,
                    endY = height
                )
            )
        }

        // 4. Centered textual score container
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "GPS",
                color = CosmicWhiteText.copy(alpha = 0.7f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = score.toString(),
                color = CosmicWhiteText,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp,
                modifier = Modifier.drawBehind {
                    // Slight soft backing shadow to keep text ultra-readable against any water level
                    drawCircle(
                        color = Color.Black.copy(alpha = 0.4f),
                        radius = 80f,
                        center = Offset(0f, 0f)
                    )
                }
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = when {
                    score >= 80 -> "Ready!"
                    score >= 60 -> "Good Pace"
                    score >= 40 -> "Action Req"
                    else -> "Low Effort"
                },
                color = if (score >= 60) BrightGreenGlow else if (score >= 40) WarmAmberGold else VividRedAlert,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Apple-style Glass Translucent Progress Slider
@Composable
fun GlassSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 1f..10f,
    steps: Int = 8,
    label: String,
    displayValue: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, color = CosmicWhiteText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(
                displayValue,
                color = CyanGlow,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.08f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = CosmicWhiteText,
                activeTrackColor = ElectricBlue,
                inactiveTrackColor = Color.White.copy(alpha = 0.15f),
                activeTickColor = CyanGlow,
                inactiveTickColor = Color.Transparent
            )
        )
    }
}

// Gorgeous Custom Curved Weekly Area Chart
@Composable
fun WeeklyTrendChart(
    hoursList: List<Double>,
    daysList: List<String>,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
    ) {
        if (hoursList.isEmpty()) {
            // Draw placeholder text in empty states
            return@Canvas
        }

        val paddingLeft = 30f
        val paddingRight = 30f
        val paddingTop = 20f
        val paddingBottom = 20f

        val chartWidth = size.width - (paddingLeft + paddingRight)
        val chartHeight = size.height - (paddingTop + paddingBottom)

        val maxHours = (hoursList.maxOrNull() ?: 1.0).coerceAtLeast(8.0)
        val pointsCount = hoursList.size

        val xSpacing = if (pointsCount > 1) chartWidth / (pointsCount - 1) else chartWidth

        // Draw horizontal grid lines
        val gridLinesCount = 3
        for (g in 0..gridLinesCount) {
            val gridY = paddingTop + (chartHeight / gridLinesCount) * g
            drawLine(
                color = Color.White.copy(alpha = 0.06f),
                start = Offset(paddingLeft, gridY),
                end = Offset(size.width - paddingRight, gridY),
                strokeWidth = 2f
            )
        }

        // Compile point coordinates
        val points = mutableListOf<Offset>()
        for (i in 0 until pointsCount) {
            val pointX = paddingLeft + i * xSpacing
            // Invert Y coordinate so 0 is at the bottom
            val ratio = (hoursList[i] / maxHours).toFloat()
            val pointY = paddingTop + chartHeight * (1f - ratio)
            points.add(Offset(pointX, pointY))
        }

        // Draw smooth filled gradient area under the curve
        if (points.isNotEmpty()) {
            val fillPath = Path()
            fillPath.moveTo(points[0].x, size.height - paddingBottom)
            fillPath.lineTo(points[0].x, points[0].y)

            // Use cubic curves for ultra-smooth layout lines
            for (i in 1 until pointsCount) {
                val prevPoint = points[i - 1]
                val currPoint = points[i]
                val controlPoint1 = Offset(prevPoint.x + xSpacing / 2f, prevPoint.y)
                val controlPoint2 = Offset(currPoint.x - xSpacing / 2f, currPoint.y)
                fillPath.cubicTo(
                    controlPoint1.x, controlPoint1.y,
                    controlPoint2.x, controlPoint2.y,
                    currPoint.x, currPoint.y
                )
            }
            fillPath.lineTo(points.last().x, size.height - paddingBottom)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ElectricBlue.copy(alpha = 0.35f),
                        CyanGlow.copy(alpha = 0.08f),
                        Color.Transparent
                    )
                )
            )

            // Draw glowing outline curve
            val linePath = Path()
            linePath.moveTo(points[0].x, points[0].y)
            for (i in 1 until pointsCount) {
                val prevPoint = points[i - 1]
                val currPoint = points[i]
                val controlPoint1 = Offset(prevPoint.x + xSpacing / 2f, prevPoint.y)
                val controlPoint2 = Offset(currPoint.x - xSpacing / 2f, currPoint.y)
                linePath.cubicTo(
                    controlPoint1.x, controlPoint1.y,
                    controlPoint2.x, controlPoint2.y,
                    currPoint.x, currPoint.y
                )
            }

            drawPath(
                path = linePath,
                color = ElectricBlue,
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )

            // Draw glowing circles on data vertices
            for (point in points) {
                drawCircle(
                    color = CyanGlow,
                    radius = 6f,
                    center = point
                )
                drawCircle(
                    color = CosmicWhiteText,
                    radius = 3f,
                    center = point
                )
            }
        }
    }
}

// Glowing Custom Progress Indicator
@Composable
fun GlowProgressBar(
    progress: Float, // 0f to 1f
    modifier: Modifier = Modifier,
    activeColor: Color = ElectricBlue,
    trackColor: Color = Color.White.copy(alpha = 0.1f)
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "progress_bar"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .background(trackColor, RoundedCornerShape(4.dp))
            .clip(RoundedCornerShape(4.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .shadow(elevation = 4.dp, spotColor = activeColor)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(activeColor, CyanGlow)
                    ),
                    shape = RoundedCornerShape(4.dp)
                )
        )
    }
}

// Translucent UI White Glow constant
val WhiteGlassTransparency = Color(0x33FFFFFF)
