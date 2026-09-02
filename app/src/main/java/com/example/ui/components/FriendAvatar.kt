package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.ExpenseRose
import com.example.ui.theme.customColors

@Composable
fun FriendAvatar(
    name: String,
    modifier: Modifier = Modifier,
    avatarColorHex: Long = 0xFF10B981,
    size: Dp = 44.dp,
    badgeState: BalanceBadgeState = BalanceBadgeState.NONE
) {
    val initials = name.trim().split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()
        .ifEmpty { "F" }

    val bgColor = Color(avatarColorHex)
    val surfaceColor = MaterialTheme.customColors.bentoCardBg

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(bgColor.copy(alpha = 0.25f))
                .border(1.5.dp, bgColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                fontSize = (size.value * 0.38f).sp,
                fontWeight = FontWeight.Bold,
                color = bgColor
            )
        }

        // Debt status indicator dot
        if (badgeState != BalanceBadgeState.NONE) {
            val badgeColor = if (badgeState == BalanceBadgeState.OWES_YOU) EmeraldAccent else ExpenseRose
            Box(
                modifier = Modifier
                    .size(size * 0.28f)
                    .align(Alignment.BottomEnd)
                    .offset(x = 1.dp, y = 1.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
                    .border(2.dp, surfaceColor, CircleShape)
            )
        }
    }
}

enum class BalanceBadgeState {
    NONE, OWES_YOU, YOU_OWE
}
