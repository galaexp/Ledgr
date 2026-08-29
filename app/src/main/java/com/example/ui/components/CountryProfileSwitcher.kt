package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CountryCurrencyCatalog
import com.example.data.model.CountryProfileType
import com.example.ui.theme.*

@Composable
fun CountryProfileTopBarButton(
    currentProfile: CountryProfileType,
    homeCurrency: String,
    expatCurrency: String,
    homeFlag: String = "",
    onProfileSelected: (CountryProfileType) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    val effectiveHomeFlag = homeFlag.ifBlank { CountryCurrencyCatalog.getFlagForCurrency(homeCurrency) }
    val expatFlag = CountryCurrencyCatalog.getFlagForCurrency(expatCurrency)

    val label = when (currentProfile) {
        CountryProfileType.HOME -> "$effectiveHomeFlag $homeCurrency"
        CountryProfileType.EXPAT -> "$expatFlag $expatCurrency"
        CountryProfileType.ALL -> "🌐 Global"
    }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            ObsidianElevated,
                            ObsidianSurface
                        )
                    )
                )
                .border(1.dp, CredMint.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                .clickable { showMenu = true }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(CredMint)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark,
                letterSpacing = 0.5.sp
            )
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = "Switch Profile",
                tint = CredMint,
                modifier = Modifier.size(16.dp)
            )
        }

        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false },
            modifier = Modifier
                .background(ObsidianElevated)
                .border(1.dp, ObsidianBorder, RoundedCornerShape(12.dp))
        ) {
            DropdownMenuItem(
                text = {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(effectiveHomeFlag, fontSize = 18.sp)
                        Column {
                            Text("Domestic Base ($homeCurrency)", fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                            Text("Home accounts & primary currency", fontSize = 11.sp, color = TextSecondaryDark)
                        }
                    }
                },
                onClick = {
                    onProfileSelected(CountryProfileType.HOME)
                    showMenu = false
                }
            )
            DropdownMenuItem(
                text = {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(expatFlag, fontSize = 18.sp)
                        Column {
                            Text("Expat & Foreign ($expatCurrency)", fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                            Text("International accounts & FX rates", fontSize = 11.sp, color = TextSecondaryDark)
                        }
                    }
                },
                onClick = {
                    onProfileSelected(CountryProfileType.EXPAT)
                    showMenu = false
                }
            )
            HorizontalDivider(color = ObsidianBorderSubtle)
            DropdownMenuItem(
                text = {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🌐", fontSize = 18.sp)
                        Column {
                            Text("Global Consolidated (All)", fontWeight = FontWeight.Bold, color = TextPrimaryDark)
                            Text("Combined global net worth view", fontSize = 11.sp, color = TextSecondaryDark)
                        }
                    }
                },
                onClick = {
                    onProfileSelected(CountryProfileType.ALL)
                    showMenu = false
                }
            )
        }
    }
}

/**
 * CRED-style Segmented Profile Switcher with animated pill indicator & neon glow
 */
@Composable
fun CredCountryProfileSegmentedBar(
    currentProfile: CountryProfileType,
    homeCurrency: String,
    expatCurrency: String,
    homeFlag: String = "",
    onProfileSelected: (CountryProfileType) -> Unit,
    modifier: Modifier = Modifier
) {
    val effectiveHomeFlag = homeFlag.ifBlank { CountryCurrencyCatalog.getFlagForCurrency(homeCurrency) }
    val expatFlag = CountryCurrencyCatalog.getFlagForCurrency(expatCurrency)

    val profiles = listOf(
        Triple(CountryProfileType.HOME, "$effectiveHomeFlag $homeCurrency", "Domestic"),
        Triple(CountryProfileType.EXPAT, "$expatFlag $expatCurrency", "Expat"),
        Triple(CountryProfileType.ALL, "🌐 ALL", "Global")
    )

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ObsidianSurface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Brush.linearGradient(
                listOf(
                    ObsidianBorder,
                    Color(0xFF2E384D)
                )
            )
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            profiles.forEach { (profile, title, subtitle) ->
                val isSelected = currentProfile == profile
                val animatedBg by animateColorAsState(
                    targetValue = if (isSelected) CredMint.copy(alpha = 0.15f) else Color.Transparent,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "ProfileSegmentBg"
                )
                val animatedBorder by animateColorAsState(
                    targetValue = if (isSelected) CredMint else Color.Transparent,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "ProfileSegmentBorder"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(animatedBg)
                        .border(1.dp, animatedBorder, RoundedCornerShape(12.dp))
                        .clickable { onProfileSelected(profile) }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isSelected) CredMint else TextSecondaryDark,
                            letterSpacing = 0.4.sp
                        )
                        Text(
                            text = subtitle.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = if (isSelected) TextPrimaryDark else TextTertiaryDark
                        )
                    }
                }
            }
        }
    }
}

