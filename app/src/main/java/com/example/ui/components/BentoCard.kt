package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalCustomColors
import com.example.ui.theme.ObsidianBorder
import com.example.ui.theme.ObsidianBorderSubtle
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.ObsidianSurface

@Composable
fun BentoCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderWidth: Dp = 1.dp,
    borderColor: Color? = null,
    borderBrush: Brush? = null,
    backgroundColor: Color? = null,
    glowBrush: Brush? = null,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val customColors = LocalCustomColors.current
    val effectiveBg = backgroundColor ?: if (customColors.isDark) ObsidianSurface else customColors.bentoCardBg
    
    val effectiveBorderBrush = borderBrush ?: if (borderColor != null) {
        Brush.linearGradient(listOf(borderColor, borderColor))
    } else if (customColors.isDark) {
        Brush.linearGradient(
            colors = listOf(
                ObsidianBorder,
                ObsidianBorderSubtle,
                Color(0xFF2D374D).copy(alpha = 0.6f)
            )
        )
    } else {
        Brush.linearGradient(listOf(customColors.bentoBorder, customColors.bentoBorder))
    }

    val clickModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else Modifier

    Surface(
        modifier = modifier
            .clip(shape)
            .then(clickModifier),
        shape = shape,
        color = effectiveBg,
        border = BorderStroke(borderWidth, effectiveBorderBrush),
        tonalElevation = 4.dp
    ) {
        val defaultInnerGradient = if (customColors.isDark) {
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF161C28).copy(alpha = 0.45f),
                    Color(0xFF0D111A).copy(alpha = 0.95f)
                )
            )
        } else {
            Brush.verticalGradient(
                colors = listOf(
                    Color.White,
                    Color(0xFFF8FAFC)
                )
            )
        }

        Box(
            modifier = Modifier
                .background(glowBrush ?: defaultInnerGradient)
                .padding(16.dp)
        ) {
            content()
        }
    }
}

