package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
import com.example.data.model.*
import com.example.data.repository.LedgrRepository
import com.example.domain.DebtSimplificationEngine
import com.example.domain.FriendBalanceSummary
import com.example.domain.SecurityManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class DateFilterType {
    ALL, TODAY, WEEK, MONTH, YEAR
}

data class DashboardSpendingPace(
    val spentThisMonth: Double,
    val monthlyBudgetLimit: Double,
    val dailyBurnRate: Double,
    val projectedMonthEndSpend: Double,
    val daysRemainingInMonth: Int,
    val isPaceSafe: Boolean,
    val unsafeProfileTag: String? = null
)

data class CategoryBudgetProgress(
    val category: CategoryEntity,
    val budget: BudgetEntity?,
    val spentAmount: Double,
    val limitAmount: Double,
    val percentage: Float
)

data class FilterCriteria(
    val query: String,
    val dateFilter: DateFilterType,
    val categoryId: Long?,
    val accountId: Long?,
    val country: CountryProfileType
)

class LedgrViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    val repository = LedgrRepository(database)
    val securityManager = SecurityManager(application)

    // User Authentication & Onboarding
    private val _isLoggedIn = MutableStateFlow(securityManager.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _userName = MutableStateFlow(securityManager.getUserName())
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow(securityManager.getUserEmail())
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userCountryCode = MutableStateFlow(securityManager.getUserCountryCode())
    val userCountryCode: StateFlow<String> = _userCountryCode.asStateFlow()

    private val _userCountryName = MutableStateFlow(securityManager.getUserCountryName())
    val userCountryName: StateFlow<String> = _userCountryName.asStateFlow()

    private val _primaryCurrency = MutableStateFlow(securityManager.getPrimaryCountryCurrency())
    val primaryCurrency: StateFlow<String> = _primaryCurrency.asStateFlow()

    private val _expatCurrency = MutableStateFlow(securityManager.getSecondCountryCurrency())
    val expatCurrency: StateFlow<String> = _expatCurrency.asStateFlow()

    // Active Country Profile Selection (HOME, EXPAT, ALL)
    private val _countryProfile = MutableStateFlow(CountryProfileType.ALL)
    val countryProfile: StateFlow<CountryProfileType> = _countryProfile.asStateFlow()

    // Dynamic active currency code based on selected profile
    val activeCurrency: StateFlow<String> = combine(_countryProfile, _primaryCurrency, _expatCurrency) { profile, homeCurr, expatCurr ->
        when (profile) {
            CountryProfileType.HOME -> homeCurr
            CountryProfileType.EXPAT -> expatCurr
            CountryProfileType.ALL -> homeCurr
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), securityManager.getPrimaryCountryCurrency())

    // Dynamic active currency symbol based on selected profile
    val activeCurrencySymbol: StateFlow<String> = activeCurrency.map { curr ->
        CountryCurrencyCatalog.getSymbolForCurrency(curr)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "$")

    // Dynamic active country flag based on selected profile
    val activeCountryFlag: StateFlow<String> = combine(_countryProfile, _userCountryCode, _primaryCurrency, _expatCurrency) { profile, code, homeCurr, expatCurr ->
        when (profile) {
            CountryProfileType.HOME -> {
                CountryCurrencyCatalog.getFlagForCountry(code).takeIf { it.isNotBlank() }
                    ?: CountryCurrencyCatalog.getFlagForCurrency(homeCurr)
            }
            CountryProfileType.EXPAT -> {
                CountryCurrencyCatalog.getFlagForCurrency(expatCurr)
            }
            CountryProfileType.ALL -> "🌐"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "🌐")

    // Dynamic active country/profile title
    val activeCountryName: StateFlow<String> = combine(_countryProfile, _userCountryName, _expatCurrency) { profile, homeName, expatCurr ->
        when (profile) {
            CountryProfileType.HOME -> homeName
            CountryProfileType.EXPAT -> "Expat ($expatCurr)"
            CountryProfileType.ALL -> "Global (All Profiles)"
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Global")

    // Legacy flag for user's home country
    val userCountryFlag: StateFlow<String> = combine(_userCountryCode, _primaryCurrency) { code, curr ->
        CountryCurrencyCatalog.getFlagForCountry(code).takeIf { it.isNotBlank() }
            ?: CountryCurrencyCatalog.getFlagForCurrency(curr)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "🌐")

    // Legacy currency symbol alias
    val currencySymbol: StateFlow<String> = activeCurrencySymbol

    // App Preferences
    private val _themePalette = MutableStateFlow(
        try { ThemePalette.valueOf(securityManager.getSelectedThemePalette()) } catch (_: Exception) { ThemePalette.EMERALD }
    )
    val themePalette: StateFlow<ThemePalette> = _themePalette.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(true)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _isLocked = MutableStateFlow(securityManager.isPinEnabled())
    val isLocked: StateFlow<Boolean> = _isLocked.asStateFlow()

    // Transaction Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _dateFilter = MutableStateFlow(DateFilterType.MONTH)
    val dateFilter: StateFlow<DateFilterType> = _dateFilter.asStateFlow()

    private val _selectedCategoryId = MutableStateFlow<Long?>(null)
    val selectedCategoryId: StateFlow<Long?> = _selectedCategoryId.asStateFlow()

    private val _selectedAccountId = MutableStateFlow<Long?>(null)
    val selectedAccountId: StateFlow<Long?> = _selectedAccountId.asStateFlow()

    // Flows from DB
    val accounts: Flow<List<AccountEntity>> = repository.allAccounts

    // Filtered Accounts by Country Profile
    val filteredAccounts: StateFlow<List<AccountEntity>> = combine(accounts, _countryProfile) { accList, profile ->
        when (profile) {
            CountryProfileType.ALL -> accList
            CountryProfileType.HOME -> accList.filter { it.countryProfile == "HOME" }
            CountryProfileType.EXPAT -> accList.filter { it.countryProfile == "EXPAT" }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: Flow<List<CategoryEntity>> = repository.allCategories
    val transactions: Flow<List<TransactionEntity>> = repository.allTransactions
    val budgets: Flow<List<BudgetEntity>> = repository.allBudgets
    val savingsGoals: Flow<List<SavingsGoalEntity>> = repository.allSavingsGoals
    val debtEmis: Flow<List<DebtEmiEntity>> = repository.allDebtEmis
    val friends: Flow<List<FriendEntity>> = repository.allFriends
    val groups: Flow<List<GroupEntity>> = repository.allGroups
    val splitExpenses: Flow<List<SplitExpenseEntity>> = repository.allSplitExpenses
    val splitParticipants: Flow<List<SplitParticipantEntity>> = repository.allSplitParticipants
    val settlements: Flow<List<SettlementEntity>> = repository.allSettlements
    val exchangeRates: Flow<List<ExchangeRateEntity>> = repository.allRates
    val recurringTransactions: Flow<List<RecurringTransactionEntity>> = repository.allRecurring

    // Filtered Budgets by Country Profile
    val filteredBudgets: StateFlow<List<BudgetEntity>> = combine(budgets, _countryProfile) { bgList, profile ->
        when (profile) {
            CountryProfileType.ALL -> bgList
            CountryProfileType.HOME -> bgList.filter { it.countryProfile == "HOME" }
            CountryProfileType.EXPAT -> bgList.filter { it.countryProfile == "EXPAT" }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Savings Goals by Country Profile
    val filteredSavingsGoals: StateFlow<List<SavingsGoalEntity>> = combine(savingsGoals, _countryProfile) { sgList, profile ->
        when (profile) {
            CountryProfileType.ALL -> sgList
            CountryProfileType.HOME -> sgList.filter { it.countryProfile == "HOME" }
            CountryProfileType.EXPAT -> sgList.filter { it.countryProfile == "EXPAT" }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Debt & EMIs by Country Profile
    val filteredDebtEmis: StateFlow<List<DebtEmiEntity>> = combine(debtEmis, _countryProfile) { deList, profile ->
        when (profile) {
            CountryProfileType.ALL -> deList
            CountryProfileType.HOME -> deList.filter { it.countryProfile == "HOME" }
            CountryProfileType.EXPAT -> deList.filter { it.countryProfile == "EXPAT" }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined filter criteria flow
    private val filterCriteria: Flow<FilterCriteria> = combine(
        _searchQuery,
        _dateFilter,
        _selectedCategoryId,
        _selectedAccountId,
        _countryProfile
    ) { query, dateFlt, catId, accId, country ->
        FilterCriteria(query, dateFlt, catId, accId, country)
    }

    // Filtered Transactions
    val filteredTransactions: StateFlow<List<TransactionEntity>> = combine(
        transactions,
        filterCriteria
    ) { txList: List<TransactionEntity>, criteria: FilterCriteria ->
        val cal = Calendar.getInstance()

        val startTimestamp = when (criteria.dateFilter) {
            DateFilterType.TODAY -> {
                cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0); cal.set(Calendar.SECOND, 0); cal.timeInMillis
            }
            DateFilterType.WEEK -> {
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek); cal.set(Calendar.HOUR_OF_DAY, 0); cal.timeInMillis
            }
            DateFilterType.MONTH -> {
                cal.set(Calendar.DAY_OF_MONTH, 1); cal.set(Calendar.HOUR_OF_DAY, 0); cal.timeInMillis
            }
            DateFilterType.YEAR -> {
                cal.set(Calendar.DAY_OF_YEAR, 1); cal.set(Calendar.HOUR_OF_DAY, 0); cal.timeInMillis
            }
            DateFilterType.ALL -> 0L
        }

        txList.filter { tx ->
            val matchesDate = tx.date >= startTimestamp
            val matchesSearch = criteria.query.isBlank() ||
                    tx.notes.contains(criteria.query, ignoreCase = true) ||
                    tx.tags.contains(criteria.query, ignoreCase = true)
            val matchesCat = criteria.categoryId == null || tx.categoryId == criteria.categoryId
            val matchesAcc = criteria.accountId == null || tx.accountId == criteria.accountId || tx.targetAccountId == criteria.accountId
            val matchesCountry = criteria.country == CountryProfileType.ALL ||
                    (criteria.country == CountryProfileType.HOME && tx.countryProfile == "HOME") ||
                    (criteria.country == CountryProfileType.EXPAT && tx.countryProfile == "EXPAT")

            matchesDate && matchesSearch && matchesCat && matchesAcc && matchesCountry
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Net Worth Calculation (incorporating FX rates & active profile conversion)
    val netWorthSummary: StateFlow<Double> = combine(
        filteredAccounts,
        exchangeRates,
        activeCurrency
    ) { accList, rates, targetCurrency ->
        val rateMap = rates.associate { "${it.fromCurrency.uppercase()}_${it.toCurrency.uppercase()}" to it.rate }
        var total = 0.0
        for (acc in accList) {
            total += CurrencyConverter.convert(acc.balance, acc.currency, targetCurrency, rateMap)
        }
        total
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // 30-Day Net Worth Trend Sparkline for active profile
    val netWorthTrend: StateFlow<List<Double>> = combine(
        netWorthSummary,
        filteredTransactions,
        exchangeRates,
        activeCurrency
    ) { currentNetWorth, txList, rates, targetCurrency ->
        val rateMap = rates.associate { "${it.fromCurrency.uppercase()}_${it.toCurrency.uppercase()}" to it.rate }
        val now = System.currentTimeMillis()
        val dayMillis = 24L * 60 * 60 * 1000

        val dailyNetChanges = DoubleArray(30) { 0.0 }
        for (tx in txList) {
            val daysAgo = ((now - tx.date) / dayMillis).toInt()
            if (daysAgo in 0..29) {
                val converted = CurrencyConverter.convert(tx.amount, tx.currency, targetCurrency, rateMap)
                when (tx.type) {
                    TransactionType.INCOME -> dailyNetChanges[29 - daysAgo] += converted
                    TransactionType.EXPENSE -> dailyNetChanges[29 - daysAgo] -= converted
                    TransactionType.TRANSFER -> { /* internal transfer net change is 0 */ }
                }
            }
        }

        val points = DoubleArray(30)
        points[29] = currentNetWorth
        for (i in 28 downTo 0) {
            points[i] = points[i + 1] - dailyNetChanges[i + 1]
        }
        points.toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), List(30) { 0.0 })

    // Vaults Total Summary for active profile (Total Saved, Total Target)
    val vaultsSummary: StateFlow<Pair<Double, Double>> = combine(
        filteredSavingsGoals,
        exchangeRates,
        activeCurrency
    ) { goals, rates, targetCurrency ->
        val rateMap = rates.associate { "${it.fromCurrency.uppercase()}_${it.toCurrency.uppercase()}" to it.rate }
        var totalSaved = 0.0
        var totalTarget = 0.0
        for (g in goals) {
            totalSaved += CurrencyConverter.convert(g.currentAmount, g.currency, targetCurrency, rateMap)
            totalTarget += CurrencyConverter.convert(g.targetAmount, g.currency, targetCurrency, rateMap)
        }
        Pair(totalSaved, totalTarget)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(0.0, 0.0))

    // Live Spending Pace Calculator (profile-aware & currency-converted)
    val spendingPace: StateFlow<DashboardSpendingPace> = combine(
        filteredTransactions,
        filteredBudgets,
        exchangeRates,
        activeCurrency,
        _countryProfile
    ) { txList, budgetList, rates, targetCurrency, country ->
        val rateMap = rates.associate { "${it.fromCurrency.uppercase()}_${it.toCurrency.uppercase()}" to it.rate }

        val cal = Calendar.getInstance()
        val currentDay = cal.get(Calendar.DAY_OF_MONTH)
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val daysRemaining = maxDays - currentDay

        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        val monthStart = cal.timeInMillis

        val monthExpenses = txList.filter { it.date >= monthStart && it.type == TransactionType.EXPENSE }
        val totalSpent = monthExpenses.sumOf { tx ->
            CurrencyConverter.convert(tx.amount, tx.currency, targetCurrency, rateMap)
        }
        val totalLimit = budgetList.sumOf { bg ->
            CurrencyConverter.convert(bg.monthlyLimit, bg.currency, targetCurrency, rateMap)
        }.let { if (it <= 0) 0.0 else it }

        val dailyBurn = if (currentDay > 0) totalSpent / currentDay else totalSpent
        val projected = totalSpent + (dailyBurn * daysRemaining)
        val isSafe = if (totalLimit <= 0) true else projected <= totalLimit

        val profileTag = when {
            !isSafe && country == CountryProfileType.ALL -> "GLOBAL"
            !isSafe && country == CountryProfileType.HOME -> "DOMESTIC"
            !isSafe && country == CountryProfileType.EXPAT -> "EXPAT"
            else -> null
        }

        DashboardSpendingPace(
            spentThisMonth = totalSpent,
            monthlyBudgetLimit = totalLimit,
            dailyBurnRate = dailyBurn,
            projectedMonthEndSpend = projected,
            daysRemainingInMonth = daysRemaining,
            isPaceSafe = isSafe,
            unsafeProfileTag = profileTag
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DashboardSpendingPace(0.0, 0.0, 0.0, 0.0, 15, true)
    )

    // Category Budgets Progress (profile-aware & currency-converted)
    val categoryBudgetsProgress: StateFlow<List<CategoryBudgetProgress>> = combine(
        categories,
        filteredBudgets,
        filteredTransactions,
        exchangeRates,
        activeCurrency
    ) { catList, bgList, txList, rates, targetCurrency ->
        val rateMap = rates.associate { "${it.fromCurrency.uppercase()}_${it.toCurrency.uppercase()}" to it.rate }

        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        val monthStart = cal.timeInMillis

        val budgetMap = bgList.associateBy { it.categoryId }
        val spendMap = txList.filter { it.date >= monthStart && it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryId }
            .mapValues { (_, txs) ->
                txs.sumOf { tx -> CurrencyConverter.convert(tx.amount, tx.currency, targetCurrency, rateMap) }
            }

        catList.filter { !it.isIncome }.map { cat ->
            val budget = budgetMap[cat.id]
            val rawLimit = budget?.monthlyLimit ?: if (cat.budgetLimit > 0) cat.budgetLimit else 0.0
            val limit = if (budget != null) {
                CurrencyConverter.convert(budget.monthlyLimit, budget.currency, targetCurrency, rateMap)
            } else rawLimit
            val spent = spendMap[cat.id] ?: 0.0
            val pct = if (limit > 0) (spent / limit).toFloat() else 0f
            CategoryBudgetProgress(
                category = cat,
                budget = budget,
                spentAmount = spent,
                limitAmount = limit,
                percentage = pct
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Friend Balances (Who owes you / You owe)
    val friendBalances: StateFlow<List<FriendBalanceSummary>> = combine(
        friends,
        splitExpenses,
        splitParticipants,
        settlements
    ) { frList, spList, partList, setList ->
        DebtSimplificationEngine.computeFriendBalances(frList, spList, partList, setList)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Total Owed to User vs User Owes
    val netSplitSummary: StateFlow<Pair<Double, Double>> = friendBalances.map { list ->
        val youAreOwed = list.filter { it.netBalance > 0 }.sumOf { it.netBalance }
        val youOwe = list.filter { it.netBalance < 0 }.sumOf { -it.netBalance }
        Pair(youAreOwed, youOwe)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Pair(0.0, 0.0))

    // Complete Onboarding & Login Setup
    fun completeLoginAndSetup(
        name: String,
        email: String,
        countryCode: String,
        countryName: String,
        primaryCurr: String,
        expatCurr: String,
        initialAccountName: String,
        initialBalance: Double,
        pin: String?
    ) {
        securityManager.setUserName(name)
        securityManager.setUserEmail(email)
        securityManager.setUserCountryCode(countryCode)
        securityManager.setUserCountryName(countryName)
        securityManager.setPrimaryCountryCurrency(primaryCurr)
        securityManager.setSecondCountryCurrency(expatCurr)
        securityManager.setLoggedIn(true)

        if (!pin.isNullOrBlank() && pin.length == 4) {
            securityManager.setPin(pin)
        }

        _userName.value = name
        _userEmail.value = email
        _userCountryCode.value = countryCode
        _userCountryName.value = countryName
        _primaryCurrency.value = primaryCurr
        _expatCurrency.value = expatCurr
        _isLoggedIn.value = true

        // Create default starting account for the user
        viewModelScope.launch {
            val account = AccountEntity(
                name = initialAccountName.ifBlank { "Main Account" },
                type = AccountType.BANK,
                balance = initialBalance,
                currency = primaryCurr,
                countryProfile = "HOME",
                colorHex = 0xFF00B594,
                iconName = "account_balance",
                isDefault = true,
                accountNumberMask = "•••• 0001"
            )
            repository.addAccount(account)
        }
    }

    fun logout() {
        securityManager.logout()
        _isLoggedIn.value = false
    }

    // Actions
    fun setDateFilter(filter: DateFilterType) { _dateFilter.value = filter }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setSelectedCategory(catId: Long?) { _selectedCategoryId.value = catId }
    fun setSelectedAccount(accId: Long?) { _selectedAccountId.value = accId }

    fun setCountryProfile(profile: CountryProfileType) { _countryProfile.value = profile }
    fun setThemePalette(palette: ThemePalette) {
        _themePalette.value = palette
        securityManager.setSelectedThemePalette(palette.name)
    }
    fun toggleDarkTheme() { _isDarkTheme.value = !_isDarkTheme.value }

    fun unlockApp() { _isLocked.value = false }
    fun lockApp() { if (securityManager.isPinEnabled()) _isLocked.value = true }
    fun updatePin(pin: String) { securityManager.setPin(pin) }
    fun removePin() { securityManager.removePin(); _isLocked.value = false }

    fun setCurrencies(home: String, expat: String) {
        _primaryCurrency.value = home
        _expatCurrency.value = expat
        securityManager.setPrimaryCountryCurrency(home)
        securityManager.setSecondCountryCurrency(expat)
    }

    fun updateCountryAndCurrency(
        countryCode: String,
        countryName: String,
        primaryCurr: String,
        expatCurr: String = primaryCurr
    ) {
        _userCountryCode.value = countryCode
        _userCountryName.value = countryName
        _primaryCurrency.value = primaryCurr
        _expatCurrency.value = expatCurr
        securityManager.setUserCountryCode(countryCode)
        securityManager.setUserCountryName(countryName)
        securityManager.setPrimaryCountryCurrency(primaryCurr)
        securityManager.setSecondCountryCurrency(expatCurr)
    }

    fun updateProfile(name: String, email: String) {
        _userName.value = name
        _userEmail.value = email
        securityManager.setUserName(name)
        securityManager.setUserEmail(email)
    }

    // CRUD wrappers
    fun addTransaction(tx: TransactionEntity) = viewModelScope.launch { repository.addTransaction(tx) }
    fun deleteTransaction(tx: TransactionEntity) = viewModelScope.launch { repository.deleteTransaction(tx) }

    fun addAccount(acc: AccountEntity) = viewModelScope.launch { repository.addAccount(acc) }
    fun updateAccount(acc: AccountEntity) = viewModelScope.launch { repository.updateAccount(acc) }
    fun deleteAccount(acc: AccountEntity) = viewModelScope.launch { repository.deleteAccount(acc) }

    fun addBudget(bg: BudgetEntity) = viewModelScope.launch { repository.addOrUpdateBudget(bg) }
    fun deleteBudget(bg: BudgetEntity) = viewModelScope.launch { repository.deleteBudget(bg) }

    fun addSavingsGoal(goal: SavingsGoalEntity) = viewModelScope.launch { repository.addSavingsGoal(goal) }
    fun addSavingsGoalContribution(goalId: Long, amount: Double, sourceAccountId: Long?) =
        viewModelScope.launch { repository.addGoalContribution(goalId, amount, sourceAccountId) }
    fun deleteSavingsGoal(goal: SavingsGoalEntity) = viewModelScope.launch { repository.deleteSavingsGoal(goal) }

    fun addDebtEmi(debtEmi: DebtEmiEntity) = viewModelScope.launch { repository.addDebtEmi(debtEmi) }
    fun recordDebtPayment(debtId: Long, amount: Double, sourceAccountId: Long?) =
        viewModelScope.launch { repository.payDebtEmi(debtId, amount, sourceAccountId) }
    fun deleteDebtEmi(debtEmi: DebtEmiEntity) = viewModelScope.launch { repository.deleteDebtEmi(debtEmi) }

    fun addFriend(friend: FriendEntity) = viewModelScope.launch { repository.addFriend(friend) }
    fun addGroup(group: GroupEntity) = viewModelScope.launch { repository.addGroup(group) }

    fun createSplitExpense(
        expense: SplitExpenseEntity,
        participants: List<SplitParticipantEntity>,
        linkedAccountId: Long?
    ) = viewModelScope.launch {
        repository.createSplitExpense(expense, participants, linkedAccountId)
    }

    fun recordSettlement(
        settlement: SettlementEntity,
        linkedAccountId: Long?
    ) = viewModelScope.launch {
        repository.recordSettlement(settlement, linkedAccountId)
    }

    fun recordCrossProfileTransfer(
        fromAccount: AccountEntity,
        toAccount: AccountEntity,
        fromAmount: Double,
        toAmount: Double,
        fxRate: Double,
        notes: String = ""
    ) = viewModelScope.launch {
        repository.recordCrossProfileTransfer(fromAccount, toAccount, fromAmount, toAmount, fxRate, notes)
    }

    fun importCsvTransactions(transactions: List<TransactionEntity>) = viewModelScope.launch {
        repository.insertImportedTransactions(transactions)
    }
}
