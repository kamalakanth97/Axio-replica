package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseCategory
import com.example.data.model.PaymentMode
import com.example.data.model.TransactionType
import com.example.ui.theme.AxioTealContainer
import com.example.ui.theme.AxioTealDark
import com.example.ui.theme.AxioTealPrimary
import com.example.util.RupeeFormatter
import com.example.util.UpiApp
import com.example.util.UpiIntentHelper
import com.example.util.UpiPaymentDetails

data class UpiQuickPreset(
    val title: String,
    val vpa: String,
    val defaultCategory: ExpenseCategory
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpiPaymentDialog(
    initialVpa: String = "",
    initialName: String = "",
    initialAmount: Double = 0.0,
    initialNote: String = "",
    initialCategory: ExpenseCategory = ExpenseCategory.FOOD,
    onDismiss: () -> Unit,
    onPaymentInitiated: (
        title: String,
        amount: Double,
        type: TransactionType,
        category: ExpenseCategory,
        paymentMode: PaymentMode,
        accountName: String,
        notes: String
    ) -> Unit,
    onOpenPostPaymentReview: ((UpiPostPaymentData) -> Unit)? = null
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var vpa by remember { mutableStateOf(initialVpa) }
    var payeeName by remember { mutableStateOf(initialName) }
    var amountText by remember { mutableStateOf(if (initialAmount > 0) String.format("%.2f", initialAmount) else "") }
    var note by remember { mutableStateOf(initialNote) }
    var selectedCategory by remember { mutableStateOf(initialCategory) }
    var selectedUpiApp by remember { mutableStateOf(UpiApp.ALL) }
    var autoRecord by remember { mutableStateOf(true) }
    var expandedCat by remember { mutableStateOf(false) }

    val quickPresets = listOf(
        UpiQuickPreset("Swiggy", "swiggy@icici", ExpenseCategory.FOOD),
        UpiQuickPreset("Zomato", "zomato@axis", ExpenseCategory.FOOD),
        UpiQuickPreset("Blinkit", "blinkit@okhdfcbank", ExpenseCategory.GROCERIES),
        UpiQuickPreset("Tata Power", "tatapower@icici", ExpenseCategory.BILLS),
        UpiQuickPreset("Uber India", "uber.pay@icici", ExpenseCategory.TRAVEL),
        UpiQuickPreset("Friend / Split", "friend@upi", ExpenseCategory.OTHER)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(AxioTealPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("UPI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                Text("Pay via UPI Intent", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Initiate instant UPI payment via installed Indian UPI apps (Google Pay, PhonePe, Paytm, BHIM, CRED, Amazon Pay).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                )

                // Quick Merchant Presets
                Text(
                    text = "Quick Presets:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    quickPresets.take(3).forEach { preset ->
                        AssistChip(
                            onClick = {
                                vpa = preset.vpa
                                if (payeeName.isBlank()) payeeName = preset.title
                                selectedCategory = preset.defaultCategory
                            },
                            label = { Text(preset.title, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Payee UPI ID / VPA
                OutlinedTextField(
                    value = vpa,
                    onValueChange = { vpa = it },
                    label = { Text("Payee UPI ID (VPA) *") },
                    placeholder = { Text("e.g. 9876543210@paytm, user@okhdfcbank") },
                    leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upi_vpa_input"),
                    singleLine = true
                )

                // Payee Name
                OutlinedTextField(
                    value = payeeName,
                    onValueChange = { payeeName = it },
                    label = { Text("Payee / Merchant Name") },
                    placeholder = { Text("e.g. Swiggy, Rahul Sharma, Store") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Amount in Rupees
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (in ₹ Rupees) *") },
                    placeholder = { Text("e.g. 450.00") },
                    leadingIcon = {
                        Text("₹", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(start = 12.dp))
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upi_amount_input"),
                    singleLine = true
                )

                // App to Launch
                Text(
                    text = "Launch With:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val popularApps = listOf(UpiApp.ALL, UpiApp.GPAY, UpiApp.PHONEPE, UpiApp.PAYTM)
                    popularApps.forEach { app ->
                        FilterChip(
                            selected = selectedUpiApp == app,
                            onClick = { selectedUpiApp = app },
                            label = { Text(app.displayName, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Category Selector
                Text("Category", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                ExposedDropdownMenuBox(
                    expanded = expandedCat,
                    onExpandedChange = { expandedCat = !expandedCat },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategory.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCat) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCat,
                        onDismissRequest = { expandedCat = false }
                    ) {
                        ExpenseCategory.values().forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                leadingIcon = {
                                    Icon(imageVector = cat.getIcon(), contentDescription = null, tint = cat.color)
                                },
                                onClick = {
                                    selectedCategory = cat
                                    expandedCat = false
                                }
                            )
                        }
                    }
                }

                // Note
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Payment Note (Optional)") },
                    placeholder = { Text("e.g. Lunch split, Grocery items") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Auto-record switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = autoRecord,
                        onCheckedChange = { autoRecord = it },
                        colors = CheckboxDefaults.colors(checkedColor = AxioTealPrimary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Auto-save in spends as UPI transaction",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            val amountVal = amountText.toDoubleOrNull() ?: 0.0
            val isValid = vpa.isNotBlank() && amountVal > 0

            Button(
                onClick = {
                    if (isValid) {
                        val paymentDetails = UpiPaymentDetails(
                            vpa = vpa.trim(),
                            name = if (payeeName.isNotBlank()) payeeName.trim() else vpa.trim(),
                            amount = amountVal,
                            note = if (note.isNotBlank()) note.trim() else "axio UPI Payment"
                        )

                        val launched = UpiIntentHelper.launchUpiPayment(
                            context = context,
                            details = paymentDetails,
                            targetApp = selectedUpiApp
                        )

                        val merchantTitle = if (payeeName.isNotBlank()) payeeName.trim() else vpa.trim()

                        if (onOpenPostPaymentReview != null) {
                            onOpenPostPaymentReview(
                                UpiPostPaymentData(
                                    merchantName = merchantTitle,
                                    vpa = vpa.trim(),
                                    amount = amountVal,
                                    initialNote = note.trim(),
                                    suggestedCategory = selectedCategory,
                                    appUsed = selectedUpiApp.displayName
                                )
                            )
                        } else {
                            onPaymentInitiated(
                                merchantTitle,
                                amountVal,
                                TransactionType.EXPENSE,
                                selectedCategory,
                                PaymentMode.UPI,
                                "HDFC Bank - 4092 (UPI)",
                                if (note.isNotBlank()) note.trim() else "Paid via ${selectedUpiApp.displayName} to $vpa"
                            )
                        }

                        if (launched) {
                            Toast.makeText(context, "Opening UPI payment for ₹$amountVal...", Toast.LENGTH_SHORT).show()
                        }
                        keyboardController?.hide()
                        focusManager.clearFocus()
                        onDismiss()
                    }
                },
                enabled = isValid,
                colors = ButtonDefaults.buttonColors(containerColor = AxioTealPrimary),
                modifier = Modifier.testTag("launch_upi_button")
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pay with UPI")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    onDismiss()
                }
            ) {
                Text("Cancel")
            }
        }
    )
}
