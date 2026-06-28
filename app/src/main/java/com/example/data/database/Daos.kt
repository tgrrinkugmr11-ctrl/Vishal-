package com.example.data.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY name ASC")
    fun getAllContacts(): Flow<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<Contact>)

    @Delete
    suspend fun deleteContact(contact: Contact)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<Transaction>)
}

@Dao
interface ScratchCardDao {
    @Query("SELECT * FROM scratch_cards ORDER BY timestamp DESC")
    fun getAllScratchCards(): Flow<List<ScratchCard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScratchCard(card: ScratchCard)

    @Update
    suspend fun updateScratchCard(card: ScratchCard)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<ScratchCard>)
}

@Dao
interface BankCardDao {
    @Query("SELECT * FROM bank_cards ORDER BY isPrimary DESC, bankName ASC")
    fun getAllBankCards(): Flow<List<BankCard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBankCard(card: BankCard)

    @Update
    suspend fun updateBankCard(card: BankCard)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(cards: List<BankCard>)
}
