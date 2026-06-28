package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.BankCard
import com.example.data.database.Contact
import com.example.data.database.ScratchCard
import com.example.data.database.Transaction
import com.example.data.repository.PayRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    object Home : Screen()
    data class Payment(val contact: Contact) : Screen()
    object BalanceInquiry : Screen()
    object QRCodeScan : Screen()
    object QRCodeReceive : Screen()
    object ScratchCardsList : Screen()
    object RechargeBills : Screen()
    object TransactionHistory : Screen()
}

class PayViewModel(private val repository: PayRepository) : ViewModel() {

    // UI state for active screen
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Navigation backstack
    private val backstack = mutableListOf<Screen>()

    // Flows from repository
    val contacts: StateFlow<List<Contact>> = repository.contacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<Transaction>> = repository.transactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scratchCards: StateFlow<List<ScratchCard>> = repository.scratchCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bankCards: StateFlow<List<BankCard>> = repository.bankCards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Security Pin Screen temporary variables
    private val _paymentProcessingResult = MutableStateFlow<PaymentResult?>(null)
    val paymentProcessingResult: StateFlow<PaymentResult?> = _paymentProcessingResult.asStateFlow()

    sealed class PaymentResult {
        object Success : PaymentResult()
        data class Error(val message: String) : PaymentResult()
    }

    fun navigateTo(screen: Screen) {
        backstack.add(_currentScreen.value)
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        if (backstack.isNotEmpty()) {
            _currentScreen.value = backstack.removeAt(backstack.lastIndex)
            return true
        }
        return false
    }

    fun resetPaymentResult() {
        _paymentProcessingResult.value = null
    }

    fun addContact(name: String, phone: String, upiId: String) {
        viewModelScope.launch {
            val randomColors = listOf("#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3", "#00BCD4", "#4CAF50", "#FF9800")
            val contact = Contact(
                name = name,
                phone = phone,
                upiId = upiId,
                avatarColorHex = randomColors.random()
            )
            repository.insertContact(contact)
        }
    }

    fun sendMoney(
        contact: Contact,
        amount: Double,
        note: String,
        selectedBankCardId: Int,
        enteredPin: String
    ) {
        viewModelScope.launch {
            val cards = repository.bankCards.stateIn(viewModelScope).value
            val card = cards.find { it.id == selectedBankCardId }

            if (card == null) {
                _paymentProcessingResult.value = PaymentResult.Error("Selected bank account not found.")
                return@launch
            }

            // Wallet/GPay balance doesn't require a PIN, but bank cards do!
            if (card.upiPin.isNotEmpty() && card.upiPin != enteredPin) {
                _paymentProcessingResult.value = PaymentResult.Error("Incorrect UPI PIN. Please try again.")
                return@launch
            }

            val success = repository.performPayment(
                amount = amount,
                recipientName = contact.name,
                recipientDetail = contact.upiId,
                senderName = "Me",
                category = "Transfer",
                type = "SEND",
                note = note,
                selectedBankCardId = selectedBankCardId
            )

            if (success) {
                _paymentProcessingResult.value = PaymentResult.Success
            } else {
                _paymentProcessingResult.value = PaymentResult.Error("Insufficient balance in selected account.")
            }
        }
    }

    fun payBill(
        billType: String,
        accountNum: String,
        amount: Double,
        selectedBankCardId: Int,
        enteredPin: String
    ) {
        viewModelScope.launch {
            val cards = repository.bankCards.stateIn(viewModelScope).value
            val card = cards.find { it.id == selectedBankCardId }

            if (card == null) {
                _paymentProcessingResult.value = PaymentResult.Error("Selected account not found.")
                return@launch
            }

            if (card.upiPin.isNotEmpty() && card.upiPin != enteredPin) {
                _paymentProcessingResult.value = PaymentResult.Error("Incorrect UPI PIN. Please try again.")
                return@launch
            }

            val success = repository.performPayment(
                amount = amount,
                recipientName = "$billType Payment",
                recipientDetail = "Ref: $accountNum",
                senderName = "Me",
                category = "Bills",
                type = "BILL",
                note = "Utility Bill Payment",
                selectedBankCardId = selectedBankCardId
            )

            if (success) {
                _paymentProcessingResult.value = PaymentResult.Success
            } else {
                _paymentProcessingResult.value = PaymentResult.Error("Insufficient balance.")
            }
        }
    }

    fun performRecharge(
        phoneNumber: String,
        amount: Double,
        operator: String,
        selectedBankCardId: Int,
        enteredPin: String
    ) {
        viewModelScope.launch {
            val cards = repository.bankCards.stateIn(viewModelScope).value
            val card = cards.find { it.id == selectedBankCardId }

            if (card == null) {
                _paymentProcessingResult.value = PaymentResult.Error("Selected account not found.")
                return@launch
            }

            if (card.upiPin.isNotEmpty() && card.upiPin != enteredPin) {
                _paymentProcessingResult.value = PaymentResult.Error("Incorrect UPI PIN. Please try again.")
                return@launch
            }

            val success = repository.performPayment(
                amount = amount,
                recipientName = "$operator Recharge",
                recipientDetail = phoneNumber,
                senderName = "Me",
                category = "Bills",
                type = "RECHARGE",
                note = "Mobile Plan Top-up",
                selectedBankCardId = selectedBankCardId
            )

            if (success) {
                _paymentProcessingResult.value = PaymentResult.Success
            } else {
                _paymentProcessingResult.value = PaymentResult.Error("Insufficient balance.")
            }
        }
    }

    fun scratchCard(card: ScratchCard) {
        viewModelScope.launch {
            repository.scratchCard(card)
        }
    }

    fun addBankCard(card: BankCard) {
        viewModelScope.launch {
            repository.addBankCard(card)
        }
    }

    fun updateBankCard(card: BankCard) {
        viewModelScope.launch {
            repository.updateBankCard(card)
        }
    }

    fun setPrimaryCard(cardId: Int) {
        viewModelScope.launch {
            val cards = bankCards.value
            cards.forEach { card ->
                repository.updateBankCard(card.copy(isPrimary = card.id == cardId))
            }
        }
    }

    class Factory(private val repository: PayRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PayViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PayViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
