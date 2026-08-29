package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ExpenseRose
import com.example.ui.theme.WarningAmber

@Composable
fun RadialProgressRing(
    progress: Float, // 0.0f to 1.0f+
    modifier: Modifier = Modifier,
    size: Dp = 68.dp,
    strokeWidth: Dp = 7.dp,
    activeColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
    warningThreshold: Float = 0.85f,
    showPercentageInside: Boolean = true,
    customCenterText: String? = null
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress.coerceAtLeast(0f),
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    val displayColor = when {
        animatedProgress.value >= 1.0f -> ExpenseRose
        animatedProgress.value >= warningThreshold -> WarningAmber
        else -> activeColor
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val arcSize = this.size.width - strokePx

            // Draw track circle
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = androidx.compose.ui.geometry.Offset(strokePx / 2f, strokePx / 2f),
                size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )

            // Draw active progress arc
            val sweep = (animatedProgress.value.coerceIn(0f, 1f) * 360f)
            if (sweep > 0f) {
                drawArc(
                    color = displayColor,
                    startAngle = -90f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = androidx.compose.ui.geometry.Offset(strokePx / 2f, strokePx / 2f),
                    size = androidx.compose.ui.geometry.Size(arcSize, arcSize),
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }

        if (showPercentageInside) {
            val text = customCenterText ?: "${(animatedProgress.value * 100).toInt()}%"
            Text(
                text = text,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = if (size > 80.dp) 16.sp else 12.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
