package com.example.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalCustomColors
import com.example.ui.theme.NeoPopPureBlack

enum class NeoPopButtonStyle { PRIMARY, SECONDARY, DESTRUCTIVE, OUTLINE }

/**
 * NeoPOP extruded button: a solid-color face lifted [depth] above a flat
 * black slab. Press = face travels down onto the slab (button "clicks");
 * release = face springs back up. No ripple, no soft shadow — the depth
 * change *is* the feedback.
 */
@Composable
fun NeoPopButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: NeoPopButtonStyle = NeoPopButtonStyle.PRIMARY,
    enabled: Boolean = true,
    depth: Dp = 5.dp,
    shape: Shape = RoundedCornerShape(4.dp),
    contentPaddingHorizontal: Dp = 24.dp,
    contentPaddingVertical: Dp = 14.dp,
    content: @Composable () -> Unit
) {
    val customColors = LocalCustomColors.current
    val faceColor = when (style) {
        NeoPopButtonStyle.PRIMARY -> MaterialTheme.colorScheme.primary
        NeoPopButtonStyle.SECONDARY -> MaterialTheme.colorScheme.secondary
        NeoPopButtonStyle.DESTRUCTIVE -> customColors.danger
        NeoPopButtonStyle.OUTLINE -> customColors.bentoCardBg
    }
    val onFaceColor = when (style) {
        NeoPopButtonStyle.OUTLINE -> customColors.textPrimary
        else -> NeoPopPureBlack
    }
    val borderColor = if (style == NeoPopButtonStyle.OUTLINE) customColors.bentoBorderStrong else Color.Transparent

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val faceOffset by animateDpAsState(
        targetValue = if (isPressed && enabled) depth else 0.dp,
        animationSpec = tween(durationMillis = 80),
        label = "neopop-button-offset"
    )

    Box(
        modifier = modifier
            .wrapContentWidth()
            .alpha(if (enabled) 1f else 0.4f)
    ) {
        // Slab — sized off the face (below), which is the only child that
        // contributes to this Box's intrinsic size.
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = depth, y = depth)
                .clip(shape)
                .background(NeoPopPureBlack)
        )
        // Face
        Box(
            modifier = Modifier
                .offset(x = faceOffset, y = faceOffset)
                .clip(shape)
                .background(faceColor)
                .border(if (style == NeoPopButtonStyle.OUTLINE) 2.dp else 0.dp, borderColor, shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    onClick = onClick
                )
                .padding(horizontal = contentPaddingHorizontal, vertical = contentPaddingVertical),
            contentAlignment = Alignment.Center
        ) {
            CompositionLocalProvider(LocalContentColor provides onFaceColor) {
                content()
            }
        }
    }
}

