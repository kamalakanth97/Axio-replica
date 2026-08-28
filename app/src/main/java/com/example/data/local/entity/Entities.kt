package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AccountType
import com.example.data.model.ExpenseCategory
import com.example.data.model.PaymentMode
import com.example.data.model.TransactionType

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double, // in INR ₹
    val type: TransactionType,
    val category: ExpenseCategory,
    val paymentMode: PaymentMode,
    val accountName: String,
    val timestamp: Long,
    val notes: String = "",
    val isSmsParsed: Boolean = false,
    val rawSms: String? = null
)

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey
    val id: String,
    val bankName: String,
    val accountType: AccountType,
    val balance: Double, // in INR ₹
    val accountNumberLast4: String,
    val creditLimit: Double = 0.0, // for credit cards in INR ₹
    val billDueDate: String = "",
    val cardColorHex: Long = 0xFF00897B
)

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: ExpenseCategory,
    val monthlyLimit: Double, // in INR ₹
    val monthYear: String // e.g. "08-2026"
)

@Entity(tableName = "bill_reminders")
data class BillReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val amount: Double, // in INR ₹
    val dueDateTimestamp: Long,
    val isPaid: Boolean = false,
    val category: ExpenseCategory = ExpenseCategory.BILLS,
    val billerName: String = ""
)

@Entity(tableName = "split_expenses")
data class SplitExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val totalAmount: Double, // in INR ₹
    val paidBy: String,
    val members: String, // comma-separated names (e.g. "Kamal, Rahul, Sneha, Amit")
    val myShare: Double, // in INR ₹
    val isSettled: Boolean = false,
    val timestamp: Long
)
