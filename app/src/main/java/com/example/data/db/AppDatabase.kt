package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AccountEntity::class,
        CategoryEntity::class,
        TransactionEntity::class,
        BudgetEntity::class,
        SavingsGoalEntity::class,
        DebtEmiEntity::class,
        RecurringTransactionEntity::class,
        FriendEntity::class,
        GroupEntity::class,
        SplitExpenseEntity::class,
        SplitParticipantEntity::class,
        SettlementEntity::class,
        ExchangeRateEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun savingsGoalDao(): SavingsGoalDao
    abstract fun debtEmiDao(): DebtEmiDao
    abstract fun recurringTransactionDao(): RecurringTransactionDao
    abstract fun friendDao(): FriendDao
    abstract fun groupDao(): GroupDao
    abstract fun splitExpenseDao(): SplitExpenseDao
    abstract fun splitParticipantDao(): SplitParticipantDao
    abstract fun settlementDao(): SettlementDao
    abstract fun exchangeRateDao(): ExchangeRateDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE savings_goals ADD COLUMN countryProfile TEXT NOT NULL DEFAULT 'HOME'")
                db.execSQL("ALTER TABLE debt_emis ADD COLUMN countryProfile TEXT NOT NULL DEFAULT 'HOME'")
                db.execSQL("ALTER TABLE transactions ADD COLUMN transferFxRate REAL")
                db.execSQL("ALTER TABLE transactions ADD COLUMN linkedTransactionId INTEGER")
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ledgr_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .addMigrations(MIGRATION_2_3)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        suspend fun populateInitialData(database: AppDatabase) {
            val categoryDao = database.categoryDao()
            val exchangeRateDao = database.exchangeRateDao()

            // Seed Standard Default Categories (Expense & Income)
            val defaultCategories = listOf(
                CategoryEntity(1, "Food & Dining", "restaurant", 0xFF10B981, false, 0.0),
                CategoryEntity(2, "Groceries", "shopping_cart", 0xFF00B594, false, 0.0),
                CategoryEntity(3, "Housing & Rent", "home", 0xFF6366F1, false, 0.0),
                CategoryEntity(4, "Utilities & Bills", "bolt", 0xFFF59E0B, false, 0.0),
                CategoryEntity(5, "Transport & Fuel", "directions_car", 0xFF3B82F6, false, 0.0),
                CategoryEntity(6, "Shopping", "shopping_bag", 0xFFEC4899, false, 0.0),
                CategoryEntity(7, "Entertainment", "movie", 0xFF8B5CF6, false, 0.0),
                CategoryEntity(8, "Health & Fitness", "fitness_center", 0xFF14B8A6, false, 0.0),
                CategoryEntity(9, "Travel", "flight", 0xFF06B6D4, false, 0.0),
                CategoryEntity(10, "Tech & Gadgets", "devices", 0xFF64748B, false, 0.0),
                // Income
                CategoryEntity(11, "Salary", "payments", 0xFF10B981, true, 0.0),
                CategoryEntity(12, "Freelance & Consulting", "work", 0xFF34D399, true, 0.0),
                CategoryEntity(13, "Investments & Dividends", "trending_up", 0xFF059669, true, 0.0),
                CategoryEntity(14, "Gifts & Cashbacks", "card_giftcard", 0xFFA7F3D0, true, 0.0)
            )
            categoryDao.insertCategories(defaultCategories)

            // Seed Exchange Rates Reference Table
            exchangeRateDao.insertRates(
                listOf(
                    ExchangeRateEntity("USD", "EUR", 0.92),
                    ExchangeRateEntity("EUR", "USD", 1.087),
                    ExchangeRateEntity("USD", "GBP", 0.79),
                    ExchangeRateEntity("GBP", "USD", 1.265),
                    ExchangeRateEntity("USD", "INR", 86.80),
                    ExchangeRateEntity("INR", "USD", 0.0115),
                    ExchangeRateEntity("USD", "AED", 3.672),
                    ExchangeRateEntity("AED", "USD", 0.272),
                    ExchangeRateEntity("USD", "JPY", 154.20),
                    ExchangeRateEntity("JPY", "USD", 0.00648),
                    ExchangeRateEntity("USD", "CAD", 1.38),
                    ExchangeRateEntity("CAD", "USD", 0.724),
                    ExchangeRateEntity("USD", "AUD", 1.53),
                    ExchangeRateEntity("AUD", "USD", 0.653),
                    ExchangeRateEntity("USD", "SGD", 1.35),
                    ExchangeRateEntity("SGD", "USD", 0.741),
                    ExchangeRateEntity("USD", "CHF", 0.88),
                    ExchangeRateEntity("CHF", "USD", 1.136)
                )
            )
        }
    }
}
