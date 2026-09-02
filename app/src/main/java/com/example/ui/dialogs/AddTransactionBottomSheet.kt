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
import com.example.ui.components.NeoPopButton
import com.example.ui.components.NeoPopButtonStyle
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.ExpenseRose
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.LocalCustomColors
import com.example.ui.theme.neoPopOnColor

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

    val selectedAccount = accounts.find { it.id == selectedAccountId }
    val currencySymbol = when (selectedAccount?.currency) {
        "EUR" -> "€"
        "GBP" -> "£"
        "INR" -> "₹"
        "AED" -> "AED "
        "JPY" -> "¥"
        else -> "$"
    }
    val parsedAmount = amountInput.toDoubleOrNull() ?: 0.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = customColors.bentoCardBg,
        dragHandle = { BottomSheetDefaults.DragHandle(color = customColors.bentoBorderStrong) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "New Transaction",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.2.sp,
                    color = customColors.textPrimary
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(customColors.bentoCardElevated)
                        .border(2.dp, EmeraldAccent, RoundedCornerShape(4.dp))
                        .clickable(onClick = onScanReceiptClick)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = Icons.Default.DocumentScanner,
                            contentDescription = "Scan Receipt",
                            tint = EmeraldAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Text("Scan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldAccent)
                    }
                }
            }

            // Transaction Type — flat NeoPOP block segments, contrast-safe text
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(4.dp))
                    .background(customColors.bentoCardElevated)
                    .border(2.dp, customColors.bentoBorderStrong, RoundedCornerShape(4.dp))
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
                            .clip(RoundedCornerShape(2.dp))
                            .background(if (isSelected) activeColor else Color.Transparent)
                            .clickable {
                                transactionType = type
                                selectedCategoryId = if (type == TransactionType.INCOME) {
                                    incomeCategories.firstOrNull()?.id ?: 11L
                                } else {
                                    expenseCategories.firstOrNull()?.id ?: 1L
                                }
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) activeColor.neoPopOnColor() else customColors.textMuted
                        )
                    }
                }
            }

            // Amount — the hero element. A flat extruded block, not a generic text field.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(customColors.bentoCardElevated)
                    .border(2.dp, customColors.bentoBorderStrong, RoundedCornerShape(6.dp))
                    .padding(horizontal = 18.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "AMOUNT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = customColors.textTertiary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currencySymbol,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = customColors.textMuted,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    BasicAmountField(
                        value = amountInput,
                        onValueChange = { new ->
                            // Allow only digits and a single decimal point, max 2 decimal places.
                            if (new.isEmpty() || new.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                amountInput = new
                            }
                        },
                        textColor = customColors.textPrimary
                    )
                }
            }

            // Account Selection
            Text(
                text = if (transactionType == TransactionType.TRANSFER) "FROM ACCOUNT" else "ACCOUNT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.6.sp,
                color = customColors.textTertiary
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(accounts) { acc ->
                    val isSelected = acc.id == selectedAccountId
                    NeoPopChip(
                        label = "${acc.name} (${acc.currency})",
                        selected = isSelected,
                        accentColor = EmeraldAccent,
                        onClick = { selectedAccountId = acc.id },
                        leadingIcon = when (acc.type) {
                            AccountType.BANK -> Icons.Default.AccountBalance
                            AccountType.CREDIT_CARD -> Icons.Default.CreditCard
                            AccountType.DIGITAL_WALLET -> Icons.Default.AccountBalanceWallet
                            AccountType.CASH -> Icons.Default.Payments
                        }
                    )
                }
            }

            // Target Account for Transfer
            if (transactionType == TransactionType.TRANSFER) {
                Text(
                    text = "TO ACCOUNT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = customColors.textTertiary
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(accounts.filter { it.id != selectedAccountId }) { acc ->
                        val isSelected = acc.id == targetAccountId
                        NeoPopChip(
                            label = "${acc.name} (${acc.currency})",
                            selected = isSelected,
                            accentColor = EmeraldAccent,
                            onClick = { targetAccountId = acc.id }
                        )
                    }
                }
            }

            // Categories (if not transfer)
            if (transactionType != TransactionType.TRANSFER) {
                val currentCats = if (transactionType == TransactionType.INCOME) incomeCategories else expenseCategories
                Text(
                    text = "CATEGORY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    color = customColors.textTertiary
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(currentCats) { cat ->
                        val isSelected = cat.id == selectedCategoryId
                        val catColor = Color(cat.colorHex)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (isSelected) catColor else customColors.bentoCardElevated)
                                .border(2.dp, if (isSelected) catColor else customColors.bentoBorder, RoundedCornerShape(4.dp))
                                .clickable { selectedCategoryId = cat.id }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                if (!isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(catColor)
                                    )
                                }
                                Text(
                                    text = cat.name,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) catColor.neoPopOnColor() else customColors.textMuted
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
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Tags
            OutlinedTextField(
                value = tagsInput,
                onValueChange = { tagsInput = it },
                label = { Text("Tags (comma separated)") },
                placeholder = { Text("e.g. Dining, Vacation, TaxDeductible") },
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // "Split this Expense" Option (Only for Expenses)
            if (transactionType == TransactionType.EXPENSE) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(customColors.bentoCardElevated)
                        .border(2.dp, customColors.bentoBorder, RoundedCornerShape(6.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
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
                                color = customColors.textPrimary
                            )
                            Text(
                                text = "Divide bill with friends or group",
                                fontSize = 11.sp,
                                color = customColors.textMuted
                            )
                        }
                    }

                    NeoPopButton(
                        onClick = {
                            onSplitBillClick(parsedAmount, notesInput.ifEmpty { "Shared Expense" })
                        },
                        style = NeoPopButtonStyle.SECONDARY,
                        depth = 4.dp,
                        contentPaddingHorizontal = 14.dp,
                        contentPaddingVertical = 8.dp
                    ) {
                        Text("Setup Split", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Save Action
            NeoPopButton(
                onClick = {
                    if (parsedAmount > 0) {
                        val acc = accounts.find { it.id == selectedAccountId }
                        val tx = TransactionEntity(
                            accountId = selectedAccountId,
                            type = transactionType,
                            amount = parsedAmount,
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
                style = NeoPopButtonStyle.PRIMARY,
                enabled = parsedAmount > 0,
                modifier = Modifier.fillMaxWidth(),
                contentPaddingVertical = 16.dp
            ) {
                Text(
                    text = "Save Transaction",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/**
 * Large borderless numeric field used inside the amount block — no
 * underline/outline of its own since the surrounding block already
 * carries the NeoPOP border.
 */
@Composable
private fun BasicAmountField(
    value: String,
    onValueChange: (String) -> Unit,
    textColor: Color
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        textStyle = LocalTextStyle.current.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 34.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor
        ),
        cursorBrush = androidx.compose.ui.graphics.SolidColor(textColor),
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text(
                    text = "0.00",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor.copy(alpha = 0.3f)
                )
            }
            inner()
        }
    )
}

@Composable
private fun NeoPopChip(
    label: String,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null
) {
    val customColors = LocalCustomColors.current
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (selected) accentColor else customColors.bentoCardElevated)
            .border(2.dp, if (selected) accentColor else customColors.bentoBorder, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (selected) accentColor.neoPopOnColor() else customColors.textMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) accentColor.neoPopOnColor() else customColors.textMuted
            )
        }
    }
}
