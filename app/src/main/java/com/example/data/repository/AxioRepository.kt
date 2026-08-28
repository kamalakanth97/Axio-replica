package com.example.data.repository

import com.example.data.DefaultSeedData
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.BillReminderEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.SplitExpenseEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.AccountType
import com.example.data.model.ExpenseCategory
import com.example.data.model.TransactionType
import com.example.util.ParsedSmsResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class AxioRepository(private val database: AppDatabase) {

    val allTransactions: Flow<List<TransactionEntity>> = database.transactionDao().getAllTransactions()
    val allAccounts: Flow<List<AccountEntity>> = database.accountDao().getAllAccounts()
    val allBills: Flow<List<BillReminderEntity>> = database.billReminderDao().getAllBills()
    val allSplits: Flow<List<SplitExpenseEntity>> = database.splitExpenseDao().getAllSplits()

    fun getBudgetsForMonth(monthYear: String): Flow<List<BudgetEntity>> =
        database.budgetDao().getBudgetsForMonth(monthYear)

    suspend fun initializeSeedDataIfNeeded() = withContext(Dispatchers.IO) {
        val existingAccounts = database.accountDao().getAllAccounts().first()
        if (existingAccounts.isEmpty()) {
            database.accountDao().insertAccounts(DefaultSeedData.defaultAccounts)
            database.transactionDao().insertTransactions(DefaultSeedData.getDefaultTransactions())
            database.budgetDao().insertBudgets(DefaultSeedData.defaultBudgets)
            database.billReminderDao().insertBills(DefaultSeedData.getDefaultBills())
            database.splitExpenseDao().insertSplits(DefaultSeedData.getDefaultSplits())
        }
    }

    suspend fun addTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        database.transactionDao().insertTransaction(transaction)
        // Update account balance
        updateAccountBalanceForTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        database.transactionDao().deleteTransaction(transaction)
        // Revert balance
        val accounts = database.accountDao().getAllAccounts().first()
        val account = accounts.find { it.bankName.contains(transaction.accountName) || transaction.accountName.contains(it.bankName) }
        if (account != null) {
            val newBalance = when (transaction.type) {
                TransactionType.EXPENSE -> {
                    if (account.accountType == AccountType.CREDIT_CARD) {
                        (account.balance - transaction.amount).coerceAtLeast(0.0)
                    } else {
                        account.balance + transaction.amount
                    }
                }
                TransactionType.INCOME -> (account.balance - transaction.amount).coerceAtLeast(0.0)
                TransactionType.TRANSFER -> account.balance
            }
            database.accountDao().updateAccount(account.copy(balance = newBalance))
        }
    }

    private suspend fun updateAccountBalanceForTransaction(transaction: TransactionEntity) {
        val accounts = database.accountDao().getAllAccounts().first()
        val account = accounts.find {
            it.bankName.contains(transaction.accountName, ignoreCase = true) ||
            transaction.accountName.contains(it.bankName, ignoreCase = true) ||
            (it.accountNumberLast4.isNotEmpty() && transaction.accountName.contains(it.accountNumberLast4))
        } ?: accounts.firstOrNull() ?: return

        val updatedBalance = when (transaction.type) {
            TransactionType.EXPENSE -> {
                if (account.accountType == AccountType.CREDIT_CARD) {
                    account.balance + transaction.amount // Credit card used amount increases
                } else {
                    (account.balance - transaction.amount).coerceAtLeast(0.0)
                }
            }
            TransactionType.INCOME -> {
                if (account.accountType == AccountType.CREDIT_CARD) {
                    (account.balance - transaction.amount).coerceAtLeast(0.0) // Credit card bill paid
                } else {
                    account.balance + transaction.amount
                }
            }
            TransactionType.TRANSFER -> account.balance
        }

        database.accountDao().updateAccount(account.copy(balance = updatedBalance))
    }

    suspend fun processParsedSms(parsed: ParsedSmsResult) = withContext(Dispatchers.IO) {
        val transaction = TransactionEntity(
            title = parsed.merchantOrSender,
            amount = parsed.amount,
            type = parsed.type,
            category = parsed.category,
            paymentMode = parsed.paymentMode,
            accountName = parsed.accountName,
            timestamp = parsed.timestamp,
            notes = "Auto-parsed from Bank SMS",
            isSmsParsed = true,
            rawSms = parsed.rawSms
        )
        addTransaction(transaction)
    }

    suspend fun addAccount(account: AccountEntity) = withContext(Dispatchers.IO) {
        database.accountDao().insertAccount(account)
    }

    suspend fun updateAccount(account: AccountEntity) = withContext(Dispatchers.IO) {
        database.accountDao().updateAccount(account)
    }

    suspend fun deleteAccount(account: AccountEntity) = withContext(Dispatchers.IO) {
        database.accountDao().deleteAccount(account)
    }

    suspend fun addOrUpdateBudget(budget: BudgetEntity) = withContext(Dispatchers.IO) {
        database.budgetDao().insertBudget(budget)
    }

    suspend fun addBill(bill: BillReminderEntity) = withContext(Dispatchers.IO) {
        database.billReminderDao().insertBill(bill)
    }

    suspend fun toggleBillPaid(bill: BillReminderEntity) = withContext(Dispatchers.IO) {
        val updated = bill.copy(isPaid = !bill.isPaid)
        database.billReminderDao().updateBill(updated)
        if (updated.isPaid) {
            // Also log as an expense transaction automatically
            val transaction = TransactionEntity(
                title = bill.title,
                amount = bill.amount,
                type = TransactionType.EXPENSE,
                category = bill.category,
                paymentMode = com.example.data.model.PaymentMode.UPI,
                accountName = "HDFC Bank - 4092",
                timestamp = System.currentTimeMillis(),
                notes = "Bill payment for ${bill.billerName}"
            )
            addTransaction(transaction)
        }
    }

    suspend fun deleteBill(bill: BillReminderEntity) = withContext(Dispatchers.IO) {
        database.billReminderDao().deleteBill(bill)
    }

    suspend fun addSplitExpense(split: SplitExpenseEntity) = withContext(Dispatchers.IO) {
        database.splitExpenseDao().insertSplit(split)
    }

    suspend fun toggleSplitSettled(split: SplitExpenseEntity) = withContext(Dispatchers.IO) {
        database.splitExpenseDao().updateSplit(split.copy(isSettled = !split.isSettled))
    }

    suspend fun deleteSplitExpense(split: SplitExpenseEntity) = withContext(Dispatchers.IO) {
        database.splitExpenseDao().deleteSplit(split)
    }
}
