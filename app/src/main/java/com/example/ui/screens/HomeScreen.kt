package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.domain.FriendBalanceSummary
import com.example.ui.components.AnimatedBalanceCounter
import com.example.ui.components.BentoCard
import com.example.ui.components.CredCountryProfileSegmentedBar
import com.example.ui.components.FriendAvatar
import com.example.ui.components.RadialProgressRing
import com.example.ui.dialogs.EditAccountDialog
import com.example.ui.theme.*
import com.example.viewmodel.LedgrViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ──────────────────────────────────────────────────────────────
// Design constants
// ──────────────────────────────────────────────────────────────
private val Elevation = 2.dp
private val SectionSpacing = 16.dp
private val CardRadius = 6.dp
private val PillRadius = 8.dp
private object HomeEntrance {
    val Visible = tween<Float>(220, easing = LinearOutSlowInEasing)
    val Slide = tween<Float>(260, easing = LinearOutSlowInEasing)
}

// ──────────────────────────────────────────────────────────────
// Formatting helpers
// ──────────────────────────────────────────────────────────────
private fun formatMoney(symbol: String, amount: Double): String =
    String.format(Locale.US, "%s%,.2f", symbol, amount)

private fun formatSigned(symbol: String, amount: Double, sign: Char): String =
    "$sign${formatMoney(symbol, if (amount < 0) -amount else amount)}"

// ──────────────────────────────────────────────────────────────
// Entry point
// ──────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(
    viewModel: LedgrViewModel,
    onNavigateToTransactions: () -> Unit,
    onNavigateToSplits: () -> Unit,
    onNavigateToBudgets: () -> Unit,
    onOpenAddTransaction: () -> Unit,
    onOpenSplitBill: () -> Unit,
    onOpenScanReceipt: () -> Unit,
    onOpenSettleUp: () -> Unit,
    onOpenAddAccount: () -> Unit
) {
    // ── State collection ───────────────────────────────────────
    val userName           by viewModel.userName.collectAsState()
    val activeCountryName  by viewModel.activeCountryName.collectAsState()
    val activeCountryFlag  by viewModel.activeCountryFlag.collectAsState()
    val activeCurrency     by viewModel.activeCurrency.collectAsState()
    val activeCurrencySym  by viewModel.activeCurrencySymbol.collectAsState()
    val primaryCurrency    by viewModel.primaryCurrency.collectAsState()
    val expatCurrency      by viewModel.expatCurrency.collectAsState()
    val countryProfile     by viewModel.countryProfile.collectAsState()

    val netWorth        by viewModel.netWorthSummary.collectAsState()
    val accounts        by viewModel.filteredAccounts.collectAsState()
    val transactions    by viewModel.filteredTransactions.collectAsState()
    val categories       by viewModel.categories.collectAsState(initial = emptyList())
    val spendingPace    by viewModel.spendingPace.collectAsState()
    val friendBalances  by viewModel.friendBalances.collectAsState()
    val splitSummary    by viewModel.netSplitSummary.collectAsState()
    val savingsGoals     by viewModel.filteredSavingsGoals.collectAsState()
    val youAreOwed = splitSummary.first
    val youOwe     = splitSummary.second

    // ── Local UI state ──────────────────────────────────────────
    var isBalanceHidden by rememberSaveable { mutableStateOf(false) }
    var accountToEdit   by remember { mutableStateOf<AccountEntity?>(null) }
    val haptics          = LocalHapticFeedback.current

    // Derived, computed once per state change — never recomputed in composition.
    val categoryMap by remember(categories) {
        derivedStateOf { categories.associateBy { it.id } }
    }
    val txDateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.US) }

    // ── Dialogs ─────────────────────────────────────────────────
    accountToEdit?.let { current ->
        EditAccountDialog(
            account = current,
            onDismiss = { accountToEdit = null },
            onSave = { updated ->
                viewModel.updateAccount(updated)
                accountToEdit = null
            },
            onDelete = { acc ->
                viewModel.deleteAccount(acc)
                accountToEdit = null
            }
        )
    }

    // ── Scaffolding layout ─────────────────────────────────────
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = "Home dashboard" },
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(SectionSpacing)
    ) {
        // 1 ─ Greeting + privacy toggle
        item(key = "header") {
            HomeHeader(
                userName = userName,
                activeCountryFlag = activeCountryFlag,
                activeCountryName = activeCountryName,
                activeCurrency = activeCurrency,
                isBalanceHidden = isBalanceHidden,
                onTogglePrivacy = { isBalanceHidden = !isBalanceHidden }
            )
        }

        // 2 ─ Country profile switcher
        item(key = "profile_switcher") {
            CredCountryProfileSegmentedBar(
                currentProfile = countryProfile,
                homeCurrency = primaryCurrency,
                expatCurrency = expatCurrency,
                homeFlag = activeCountryFlag,
                onProfileSelected = { viewModel.setCountryProfile(it) }
            )
        }

        // 3 ─ Hero net worth
        item(key = "net_worth_hero") {
            NetWorthHeroCard(
                netWorth = netWorth,
                currencySymbol = activeCurrencySym,
                isBalanceHidden = isBalanceHidden,
                countryProfile = countryProfile,
                activeCountryFlag = activeCountryFlag,
                youAreOwed = youAreOwed,
                youOwe = youOwe,
                onOpenSplits = onNavigateToSplits
            )
        }

        // 4 ─ Quick actions
        item(key = "quick_actions") {
            QuickActionsRow(
                onAddTransaction = onOpenAddTransaction,
                onSplitBill = onOpenSplitBill,
                onScanReceipt = onOpenScanReceipt,
                onSettleUp = onOpenSettleUp,
                haptics = haptics
            )
        }

        // 5 ─ Accounts section
        item(key = "accounts") {
            AccountsSection(
                accounts = accounts,
                isBalanceHidden = isBalanceHidden,
                onAddAccount = onOpenAddAccount,
                onEditAccount = { accountToEdit = it }
            )
        }

        // 6 ─ Spending pace gauge
        item(key = "spending_pace") {
            SpendingPaceCard(
                spent = spendingPace.spentThisMonth,
                budget = spendingPace.monthlyBudgetLimit,
                projected = spendingPace.projectedMonthEndSpend,
                isPaceSafe = spendingPace.isPaceSafe,
                currencySymbol = activeCurrencySym,
                onClick = onNavigateToBudgets
            )
        }

        // 7 ─ Split balances (conditional)
        if (friendBalances.isNotEmpty()) {
            item(key = "split_balances") {
                SplitBalancesSection(
                    friends = friendBalances,
                    currencySymbol = activeCurrencySym,
                    onSeeAll = onNavigateToSplits,
                    onPick = onNavigateToSplits
                )
            }
        }

        // 8 ─ Savings vaults (conditional)
        if (savingsGoals.isNotEmpty()) {
            item(key = "savings_vaults") {
                SavingsVaultsSection(
                    goals = savingsGoals,
                    currencySymbol = activeCurrencySym,
                    onViewAll = onNavigateToBudgets,
                    onPick = onNavigateToBudgets
                )
            }
        }

        // 9 ─ Recent transactions
        item(key = "recent_tx_header") {
            SectionHeader(
                title = "Recent Transactions",
                subtitle = "${transactions.size} total",
                actionLabel = "See All",
                onAction = onNavigateToTransactions
            )
        }

        if (transactions.isEmpty()) {
            item(key = "empty_tx") {
                EmptyStateCard(
                    icon = Icons.Default.ReceiptLong,
                    title = "No transactions in this profile",
                    subtitle = "Tap + to record your first expense or income",
                    onClick = onOpenAddTransaction
                )
            }
        } else {
            itemsIndexed(
                items = transactions.take(5),
                key = { _, tx -> "tx_${tx.id}" }
            ) { index, tx ->
                val category = categoryMap[tx.categoryId]
                TransactionRow(
                    tx = tx,
                    category = category?.name ?: "General",
                    currencySymbol = activeCurrencySym,
                    dateLabel = txDateFormat.format(Date(tx.date)),
                    onClick = onNavigateToTransactions,
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

// ══════════════════════════════════════════════════════════════
// SECTION COMPOSABLES
// ══════════════════════════════════════════════════════════════

// 1 ─ Header
@Composable
private fun HomeHeader(
    userName: String,
    activeCountryFlag: String,
    activeCountryName: String,
    activeCurrency: String,
    isBalanceHidden: Boolean,
    onTogglePrivacy: () -> Unit
) {
    val colors = MaterialTheme.customColors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(colors.bentoCardElevated)
                    .border(Elevation, CredMint.copy(alpha = 0.4f), CircleShape)
                    .semantics { contentDescription = "Active country $activeCountryName" },
                contentAlignment = Alignment.Center
            ) {
                Text(text = activeCountryFlag, fontSize = 20.sp)
            }
            Column {
                Text(
                    text = if (userName.isNotBlank()) "Hello, $userName" else "Welcome to LEDGR",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    letterSpacing = 0.2.sp
                )
                Text(
                    text = "$activeCountryFlag $activeCountryName • $activeCurrency",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.textMuted
                )
            }
        }

        IconButton(
            onClick = onTogglePrivacy,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colors.bentoCardElevated)
                .border(Elevation, colors.bentoBorder, CircleShape)
                .semantics {
                    contentDescription = if (isBalanceHidden) "Show balances" else "Hide balances"
                }
        ) {
            Icon(
                imageVector = if (isBalanceHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = null,
                tint = if (isBalanceHidden) colors.danger else colors.success,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// 3 ─ Net worth hero card
@Composable
private fun NetWorthHeroCard(
    netWorth: Double,
    currencySymbol: String,
    isBalanceHidden: Boolean,
    countryProfile: CountryProfileType,
    activeCountryFlag: String,
    youAreOwed: Double,
    youOwe: Double,
    onOpenSplits: () -> Unit
) {
    val colors = MaterialTheme.customColors
    BentoCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = colors.bentoCardElevated,
        borderColor = CredMint
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(CredMint)
                    )
                    Text(
                        text = "NET LEDGER BALANCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = colors.textMuted
                    )
                }

                val profileLabel = when (countryProfile) {
                    CountryProfileType.ALL   -> "🌐 GLOBAL CONSOLIDATED"
                    CountryProfileType.HOME  -> "$activeCountryFlag DOMESTIC"
                    CountryProfileType.EXPAT -> "✈ EXPAT FOREIGN"
                }
                StatusPill(text = profileLabel, color = CredMint)
            }

            if (isBalanceHidden) {
                Text(
                    text = "••••••••",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = colors.textPrimary
                )
            } else {
                AnimatedBalanceCounter(
                    targetAmount = netWorth,
                    currencySymbol = currencySymbol,
                    color = colors.textPrimary
                )
            }

            HorizontalDivider(color = colors.bentoBorderSubtle)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SplitBadge(
                    label = "You are owed",
                    amount = youAreOwed,
                    currencySymbol = currencySymbol,
                    color = CredMint,
                    isHidden = isBalanceHidden,
                    icon = Icons.Default.ArrowDownward,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenSplits)
                )
                SplitBadge(
                    label = "You owe",
                    amount = youOwe,
                    currencySymbol = currencySymbol,
                    color = CredRose,
                    isHidden = isBalanceHidden,
                    icon = Icons.Default.ArrowUpward,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onOpenSplits)
                )
            }
        }
    }
}

// 4 ─ Quick actions
@Composable
private fun QuickActionsRow(
    onAddTransaction: () -> Unit,
    onSplitBill: () -> Unit,
    onScanReceipt: () -> Unit,
    onSettleUp: () -> Unit,
    haptics: androidx.compose.ui.hapticfeedback.HapticFeedback
) {
    val colors = MaterialTheme.customColors
    val actions = remember {
        listOf(
            QuickActionDef(Icons.Default.Add, "+ Record", CredMint, onAddTransaction),
            QuickActionDef(Icons.Default.CallSplit, "⚡ Split", CredIndigo, onSplitBill),
            QuickActionDef(Icons.Default.DocumentScanner, "📷 Scan OCR", CredCyan, onScanReceipt),
            QuickActionDef(Icons.Default.Handshake, "🤝 Settle", CredGold, onSettleUp)
        )
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        actions.forEach { action ->
            QuickActionTile(
                icon = action.icon,
                label = action.label,
                color = action.color,
                modifier = Modifier.weight(1f),
                onClick = {
                    haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    action.onClick()
                }
            )
        }
    }
}

private data class QuickActionDef(
    val icon: ImageVector,
    val label: String,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
private fun QuickActionTile(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.customColors
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val depth = 4.dp
    val faceOffset by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isPressed) depth else 0.dp,
        animationSpec = tween(90),
        label = "quick_action_press"
    )

    Box(modifier = modifier) {
        // Extrusion slab
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = depth, y = depth)
                .clip(RoundedCornerShape(CardRadius))
                .background(colors.extrusionShadow)
        )
        // Face
        Surface(
            modifier = Modifier
                .offset(x = faceOffset, y = faceOffset)
                .clip(RoundedCornerShape(CardRadius))
                .clickable(interactionSource = interaction, indication = null, onClick = onClick)
                .semantics { contentDescription = label },
            shape = RoundedCornerShape(CardRadius),
            color = colors.bentoCardBg,
            border = BorderStroke(2.dp, color)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 14.dp, horizontal = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = color.neoPopOnColor(), modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary,
                    maxLines = 1
                )
            }
        }
    }
}

// 5 ─ Accounts section
@Composable
private fun AccountsSection(
    accounts: List<AccountEntity>,
    isBalanceHidden: Boolean,
    onAddAccount: () -> Unit,
    onEditAccount: (AccountEntity) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(
            title = "Accounts & Vaults",
            subtitle = "${accounts.size}",
            actionLabel = "Add Account",
            actionIcon = Icons.Default.Add,
            onAction = onAddAccount
        )
        if (accounts.isEmpty()) {
            EmptyStateCard(
                icon = Icons.Default.AccountBalanceWallet,
                title = "No accounts in this profile",
                subtitle = "Tap to add a Bank, Card, or Cash vault",
                onClick = onAddAccount
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(
                    items = accounts,
                    key = { it.id }
                ) { account ->
                    AccountCardItem(
                        account = account,
                        isHidden = isBalanceHidden,
                        onEditClick = { onEditAccount(account) }
                    )
                }
            }
        }
    }
}

@Composable
fun AccountCardItem(
    account: AccountEntity,
    isHidden: Boolean,
    onEditClick: () -> Unit
) {
    val colors = MaterialTheme.customColors
    val currencySymbol = CountryCurrencyCatalog.getSymbolForCurrency(account.currency)
    val cardColor = Color(account.colorHex)

    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = tween(120),
        label = "account_press"
    )

    Surface(
        modifier = Modifier
            .width(190.dp)
            .scale(scale)
            .clip(RoundedCornerShape(6.dp))
            .clickable(interactionSource = interaction, indication = null, onClick = onEditClick),
        shape = RoundedCornerShape(6.dp),
        color = colors.bentoCardElevated,
        border = BorderStroke(2.dp, cardColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(cardColor.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (account.type) {
                                AccountType.BANK            -> Icons.Default.AccountBalance
                                AccountType.CREDIT_CARD     -> Icons.Default.CreditCard
                                AccountType.CASH            -> Icons.Default.Payments
                                AccountType.DIGITAL_WALLET   -> Icons.Default.AccountBalanceWallet
                            },
                            contentDescription = null,
                            tint = cardColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = account.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        color = colors.textPrimary
                    )
                }
                IconButton(onClick = onEditClick, modifier = Modifier.size(22.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit account",
                        tint = colors.textMuted,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Column {
                Text(
                    text = if (isHidden) "••••" else formatMoney(currencySymbol, account.balance),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = if (account.balance >= 0) colors.textPrimary else colors.danger
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (account.accountNumberMask.isNotBlank())
                            account.accountNumberMask
                        else account.type.name.replace("_", " "),
                        fontSize = 10.sp,
                        color = colors.textTertiary
                    )
                    Surface(shape = RoundedCornerShape(6.dp), color = cardColor.copy(alpha = 0.2f)) {
                        Text(
                            text = "${account.currency} • ${account.countryProfile}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = cardColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// 6 ─ Spending pace card
@Composable
private fun SpendingPaceCard(
    spent: Double,
    budget: Double,
    projected: Double,
    isPaceSafe: Boolean,
    currencySymbol: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.customColors
    val progress by remember(spent, budget) {
        derivedStateOf {
            if (budget > 0) (spent / budget).toFloat().coerceIn(0f, 1f) else 0f
        }
    }
    val statusColor = if (isPaceSafe) colors.success else colors.danger
    val statusText = when {
        budget <= 0   -> "No monthly limit set"
        isPaceSafe     -> "✓ Projected ${formatMoney(currencySymbol, projected)} (Safe pace)"
        else           -> "⚠ Exceeding by ${formatMoney(currencySymbol, projected - budget)}"
    }

    BentoCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "MONTHLY SPENDING PACE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = colors.textMuted
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = colors.textMuted,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${formatMoney(currencySymbol, spent)} spent of ${formatMoney(currencySymbol, budget)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = statusText, fontSize = 11.sp, color = statusColor)
            }
            RadialProgressRing(
                progress = progress,
                activeColor = statusColor,
                size = 52.dp,
                strokeWidth = 5.dp
            )
        }
    }
}

// 7 ─ Split balances
@Composable
private fun SplitBalancesSection(
    friends: List<FriendBalanceSummary>,
    currencySymbol: String,
    onSeeAll: () -> Unit,
    onPick: () -> Unit
) {
    val colors = MaterialTheme.customColors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(
            title = "Split Balances",
            actionLabel = "See All",
            onAction = onSeeAll
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(friends.take(6)) { friendSummary ->
                BentoCard(
                    modifier = Modifier.width(140.dp),
                    onClick = onPick
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FriendAvatar(name = friendSummary.friend.name, size = 36.dp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = friendSummary.friend.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            color = colors.textPrimary
                        )
                        val (amountText, amountColor) = when {
                            friendSummary.netBalance > 0 -> formatSigned(currencySymbol, friendSummary.netBalance, '+') to CredMint
                            friendSummary.netBalance < 0 -> formatSigned(currencySymbol, friendSummary.netBalance, '-') to CredRose
                            else                        -> "Settled" to colors.textMuted
                        }
                        Text(
                            text = amountText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = amountColor
                        )
                    }
                }
            }
        }
    }
}

// 8 ─ Savings vaults
@Composable
private fun SavingsVaultsSection(
    goals: List<SavingsGoalEntity>,
    currencySymbol: String,
    onViewAll: () -> Unit,
    onPick: () -> Unit
) {
    val colors = MaterialTheme.customColors
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(
            title = "Savings Vaults",
            actionLabel = "View All",
            onAction = onViewAll
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(goals.take(4)) { goal ->
                val progress by remember(goal.id, goal.currentAmount, goal.targetAmount) {
                    derivedStateOf {
                        if (goal.targetAmount > 0)
                            (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
                        else 0f
                    }
                }
                val pct by remember(goal.id, goal.currentAmount, goal.targetAmount) {
                    derivedStateOf {
                        if (goal.targetAmount > 0) ((goal.currentAmount / goal.targetAmount) * 100).toInt() else 0
                    }
                }
                BentoCard(
                    modifier = Modifier.width(170.dp),
                    onClick = onPick
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = goal.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                color = colors.textPrimary
                            )
                            Text(
                                text = "$pct%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CredMint
                            )
                        }
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = CredMint,
                            trackColor = colors.bentoCardElevated
                        )
                        Text(
                            text = "${formatMoney(currencySymbol, goal.currentAmount)} / ${formatMoney(currencySymbol, goal.targetAmount)}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            color = colors.textMuted
                        )
                    }
                }
            }
        }
    }
}

// 9 ─ Transaction row
@Composable
private fun TransactionRow(
    tx: com.example.data.model.TransactionEntity,
    category: String,
    currencySymbol: String,
    dateLabel: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.customColors
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(120),
        label = "tx_press"
    )

    val typeColor: Color
    val typeIcon: ImageVector
    val amountText: String
    val amountColor: Color
    when (tx.type) {
        TransactionType.INCOME -> {
            typeColor = CredMint
            typeIcon = Icons.Default.ArrowDownward
            amountText = formatSigned(currencySymbol, tx.amount, '+')
            amountColor = CredMint
        }
        TransactionType.EXPENSE -> {
            typeColor = CredRose
            typeIcon = Icons.Default.ArrowUpward
            amountText = formatSigned(currencySymbol, tx.amount, '-')
            amountColor = colors.textPrimary
        }
        TransactionType.TRANSFER -> {
            typeColor = CredCyan
            typeIcon = Icons.Default.SwapHoriz
            amountText = formatMoney(currencySymbol, tx.amount)
            amountColor = CredCyan
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
        shape = RoundedCornerShape(CardRadius),
        color = colors.bentoCardBg,
        border = BorderStroke(Elevation, colors.bentoBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(typeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = typeIcon,
                        contentDescription = null,
                        tint = typeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = tx.notes.ifBlank { category },
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "$dateLabel • $category",
                        fontSize = 11.sp,
                        color = colors.textMuted
                    )
                }
            }
            Text(
                text = amountText,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                color = amountColor
            )
        }
    }
}

// ══════════════════════════════════════════════════════════════
// SHARED PRIMITIVES
// ══════════════════════════════════════════════════════════════

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String? = null,
    actionLabel: String? = null,
    actionIcon: ImageVector? = null,
    onAction: (() -> Unit)? = null
) {
    val colors = MaterialTheme.customColors
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title.uppercase(Locale.US),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = colors.textMuted
            )
            if (subtitle != null) {
                Text("·", color = colors.textTertiary, fontSize = 11.sp)
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = colors.textTertiary
                )
            }
        }
        if (actionLabel != null && onAction != null) {
            TextButton(
                onClick = onAction,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                if (actionIcon != null) {
                    Icon(
                        imageVector = actionIcon,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = CredMint
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = actionLabel,
                    fontSize = 12.sp,
                    color = CredMint,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StatusPill(
    text: String,
    color: Color
) {
    Surface(
        shape = RoundedCornerShape(PillRadius),
        color = color.copy(alpha = 0.12f),
        border = BorderStroke(Elevation, color.copy(alpha = 0.35f))
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.8.sp,
            color = color,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun SplitBadge(
    label: String,
    amount: Double,
    currencySymbol: String,
    color: Color,
    isHidden: Boolean,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.customColors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(Elevation, color.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(15.dp)
                )
            }
            Column {
                Text(label, fontSize = 10.sp, color = colors.textMuted)
                Text(
                    text = if (isHidden) "•••" else formatMoney(currencySymbol, amount),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = MaterialTheme.customColors
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(CardRadius),
        color = colors.bentoCardBg,
        border = BorderStroke(Elevation, colors.bentoBorder)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(CredMint.copy(alpha = 0.08f))
                    .border(Elevation, CredMint.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = CredMint,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = colors.textMuted
            )
        }
    }
}