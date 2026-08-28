package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.DefaultSeedData
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.BillReminderEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.SplitExpenseEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.AccountType
import com.example.data.model.ExpenseCategory
import com.example.data.model.PaymentMode
import com.example.data.model.TransactionType
import com.example.data.repository.AxioRepository
import com.example.util.ParsedSmsResult
import com.example.util.SmsExpenseParser
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

data class CategorySpend(
    val category: ExpenseCategory,
    val totalAmount: Double, // in INR ₹
    val percentage: Float,
    val transactionCount: Int
)

data class DataBundle(
    val transactions: List<TransactionEntity>,
    val accounts: List<AccountEntity>,
    val budgets: List<BudgetEntity>,
    val bills: List<BillReminderEntity>,
    val splits: List<SplitExpenseEntity>
)

data class UpiPromptState(
    val isOpen: Boolean = false,
    val initialVpa: String = "",
    val initialName: String = "",
    val initialAmount: Double = 0.0,
    val initialNote: String = "",
    val initialCategory: ExpenseCategory = ExpenseCategory.FOOD
)

data class AxioUiState(
    val totalBankBalance: Double = 0.0, // in INR ₹
    val totalCreditCardDue: Double = 0.0, // in INR ₹
    val totalCashInHand: Double = 0.0, // in INR ₹
    val currentMonthSpend: Double = 0.0, // in INR ₹
    val currentMonthIncome: Double = 0.0, // in INR ₹
    val transactions: List<TransactionEntity> = emptyList(),
    val accounts: List<AccountEntity> = emptyList(),
    val budgets: List<BudgetEntity> = emptyList(),
    val bills: List<BillReminderEntity> = emptyList(),
    val splits: List<SplitExpenseEntity> = emptyList(),
    val categorySpends: List<CategorySpend> = emptyList(),
    val selectedCategoryFilter: ExpenseCategory? = null,
    val searchQuery: String = "",
    val isSmsSimulatorOpen: Boolean = false,
    val isAddTransactionOpen: Boolean = false,
    val isAddAccountOpen: Boolean = false,
    val isAddBillOpen: Boolean = false,
    val isAddSplitOpen: Boolean = false,
    val isAddBudgetOpen: Boolean = false,
    val lastParsedSms: ParsedSmsResult? = null,
    val smsBannerMessage: String? = null
)

class AxioViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AxioRepository(AppDatabase.getDatabase(application))

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategoryFilter = MutableStateFlow<ExpenseCategory?>(null)
    val selectedCategoryFilter: StateFlow<ExpenseCategory?> = _selectedCategoryFilter.asStateFlow()

    private val _smsBannerMessage = MutableStateFlow<String?>(null)
    val smsBannerMessage: StateFlow<String?> = _smsBannerMessage.asStateFlow()

    private val _isSmsSimulatorOpen = MutableStateFlow(false)
    val isSmsSimulatorOpen: StateFlow<Boolean> = _isSmsSimulatorOpen.asStateFlow()

    private val _isAddTransactionOpen = MutableStateFlow(false)
    val isAddTransactionOpen: StateFlow<Boolean> = _isAddTransactionOpen.asStateFlow()

    private val _isAddAccountOpen = MutableStateFlow(false)
    val isAddAccountOpen: StateFlow<Boolean> = _isAddAccountOpen.asStateFlow()

    private val _isAddBillOpen = MutableStateFlow(false)
    val isAddBillOpen: StateFlow<Boolean> = _isAddBillOpen.asStateFlow()

    private val _isAddSplitOpen = MutableStateFlow(false)
    val isAddSplitOpen: StateFlow<Boolean> = _isAddSplitOpen.asStateFlow()

    private val _isAddBudgetOpen = MutableStateFlow(false)
    val isAddBudgetOpen: StateFlow<Boolean> = _isAddBudgetOpen.asStateFlow()

    private val _isQrScannerOpen = MutableStateFlow(false)
    val isQrScannerOpen: StateFlow<Boolean> = _isQrScannerOpen.asStateFlow()

    private val _isExportDialogOpen = MutableStateFlow(false)
    val isExportDialogOpen: StateFlow<Boolean> = _isExportDialogOpen.asStateFlow()

    private val _postPaymentData = MutableStateFlow<com.example.ui.dialogs.UpiPostPaymentData?>(null)
    val postPaymentData: StateFlow<com.example.ui.dialogs.UpiPostPaymentData?> = _postPaymentData.asStateFlow()

    private val _upiPromptState = MutableStateFlow(UpiPromptState())
    val upiPromptState: StateFlow<UpiPromptState> = _upiPromptState.asStateFlow()

    @Suppress("UNCHECKED_CAST")
    val uiState: StateFlow<AxioUiState> = combine(
        combine(
            repository.allTransactions,
            repository.allAccounts,
            repository.getBudgetsForMonth("08-2026"),
            repository.allBills,
            repository.allSplits
        ) { txns, accts, bgts, bills, splits ->
            DataBundle(txns, accts, bgts, bills, splits)
        },
        _searchQuery,
        _selectedCategoryFilter
    ) { bundle, query, catFilter ->
        val txns = bundle.transactions
        val accts = bundle.accounts
        val bgts = bundle.budgets
        val bills = bundle.bills
        val splits = bundle.splits

        val filteredTxns = txns.filter { txn ->
            val matchesCat = catFilter == null || txn.category == catFilter
            val matchesQuery = query.isBlank() ||
                    txn.title.contains(query, ignoreCase = true) ||
                    txn.accountName.contains(query, ignoreCase = true) ||
                    txn.category.displayName.contains(query, ignoreCase = true)
            matchesCat && matchesQuery
        }

        // Calculations in Indian Rupees
        val totalBankBal = accts.filter { it.accountType == AccountType.SAVINGS }.sumOf { it.balance }
        val totalCCDue = accts.filter { it.accountType == AccountType.CREDIT_CARD }.sumOf { it.balance }
        val totalCash = accts.filter { it.accountType == AccountType.CASH || it.accountType == AccountType.WALLET }.sumOf { it.balance }

        val currentMonthSpend = txns.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val currentMonthIncome = txns.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

        // Category breakdown
        val expenseTxns = txns.filter { it.type == TransactionType.EXPENSE }
        val totalExp = currentMonthSpend.coerceAtLeast(1.0)
        val categorySpends = expenseTxns.groupBy { it.category }
            .map { (cat, list) ->
                val sum = list.sumOf { it.amount }
                CategorySpend(
                    category = cat,
                    totalAmount = sum,
                    percentage = ((sum / totalExp) * 100).toFloat(),
                    transactionCount = list.size
                )
            }.sortedByDescending { it.totalAmount }

        AxioUiState(
            totalBankBalance = totalBankBal,
            totalCreditCardDue = totalCCDue,
            totalCashInHand = totalCash,
            currentMonthSpend = currentMonthSpend,
            currentMonthIncome = currentMonthIncome,
            transactions = filteredTxns,
            accounts = accts,
            budgets = bgts,
            bills = bills,
            splits = splits,
            categorySpends = categorySpends,
            selectedCategoryFilter = catFilter,
            searchQuery = query
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AxioUiState()
    )

    init {
        viewModelScope.launch {
            repository.initializeSeedDataIfNeeded()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCategoryFilter(category: ExpenseCategory?) {
        _selectedCategoryFilter.value = category
    }

    fun openSmsSimulator(open: Boolean) {
        _isSmsSimulatorOpen.value = open
    }

    fun openAddTransaction(open: Boolean) {
        _isAddTransactionOpen.value = open
    }

    fun openAddAccount(open: Boolean) {
        _isAddAccountOpen.value = open
    }

    fun openAddBill(open: Boolean) {
        _isAddBillOpen.value = open
    }

    fun openAddSplit(open: Boolean) {
        _isAddSplitOpen.value = open
    }

    fun openAddBudget(open: Boolean) {
        _isAddBudgetOpen.value = open
    }

    fun openQrScanner(open: Boolean) {
        _isQrScannerOpen.value = open
    }

    fun openExportDialog(open: Boolean) {
        _isExportDialogOpen.value = open
    }

    fun openPostPaymentReview(data: com.example.ui.dialogs.UpiPostPaymentData) {
        _postPaymentData.value = data
    }

    fun dismissPostPaymentReview() {
        _postPaymentData.value = null
    }

    fun savePostPaymentTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: ExpenseCategory,
        paymentMode: PaymentMode,
        accountName: String,
        notes: String,
        splitWithFriends: Boolean,
        splitMembers: String
    ) {
        viewModelScope.launch {
            val transaction = TransactionEntity(
                title = title,
                amount = amount,
                type = type,
                category = category,
                paymentMode = paymentMode,
                accountName = accountName,
                timestamp = System.currentTimeMillis(),
                notes = notes
            )
            repository.addTransaction(transaction)

            if (splitWithFriends && splitMembers.isNotBlank()) {
                val memberList = splitMembers.split(",").map { it.trim() }.filter { it.isNotBlank() }
                val totalMembers = memberList.size.coerceAtLeast(1)
                val myShare = amount / totalMembers
                val split = SplitExpenseEntity(
                    title = "Split: $title",
                    totalAmount = amount,
                    paidBy = "You",
                    members = splitMembers,
                    myShare = myShare,
                    isSettled = false,
                    timestamp = System.currentTimeMillis()
                )
                repository.addSplitExpense(split)
            }

            _smsBannerMessage.value = "✓ Logged ₹${amount.toInt()} for $title under ${category.displayName}"
        }
    }

    fun openUpiPayment(
        vpa: String = "",
        name: String = "",
        amount: Double = 0.0,
        note: String = "",
        category: ExpenseCategory = ExpenseCategory.FOOD
    ) {
        _upiPromptState.value = UpiPromptState(
            isOpen = true,
            initialVpa = vpa,
            initialName = name,
            initialAmount = amount,
            initialNote = note,
            initialCategory = category
        )
    }

    fun closeUpiPayment() {
        _upiPromptState.value = UpiPromptState(isOpen = false)
    }

    fun dismissSmsBanner() {
        _smsBannerMessage.value = null
    }

    fun parseAndProcessSms(smsText: String, sender: String = "") {
        val result = SmsExpenseParser.parseSms(smsText, sender)
        if (result != null) {
            viewModelScope.launch {
                repository.processParsedSms(result)
                val typeWord = if (result.type == TransactionType.INCOME) "credited" else "debited"
                _smsBannerMessage.value = "⚡ Auto-detected: ₹${result.amount} $typeWord for ${result.merchantOrSender}"
            }
        }
    }

    fun addTransaction(
        title: String,
        amount: Double,
        type: TransactionType,
        category: ExpenseCategory,
        paymentMode: PaymentMode,
        accountName: String,
        notes: String
    ) {
        viewModelScope.launch {
            val transaction = TransactionEntity(
                title = title,
                amount = amount,
                type = type,
                category = category,
                paymentMode = paymentMode,
                accountName = accountName,
                timestamp = System.currentTimeMillis(),
                notes = notes
            )
            repository.addTransaction(transaction)
            _isAddTransactionOpen.value = false
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

    fun addAccount(
        bankName: String,
        type: AccountType,
        balance: Double,
        last4: String,
        creditLimit: Double,
        billDueDate: String
    ) {
        viewModelScope.launch {
            val account = AccountEntity(
                id = "acc_${System.currentTimeMillis()}",
                bankName = bankName,
                accountType = type,
                balance = balance,
                accountNumberLast4 = last4,
                creditLimit = creditLimit,
                billDueDate = billDueDate,
                cardColorHex = if (type == AccountType.CREDIT_CARD) 0xFF831843 else 0xFF004D40
            )
            repository.addAccount(account)
            _isAddAccountOpen.value = false
        }
    }

    fun toggleBillPaid(bill: BillReminderEntity) {
        viewModelScope.launch {
            repository.toggleBillPaid(bill)
        }
    }

    fun addBill(title: String, amount: Double, daysFromNow: Int, biller: String, category: ExpenseCategory) {
        viewModelScope.launch {
            val dueDate = System.currentTimeMillis() + (daysFromNow * 24 * 3600 * 1000L)
            val bill = BillReminderEntity(
                title = title,
                amount = amount,
                dueDateTimestamp = dueDate,
                isPaid = false,
                category = category,
                billerName = biller
            )
            repository.addBill(bill)
            _isAddBillOpen.value = false
        }
    }

    fun deleteBill(bill: BillReminderEntity) {
        viewModelScope.launch {
            repository.deleteBill(bill)
        }
    }

    fun addSplitExpense(title: String, totalAmount: Double, paidBy: String, members: String, myShare: Double) {
        viewModelScope.launch {
            val split = SplitExpenseEntity(
                title = title,
                totalAmount = totalAmount,
                paidBy = paidBy,
                members = members,
                myShare = myShare,
                isSettled = false,
                timestamp = System.currentTimeMillis()
            )
            repository.addSplitExpense(split)
            _isAddSplitOpen.value = false
        }
    }

    fun toggleSplitSettled(split: SplitExpenseEntity) {
        viewModelScope.launch {
            repository.toggleSplitSettled(split)
        }
    }

    fun deleteSplit(split: SplitExpenseEntity) {
        viewModelScope.launch {
            repository.deleteSplitExpense(split)
        }
    }

    fun setBudgetLimit(category: ExpenseCategory, limit: Double) {
        viewModelScope.launch {
            val budget = BudgetEntity(
                category = category,
                monthlyLimit = limit,
                monthYear = "08-2026"
            )
            repository.addOrUpdateBudget(budget)
            _isAddBudgetOpen.value = false
        }
    }
}
