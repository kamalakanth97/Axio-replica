package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.AxioTealPrimary
import com.example.ui.theme.RupeeAccentIndigo
import com.example.ui.theme.RupeeBlue
import com.example.ui.theme.RupeeIncomeGreen
import com.example.ui.theme.RupeeOrange
import com.example.ui.theme.RupeePink
import com.example.ui.theme.RupeePurple
import com.example.ui.theme.RupeeWarningAmber

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

enum class PaymentMode(val displayName: String) {
    UPI("UPI (GPay/PhonePe/Paytm)"),
    CREDIT_CARD("Credit Card"),
    DEBIT_CARD("Debit Card"),
    NET_BANKING("Net Banking"),
    CASH("Cash in Hand")
}

enum class ExpenseCategory(
    val displayName: String,
    val color: Color
) {
    FOOD("Food & Dining", RupeeOrange),
    GROCERIES("Groceries", RupeeIncomeGreen),
    SHOPPING("Shopping", RupeePink),
    TRAVEL("Travel & Commute", RupeeBlue),
    BILLS("Bills & Utilities", RupeeWarningAmber),
    ENTERTAINMENT("Entertainment", RupeePurple),
    HEALTH("Health & Medical", Color(0xFFEF4444)),
    INVESTMENT("Investments & MF", AxioTealPrimary),
    SALARY("Salary & Income", RupeeIncomeGreen),
    EDUCATION("Education", RupeeAccentIndigo),
    OTHER("Other Expenses", Color(0xFF64748B));

    fun getIcon(): ImageVector {
        return when (this) {
            FOOD -> Icons.Default.Fastfood
            GROCERIES -> Icons.Default.ShoppingCart
            SHOPPING -> Icons.Default.ShoppingCart
            TRAVEL -> Icons.Default.DirectionsCar
            BILLS -> Icons.Default.Receipt
            ENTERTAINMENT -> Icons.Default.Movie
            HEALTH -> Icons.Default.LocalHospital
            INVESTMENT -> Icons.Default.TrendingUp
            SALARY -> Icons.Default.Work
            EDUCATION -> Icons.Default.School
            OTHER -> Icons.Default.Payments
        }
    }
}

enum class AccountType(val displayName: String) {
    SAVINGS("Savings Account"),
    CREDIT_CARD("Credit Card"),
    WALLET("Digital Wallet"),
    CASH("Cash in Hand")
}
