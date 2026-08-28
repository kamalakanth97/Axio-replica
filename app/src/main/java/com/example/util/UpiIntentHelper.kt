package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID

enum class UpiApp(val displayName: String, val packageName: String) {
    ALL("Any UPI App", ""),
    GPAY("Google Pay", "com.google.android.apps.nbu.paisa.user"),
    PHONEPE("PhonePe", "com.phonepe.app"),
    PAYTM("Paytm", "net.one97.paytm"),
    BHIM("BHIM UPI", "in.org.npci.upiapp"),
    CRED("CRED", "com.dreamplug.androidapp"),
    AMAZON_PAY("Amazon Pay", "in.amazon.mShop.android.shopping")
}

data class UpiPaymentDetails(
    val vpa: String, // Payee UPI ID (e.g. merchant@icici, friend@okhdfcbank)
    val name: String, // Payee Name
    val amount: Double, // in INR ₹
    val note: String = "Payment via axio", // Note
    val transactionRef: String = "TXN" + UUID.randomUUID().toString().take(8).uppercase()
)

object UpiIntentHelper {

    /**
     * Parses any scanned UPI QR string or link (e.g. upi://pay?pa=...&pn=...&am=...&cu=INR)
     */
    fun parseUpiUri(rawQrString: String): UpiPaymentDetails? {
        val trimmed = rawQrString.trim()
        if (trimmed.isEmpty()) return null

        try {
            val uri = Uri.parse(trimmed)
            if (uri.scheme?.lowercase() == "upi") {
                val pa = uri.getQueryParameter("pa") ?: ""
                val pn = uri.getQueryParameter("pn") ?: ""
                val amStr = uri.getQueryParameter("am") ?: ""
                val tn = uri.getQueryParameter("tn") ?: ""
                val tr = uri.getQueryParameter("tr") ?: ("TXN" + UUID.randomUUID().toString().take(8).uppercase())
                val amount = amStr.toDoubleOrNull() ?: 0.0

                if (pa.isNotEmpty()) {
                    return UpiPaymentDetails(
                        vpa = pa,
                        name = pn.ifBlank { pa.substringBefore("@") },
                        amount = amount,
                        note = tn.ifBlank { "UPI Payment" },
                        transactionRef = tr
                    )
                }
            } else if (trimmed.contains("@")) {
                // Raw VPA string pasted or scanned (e.g. "store@okaxis")
                return UpiPaymentDetails(
                    vpa = trimmed,
                    name = trimmed.substringBefore("@").replace(".", " ").capitalizeWords(),
                    amount = 0.0,
                    note = "Payment to $trimmed"
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * Guesses the most likely Indian expense category from merchant name, VPA or note
     */
    fun guessCategory(merchantName: String, vpa: String = "", note: String = ""): com.example.data.model.ExpenseCategory {
        val combined = "$merchantName $vpa $note".lowercase()

        return when {
            combined.contains("swiggy") || combined.contains("zomato") ||
                    combined.contains("restaurant") || combined.contains("cafe") ||
                    combined.contains("eats") || combined.contains("dine") ||
                    combined.contains("mcdonald") || combined.contains("domino") ||
                    combined.contains("kfc") || combined.contains("chai") ||
                    combined.contains("bakery") || combined.contains("burger") ||
                    combined.contains("food") -> com.example.data.model.ExpenseCategory.FOOD

            combined.contains("blinkit") || combined.contains("zepto") ||
                    combined.contains("instamart") || combined.contains("dmart") ||
                    combined.contains("supermarket") || combined.contains("grocery") ||
                    combined.contains("bigbasket") || combined.contains("nature") -> com.example.data.model.ExpenseCategory.GROCERIES

            combined.contains("uber") || combined.contains("ola") ||
                    combined.contains("rapido") || combined.contains("petrol") ||
                    combined.contains("fuel") || combined.contains("hpcl") ||
                    combined.contains("bpcl") || combined.contains("ioc") ||
                    combined.contains("metro") || combined.contains("irctc") ||
                    combined.contains("flight") || combined.contains("indigo") -> com.example.data.model.ExpenseCategory.TRAVEL

            combined.contains("tata power") || combined.contains("bescom") ||
                    combined.contains("airtel") || combined.contains("jio") ||
                    combined.contains("vi ") || combined.contains("electricity") ||
                    combined.contains("bill") || combined.contains("gas") ||
                    combined.contains("water") || combined.contains("broadband") -> com.example.data.model.ExpenseCategory.BILLS

            combined.contains("amazon") || combined.contains("flipkart") ||
                    combined.contains("myntra") || combined.contains("nykaa") ||
                    combined.contains("ajio") || combined.contains("zara") ||
                    combined.contains("h&m") || combined.contains("shopping") -> com.example.data.model.ExpenseCategory.SHOPPING

            combined.contains("pvr") || combined.contains("inox") ||
                    combined.contains("bookmyshow") || combined.contains("netflix") ||
                    combined.contains("prime") || combined.contains("hotstar") ||
                    combined.contains("spotify") || combined.contains("movie") -> com.example.data.model.ExpenseCategory.ENTERTAINMENT

            combined.contains("apollo") || combined.contains("pharmacy") ||
                    combined.contains("medplus") || combined.contains("1mg") ||
                    combined.contains("hospital") || combined.contains("clinic") ||
                    combined.contains("doctor") -> com.example.data.model.ExpenseCategory.HEALTH

            combined.contains("zerodha") || combined.contains("groww") ||
                    combined.contains("kuvera") || combined.contains("upstox") ||
                    combined.contains("mutual fund") || combined.contains("sip") -> com.example.data.model.ExpenseCategory.INVESTMENT

            combined.contains("salary") || combined.contains("stipend") ||
                    combined.contains("payroll") || combined.contains("bonus") -> com.example.data.model.ExpenseCategory.SALARY

            combined.contains("school") || combined.contains("college") ||
                    combined.contains("tuition") || combined.contains("udemy") ||
                    combined.contains("coursera") || combined.contains("course") -> com.example.data.model.ExpenseCategory.EDUCATION

            else -> com.example.data.model.ExpenseCategory.FOOD
        }
    }

    private fun String.capitalizeWords(): String {
        return split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
    }

    /**
     * Builds the standard NPCI compliant UPI Intent URI
     * Example: upi://pay?pa=payee@bank&pn=PayeeName&am=450.00&cu=INR&tn=Note&tr=REF123
     */
    fun buildUpiUri(details: UpiPaymentDetails): Uri {
        val encodedName = URLEncoder.encode(details.name.ifBlank { "Merchant" }, StandardCharsets.UTF_8.name())
        val encodedNote = URLEncoder.encode(details.note.ifBlank { "Payment" }, StandardCharsets.UTF_8.name())
        val formattedAmount = String.format(java.util.Locale.US, "%.2f", details.amount)

        val uriString = "upi://pay?pa=${details.vpa.trim()}" +
                "&pn=$encodedName" +
                "&am=$formattedAmount" +
                "&cu=INR" +
                "&tn=$encodedNote" +
                "&tr=${details.transactionRef}"

        return Uri.parse(uriString)
    }

    /**
     * Launches the UPI payment intent.
     * If a specific app is selected, tries to launch that package directly.
     * Otherwise displays the system UPI chooser.
     * If no UPI apps are installed, copies the payment link to clipboard and notifies user.
     */
    fun launchUpiPayment(
        context: Context,
        details: UpiPaymentDetails,
        targetApp: UpiApp = UpiApp.ALL
    ): Boolean {
        val uri = buildUpiUri(details)
        val intent = Intent(Intent.ACTION_VIEW, uri)

        if (targetApp != UpiApp.ALL && targetApp.packageName.isNotEmpty()) {
            intent.setPackage(targetApp.packageName)
        }

        return try {
            if (targetApp == UpiApp.ALL) {
                val chooser = Intent.createChooser(intent, "Pay ₹${details.amount} with UPI App")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } else {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            true
        } catch (e: Exception) {
            // If specified app failed, fallback to generic chooser
            try {
                val fallbackIntent = Intent(Intent.ACTION_VIEW, uri)
                val chooser = Intent.createChooser(fallbackIntent, "Pay ₹${details.amount} with UPI App")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
                true
            } catch (fallbackEx: Exception) {
                // If no UPI app installed on device / emulator
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("UPI URI", uri.toString())
                clipboard.setPrimaryClip(clip)
                Toast.makeText(
                    context,
                    "No UPI app found. UPI Payment URI copied to clipboard!",
                    Toast.LENGTH_LONG
                ).show()
                false
            }
        }
    }

    /**
     * Returns a list of supported UPI apps installed on this device
     */
    fun getInstalledUpiApps(context: Context): List<UpiApp> {
        val pm = context.packageManager
        val installedList = mutableListOf<UpiApp>()
        installedList.add(UpiApp.ALL)

        for (app in UpiApp.values()) {
            if (app == UpiApp.ALL || app.packageName.isEmpty()) continue
            try {
                pm.getPackageInfo(app.packageName, PackageManager.GET_ACTIVITIES)
                installedList.add(app)
            } catch (_: PackageManager.NameNotFoundException) {
                // Not installed
            }
        }
        return installedList
    }
}
