package com.example.data.repository

import com.example.data.database.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.random.Random

class PayRepository(
    private val contactDao: ContactDao,
    private val transactionDao: TransactionDao,
    private val scratchCardDao: ScratchCardDao,
    private val bankCardDao: BankCardDao
) {
    val contacts: Flow<List<Contact>> = contactDao.getAllContacts()
    val transactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val scratchCards: Flow<List<ScratchCard>> = scratchCardDao.getAllScratchCards()
    val bankCards: Flow<List<BankCard>> = bankCardDao.getAllBankCards()

    suspend fun insertContact(contact: Contact) {
        contactDao.insertContact(contact)
    }

    suspend fun deleteContact(contact: Contact) {
        contactDao.deleteContact(contact)
    }

    suspend fun performPayment(
        amount: Double,
        recipientName: String,
        recipientDetail: String,
        senderName: String,
        category: String,
        type: String, // "SEND", "BILL", "RECHARGE"
        note: String,
        selectedBankCardId: Int
    ): Boolean {
        // Find the selected bank card
        val currentCards = bankCards.first()
        val selectedCard = currentCards.find { it.id == selectedBankCardId } ?: return false

        if (selectedCard.balance < amount) {
            // Insufficient balance, insert a failed transaction log
            val failedTx = Transaction(
                type = type,
                amount = amount,
                recipientName = recipientName,
                recipientDetail = recipientDetail,
                senderName = senderName,
                category = category,
                status = "FAILED",
                note = "$note (Failed: Insufficient funds)"
            )
            transactionDao.insertTransaction(failedTx)
            return false
        }

        // Deduct balance from the bank card
        val updatedCard = selectedCard.copy(balance = selectedCard.balance - amount)
        bankCardDao.updateBankCard(updatedCard)

        // Add successful transaction
        val successTx = Transaction(
            type = type,
            amount = amount,
            recipientName = recipientName,
            recipientDetail = recipientDetail,
            senderName = senderName,
            category = category,
            status = "SUCCESS",
            note = note
        )
        transactionDao.insertTransaction(successTx)

        // 50% chance of earning a scratch card reward!
        if (Random.nextFloat() < 0.5f) {
            val isCashback = Random.nextBoolean()
            val rewardAmount = if (isCashback) {
                // Random cashback amount between $2.00 and $20.00
                Random.nextDouble(2.0, 20.0)
            } else {
                0.0
            }

            val scratchCard = if (isCashback) {
                ScratchCard(
                    title = "Cashback Reward",
                    subtitle = "Earned for paying $recipientName",
                    amount = Math.round(rewardAmount * 100.0) / 100.0,
                    rewardType = "CASHBACK"
                )
            } else {
                val brands = listOf("Starbucks", "Amazon", "Nike", "Uber", "Target")
                val brand = brands.random()
                val discount = listOf("20%", "30%", "50%", "$5 Off", "Buy 1 Get 1")
                val code = brand.uppercase() + Random.nextInt(100, 999).toString()
                ScratchCard(
                    title = "$brand Voucher",
                    subtitle = "${discount.random()} Promo Code",
                    amount = 0.0,
                    rewardType = "VOUCHER",
                    code = code
                )
            }
            scratchCardDao.insertScratchCard(scratchCard)
        }

        return true
    }

    suspend fun scratchCard(card: ScratchCard) {
        if (card.isScratched) return

        // Update card to scratched
        val updatedCard = card.copy(isScratched = true)
        scratchCardDao.updateScratchCard(updatedCard)

        // If it's a cashback reward, credit it to our GPay Wallet balance!
        if (card.rewardType == "CASHBACK" && card.amount > 0) {
            val cards = bankCards.first()
            // Find the GPay Wallet card, or use the primary card
            val targetCard = cards.find { it.bankName == "GPay Balance" }
                ?: cards.find { it.isPrimary }
                ?: cards.firstOrNull()

            targetCard?.let {
                val updatedBalanceCard = it.copy(balance = it.balance + card.amount)
                bankCardDao.updateBankCard(updatedBalanceCard)

                // Log the reward transaction as RECEIVE
                val tx = Transaction(
                    type = "RECEIVE",
                    amount = card.amount,
                    recipientName = "Me",
                    recipientDetail = "GPay Wallet",
                    senderName = "GPay Rewards",
                    category = "Rewards",
                    status = "SUCCESS",
                    note = "Cashback scratch card cashback! 🎉"
                )
                transactionDao.insertTransaction(tx)
            }
        }
    }

    suspend fun addBankCard(card: BankCard) {
        bankCardDao.insertBankCard(card)
    }

    suspend fun updateBankCard(card: BankCard) {
        bankCardDao.updateBankCard(card)
    }
}
