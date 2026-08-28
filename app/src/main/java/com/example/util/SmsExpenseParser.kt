package com.example.util

import com.example.data.local.entity.TransactionEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.PaymentMode
import com.example.data.model.TransactionType
import java.util.regex.Pattern

data class ParsedSmsResult(
    val amount: Double,
    val type: TransactionType,
    val merchantOrSender: String,
    val category: ExpenseCategory,
    val paymentMode: PaymentMode,
    val accountName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val rawSms: String
)

object SmsExpenseParser {

    /**
     * Parses an Indian bank or UPI transactional SMS into structured transaction data.
     */
    fun parseSms(smsBody: String, sender: String = ""): ParsedSmsResult? {
        val cleanText = smsBody.replace("\n", " ").trim()
        val lower = cleanText.lowercase()

        // 1. Determine Type: EXPENSE or INCOME
        val isCredit = lower.contains("credited") || lower.contains("received") || lower.contains("deposited") || lower.contains("refund")
        val isDebit = lower.contains("debited") || lower.contains("spent") || lower.contains("paid") || lower.contains("sent") || lower.contains("withdrawn") || lower.contains("purchase") || lower.contains("used")

        if (!isCredit && !isDebit) {
            // Not a financial transaction SMS
            return null
        }

        val type = if (isCredit) TransactionType.INCOME else TransactionType.EXPENSE

        // 2. Extract Amount in INR (₹ / Rs / INR)
        val amount = extractAmount(cleanText) ?: return null

        // 3. Extract Account or Bank
        val accountName = extractAccountName(cleanText, sender)

        // 4. Extract Payment Mode
        val paymentMode = when {
            lower.contains("upi") || lower.contains("vpa") || lower.contains("gpay") || lower.contains("phonepe") || lower.contains("paytm upi") -> PaymentMode.UPI
            lower.contains("credit card") || lower.contains("creditcard") || lower.contains("cc") -> PaymentMode.CREDIT_CARD
            lower.contains("debit card") || lower.contains("debitcard") || lower.contains("atm") -> PaymentMode.DEBIT_CARD
            lower.contains("wallet") -> PaymentMode.UPI
            lower.contains("netbanking") || lower.contains("neft") || lower.contains("imps") || lower.contains("rtgs") -> PaymentMode.NET_BANKING
            else -> PaymentMode.UPI
        }

        // 5. Extract Merchant & Category
        val (merchant, category) = detectMerchantAndCategory(cleanText, type)

        return ParsedSmsResult(
            amount = amount,
            type = type,
            merchantOrSender = merchant,
            category = category,
            paymentMode = paymentMode,
            accountName = accountName,
            timestamp = System.currentTimeMillis(),
            rawSms = smsBody
        )
    }

    private fun extractAmount(text: String): Double? {
        // Regex patterns matching: Rs. 450.00, Rs 1,200.50, INR 3499.00, ₹500, Rs.1450
        val patterns = listOf(
            Pattern.compile("""(?:rs\.?|inr|₹)\s*([\d,]+(?:\.\d{1,2})?)""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""(?:amount|sum of|for)\s*(?:rs\.?|inr|₹)?\s*([\d,]+(?:\.\d{1,2})?)""", Pattern.CASE_INSENSITIVE),
            Pattern.compile("""([\d,]+(?:\.\d{1,2})?)\s*(?:debited|credited|spent)""", Pattern.CASE_INSENSITIVE)
        )

        for (pattern in patterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                val match = matcher.group(1)?.replace(",", "")
                val amt = match?.toDoubleOrNull()
                if (amt != null && amt > 0) {
                    return amt
                }
            }
        }
        return null
    }

    private fun extractAccountName(text: String, sender: String): String {
        val lower = text.lowercase()
        val sLower = sender.lowercase()

        val bankPrefix = when {
            lower.contains("hdfc") || sLower.contains("hdfc") -> "HDFC Bank"
            lower.contains("sbi") || sLower.contains("sbi") -> "SBI Bank"
            lower.contains("icici") || sLower.contains("icici") -> "ICICI Bank"
            lower.contains("axis") || sLower.contains("axis") -> "Axis Bank"
            lower.contains("kotak") || sLower.contains("kotak") -> "Kotak Bank"
            lower.contains("paytm") || sLower.contains("paytm") -> "Paytm Wallet"
            lower.contains("cred") || sLower.contains("cred") -> "CRED Pay"
            else -> "Primary Bank A/c"
        }

        // Check for last 4 digits (e.g. A/c XX4092, Card XX8812)
        val acctMatch = Pattern.compile("""(?:a/c|acct|card|ending)\s*(?:no\.?)?\s*([xX\d]*\d{4})""", Pattern.CASE_INSENSITIVE).matcher(text)
        val last4 = if (acctMatch.find()) {
            val full = acctMatch.group(1) ?: ""
            if (full.length >= 4) full.takeLast(4) else ""
        } else ""

        val isCard = lower.contains("credit card") || lower.contains("card")
        return when {
            last4.isNotEmpty() && isCard -> "$bankPrefix Card - $last4"
            last4.isNotEmpty() -> "$bankPrefix - $last4"
            else -> bankPrefix
        }
    }

    private fun detectMerchantAndCategory(text: String, type: TransactionType): Pair<String, ExpenseCategory> {
        val lower = text.lowercase()

        if (type == TransactionType.INCOME) {
            return when {
                lower.contains("salary") -> "TechCorp Salary" to ExpenseCategory.SALARY
                lower.contains("dividend") || lower.contains("interest") || lower.contains("zerodha") || lower.contains("groww") -> "Investment Return" to ExpenseCategory.INVESTMENT
                lower.contains("refund") -> "Merchant Refund" to ExpenseCategory.OTHER
                else -> "UPI Inward Credit" to ExpenseCategory.SALARY
            }
        }

        // Expense Categorization
        return when {
            lower.contains("swiggy") -> "Swiggy" to ExpenseCategory.FOOD
            lower.contains("zomato") -> "Zomato" to ExpenseCategory.FOOD
            lower.contains("mcdonald") || lower.contains("starbucks") || lower.contains("domino") || lower.contains("kfc") || lower.contains("cafe") || lower.contains("restaurant") -> "Dining & Cafe" to ExpenseCategory.FOOD
            lower.contains("blinkit") || lower.contains("zepto") || lower.contains("instamart") || lower.contains("bigbasket") || lower.contains("dmart") || lower.contains("grocery") -> "Groceries" to ExpenseCategory.GROCERIES
            lower.contains("amazon") -> "Amazon India" to ExpenseCategory.SHOPPING
            lower.contains("flipkart") -> "Flipkart" to ExpenseCategory.SHOPPING
            lower.contains("myntra") || lower.contains("ajio") || lower.contains("zara") || lower.contains("nykaa") -> "Fashion Shopping" to ExpenseCategory.SHOPPING
            lower.contains("uber") -> "Uber India" to ExpenseCategory.TRAVEL
            lower.contains("ola") || lower.contains("rapido") || lower.contains("metro") || lower.contains("irctc") || lower.contains("makemytrip") || lower.contains("petrol") || lower.contains("fuel") -> "Travel & Fuel" to ExpenseCategory.TRAVEL
            lower.contains("electricity") || lower.contains("bescom") || lower.contains("tata power") || lower.contains("jio") || lower.contains("airtel") || lower.contains("broadband") || lower.contains("water") || lower.contains("gas") -> "Utility Bill" to ExpenseCategory.BILLS
            lower.contains("netflix") || lower.contains("prime video") || lower.contains("hotstar") || lower.contains("spotify") || lower.contains("bookmyshow") || lower.contains("cinema") || lower.contains("pvr") -> "Entertainment" to ExpenseCategory.ENTERTAINMENT
            lower.contains("pharmacy") || lower.contains("apollo") || lower.contains("1mg") || lower.contains("hospital") || lower.contains("clinic") || lower.contains("doctor") -> "Medical & Health" to ExpenseCategory.HEALTH
            lower.contains("zerodha") || lower.contains("groww") || lower.contains("sip") || lower.contains("mutual fund") || lower.contains("indmoney") || lower.contains("kuvera") -> "Mutual Fund / Stock SIP" to ExpenseCategory.INVESTMENT
            lower.contains("udemy") || lower.contains("coursera") || lower.contains("college") || lower.contains("tuition") || lower.contains("school") -> "Education" to ExpenseCategory.EDUCATION
            else -> {
                // Try extracting merchant after "to", "at", "info:"
                val toPattern = Pattern.compile("""(?:to|at|vpa|info:?\s*upi\/[^\/]+\/)\s*([A-Za-z0-9\s&]{3,20})""", Pattern.CASE_INSENSITIVE)
                val matcher = toPattern.matcher(text)
                val detected = if (matcher.find()) matcher.group(1)?.trim()?.take(25) else "UPI Payment"
                (detected ?: "General Expense") to ExpenseCategory.OTHER
            }
        }
    }
}
