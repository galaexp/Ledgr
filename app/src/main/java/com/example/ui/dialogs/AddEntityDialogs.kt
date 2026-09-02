package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.neoPopOnColor

@Composable
fun AddAccountDialog(
    defaultCurrency: String = "USD",
    onDismiss: () -> Unit,
    onSave: (AccountEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var accountType by remember { mutableStateOf(AccountType.BANK) }
    var initialBalance by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf(defaultCurrency) }
    var countryProfile by remember { mutableStateOf("HOME") }
    var accountNumberMask by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Account / Wallet", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name") },
                    placeholder = { Text("e.g. Main Bank, Cash, Credit Card") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = initialBalance,
                        onValueChange = { initialBalance = it },
                        label = { Text("Initial Balance") },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it.uppercase() },
                        label = { Text("Currency") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(0.7f)
                    )
                }

                Text("Account Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(AccountType.values()) { type ->
                        FilterChip(
                            selected = accountType == type,
                            onClick = { accountType = type },
                            label = { Text(type.name.replace("_", " ")) }
                        )
                    }
                }

                Text("Profile Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = countryProfile == "HOME",
                        onClick = { countryProfile = "HOME" },
                        label = { Text("Home Base") }
                    )
                    FilterChip(
                        selected = countryProfile == "EXPAT",
                        onClick = { countryProfile = "EXPAT" },
                        label = { Text("Expat / Foreign") }
                    )
                }

                OutlinedTextField(
                    value = accountNumberMask,
                    onValueChange = { accountNumberMask = it },
                    label = { Text("Card/Account Mask (Optional)") },
                    placeholder = { Text("e.g. •••• 4921") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val acc = AccountEntity(
                            name = name,
                            type = accountType,
                            balance = initialBalance.toDoubleOrNull() ?: 0.0,
                            currency = currency,
                            countryProfile = countryProfile,
                            accountNumberMask = accountNumberMask
                        )
                        onSave(acc)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
            ) {
                Text("Add Account", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EditAccountDialog(
    account: AccountEntity,
    onDismiss: () -> Unit,
    onSave: (AccountEntity) -> Unit,
    onDelete: (AccountEntity) -> Unit
) {
    var name by remember { mutableStateOf(account.name) }
    var balanceText by remember { mutableStateOf(account.balance.toString()) }
    var currency by remember { mutableStateOf(account.currency) }
    var accountType by remember { mutableStateOf(account.type) }
    var countryProfile by remember { mutableStateOf(account.countryProfile) }
    var accountNumberMask by remember { mutableStateOf(account.accountNumberMask) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Account?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${account.name}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(account)
                        showDeleteConfirm = false
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Account", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = balanceText,
                        onValueChange = { balanceText = it },
                        label = { Text("Current Balance") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it.uppercase() },
                        label = { Text("Currency") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(0.7f)
                    )
                }

                Text("Account Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(AccountType.values()) { type ->
                        FilterChip(
                            selected = accountType == type,
                            onClick = { accountType = type },
                            label = { Text(type.name.replace("_", " ")) }
                        )
                    }
                }

                Text("Profile Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = countryProfile == "HOME",
                        onClick = { countryProfile = "HOME" },
                        label = { Text("Home Base") }
                    )
                    FilterChip(
                        selected = countryProfile == "EXPAT",
                        onClick = { countryProfile = "EXPAT" },
                        label = { Text("Expat / Foreign") }
                    )
                }

                OutlinedTextField(
                    value = accountNumberMask,
                    onValueChange = { accountNumberMask = it },
                    label = { Text("Card/Account Mask (Optional)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            val updated = account.copy(
                                name = name,
                                type = accountType,
                                balance = balanceText.toDoubleOrNull() ?: account.balance,
                                currency = currency,
                                countryProfile = countryProfile,
                                accountNumberMask = accountNumberMask
                            )
                            onSave(updated)
                            onDismiss()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddSavingsGoalDialog(
    accounts: List<AccountEntity>,
    activeProfile: CountryProfileType = CountryProfileType.HOME,
    defaultCurrency: String = "USD",
    onDismiss: () -> Unit,
    onSave: (SavingsGoalEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var currentAmount by remember { mutableStateOf("0") }
    var currency by remember { mutableStateOf(defaultCurrency) }
    var countryProfile by remember {
        mutableStateOf(if (activeProfile == CountryProfileType.EXPAT) "EXPAT" else "HOME")
    }
    var linkedAccountId by remember { mutableStateOf<Long?>(accounts.firstOrNull()?.id) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Savings Goal", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Goal Title") },
                    placeholder = { Text("e.g. Japan Trip 2027, Emergency Fund") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = targetAmount,
                        onValueChange = { targetAmount = it },
                        label = { Text("Target") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = currentAmount,
                        onValueChange = { currentAmount = it },
                        label = { Text("Saved So Far") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it.uppercase() },
                        label = { Text("Currency") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Text("Profile Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = countryProfile == "HOME",
                        onClick = { countryProfile = "HOME" },
                        label = { Text("Home Base") }
                    )
                    FilterChip(
                        selected = countryProfile == "EXPAT",
                        onClick = { countryProfile = "EXPAT" },
                        label = { Text("Expat / Foreign") }
                    )
                }

                if (accounts.isNotEmpty()) {
                    Text("Linked Wallet Account (Optional)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(accounts) { acc ->
                            FilterChip(
                                selected = linkedAccountId == acc.id,
                                onClick = { linkedAccountId = acc.id },
                                label = { Text(acc.name) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val target = targetAmount.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && target > 0) {
                        onSave(
                            SavingsGoalEntity(
                                title = title,
                                targetAmount = target,
                                currentAmount = currentAmount.toDoubleOrNull() ?: 0.0,
                                currency = currency,
                                countryProfile = countryProfile,
                                linkedAccountId = linkedAccountId
                            )
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
            ) {
                Text("Create Goal", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddDebtEmiDialog(
    friends: List<FriendEntity>,
    activeProfile: CountryProfileType = CountryProfileType.HOME,
    defaultCurrency: String = "USD",
    onDismiss: () -> Unit,
    onSave: (DebtEmiEntity) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var debtType by remember { mutableStateOf(DebtType.EMI) }
    var totalAmount by remember { mutableStateOf("") }
    var tenureMonths by remember { mutableStateOf("12") }
    var interestRate by remember { mutableStateOf("0") }
    var currency by remember { mutableStateOf(defaultCurrency) }
    var countryProfile by remember {
        mutableStateOf(if (activeProfile == CountryProfileType.EXPAT) "EXPAT" else "HOME")
    }
    var selectedFriendId by remember { mutableStateOf(friends.firstOrNull()?.id) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Loan / EMI / Debt", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Description") },
                    placeholder = { Text("e.g. MacBook Pro, Car Loan, Lent to Friend") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Debt Type", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(DebtType.EMI to "EMI", DebtType.LOAN to "Loan", DebtType.LENT to "Lent", DebtType.BORROWED to "Borrowed").forEach { (type, label) ->
                        FilterChip(
                            selected = debtType == type,
                            onClick = { debtType = type },
                            label = { Text(label) }
                        )
                    }
                }

                if (debtType == DebtType.LENT || debtType == DebtType.BORROWED) {
                    Text("Select Friend", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(friends) { friend ->
                            FilterChip(
                                selected = selectedFriendId == friend.id,
                                onClick = { selectedFriendId = friend.id },
                                label = { Text(friend.name) }
                            )
                        }
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = totalAmount,
                        onValueChange = { totalAmount = it },
                        label = { Text("Total Amount") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = currency,
                        onValueChange = { currency = it.uppercase() },
                        label = { Text("Currency") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(0.8f)
                    )
                }

                if (debtType == DebtType.EMI || debtType == DebtType.LOAN) {
                    OutlinedTextField(
                        value = tenureMonths,
                        onValueChange = { tenureMonths = it },
                        label = { Text("Tenure (Mos)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Text("Profile Category", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = countryProfile == "HOME",
                        onClick = { countryProfile = "HOME" },
                        label = { Text("Home Base") }
                    )
                    FilterChip(
                        selected = countryProfile == "EXPAT",
                        onClick = { countryProfile = "EXPAT" },
                        label = { Text("Expat / Foreign") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tot = totalAmount.toDoubleOrNull() ?: 0.0
                    val months = tenureMonths.toIntOrNull() ?: 12
                    val monthly = if (months > 0) tot / months else tot
                    if (title.isNotBlank() && tot > 0) {
                        onSave(
                            DebtEmiEntity(
                                title = title,
                                type = debtType,
                                friendId = if (debtType == DebtType.LENT || debtType == DebtType.BORROWED) selectedFriendId else null,
                                totalAmount = tot,
                                remainingAmount = tot,
                                tenureMonths = months,
                                monthlyPayment = monthly,
                                currency = currency,
                                countryProfile = countryProfile
                            )
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
            ) {
                Text("Save Debt / EMI", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun CrossProfileTransferDialog(
    accounts: List<AccountEntity>,
    exchangeRates: List<ExchangeRateEntity>,
    onDismiss: () -> Unit,
    onTransfer: (fromAccount: AccountEntity, toAccount: AccountEntity, fromAmount: Double, toAmount: Double, fxRate: Double, notes: String) -> Unit
) {
    var fromAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 0L) }
    var toAccountId by remember { mutableStateOf(accounts.getOrNull(1)?.id ?: accounts.firstOrNull()?.id ?: 0L) }
    var fromAmountText by remember { mutableStateOf("") }
    var customFxRateText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val fromAcc = accounts.find { it.id == fromAccountId }
    val toAcc = accounts.find { it.id == toAccountId }

    val rateMap = remember(exchangeRates) {
        exchangeRates.associate { "${it.fromCurrency.uppercase()}_${it.toCurrency.uppercase()}" to it.rate }
    }

    val defaultRate = remember(fromAcc, toAcc, rateMap) {
        if (fromAcc == null || toAcc == null || fromAcc.currency.equals(toAcc.currency, ignoreCase = true)) {
            1.0
        } else {
            val directKey = "${fromAcc.currency.uppercase()}_${toAcc.currency.uppercase()}"
            val directRate = rateMap[directKey]
            if (directRate != null && directRate > 0) directRate
            else {
                val inverseKey = "${toAcc.currency.uppercase()}_${fromAcc.currency.uppercase()}"
                val inv = rateMap[inverseKey]
                if (inv != null && inv > 0) 1.0 / inv else 1.0
            }
        }
    }

    val effectiveRate = customFxRateText.toDoubleOrNull() ?: defaultRate
    val fromAmount = fromAmountText.toDoubleOrNull() ?: 0.0
    val toAmount = fromAmount * effectiveRate

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.CurrencyExchange, contentDescription = null, tint = EmeraldAccent)
                Text("Cross-Profile Transfer", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Transfer funds between your Domestic and Expat accounts with automatic FX conversion.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // From Account
                Text("From Account", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(accounts) { acc ->
                        val isSelected = acc.id == fromAccountId
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                fromAccountId = acc.id
                                if (toAccountId == acc.id) {
                                    toAccountId = accounts.firstOrNull { it.id != acc.id }?.id ?: acc.id
                                }
                            },
                            label = { Text("${acc.name} (${acc.countryProfile} · ${acc.currency})") }
                        )
                    }
                }

                // To Account
                Text("To Account", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(accounts.filter { it.id != fromAccountId }) { acc ->
                        val isSelected = acc.id == toAccountId
                        FilterChip(
                            selected = isSelected,
                            onClick = { toAccountId = acc.id },
                            label = { Text("${acc.name} (${acc.countryProfile} · ${acc.currency})") }
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = fromAmountText,
                        onValueChange = { fromAmountText = it },
                        label = { Text("Send (${fromAcc?.currency ?: "USD"})") },
                        placeholder = { Text("0.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = if (customFxRateText.isNotEmpty()) customFxRateText else String.format(java.util.Locale.US, "%.4f", defaultRate),
                        onValueChange = { customFxRateText = it },
                        label = { Text("FX Rate") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Calculation preview
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Destination Receives:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "${toAcc?.currency ?: "USD"} ${String.format(java.util.Locale.US, "%,.2f", toAmount)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldAccent
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Transfer Note (Optional)") },
                    placeholder = { Text("e.g. Monthly Remittance, FX Transfer") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fromAcc != null && toAcc != null && fromAmount > 0 && effectiveRate > 0) {
                        onTransfer(fromAcc, toAcc, fromAmount, toAmount, effectiveRate, notes)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                enabled = fromAcc != null && toAcc != null && fromAmount > 0
            ) {
                Text("Execute Transfer", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddFriendDialog(
    onDismiss: () -> Unit,
    onSave: (FriendEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var paymentHandle by remember { mutableStateOf("") }
    val colorPalettes = listOf(0xFF10B981, 0xFF6366F1, 0xFF3B82F6, 0xFF8B5CF6, 0xFFF43F5E, 0xFFF59E0B, 0xFF14B8A6)
    var selectedColor by remember { mutableStateOf(colorPalettes[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Friend to Ledgr", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Friend's Full Name") },
                    placeholder = { Text("e.g. Alex Morgan") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone / Contact") },
                    placeholder = { Text("+1 (555) 000-0000") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = paymentHandle,
                    onValueChange = { paymentHandle = it },
                    label = { Text("UPI / Venmo / Revolut Tag") },
                    placeholder = { Text("@alex.morgan / alex@upi") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Avatar Color", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colorPalettes.forEach { colorHex ->
                        val isSelected = selectedColor == colorHex
                        val swatchColor = Color(colorHex)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(swatchColor)
                                .border(
                                    2.dp,
                                    if (isSelected) swatchColor.neoPopOnColor() else Color.Transparent,
                                    CircleShape
                                )
                                .clickable { selectedColor = colorHex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            FriendEntity(
                                name = name,
                                phone = phone,
                                avatarColorHex = selectedColor,
                                paymentHandle = paymentHandle
                            )
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
            ) {
                Text("Add Friend", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddGroupDialog(
    friends: List<FriendEntity>,
    onDismiss: () -> Unit,
    onSave: (GroupEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val emojiOptions = listOf("🏖️", "🏡", "♠️", "🚗", "🍕", "🎉", "✈️", "💡")
    var selectedEmoji by remember { mutableStateOf("🏖️") }
    val selectedMembers = remember { mutableStateListOf<Long>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Expense Group", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = selectedEmoji,
                        onValueChange = { selectedEmoji = it },
                        label = { Text("Emoji") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.width(70.dp)
                    )
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Group Name") },
                        placeholder = { Text("e.g. Goa Trip 2026, Flatmates") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    emojiOptions.forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { selectedEmoji = emoji }
                                .padding(4.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Add Friends to Roster", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(friends) { friend ->
                        val isSelected = selectedMembers.contains(friend.id)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (isSelected) selectedMembers.remove(friend.id) else selectedMembers.add(friend.id)
                            },
                            label = { Text(friend.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            GroupEntity(
                                name = name,
                                iconEmoji = selectedEmoji,
                                description = description,
                                memberFriendIds = selectedMembers.joinToString(",")
                            )
                        )
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
            ) {
                Text("Create Group", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
