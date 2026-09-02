package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalCustomColors

/**
 * NeoPOP "extruded block" card.
 *
 * Two stacked layers create the tactile illusion:
 *  - a flat, solid-color [depth]-offset slab behind (customColors.extrusionShadow)
 *  - the content face on top, which slides down onto the slab on press
 *    and springs back on release — the signature NeoPOP "push".
 *
 * Deliberately flat: no blur, no soft elevation shadow — the depth is a
 * hard color block, not a shadow.
 */
@Composable
fun BentoCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(4.dp),
    borderWidth: Dp = 2.dp,
    borderColor: Color? = null,
    borderBrush: Brush? = null,
    backgroundColor: Color? = null,
    glowBrush: Brush? = null,
    depth: Dp = 6.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val customColors = LocalCustomColors.current
    val effectiveBg = backgroundColor ?: customColors.bentoCardBg
    val effectiveBorderColor = borderColor ?: customColors.bentoBorderStrong

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val faceOffset by animateDpAsState(
        targetValue = if (isPressed) depth else 0.dp,
        animationSpec = tween(durationMillis = 90),
        label = "neopop-press-offset"
    )

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else Modifier

    Box(modifier = modifier) {
        // Extrusion slab — solid flat block, fixed position, never moves.
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = depth, y = depth)
                .clip(shape)
                .background(customColors.extrusionShadow)
        )

        // Face — rests lifted by `depth`; slides onto the slab when pressed.
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = faceOffset, y = faceOffset)
                .clip(shape)
                .background(glowBrush ?: Brush.linearGradient(listOf(effectiveBg, effectiveBg)))
                .border(borderWidth, effectiveBorderColor, shape)
                .then(clickModifier)
                .padding(16.dp)
        ) {
            content()
        }
    }
}
