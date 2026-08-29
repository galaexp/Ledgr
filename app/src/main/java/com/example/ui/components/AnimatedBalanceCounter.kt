package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MonospaceCurrencyStyle
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AnimatedBalanceCounter(
    targetAmount: Double,
    currencySymbol: String = "$",
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle(
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp
    ),
    color: Color = MaterialTheme.colorScheme.onBackground,
    prefix: String = "",
    decimals: Int = 2
) {
    val animatedValue = remember { Animatable(0f) }

    LaunchedEffect(targetAmount) {
        animatedValue.animateTo(
            targetValue = targetAmount.toFloat(),
            animationSpec = tween(
                durationMillis = 750,
                easing = FastOutSlowInEasing
            )
        )
    }

    val formatted = remember(animatedValue.value, currencySymbol, decimals) {
        val format = NumberFormat.getNumberInstance(Locale.US)
        format.minimumFractionDigits = decimals
        format.maximumFractionDigits = decimals
        format.format(animatedValue.value)
    }

    Text(
        text = "$prefix$currencySymbol$formatted",
        modifier = modifier,
        style = style.merge(MonospaceCurrencyStyle),
        color = color
    )
}
