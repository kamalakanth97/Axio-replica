package com.example.data

import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.BillReminderEntity
import com.example.data.local.entity.BudgetEntity
import com.example.data.local.entity.SplitExpenseEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.AccountType
import com.example.data.model.ExpenseCategory
import com.example.data.model.PaymentMode
import com.example.data.model.TransactionType

object DefaultSeedData {

    val defaultAccounts = listOf(
        AccountEntity(
            id = "acc_hdfc",
            bankName = "HDFC Bank Salary A/c",
            accountType = AccountType.SAVINGS,
            balance = 163250.00, // ₹1,63,250.00
            accountNumberLast4 = "4092",
            creditLimit = 0.0,
            billDueDate = "",
            cardColorHex = 0xFF004D40
        ),
        AccountEntity(
            id = "acc_sbi",
            bankName = "SBI Savings A/c",
            accountType = AccountType.SAVINGS,
            balance = 48500.00, // ₹48,500.00
            accountNumberLast4 = "1104",
            creditLimit = 0.0,
            billDueDate = "",
            cardColorHex = 0xFF1E3A8A
        ),
        AccountEntity(
            id = "acc_icici_cc",
            bankName = "ICICI Sapphiro Credit Card",
            accountType = AccountType.CREDIT_CARD,
            balance = 14280.00, // Used amount: ₹14,280.00
            accountNumberLast4 = "8812",
            creditLimit = 300000.00, // Limit: ₹3,00,000.00
            billDueDate = "02 Sep 2026",
            cardColorHex = 0xFF991B1B
        ),
        AccountEntity(
            id = "acc_axis_cc",
            bankName = "Axis Bank Magnus Card",
            accountType = AccountType.CREDIT_CARD,
            balance = 8450.00, // Used: ₹8,450.00
            accountNumberLast4 = "9021",
            creditLimit = 500000.00, // Limit: ₹5,00,000.00
            billDueDate = "10 Sep 2026",
            cardColorHex = 0xFF701A75
        ),
        AccountEntity(
            id = "acc_cash",
            bankName = "Cash in Hand & Wallet",
            accountType = AccountType.CASH,
            balance = 4600.00, // ₹4,600.00
            accountNumberLast4 = "CASH",
            creditLimit = 0.0,
            billDueDate = "",
            cardColorHex = 0xFF047857
        )
    )

    fun getDefaultTransactions(): List<TransactionEntity> {
        val now = System.currentTimeMillis()
        val oneHour = 3600 * 1000L
        val oneDay = 24 * 3600 * 1000L

        return listOf(
            TransactionEntity(
                title = "Swiggy - Gourmet Lunch",
                amount = 540.00, // ₹540.00
                type = TransactionType.EXPENSE,
                category = ExpenseCategory.FOOD,
                paymentMode = PaymentMode.UPI,
                accountName = "HDFC Bank - 4092",
                timestamp = now - (2 * oneHour),
                notes = "Lunch with team",
                isSmsParsed = true,
                rawSms = "Rs 540.00 debited from HDFC A/c XX4092 on 26-AUG-26 to SWIGGY UPI. Avail Bal: Rs 1,63,250.00"
            ),
            TransactionEntity(
                title = "Blinkit Instant Groceries",
                amount = 890.00, // ₹890.00
                type = TransactionType.EXPENSE,
                category = ExpenseCategory.GROCERIES,
                paymentMode = PaymentMode.UPI,
                accountName = "SBI Bank - 1104",
                timestamp = now - (5 * oneHour),
                notes = "Daily essentials & vegetables",
                isSmsParsed = true,
                rawSms = "Sent Rs. 890.00 from SBI A/c 1104 to BLINKIT via UPI. Avail Bal Rs 48,500.00"
            ),
            TransactionEntity(
                title = "Uber Premier to Airport",
                amount = 750.00, // ₹750.00
                type = TransactionType.EXPENSE,
                category = ExpenseCategory.TRAVEL,
                paymentMode = PaymentMode.CREDIT_CARD,
                accountName = "ICICI Card - 8812",
                timestamp = now - (1 * oneDay),
                notes = "Airport ride",
                isSmsParsed = true,
                rawSms = "INR 750.00 spent on ICICI Credit Card XX8812 at UBER INDIA on 25-AUG-26."
            ),
            TransactionEntity(
                title = "Amazon India - Tech Gear",
                amount = 2499.00, // ₹2,499.00
                type = TransactionType.EXPENSE,
                category = ExpenseCategory.SHOPPING,
                paymentMode = PaymentMode.CREDIT_CARD,
                accountName = "Axis Card - 9021",
                timestamp = now - (2 * oneDay),
                notes = "Wireless charging pad",
                isSmsParsed = true,
                rawSms = "Alert: Rs 2,499.00 spent on Axis Bank CC XX9021 at AMAZON INDIA."
            ),
            TransactionEntity(
                title = "Tata Power Electricity Bill",
                amount = 3120.00, // ₹3,120.00
                type = TransactionType.EXPENSE,
                category = ExpenseCategory.BILLS,
                paymentMode = PaymentMode.NET_BANKING,
                accountName = "HDFC Bank - 4092",
                timestamp = now - (3 * oneDay),
                notes = "Electricity bill for July-August",
                isSmsParsed = true,
                rawSms = "Rs 3120.00 paid for Tata Power from HDFC NetBanking."
            ),
            TransactionEntity(
                title = "Zerodha Coin - Nifty 50 Index SIP",
                amount = 15000.00, // ₹15,000.00
                type = TransactionType.EXPENSE,
                category = ExpenseCategory.INVESTMENT,
                paymentMode = PaymentMode.NET_BANKING,
                accountName = "HDFC Bank - 4092",
                timestamp = now - (4 * oneDay),
                notes = "Monthly Mutual Fund SIP",
                isSmsParsed = false
            ),
            TransactionEntity(
                title = "BookMyShow - IMAX Movie Tickets",
                amount = 920.00, // ₹920.00
                type = TransactionType.EXPENSE,
                category = ExpenseCategory.ENTERTAINMENT,
                paymentMode = PaymentMode.UPI,
                accountName = "HDFC Bank - 4092",
                timestamp = now - (5 * oneDay),
                notes = "Weekend movie with friends",
                isSmsParsed = true,
                rawSms = "Rs 920.00 debited from A/c XX4092 to BookMyShow UPI."
            ),
            TransactionEntity(
                title = "Apollo Pharmacy Medicine",
                amount = 460.00, // ₹460.00
                type = TransactionType.EXPENSE,
                category = ExpenseCategory.HEALTH,
                paymentMode = PaymentMode.CASH,
                accountName = "Cash in Hand & Wallet",
                timestamp = now - (6 * oneDay),
                notes = "First aid & vitamins",
                isSmsParsed = false
            ),
            TransactionEntity(
                title = "Monthly Salary - TechCorp Solutions",
                amount = 145000.00, // ₹1,45,000.00
                type = TransactionType.INCOME,
                category = ExpenseCategory.SALARY,
                paymentMode = PaymentMode.NET_BANKING,
                accountName = "HDFC Bank - 4092",
                timestamp = now - (25 * oneDay),
                notes = "August Salary credit",
                isSmsParsed = true,
                rawSms = "Salary of Rs 1,45,000.00 credited to HDFC A/c XX4092 on 01-AUG-26. Total Bal Rs. 1,63,250.00"
            )
        )
    }

    val defaultBudgets = listOf(
        BudgetEntity(category = ExpenseCategory.FOOD, monthlyLimit = 12000.00, monthYear = "08-2026"),
        BudgetEntity(category = ExpenseCategory.GROCERIES, monthlyLimit = 10000.00, monthYear = "08-2026"),
        BudgetEntity(category = ExpenseCategory.SHOPPING, monthlyLimit = 15000.00, monthYear = "08-2026"),
        BudgetEntity(category = ExpenseCategory.TRAVEL, monthlyLimit = 6000.00, monthYear = "08-2026"),
        BudgetEntity(category = ExpenseCategory.BILLS, monthlyLimit = 8000.00, monthYear = "08-2026"),
        BudgetEntity(category = ExpenseCategory.ENTERTAINMENT, monthlyLimit = 5000.00, monthYear = "08-2026"),
        BudgetEntity(category = ExpenseCategory.INVESTMENT, monthlyLimit = 30000.00, monthYear = "08-2026")
    )

    fun getDefaultBills(): List<BillReminderEntity> {
        val now = System.currentTimeMillis()
        val oneDay = 24 * 3600 * 1000L
        return listOf(
            BillReminderEntity(
                title = "ICICI Sapphiro Credit Card Bill",
                amount = 14280.00, // ₹14,280.00
                dueDateTimestamp = now + (7 * oneDay),
                isPaid = false,
                category = ExpenseCategory.BILLS,
                billerName = "ICICI Bank"
            ),
            BillReminderEntity(
                title = "JioFiber Ultra Broadband",
                amount = 1178.00, // ₹1,178.00
                dueDateTimestamp = now + (3 * oneDay),
                isPaid = false,
                category = ExpenseCategory.BILLS,
                billerName = "Reliance Jio"
            ),
            BillReminderEntity(
                title = "Axis Bank Credit Card Bill",
                amount = 8450.00, // ₹8,450.00
                dueDateTimestamp = now + (15 * oneDay),
                isPaid = false,
                category = ExpenseCategory.BILLS,
                billerName = "Axis Bank"
            ),
            BillReminderEntity(
                title = "Netflix Premium 4K",
                amount = 649.00, // ₹649.00
                dueDateTimestamp = now - (2 * oneDay),
                isPaid = true,
                category = ExpenseCategory.ENTERTAINMENT,
                billerName = "Netflix"
            )
        )
    }

    fun getDefaultSplits(): List<SplitExpenseEntity> {
        val now = System.currentTimeMillis()
        val oneDay = 24 * 3600 * 1000L
        return listOf(
            SplitExpenseEntity(
                title = "Goa Trip Villa & BBQ Dinner",
                totalAmount = 14800.00, // ₹14,800.00
                paidBy = "You",
                members = "You, Kamal, Sneha, Amit, Rahul",
                myShare = 2960.00, // ₹2,960.00
                isSettled = false,
                timestamp = now - (2 * oneDay)
            ),
            SplitExpenseEntity(
                title = "Flat High-Speed Wifi & Groceries",
                totalAmount = 3600.00, // ₹3,600.00
                paidBy = "Rahul",
                members = "You, Rahul, Sneha",
                myShare = 1200.00, // ₹1,200.00
                isSettled = false,
                timestamp = now - (4 * oneDay)
            ),
            SplitExpenseEntity(
                title = "Friday Team Pizza Party",
                totalAmount = 2400.00, // ₹2,400.00
                paidBy = "You",
                members = "You, Vikas, Priya, Neha",
                myShare = 600.00, // ₹600.00
                isSettled = true,
                timestamp = now - (10 * oneDay)
            )
        )
    }

    val sampleSmsTemplates = listOf(
        "Rs. 450.00 debited from A/c XX4092 on 26-AUG-26 by UPI to SWIGGY. Info: UPI/32810928/Swiggy. Bal: Rs. 1,62,800.00",
        "Sent Rs.1,200.00 from SBI A/c 1104 to ZOMATO via UPI ref 89218201. Available Bal Rs.47,300.00",
        "Alert: INR 3,499.00 spent on your ICICI Bank Credit Card XX8812 at AMAZON INDIA on 26-AUG-26.",
        "Rs 280.00 paid to UBER INDIA from Paytm Wallet. Updated Bal: Rs 1,450.00",
        "HDFC Bank: Salary of Rs 1,45,000.00 credited to A/c XX4092 on 01-AUG-26. Total Bal Rs. 1,63,250.00",
        "INR 850.00 spent on Axis Bank Card XX9021 at BLINKIT GROCERY on 26-AUG-26. Avail Limit Rs 4,91,550.00",
        "Paid Rs. 1,178.00 from HDFC A/c XX4092 to JIO FIBER BROADBAND via UPI."
    )
}
