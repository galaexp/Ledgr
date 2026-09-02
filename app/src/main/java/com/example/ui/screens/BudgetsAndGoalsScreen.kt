package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.BentoCard
import com.example.ui.components.RadialProgressRing
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.ExpenseRose
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.neoPopOnColor
import com.example.viewmodel.CategoryBudgetProgress
import com.example.viewmodel.LedgrViewModel
import java.util.Locale

enum class BudgetGoalTab {
    BUDGETS, SAVINGS, DEBTS_EMI
}

@Composable
fun BudgetsAndGoalsScreen(
    viewModel: LedgrViewModel,
    onOpenAddSavingsGoal: () -> Unit,
    onOpenAddDebtEmi: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(BudgetGoalTab.BUDGETS) }

    val categoryBudgets by viewModel.categoryBudgetsProgress.collectAsState()
    val savingsGoals by viewModel.filteredSavingsGoals.collectAsState()
    val debtEmis by viewModel.filteredDebtEmis.collectAsState()
    val accounts by viewModel.filteredAccounts.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val countryProfile by viewModel.countryProfile.collectAsState()

    var contributeGoalDialog by remember { mutableStateOf<SavingsGoalEntity?>(null) }
    var payDebtDialog by remember { mutableStateOf<DebtEmiEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Tab Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(
                BudgetGoalTab.BUDGETS to "Budgets",
                BudgetGoalTab.SAVINGS to "Savings Vaults",
                BudgetGoalTab.DEBTS_EMI to "Loans & EMI"
            ).forEach { (tab, title) ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) EmeraldAccent else Color.Transparent)
                        .clickable { selectedTab = tab }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) EmeraldAccent.neoPopOnColor() else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        when (selectedTab) {
            BudgetGoalTab.BUDGETS -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    item {
                        Text(
                            text = "Monthly Spending Limits by Category",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (categoryBudgets.isEmpty()) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.PieChart, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(36.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No category budgets configured", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("Categories will track your spending pace automatically.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        items(categoryBudgets) { item ->
                            CategoryBudgetCard(item = item, currencySymbol = currencySymbol)
                        }
                    }
                }
            }

            BudgetGoalTab.SAVINGS -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Savings Goals & Targets", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onOpenAddSavingsGoal) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Goal", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (savingsGoals.isEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenAddSavingsGoal() },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Savings, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No savings targets created yet", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Tap here to create a goal (e.g. Emergency Vault, Dream Vacation, New Car)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 96.dp)
                    ) {
                        items(savingsGoals) { goal ->
                            SavingsGoalCard(
                                goal = goal,
                                currencySymbol = currencySymbol,
                                onContributeClick = { contributeGoalDialog = goal }
                            )
                        }
                    }
                }
            }

            BudgetGoalTab.DEBTS_EMI -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Loans, EMIs & Borrowed Funds", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onOpenAddDebtEmi) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Debt/EMI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (debtEmis.isEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenAddDebtEmi() },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.CreditScore, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No active loans or EMIs", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Tap here to track home loans, auto loans, or student debt repayments", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 96.dp)
                    ) {
                        items(debtEmis) { debt ->
                            DebtEmiCard(
                                debt = debt,
                                currencySymbol = currencySymbol,
                                onPayClick = { payDebtDialog = debt }
                            )
                        }
                    }
                }
            }
        }
    }

    // Contribute to Savings Dialog
    contributeGoalDialog?.let { goal ->
        var contributeAmount by remember { mutableStateOf("100") }
        var selectedAccId by remember { mutableStateOf(accounts.firstOrNull()?.id) }

        AlertDialog(
            onDismissRequest = { contributeGoalDialog = null },
            title = { Text("Contribute to ${goal.title}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = contributeAmount,
                        onValueChange = { contributeAmount = it },
                        label = { Text("Amount ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (accounts.isNotEmpty()) {
                        Text("Deduct from Account", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            accounts.forEach { acc ->
                                FilterChip(
                                    selected = selectedAccId == acc.id,
                                    onClick = { selectedAccId = acc.id },
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
                        val amt = contributeAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.addSavingsGoalContribution(goal.id, amt, selectedAccId)
                            contributeGoalDialog = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                ) {
                    Text("Add Contribution", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { contributeGoalDialog = null }) { Text("Cancel") }
            }
        )
    }

    // Pay EMI Dialog
    payDebtDialog?.let { debt ->
        var payAmount by remember { mutableStateOf(String.format(Locale.US, "%.2f", debt.monthlyPayment)) }
        var selectedAccId by remember { mutableStateOf(accounts.firstOrNull()?.id) }

        AlertDialog(
            onDismissRequest = { payDebtDialog = null },
            title = { Text("Record Payment for ${debt.title}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = payAmount,
                        onValueChange = { payAmount = it },
                        label = { Text("Payment Amount ($currencySymbol)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (accounts.isNotEmpty()) {
                        Text("Deduct from Account", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            accounts.forEach { acc ->
                                FilterChip(
                                    selected = selectedAccId == acc.id,
                                    onClick = { selectedAccId = acc.id },
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
                        val amt = payAmount.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            viewModel.recordDebtPayment(debt.id, amt, selectedAccId)
                            payDebtDialog = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent)
                ) {
                    Text("Record Payment", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { payDebtDialog = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun CategoryBudgetCard(item: CategoryBudgetProgress, currencySymbol: String) {
    val catColor = Color(item.category.colorHex)
    val pct = (item.percentage * 100).toInt()
    val isOver = item.percentage >= 1.0f

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(catColor.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(catColor)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.category.name,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isOver && item.limitAmount > 0) {
                            Text("Exceeded", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ExpenseRose)
                        }
                    }

                    Text(
                        text = if (item.limitAmount > 0)
                            "$currencySymbol${String.format(Locale.US, "%.2f", item.spentAmount)} spent of $currencySymbol${String.format(Locale.US, "%.0f", item.limitAmount)} limit"
                        else
                            "$currencySymbol${String.format(Locale.US, "%.2f", item.spentAmount)} spent (No limit)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Linear progress bar
                    if (item.limitAmount > 0) {
                        LinearProgressIndicator(
                            progress = item.percentage.coerceIn(0f, 1f),
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (isOver) ExpenseRose else if (item.percentage >= 0.8f) WarningAmber else EmeraldAccent,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            if (item.limitAmount > 0) {
                RadialProgressRing(
                    progress = item.percentage,
                    activeColor = catColor,
                    size = 46.dp,
                    strokeWidth = 5.dp
                )
            }
        }
    }
}

@Composable
fun SavingsGoalCard(
    goal: SavingsGoalEntity,
    currencySymbol: String,
    onContributeClick: () -> Unit
) {
    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f
    val isCompleted = goal.currentAmount >= goal.targetAmount

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
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
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                RadialProgressRing(
                    progress = progress,
                    activeColor = EmeraldAccent,
                    size = 54.dp,
                    strokeWidth = 6.dp
                )

                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = goal.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (goal.countryProfile == "EXPAT") "EXPAT" else "HOME",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (goal.countryProfile == "EXPAT") MaterialTheme.colorScheme.tertiary else EmeraldAccent,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    Text(
                        text = "$currencySymbol${String.format(Locale.US, "%,.2f", goal.currentAmount)} of $currencySymbol${String.format(Locale.US, "%,.2f", goal.targetAmount)}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Button(
                onClick = onContributeClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Deposit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DebtEmiCard(
    debt: DebtEmiEntity,
    currencySymbol: String,
    onPayClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = debt.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = debt.type.name,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldAccent,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = if (debt.countryProfile == "EXPAT") "EXPAT" else "HOME",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (debt.countryProfile == "EXPAT") MaterialTheme.colorScheme.tertiary else EmeraldAccent,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "Remaining: $currencySymbol${String.format(Locale.US, "%,.2f", debt.remainingAmount)} · EMI: $currencySymbol${String.format(Locale.US, "%.2f", debt.monthlyPayment)}/mo",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onPayClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text("Pay EMI", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
