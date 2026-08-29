package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.ExpenseRose
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.LocalCustomColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionBottomSheet(
    accounts: List<AccountEntity>,
    categories: List<CategoryEntity>,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit,
    onScanReceiptClick: () -> Unit,
    onSplitBillClick: (Double, String) -> Unit,
    prefilledAmount: Double? = null,
    prefilledMerchant: String? = null,
    prefilledDate: Long? = null
) {
    val customColors = LocalCustomColors.current
    var transactionType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amountInput by remember { mutableStateOf(prefilledAmount?.let { String.format("%.2f", it) } ?: "") }
    var notesInput by remember { mutableStateOf(prefilledMerchant ?: "") }
    var tagsInput by remember { mutableStateOf("") }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id ?: 1L) }
    var targetAccountId by remember { mutableStateOf(accounts.getOrNull(1)?.id ?: 2L) }

    val expenseCategories = categories.filter { !it.isIncome }
    val incomeCategories = categories.filter { it.isIncome }
    var selectedCategoryId by remember {
        mutableStateOf(expenseCategories.firstOrNull()?.id ?: 1L)
    }

    var isSplitExpense by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = customColors.bentoCardBg,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outline) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header + OCR Scanner Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New Transaction",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Scan Receipt Quick Action
                FilledTonalButton(
                    onClick = onScanReceiptClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = EmeraldAccent.copy(alpha = 0.15f),
                        contentColor = EmeraldAccent
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DocumentScanner,
                        contentDescription = "Scan Receipt",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scan Receipt", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            // Transaction Type Selector (Expense / Income / Transfer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf(
                    TransactionType.EXPENSE to "Expense",
                    TransactionType.INCOME to "Income",
                    TransactionType.TRANSFER to "Transfer"
                ).forEach { (type, label) ->
                    val isSelected = transactionType == type
                    val activeColor = when (type) {
                        TransactionType.EXPENSE -> ExpenseRose
                        TransactionType.INCOME -> IncomeGreen
                        TransactionType.TRANSFER -> EmeraldAccent
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) activeColor else Color.Transparent)
                            .clickable {
                                transactionType = type
                                if (type == TransactionType.INCOME) {
                                    selectedCategoryId = incomeCategories.firstOrNull()?.id ?: 11L
                                } else {
                                    selectedCategoryId = expenseCategories.firstOrNull()?.id ?: 1L
                                }
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Amount Input Card
            val selectedAccount = accounts.find { it.id == selectedAccountId }
            val currencySymbol = when (selectedAccount?.currency) {
                "EUR" -> "€"
                "GBP" -> "£"
                "INR" -> "₹"
                "AED" -> "AED "
                "JPY" -> "¥"
                else -> "$"
            }

            OutlinedTextField(
                value = amountInput,
                onValueChange = { amountInput = it },
                label = { Text("Amount ($currencySymbol)") },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Text(
                        text = currencySymbol,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldAccent,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            )

            // Account Selection
            Text(
                text = if (transactionType == TransactionType.TRANSFER) "From Account" else "Account",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(accounts) { acc ->
                    val isSelected = acc.id == selectedAccountId
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedAccountId = acc.id },
                        label = { Text("${acc.name} (${acc.currency})") },
                        leadingIcon = {
                            Icon(
                                imageVector = when (acc.type) {
                                    AccountType.BANK -> Icons.Default.AccountBalance
                                    AccountType.CREDIT_CARD -> Icons.Default.CreditCard
                                    AccountType.DIGITAL_WALLET -> Icons.Default.AccountBalanceWallet
                                    AccountType.CASH -> Icons.Default.Payments
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }

            // Target Account for Transfer
            if (transactionType == TransactionType.TRANSFER) {
                Text(
                    text = "To Account",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(accounts.filter { it.id != selectedAccountId }) { acc ->
                        val isSelected = acc.id == targetAccountId
                        FilterChip(
                            selected = isSelected,
                            onClick = { targetAccountId = acc.id },
                            label = { Text("${acc.name} (${acc.currency})") }
                        )
                    }
                }
            }

            // Categories (if not transfer)
            if (transactionType != TransactionType.TRANSFER) {
                val currentCats = if (transactionType == TransactionType.INCOME) incomeCategories else expenseCategories
                Text(
                    text = "Category",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(currentCats) { cat ->
                        val isSelected = cat.id == selectedCategoryId
                        val catColor = Color(cat.colorHex)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) catColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) catColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.clickable { selectedCategoryId = cat.id }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(catColor)
                                )
                                Text(
                                    text = cat.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Description / Merchant Notes
            OutlinedTextField(
                value = notesInput,
                onValueChange = { notesInput = it },
                label = { Text("Description / Merchant") },
                placeholder = { Text("e.g. Whole Foods Organic, Dinner") },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Tags
            OutlinedTextField(
                value = tagsInput,
                onValueChange = { tagsInput = it },
                label = { Text("Tags (comma separated)") },
                placeholder = { Text("e.g. Dining, Vacation, TaxDeductible") },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // "Split this Expense" Option (Only for Expenses)
            if (transactionType == TransactionType.EXPENSE) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.GroupAdd,
                                contentDescription = "Split",
                                tint = EmeraldAccent
                            )
                            Column {
                                Text(
                                    text = "Split this expense",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Divide bill with friends or group",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Button(
                            onClick = {
                                val amt = amountInput.toDoubleOrNull() ?: 0.0
                                onSplitBillClick(amt, notesInput.ifEmpty { "Shared Expense" })
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                        ) {
                            Text("Setup Split", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Save Action Button
            Button(
                onClick = {
                    val amount = amountInput.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        val acc = accounts.find { it.id == selectedAccountId }
                        val tx = TransactionEntity(
                            accountId = selectedAccountId,
                            type = transactionType,
                            amount = amount,
                            currency = acc?.currency ?: "USD",
                            categoryId = if (transactionType == TransactionType.TRANSFER) 4L else selectedCategoryId,
                            date = prefilledDate ?: System.currentTimeMillis(),
                            notes = notesInput.ifEmpty { if (transactionType == TransactionType.TRANSFER) "Account Transfer" else "Transaction" },
                            tags = tagsInput,
                            targetAccountId = if (transactionType == TransactionType.TRANSFER) targetAccountId else null,
                            countryProfile = acc?.countryProfile ?: "HOME"
                        )
                        onSave(tx)
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = (amountInput.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text(
                    text = "Save Transaction",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
