package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Contact::class, Transaction::class, ScratchCard::class, BankCard::class],
    version = 1,
    exportSchema = false
)
abstract class PayDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDao
    abstract fun transactionDao(): TransactionDao
    abstract fun scratchCardDao(): ScratchCardDao
    abstract fun bankCardDao(): BankCardDao

    companion object {
        @Volatile
        private var INSTANCE: PayDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): PayDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PayDatabase::class.java,
                    "gpay_database"
                )
                .addCallback(PayDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class PayDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(
                        database.contactDao(),
                        database.bankCardDao(),
                        database.transactionDao(),
                        database.scratchCardDao()
                    )
                }
            }
        }

        suspend fun populateDatabase(
            contactDao: ContactDao,
            bankCardDao: BankCardDao,
            transactionDao: TransactionDao,
            scratchCardDao: ScratchCardDao
        ) {
            // 1. Initial Contacts
            val contacts = listOf(
                Contact(name = "Sarah Parker", phone = "+1 555-0142", upiId = "sarah@okaxis", avatarColorHex = "#E91E63"),
                Contact(name = "David Miller", phone = "+1 555-0928", upiId = "davidm@okhdfc", avatarColorHex = "#FF9800"),
                Contact(name = "Alex Johnson", phone = "+1 555-0381", upiId = "alexj@okicici", avatarColorHex = "#4CAF50"),
                Contact(name = "Emma Watson", phone = "+1 555-0723", upiId = "emmaw@oksbi", avatarColorHex = "#9C27B0"),
                Contact(name = "Chris Evans", phone = "+1 555-0814", upiId = "cevans@okaxis", avatarColorHex = "#2196F3"),
                Contact(name = "Jane Smith", phone = "+1 555-0456", upiId = "janes@okaxis", avatarColorHex = "#00BCD4")
            )
            contactDao.insertAll(contacts)

            // 2. Initial Bank Accounts
            val bankCards = listOf(
                BankCard(bankName = "Chase Savings", accountType = "Savings", lastFourDigits = "4820", balance = 3450.50, isPrimary = true, upiPin = "1234"),
                BankCard(bankName = "Wells Fargo Checking", accountType = "Checking", lastFourDigits = "9218", balance = 820.75, isPrimary = false, upiPin = "9999"),
                BankCard(bankName = "GPay Balance", accountType = "Wallet Balance", lastFourDigits = "GPAY", balance = 150.00, isPrimary = false, upiPin = "")
            )
            bankCardDao.insertAll(bankCards)

            // 3. Initial Transactions
            val transactions = listOf(
                Transaction(
                    type = "SEND",
                    amount = 45.00,
                    recipientName = "Sarah Parker",
                    recipientDetail = "sarah@okaxis",
                    senderName = "Me",
                    category = "Food",
                    status = "SUCCESS",
                    timestamp = System.currentTimeMillis() - 2 * 3600 * 1000, // 2 hours ago
                    note = "Dinner split 🍕"
                ),
                Transaction(
                    type = "RECEIVE",
                    amount = 120.00,
                    recipientName = "Me",
                    recipientDetail = "GPay Wallet",
                    senderName = "David Miller",
                    category = "Transfer",
                    status = "SUCCESS",
                    timestamp = System.currentTimeMillis() - 24 * 3600 * 1000, // 1 day ago
                    note = "Rent reimbursement"
                ),
                Transaction(
                    type = "BILL",
                    amount = 85.50,
                    recipientName = "Electricity Board",
                    recipientDetail = "A/C 192830182",
                    senderName = "Me",
                    category = "Bills",
                    status = "SUCCESS",
                    timestamp = System.currentTimeMillis() - 3 * 24 * 3600 * 1000, // 3 days ago
                    note = "June Bill Payment"
                ),
                Transaction(
                    type = "RECHARGE",
                    amount = 15.00,
                    recipientName = "Verizon Prepaid",
                    recipientDetail = "+1 555-0100",
                    senderName = "Me",
                    category = "Bills",
                    status = "SUCCESS",
                    timestamp = System.currentTimeMillis() - 5 * 24 * 3600 * 1000, // 5 days ago
                    note = "Simulated Talktime top-up"
                )
            )
            transactionDao.insertAll(transactions)

            // 4. Initial Scratch Cards
            val scratchCards = listOf(
                ScratchCard(title = "Cashback Reward", subtitle = "Earned on June 25", amount = 12.50, isScratched = false, rewardType = "CASHBACK"),
                ScratchCard(title = "Starbucks Promo", subtitle = "Unlocked on Bill Payment", amount = 0.00, isScratched = false, rewardType = "VOUCHER", code = "STARBUCKS50"),
                ScratchCard(title = "Cashback Reward", subtitle = "Earned on Dinner split", amount = 5.00, isScratched = true, rewardType = "CASHBACK"),
                ScratchCard(title = "Amazon Voucher", subtitle = "Earned on Signup", amount = 15.00, isScratched = true, rewardType = "VOUCHER", code = "AMAZON15")
            )
            scratchCardDao.insertAll(scratchCards)
        }
    }
}
