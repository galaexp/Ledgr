package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CountryCurrencyCatalog
import com.example.data.model.ThemePalette
import com.example.domain.CsvExportImportManager
import com.example.ui.components.BentoCard
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.ExpenseRose
import com.example.viewmodel.LedgrViewModel

@Composable
fun SettingsScreen(
    viewModel: LedgrViewModel
) {
    val context = LocalContext.current
    val userName by viewModel.userName.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val userCountryCode by viewModel.userCountryCode.collectAsState()
    val userCountryName by viewModel.userCountryName.collectAsState()
    val currentPalette by viewModel.themePalette.collectAsState()
    val isDark by viewModel.isDarkTheme.collectAsState()
    val primaryCurrency by viewModel.primaryCurrency.collectAsState()
    val expatCurrency by viewModel.expatCurrency.collectAsState()
    val isLocked by viewModel.isLocked.collectAsState()
    val isPinSet = viewModel.securityManager.isPinEnabled()

    val accounts by viewModel.accounts.collectAsState(initial = emptyList())
    val transactions by viewModel.transactions.collectAsState(initial = emptyList())
    val categories by viewModel.categories.collectAsState(initial = emptyList())

    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }

    var showCurrencyDialog by remember { mutableStateOf(false) }
    var homeCurrInput by remember { mutableStateOf(primaryCurrency) }
    var expatCurrInput by remember { mutableStateOf(expatCurrency) }
    var selectedCountryCode by remember { mutableStateOf(userCountryCode) }
    var selectedCountryName by remember { mutableStateOf(userCountryName) }
    var countrySearchQuery by remember { mutableStateOf("") }

    val userCountryFlag by viewModel.userCountryFlag.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
    ) {
        // User Profile & Identity Header
        item {
            BentoCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(EmeraldAccent.copy(alpha = 0.2f))
                                .border(1.5.dp, EmeraldAccent, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (userName.isNotBlank()) userName.take(1).uppercase() else "L",
                                fontSize = 22.sp,
                                color = EmeraldAccent,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column {
                            Text(
                                text = if (userName.isNotBlank()) userName else "Ledgr User",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (userEmail.isNotBlank()) userEmail else "offline@ledgr.local",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$userCountryFlag $userCountryName • $primaryCurrency",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = EmeraldAccent
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.logout()
                            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ExpenseRose),
                        modifier = Modifier.height(36.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Log Out", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 1: Appearance & Accent Themes
        item {
            Text("APPEARANCE & THEMES", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Dark / Light Mode", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Obsidian Dark Canvas by default", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(checked = isDark, onCheckedChange = { viewModel.toggleDarkTheme() })
                    }

                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                    Text("Accent Theme Palette", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ThemePalette.values()) { palette ->
                            val isSelected = currentPalette == palette
                            val pColor = Color(palette.primaryHex)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) pColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, if (isSelected) pColor else Color.Transparent),
                                modifier = Modifier.clickable { viewModel.setThemePalette(palette) }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(pColor)
                                    )
                                    Text(
                                        text = palette.name.lowercase().replaceFirstChar { it.uppercase() },
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) pColor else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Dual-Currency & Country Profiles
        item {
            Text("MULTI-COUNTRY & DUAL CURRENCIES", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Base Country Currencies", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Home: $primaryCurrency | Expat/Second: $expatCurrency", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Button(
                            onClick = {
                                homeCurrInput = primaryCurrency
                                expatCurrInput = expatCurrency
                                showCurrencyDialog = true
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                        ) {
                            Text("Edit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Section 3: Security & PIN Vault
        item {
            Text("VAULT SECURITY & PIN LOCK", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("4-Digit PIN Security", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(if (isPinSet) "PIN lock is active" else "No PIN set (unlocked)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = isPinSet,
                            onCheckedChange = { checked ->
                                if (checked) {
                                    showPinDialog = true
                                } else {
                                    viewModel.removePin()
                                    Toast.makeText(context, "PIN protection removed", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }

                    if (isPinSet) {
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Lock Application Now", fontSize = 13.sp)
                            FilledTonalButton(onClick = { viewModel.lockApp() }) {
                                Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Lock", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // Section 4: Data Backup & CSV Export
        item {
            Text("DATA BACKUP & CSV EXPORT", fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Export Full Ledger CSV", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("${transactions.size} transactions ready to export", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(
                            onClick = {
                                val file = CsvExportImportManager.exportTransactionsToCsv(context, transactions, accounts, categories)
                                if (file != null) {
                                    CsvExportImportManager.shareCsvFile(context, file, "Ledgr Backup Export")
                                } else {
                                    Toast.makeText(context, "Export error", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = EmeraldAccent)
                        }
                    }
                }
            }
        }
    }

    // Set PIN Dialog
    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text("Set 4-Digit Security PIN", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter a 4-digit numeric code to protect your ledger.")
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it },
                        label = { Text("4-Digit PIN") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (pinInput.length == 4) {
                            viewModel.updatePin(pinInput)
                            showPinDialog = false
                            pinInput = ""
                            Toast.makeText(context, "PIN successfully enabled!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                    enabled = pinInput.length == 4
                ) {
                    Text("Enable PIN", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Country & Currency Configuration Dialog
    if (showCurrencyDialog) {
        val filteredCountries = remember(countrySearchQuery) {
            if (countrySearchQuery.isBlank()) CountryCurrencyCatalog.supportedCountries
            else CountryCurrencyCatalog.supportedCountries.filter {
                it.name.contains(countrySearchQuery, ignoreCase = true) ||
                it.code.contains(countrySearchQuery, ignoreCase = true) ||
                it.defaultCurrency.contains(countrySearchQuery, ignoreCase = true)
            }
        }

        AlertDialog(
            onDismissRequest = { showCurrencyDialog = false },
            title = { Text("Country & Currency Settings", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Select Home Country:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                    OutlinedTextField(
                        value = countrySearchQuery,
                        onValueChange = { countrySearchQuery = it },
                        placeholder = { Text("Search country...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredCountries) { country ->
                            val isSelected = selectedCountryCode == country.code
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) EmeraldAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) EmeraldAccent else Color.Transparent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedCountryCode = country.code
                                        selectedCountryName = country.name
                                        homeCurrInput = country.defaultCurrency
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("${country.flagEmoji} ${country.name}", fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                                    Text(country.defaultCurrency, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = EmeraldAccent)
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

                    OutlinedTextField(
                        value = homeCurrInput,
                        onValueChange = { homeCurrInput = it.uppercase() },
                        label = { Text("Primary Currency Code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = expatCurrInput,
                        onValueChange = { expatCurrInput = it.uppercase() },
                        label = { Text("Secondary / Expat Currency") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (homeCurrInput.isNotBlank() && expatCurrInput.isNotBlank()) {
                            viewModel.updateCountryAndCurrency(
                                countryCode = selectedCountryCode,
                                countryName = selectedCountryName,
                                primaryCurr = homeCurrInput,
                                expatCurr = expatCurrInput
                            )
                            showCurrencyDialog = false
                            Toast.makeText(context, "Country & Currency updated app-wide!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCurrencyDialog = false }) { Text("Cancel") }
            }
        )
    }
}
