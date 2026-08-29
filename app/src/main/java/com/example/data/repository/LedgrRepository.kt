package com.example.data.repository

import com.example.data.db.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class LedgrRepository(private val database: AppDatabase) {

    // DAOs
    val allAccounts: Flow<List<AccountEntity>> = database.accountDao().getAllAccounts()
    val allCategories: Flow<List<CategoryEntity>> = database.categoryDao().getAllCategories()
    val allTransactions: Flow<List<TransactionEntity>> = database.transactionDao().getAllTransactions()
    val allBudgets: Flow<List<BudgetEntity>> = database.budgetDao().getAllBudgets()
    val allSavingsGoals: Flow<List<SavingsGoalEntity>> = database.savingsGoalDao().getAllSavingsGoals()
    val allDebtEmis: Flow<List<DebtEmiEntity>> = database.debtEmiDao().getAllDebtEmis()
    val allRecurring: Flow<List<RecurringTransactionEntity>> = database.recurringTransactionDao().getAllRecurring()
    val allFriends: Flow<List<FriendEntity>> = database.friendDao().getAllFriends()
    val allGroups: Flow<List<GroupEntity>> = database.groupDao().getAllGroups()
    val allSplitExpenses: Flow<List<SplitExpenseEntity>> = database.splitExpenseDao().getAllSplitExpenses()
    val allSplitParticipants: Flow<List<SplitParticipantEntity>> = database.splitParticipantDao().getAllParticipants()
    val allSettlements: Flow<List<SettlementEntity>> = database.settlementDao().getAllSettlements()
    val allRates: Flow<List<ExchangeRateEntity>> = database.exchangeRateDao().getAllRates()

    // Transaction Operations with Account Balance syncing
    suspend fun addTransaction(transaction: TransactionEntity): Long = withContext(Dispatchers.IO) {
        val id = database.transactionDao().insertTransaction(transaction)
        when (transaction.type) {
            TransactionType.EXPENSE -> {
                database.accountDao().updateBalance(transaction.accountId, -transaction.amount)
            }
            TransactionType.INCOME -> {
                database.accountDao().updateBalance(transaction.accountId, transaction.amount)
            }
            TransactionType.TRANSFER -> {
                database.accountDao().updateBalance(transaction.accountId, -transaction.amount)
                transaction.targetAccountId?.let { targetId ->
                    database.accountDao().updateBalance(targetId, transaction.amount)
                }
            }
        }
        id
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        database.transactionDao().deleteTransaction(transaction)
        // Reverse account balance effect
        when (transaction.type) {
            TransactionType.EXPENSE -> {
                database.accountDao().updateBalance(transaction.accountId, transaction.amount)
            }
            TransactionType.INCOME -> {
                database.accountDao().updateBalance(transaction.accountId, -transaction.amount)
            }
            TransactionType.TRANSFER -> {
                database.accountDao().updateBalance(transaction.accountId, transaction.amount)
                transaction.targetAccountId?.let { targetId ->
                    database.accountDao().updateBalance(targetId, -transaction.amount)
                }
            }
        }
    }

    // Account Operations
    suspend fun addAccount(account: AccountEntity): Long = withContext(Dispatchers.IO) {
        database.accountDao().insertAccount(account)
    }

    suspend fun updateAccount(account: AccountEntity) = withContext(Dispatchers.IO) {
        database.accountDao().updateAccount(account)
    }

    suspend fun deleteAccount(account: AccountEntity) = withContext(Dispatchers.IO) {
        database.accountDao().deleteAccount(account)
    }

    // Category Operations
    suspend fun addCategory(category: CategoryEntity): Long = withContext(Dispatchers.IO) {
        database.categoryDao().insertCategory(category)
    }

    // Budget Operations
    suspend fun addOrUpdateBudget(budget: BudgetEntity): Long = withContext(Dispatchers.IO) {
        database.budgetDao().insertBudget(budget)
    }

    suspend fun deleteBudget(budget: BudgetEntity) = withContext(Dispatchers.IO) {
        database.budgetDao().deleteBudget(budget)
    }

    // Savings Goals
    suspend fun addSavingsGoal(goal: SavingsGoalEntity): Long = withContext(Dispatchers.IO) {
        database.savingsGoalDao().insertSavingsGoal(goal)
    }

    suspend fun addGoalContribution(goalId: Long, amount: Double, sourceAccountId: Long?) = withContext(Dispatchers.IO) {
        database.savingsGoalDao().addContribution(goalId, amount)
        if (sourceAccountId != null) {
            database.accountDao().updateBalance(sourceAccountId, -amount)
            database.transactionDao().insertTransaction(
                TransactionEntity(
                    accountId = sourceAccountId,
                    type = TransactionType.EXPENSE,
                    amount = amount,
                    categoryId = 13,
                    date = System.currentTimeMillis(),
                    notes = "Savings Goal Contribution"
                )
            )
        }
    }

    suspend fun deleteSavingsGoal(goal: SavingsGoalEntity) = withContext(Dispatchers.IO) {
        database.savingsGoalDao().deleteSavingsGoal(goal)
    }

    // Debt & EMI
    suspend fun addDebtEmi(debtEmi: DebtEmiEntity): Long = withContext(Dispatchers.IO) {
        database.debtEmiDao().insertDebtEmi(debtEmi)
    }

    suspend fun payDebtEmi(debtId: Long, paymentAmount: Double, accountId: Long?) = withContext(Dispatchers.IO) {
        database.debtEmiDao().recordPayment(debtId, paymentAmount)
        if (accountId != null) {
            database.accountDao().updateBalance(accountId, -paymentAmount)
            database.transactionDao().insertTransaction(
                TransactionEntity(
                    accountId = accountId,
                    type = TransactionType.EXPENSE,
                    amount = paymentAmount,
                    categoryId = 4, // Utilities/Bills
                    date = System.currentTimeMillis(),
                    notes = "EMI / Debt Payment"
                )
            )
        }
    }

    suspend fun deleteDebtEmi(debtEmi: DebtEmiEntity) = withContext(Dispatchers.IO) {
        database.debtEmiDao().deleteDebtEmi(debtEmi)
    }

    // Bill Splitting Operations
    suspend fun addFriend(friend: FriendEntity): Long = withContext(Dispatchers.IO) {
        database.friendDao().insertFriend(friend)
    }

    suspend fun addGroup(group: GroupEntity): Long = withContext(Dispatchers.IO) {
        database.groupDao().insertGroup(group)
    }

    suspend fun createSplitExpense(
        expense: SplitExpenseEntity,
        participants: List<SplitParticipantEntity>,
        linkedAccountId: Long? = null
    ): Long = withContext(Dispatchers.IO) {
        val expenseId = database.splitExpenseDao().insertSplitExpense(expense)
        val updatedParticipants = participants.map { it.copy(splitExpenseId = expenseId) }
        database.splitParticipantDao().insertParticipants(updatedParticipants)

        // If user is payer and chose a linked account, record the transaction in the account
        if (expense.payerFriendId == 0L && linkedAccountId != null) {
            val txId = database.transactionDao().insertTransaction(
                TransactionEntity(
                    accountId = linkedAccountId,
                    type = TransactionType.EXPENSE,
                    amount = expense.totalAmount,
                    currency = expense.currency,
                    categoryId = 1,
                    date = expense.date,
                    notes = "${expense.title} (Bill Split)",
                    splitExpenseId = expenseId
                )
            )
            database.accountDao().updateBalance(linkedAccountId, -expense.totalAmount)
            database.splitExpenseDao().updateSplitExpense(expense.copy(id = expenseId, transactionId = txId))
        }
        expenseId
    }

    suspend fun recordSettlement(
        settlement: SettlementEntity,
        linkedAccountId: Long? = null
    ): Long = withContext(Dispatchers.IO) {
        val id = database.settlementDao().insertSettlement(settlement)

        // If linked account specified:
        if (linkedAccountId != null) {
            if (settlement.fromFriendId == 0L) {
                // User paid friend -> expense from user account
                database.accountDao().updateBalance(linkedAccountId, -settlement.amount)
                database.transactionDao().insertTransaction(
                    TransactionEntity(
                        accountId = linkedAccountId,
                        type = TransactionType.EXPENSE,
                        amount = settlement.amount,
                        currency = settlement.currency,
                        categoryId = 4,
                        date = settlement.date,
                        notes = "Settled debt with Friend"
                    )
                )
            } else if (settlement.toFriendId == 0L) {
                // Friend paid user -> income to user account
                database.accountDao().updateBalance(linkedAccountId, settlement.amount)
                database.transactionDao().insertTransaction(
                    TransactionEntity(
                        accountId = linkedAccountId,
                        type = TransactionType.INCOME,
                        amount = settlement.amount,
                        currency = settlement.currency,
                        categoryId = 14,
                        date = settlement.date,
                        notes = "Settlement received from Friend"
                    )
                )
            }
        }
        id
    }

    suspend fun insertImportedTransactions(transactions: List<TransactionEntity>) = withContext(Dispatchers.IO) {
        for (tx in transactions) {
            addTransaction(tx)
        }
    }
}
