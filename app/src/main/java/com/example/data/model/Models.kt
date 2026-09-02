package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE, INCOME, TRANSFER
}

enum class AccountType {
    BANK, CASH, CREDIT_CARD, DIGITAL_WALLET
}

enum class SplitType {
    EQUAL, EXACT, PERCENTAGE, SHARES, ITEMIZED
}

enum class DebtType {
    EMI, LOAN, LENT, BORROWED
}

enum class RecurringFrequency {
    DAILY, WEEKLY, MONTHLY, YEARLY
}

enum class ThemePalette(val displayName: String, val primaryHex: Long, val accentHex: Long) {
    EMERALD("Obsidian Emerald", 0xFF00B594, 0xFF10B981),
    INDIGO("Indigo Night", 0xFF6366F1, 0xFF818CF8),
    BLUE("Electric Blue", 0xFF3B82F6, 0xFF60A5FA),
    PURPLE("Royal Purple", 0xFF8B5CF6, 0xFFA78BFA),
    TEAL("Cyber Teal", 0xFF14B8A6, 0xFF2DD4BF),
    ROSE("Neon Rose", 0xFFF43F5E, 0xFFFB7185)
}

enum class CountryProfileType {
    HOME, EXPAT, ALL
}

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AccountType,
    val balance: Double,
    val currency: String = "USD",
    val countryProfile: String = "HOME", // "HOME" or "EXPAT"
    val colorHex: Long = 0xFF00B594,
    val iconName: String = "account_balance",
    val isDefault: Boolean = false,
    val accountNumberMask: String = ""
)

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconName: String,
    val colorHex: Long,
    val isIncome: Boolean = false,
    val budgetLimit: Double = 0.0
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long,
    val type: TransactionType,
    val amount: Double,
    val currency: String = "USD",
    val categoryId: Long,
    val date: Long = System.currentTimeMillis(),
    val notes: String = "",
    val tags: String = "", // Comma-separated tags
    val receiptImagePath: String = "",
    val targetAccountId: Long? = null, // For TRANSFER
    val splitExpenseId: Long? = null,
    val countryProfile: String = "HOME",
    val isRecurringInstance: Boolean = false,
    val transferFxRate: Double? = null,
    val linkedTransactionId: Long? = null
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val monthlyLimit: Double,
    val currency: String = "USD",
    val countryProfile: String = "HOME",
    val periodMonthYear: String = "" // "YYYY-MM"
)

@Entity(tableName = "savings_goals")
data class SavingsGoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val currency: String = "USD",
    val countryProfile: String = "HOME", // "HOME" or "EXPAT"
    val targetDate: Long = System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000), // 3 months default
    val linkedAccountId: Long? = null,
    val iconName: String = "savings",
    val colorHex: Long = 0xFF00B594,
    val isCompleted: Boolean = false
)

@Entity(tableName = "debt_emis")
data class DebtEmiEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: DebtType,
    val friendId: Long? = null, // For LENT or BORROWED
    val totalAmount: Double,
    val remainingAmount: Double,
    val interestRate: Double = 0.0, // annual %
    val tenureMonths: Int = 12,
    val monthlyPayment: Double = 0.0,
    val dueDate: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
    val currency: String = "USD",
    val countryProfile: String = "HOME", // "HOME" or "EXPAT"
    val notes: String = "",
    val isSettled: Boolean = false
)

@Entity(tableName = "recurring_transactions")
data class RecurringTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val accountId: Long,
    val targetAccountId: Long? = null,
    val frequency: RecurringFrequency = RecurringFrequency.MONTHLY,
    val nextDueDate: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000),
    val lastProcessedDate: Long = 0,
    val isActive: Boolean = true,
    val currency: String = "USD"
)

@Entity(tableName = "friends")
data class FriendEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val avatarColorHex: Long = 0xFF10B981,
    val paymentHandle: String = "" // e.g. PayPal, Venmo, UPI, IBAN
)

@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val iconEmoji: String = "🏖️",
    val description: String = "",
    val memberFriendIds: String = "" // Comma-separated friend IDs
)

@Entity(tableName = "split_expenses")
data class SplitExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val totalAmount: Double,
    val currency: String = "USD",
    val payerFriendId: Long = 0, // 0 = User ("You"), >0 = Friend ID
    val groupId: Long? = null,
    val date: Long = System.currentTimeMillis(),
    val splitType: SplitType = SplitType.EQUAL,
    val notes: String = "",
    val receiptImagePath: String = "",
    val transactionId: Long? = null
)

@Entity(tableName = "split_participants")
data class SplitParticipantEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val splitExpenseId: Long,
    val friendId: Long, // 0 = User ("You")
    val shareAmount: Double,
    val sharePercentage: Double = 0.0,
    val shareUnits: Double = 1.0,
    val isSettled: Boolean = false,
    val itemizedItemsJson: String = ""
)

@Entity(tableName = "settlements")
data class SettlementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fromFriendId: Long, // 0 = User
    val toFriendId: Long,   // 0 = User
    val amount: Double,
    val currency: String = "USD",
    val date: Long = System.currentTimeMillis(),
    val paymentMethod: String = "Cash", // Cash, Bank, Digital Wallet
    val note: String = "",
    val linkedGroupId: Long? = null
)

@Entity(tableName = "exchange_rates", primaryKeys = ["fromCurrency", "toCurrency"])
data class ExchangeRateEntity(
    val fromCurrency: String,
    val toCurrency: String,
    val rate: Double,
    val lastUpdated: Long = System.currentTimeMillis()
)
