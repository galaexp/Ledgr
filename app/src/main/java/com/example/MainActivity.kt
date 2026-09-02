package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.domain.ParsedReceiptItem
import com.example.ui.components.CountryProfileTopBarButton
import com.example.ui.components.PinLockScreen
import com.example.ui.dialogs.*
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.LedgrViewModel

enum class MainNavigationTab {
    HOME, TRANSACTIONS, SPLITS, BUDGETS, SETTINGS
}

class MainActivity : ComponentActivity() {
    private val viewModel: LedgrViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val palette by viewModel.themePalette.collectAsState()
            val isDark by viewModel.isDarkTheme.collectAsState()
            val isLocked by viewModel.isLocked.collectAsState()
            val isLoggedIn by viewModel.isLoggedIn.collectAsState()

            LedgrTheme(palette = palette, darkTheme = isDark) {
                if (!isLoggedIn) {
                    LoginOnboardingScreen(
                        viewModel = viewModel,
                        onLoginSuccess = { /* state handled reactively */ }
                    )
                } else if (isLocked && viewModel.securityManager.isPinEnabled()) {
                    PinLockScreen(
                        correctPin = viewModel.securityManager.getStoredPin(),
                        onUnlocked = { viewModel.unlockApp() }
                    )
                } else {
                    LedgrMainApp(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LedgrMainApp(viewModel: LedgrViewModel) {
    var currentTab by remember { mutableStateOf(MainNavigationTab.HOME) }
    val customColors = MaterialTheme.customColors

    val accounts by viewModel.accounts.collectAsState(initial = emptyList())
    val categories by viewModel.categories.collectAsState(initial = emptyList())
    val friends by viewModel.friends.collectAsState(initial = emptyList())
    val groups by viewModel.groups.collectAsState(initial = emptyList())
    val countryProfile by viewModel.countryProfile.collectAsState()
    val homeCurrency by viewModel.primaryCurrency.collectAsState()
    val expatCurrency by viewModel.expatCurrency.collectAsState()
    val userCountryFlag by viewModel.userCountryFlag.collectAsState()

    // Dialog & BottomSheet visibility states
    var showAddTransactionSheet by remember { mutableStateOf(false) }
    var showSplitBillSheet by remember { mutableStateOf(false) }
    var showReceiptScannerSheet by remember { mutableStateOf(false) }
    var showSettleUpSheet by remember { mutableStateOf(false) }
    var settleUpFriendId by remember { mutableStateOf<Long?>(null) }
    var settleUpAmount by remember { mutableStateOf(0.0) }

    // Pre-filled states for Tx / Split from OCR scanner
    var prefilledTxAmount by remember { mutableStateOf<Double?>(null) }
    var prefilledTxMerchant by remember { mutableStateOf<String?>(null) }
    var prefilledTxDate by remember { mutableStateOf<Long?>(null) }
    var prefilledSplitAmount by remember { mutableStateOf(0.0) }
    var prefilledSplitTitle by remember { mutableStateOf("") }
    var prefilledSplitItems by remember { mutableStateOf<List<ParsedReceiptItem>>(emptyList()) }

    // Entity Creation Dialogs
    var showAddAccountDialog by remember { mutableStateOf(false) }
    var showAddSavingsGoalDialog by remember { mutableStateOf(false) }
    var showAddDebtEmiDialog by remember { mutableStateOf(false) }
    var showAddFriendDialog by remember { mutableStateOf(false) }
    var showAddGroupDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CredMint.copy(alpha = 0.15f))
                                .border(1.dp, CredMint.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("⬡", fontSize = 18.sp, color = CredMint, fontWeight = FontWeight.ExtraBold)
                        }
                        Text(
                            text = "LEDGR",
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp,
                            fontSize = 18.sp,
                            color = customColors.textPrimary
                        )
                    }
                },
                actions = {
                    CountryProfileTopBarButton(
                        currentProfile = countryProfile,
                        homeCurrency = homeCurrency,
                        expatCurrency = expatCurrency,
                        homeFlag = userCountryFlag,
                        onProfileSelected = { viewModel.setCountryProfile(it) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = customColors.textPrimary
                )
            )
        },
        floatingActionButton = {
            if (currentTab != MainNavigationTab.SETTINGS) {
                FloatingActionButton(
                    onClick = {
                        prefilledTxAmount = null
                        prefilledTxMerchant = null
                        prefilledTxDate = null
                        showAddTransactionSheet = true
                    },
                    containerColor = CredMint,
                    contentColor = NeoPopPureBlack,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Transaction",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        bottomBar = {
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(customColors.bentoBorderStrong)
                )
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = customColors.textMuted,
                    tonalElevation = 0.dp
                ) {
                listOf(
                    MainNavigationTab.HOME to ("Home" to Icons.Default.Home),
                    MainNavigationTab.TRANSACTIONS to ("Ledger" to Icons.Default.ReceiptLong),
                    MainNavigationTab.SPLITS to ("Splits" to Icons.Default.Hub),
                    MainNavigationTab.BUDGETS to ("Budgets" to Icons.Default.PieChart),
                    MainNavigationTab.SETTINGS to ("Vault" to Icons.Default.Settings)
                ).forEach { (tab, pair) ->
                    val (label, icon) = pair
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = if (isSelected) CredMint else customColors.textMuted
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) CredMint else customColors.textMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = CredMint.copy(alpha = 0.15f),
                            selectedIconColor = CredMint,
                            unselectedIconColor = customColors.textMuted,
                            selectedTextColor = CredMint,
                            unselectedTextColor = customColors.textMuted
                        )
                    )
                }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (currentTab) {
                MainNavigationTab.HOME -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onNavigateToTransactions = { currentTab = MainNavigationTab.TRANSACTIONS },
                        onNavigateToSplits = { currentTab = MainNavigationTab.SPLITS },
                        onNavigateToBudgets = { currentTab = MainNavigationTab.BUDGETS },
                        onOpenAddTransaction = {
                            prefilledTxAmount = null
                            prefilledTxMerchant = null
                            prefilledTxDate = null
                            showAddTransactionSheet = true
                        },
                        onOpenSplitBill = {
                            prefilledSplitAmount = 0.0
                            prefilledSplitTitle = ""
                            prefilledSplitItems = emptyList()
                            showSplitBillSheet = true
                        },
                        onOpenScanReceipt = { showReceiptScannerSheet = true },
                        onOpenSettleUp = {
                            settleUpFriendId = null
                            settleUpAmount = 0.0
                            showSettleUpSheet = true
                        },
                        onOpenAddAccount = { showAddAccountDialog = true }
                    )
                }
                MainNavigationTab.TRANSACTIONS -> {
                    TransactionsScreen(
                        viewModel = viewModel,
                        onOpenAddTransaction = {
                            prefilledTxAmount = null
                            prefilledTxMerchant = null
                            prefilledTxDate = null
                            showAddTransactionSheet = true
                        }
                    )
                }
                MainNavigationTab.SPLITS -> {
                    SplitsScreen(
                        viewModel = viewModel,
                        onOpenSplitBill = {
                            prefilledSplitAmount = 0.0
                            prefilledSplitTitle = ""
                            prefilledSplitItems = emptyList()
                            showSplitBillSheet = true
                        },
                        onOpenSettleUp = { friendId, balance ->
                            settleUpFriendId = friendId
                            settleUpAmount = balance
                            showSettleUpSheet = true
                        },
                        onOpenAddFriend = { showAddFriendDialog = true },
                        onOpenAddGroup = { showAddGroupDialog = true }
                    )
                }
                MainNavigationTab.BUDGETS -> {
                    BudgetsAndGoalsScreen(
                        viewModel = viewModel,
                        onOpenAddSavingsGoal = { showAddSavingsGoalDialog = true },
                        onOpenAddDebtEmi = { showAddDebtEmiDialog = true }
                    )
                }
                MainNavigationTab.SETTINGS -> {
                    SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Modal Bottom Sheets
    if (showAddTransactionSheet) {
        AddTransactionBottomSheet(
            accounts = accounts,
            categories = categories,
            prefilledAmount = prefilledTxAmount,
            prefilledMerchant = prefilledTxMerchant,
            prefilledDate = prefilledTxDate,
            onDismiss = { showAddTransactionSheet = false },
            onSave = { tx -> viewModel.addTransaction(tx) },
            onScanReceiptClick = {
                showAddTransactionSheet = false
                showReceiptScannerSheet = true
            },
            onSplitBillClick = { amount, title ->
                showAddTransactionSheet = false
                prefilledSplitAmount = amount
                prefilledSplitTitle = title
                prefilledSplitItems = emptyList()
                showSplitBillSheet = true
            }
        )
    }

    if (showSplitBillSheet) {
        SplitBillBottomSheet(
            friends = friends,
            groups = groups,
            accounts = accounts,
            initialAmount = prefilledSplitAmount,
            initialTitle = prefilledSplitTitle,
            receiptItems = prefilledSplitItems,
            onDismiss = { showSplitBillSheet = false },
            onSaveSplit = { expense, participants, linkedAccountId ->
                viewModel.createSplitExpense(expense, participants, linkedAccountId)
            }
        )
    }

    val currencySymbol = CountryCurrencyCatalog.getSymbolForCurrency(homeCurrency)

    if (showReceiptScannerSheet) {
        ReceiptScannerBottomSheet(
            currencySymbol = currencySymbol,
            onDismiss = { showReceiptScannerSheet = false },
            onApplyToTransaction = { total, merchant, date ->
                prefilledTxAmount = total
                prefilledTxMerchant = merchant
                prefilledTxDate = date
                showAddTransactionSheet = true
            },
            onApplyToSplit = { total, merchant, items ->
                prefilledSplitAmount = total
                prefilledSplitTitle = merchant
                prefilledSplitItems = items
                showSplitBillSheet = true
            }
        )
    }

    if (showSettleUpSheet) {
        SettleUpBottomSheet(
            friends = friends,
            groups = groups,
            accounts = accounts,
            initialFriendId = settleUpFriendId,
            initialBalance = settleUpAmount,
            onDismiss = { showSettleUpSheet = false },
            onConfirmSettlement = { settlement, linkedAccountId ->
                viewModel.recordSettlement(settlement, linkedAccountId)
            }
        )
    }

    // Entity Creation Dialogs
    if (showAddAccountDialog) {
        AddAccountDialog(
            defaultCurrency = homeCurrency,
            onDismiss = { showAddAccountDialog = false },
            onSave = { acc -> viewModel.addAccount(acc) }
        )
    }

    if (showAddSavingsGoalDialog) {
        AddSavingsGoalDialog(
            accounts = accounts,
            activeProfile = countryProfile,
            defaultCurrency = if (countryProfile == CountryProfileType.EXPAT) expatCurrency else homeCurrency,
            onDismiss = { showAddSavingsGoalDialog = false },
            onSave = { goal -> viewModel.addSavingsGoal(goal) }
        )
    }

    if (showAddDebtEmiDialog) {
        AddDebtEmiDialog(
            friends = friends,
            activeProfile = countryProfile,
            defaultCurrency = if (countryProfile == CountryProfileType.EXPAT) expatCurrency else homeCurrency,
            onDismiss = { showAddDebtEmiDialog = false },
            onSave = { debt -> viewModel.addDebtEmi(debt) }
        )
    }

    if (showAddFriendDialog) {
        AddFriendDialog(
            onDismiss = { showAddFriendDialog = false },
            onSave = { friend -> viewModel.addFriend(friend) }
        )
    }

    if (showAddGroupDialog) {
        AddGroupDialog(
            friends = friends,
            onDismiss = { showAddGroupDialog = false },
            onSave = { group -> viewModel.addGroup(group) }
        )
    }
}
