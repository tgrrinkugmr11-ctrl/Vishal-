package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val upiId: String,
    val avatarColorHex: String
)

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "SEND", "RECEIVE", "BILL", "RECHARGE"
    val amount: Double,
    val recipientName: String,
    val recipientDetail: String, // Phone or UPI or Card number
    val senderName: String,
    val category: String, // "Food", "Bills", "Transfer", "Travel", "Shopping", "Rewards"
    val status: String, // "SUCCESS", "PENDING", "FAILED"
    val timestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

@Entity(tableName = "scratch_cards")
data class ScratchCard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val subtitle: String,
    val amount: Double,
    val isScratched: Boolean = false,
    val rewardType: String, // "CASHBACK", "VOUCHER"
    val code: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "bank_cards")
data class BankCard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bankName: String,
    val accountType: String, // "Savings", "Checking"
    val lastFourDigits: String,
    val balance: Double,
    val isPrimary: Boolean = false,
    val upiPin: String = "1234" // For simulated balance inquiry verification
)
