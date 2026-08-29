package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CountryCurrencyCatalog
import com.example.data.model.CountryInfo
import com.example.data.model.CurrencyInfo
import com.example.ui.components.BentoCard
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.EmeraldGlow
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianSurface
import com.example.viewmodel.LedgrViewModel

@Composable
fun LoginOnboardingScreen(
    viewModel: LedgrViewModel,
    onLoginSuccess: () -> Unit
) {
    var currentStep by remember { mutableStateOf(1) } // Step 1: Login/Profile, Step 2: Country & Currency

    // Step 1 States
    var nameInput by remember { mutableStateOf("") }
    var emailInput by remember { mutableStateOf("") }
    var pinInput by remember { mutableStateOf("") }
    var step1Error by remember { mutableStateOf<String?>(null) }

    // Step 2 States
    var selectedCountry by remember {
        mutableStateOf(
            CountryCurrencyCatalog.supportedCountries.firstOrNull { it.code == "US" }
                ?: CountryCurrencyCatalog.supportedCountries.first()
        )
    }
    var selectedPrimaryCurrency by remember { mutableStateOf("USD") }
    var selectedExpatCurrency by remember { mutableStateOf("EUR") }
    var initialAccountName by remember { mutableStateOf("Main Checking") }
    var initialBalanceInput by remember { mutableStateOf("0.00") }
    var countrySearchQuery by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Brand Header with Glowing Emblem
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(EmeraldAccent.copy(alpha = 0.15f))
                    .border(1.5.dp, EmeraldAccent, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text("⬡", fontSize = 32.sp, color = EmeraldAccent, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "LEDGR",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "Offline-First Personal Finance & Split Engine",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // Step Indicator Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(1 to "1. Profile & Login", 2 to "2. Country & Currency").forEach { (step, label) ->
                    val isCurrent = currentStep == step
                    val isPast = currentStep > step
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (isCurrent) EmeraldAccent
                                else if (isPast) EmeraldAccent.copy(alpha = 0.3f)
                                else Color.Transparent
                            )
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (isPast) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isCurrent || isPast) FontWeight.Bold else FontWeight.Medium,
                                color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Step 1: Login & Profile
            AnimatedVisibility(
                visible = currentStep == 1,
                enter = fadeIn() + slideInHorizontally(),
                exit = fadeOut() + slideOutHorizontally()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        Text(
                            text = "Welcome to Ledgr",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Create your offline vault or log in to manage your local accounts, budgets, and split debts.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = nameInput,
                            onValueChange = {
                                nameInput = it
                                step1Error = null
                            },
                            label = { Text("Your Full Name *") },
                            placeholder = { Text("e.g. Alex Morgan") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = {
                                emailInput = it
                                step1Error = null
                            },
                            label = { Text("Email Address *") },
                            placeholder = { Text("alex@example.com") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = pinInput,
                            onValueChange = {
                                if (it.length <= 4 && it.all { c -> c.isDigit() }) pinInput = it
                            },
                            label = { Text("4-Digit Security PIN (Optional)") },
                            placeholder = { Text("e.g. 1234") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "If provided, Ledgr will require this PIN each time you open the app.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                        )
                    }

                    if (step1Error != null) {
                        item {
                            Text(
                                text = step1Error!!,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = {
                                if (nameInput.trim().isBlank()) {
                                    step1Error = "Please enter your name to proceed."
                                } else if (emailInput.trim().isBlank() || !emailInput.contains("@")) {
                                    step1Error = "Please enter a valid email address."
                                } else {
                                    step1Error = null
                                    currentStep = 2
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                        ) {
                            Text(
                                text = "Next: Country & Currency →",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // Step 2: Country & Currency Selection
            AnimatedVisibility(
                visible = currentStep == 2,
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { it }),
                exit = fadeOut() + slideOutHorizontally(targetOffsetX = { it })
            ) {
                val filteredCountries = remember(countrySearchQuery) {
                    if (countrySearchQuery.isBlank()) {
                        CountryCurrencyCatalog.supportedCountries
                    } else {
                        CountryCurrencyCatalog.supportedCountries.filter {
                            it.name.contains(countrySearchQuery, ignoreCase = true) ||
                                    it.code.contains(countrySearchQuery, ignoreCase = true) ||
                                    it.defaultCurrency.contains(countrySearchQuery, ignoreCase = true)
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        Text(
                            text = "Select Country & Base Currency",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Pick your home country to set your primary currency symbol, formats, and default bank profile.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Country Search Bar
                    item {
                        OutlinedTextField(
                            value = countrySearchQuery,
                            onValueChange = { countrySearchQuery = it },
                            placeholder = { Text("Search country (e.g. US, UK, India, Germany)...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Country Picker Carousel / Chips
                    item {
                        Text("Selected Country", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldAccent)
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, EmeraldAccent),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(selectedCountry.flagEmoji, fontSize = 28.sp)
                                    Column {
                                        Text(
                                            selectedCountry.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            "Default Currency: ${selectedCountry.defaultCurrency} (${selectedCountry.currencySymbol})",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldAccent)
                            }
                        }
                    }

                    // Countries List Grid / Row
                    item {
                        Text("All Countries", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(filteredCountries) { country ->
                                val isSelected = country.code == selectedCountry.code
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) EmeraldAccent.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) EmeraldAccent else Color.Transparent
                                    ),
                                    modifier = Modifier.clickable {
                                        selectedCountry = country
                                        selectedPrimaryCurrency = country.defaultCurrency
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(country.flagEmoji, fontSize = 18.sp)
                                        Text(
                                            text = country.name,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) EmeraldAccent else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Primary Currency Selector
                    item {
                        Text("Primary Base Currency", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(CountryCurrencyCatalog.supportedCurrencies) { curr ->
                                val isSelected = selectedPrimaryCurrency == curr.code
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) EmeraldAccent else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.clickable {
                                        selectedPrimaryCurrency = curr.code
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(curr.flagEmoji, fontSize = 14.sp)
                                        Text(
                                            text = "${curr.code} (${curr.symbol})",
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Secondary / Expat Currency
                    item {
                        Text("Second / Expat Currency (For Cross-border conversions)", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(CountryCurrencyCatalog.supportedCurrencies) { curr ->
                                val isSelected = selectedExpatCurrency == curr.code
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
                                    modifier = Modifier.clickable {
                                        selectedExpatCurrency = curr.code
                                    }
                                ) {
                                    Text(
                                        text = "${curr.flagEmoji} ${curr.code}",
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) EmeraldAccent else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Initial Account Name & Opening Balance
                    item {
                        Text("Initial Account Setup", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = initialAccountName,
                                onValueChange = { initialAccountName = it },
                                label = { Text("Account Name") },
                                placeholder = { Text("Main Checking") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1.3f)
                            )
                            OutlinedTextField(
                                value = initialBalanceInput,
                                onValueChange = { initialBalanceInput = it },
                                label = { Text("Opening (${selectedPrimaryCurrency})") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Navigation Buttons
                    item {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { currentStep = 1 },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(0.7f)
                                    .height(52.dp)
                            ) {
                                Text("← Back")
                            }

                            Button(
                                onClick = {
                                    val startingBalance = initialBalanceInput.toDoubleOrNull() ?: 0.0
                                    viewModel.completeLoginAndSetup(
                                        name = nameInput.trim(),
                                        email = emailInput.trim(),
                                        countryCode = selectedCountry.code,
                                        countryName = selectedCountry.name,
                                        primaryCurr = selectedPrimaryCurrency,
                                        expatCurr = selectedExpatCurrency,
                                        initialAccountName = initialAccountName.trim(),
                                        initialBalance = startingBalance,
                                        pin = pinInput.takeIf { it.isNotBlank() }
                                    )
                                    onLoginSuccess()
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(52.dp)
                            ) {
                                Text(
                                    text = "Launch Ledgr Home →",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
