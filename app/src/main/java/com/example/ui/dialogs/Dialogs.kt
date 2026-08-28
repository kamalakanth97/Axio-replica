package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.DefaultSeedData
import com.example.data.model.AccountType
import com.example.data.model.ExpenseCategory
import com.example.data.model.PaymentMode
import com.example.data.model.TransactionType
import com.example.ui.theme.*
import com.example.util.RupeeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        title: String,
        amount: Double,
        type: TransactionType,
        category: ExpenseCategory,
        paymentMode: PaymentMode,
        accountName: String,
        notes: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(TransactionType.EXPENSE) }
    var category by remember { mutableStateOf(ExpenseCategory.FOOD) }
    var paymentMode by remember { mutableStateOf(PaymentMode.UPI) }
    var accountName by remember { mutableStateOf("HDFC Bank - 4092") }
    var notes by remember { mutableStateOf("") }

    val accounts = listOf(
        "HDFC Bank - 4092",
        "SBI Bank - 1104",
        "ICICI Card - 8812",
        "Axis Card - 9021",
        "Cash in Hand & Wallet"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Transaction (in ₹)",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type Selector: Expense vs Income
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = type == TransactionType.EXPENSE,
                        onClick = { type = TransactionType.EXPENSE },
                        label = { Text("Expense (-)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RupeeExpenseRed,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = type == TransactionType.INCOME,
                        onClick = { type = TransactionType.INCOME },
                        label = { Text("Income (+)") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RupeeIncomeGreen,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Amount Field in Rupees
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (in ₹ Rupees)") },
                    placeholder = { Text("e.g. 450") },
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(start = 12.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("txn_amount_input"),
                    singleLine = true
                )

                // Merchant / Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (type == TransactionType.EXPENSE) "Merchant / Spend Title" else "Income Source") },
                    placeholder = { Text("e.g. Swiggy, Uber, Grocery, Salary") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Store, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("txn_title_input"),
                    singleLine = true
                )

                // Category Selector
                Text("Category", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    var expandedCat by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expandedCat,
                        onExpandedChange = { expandedCat = !expandedCat },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = category.displayName,
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
                                        category = cat
                                        expandedCat = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Payment Mode
                Text("Payment Mode", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                var expandedMode by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedMode,
                    onExpandedChange = { expandedMode = !expandedMode },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = paymentMode.displayName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMode) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedMode,
                        onDismissRequest = { expandedMode = false }
                    ) {
                        PaymentMode.values().forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode.displayName) },
                                onClick = {
                                    paymentMode = mode
                                    expandedMode = false
                                }
                            )
                        }
                    }
                }

                // Account
                Text("Bank / Account", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                var expandedAccount by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedAccount,
                    onExpandedChange = { expandedAccount = !expandedAccount },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = accountName,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccount) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAccount,
                        onDismissRequest = { expandedAccount = false }
                    ) {
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc) },
                                onClick = {
                                    accountName = acc
                                    expandedAccount = false
                                }
                            )
                        }
                    }
                }

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("e.g. Dinner with clients") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    if (amt > 0 && title.isNotBlank()) {
                        onConfirm(title.trim(), amt, type, category, paymentMode, accountName, notes.trim())
                    }
                },
                enabled = amountText.toDoubleOrNull() != null && title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AxioTealPrimary),
                modifier = Modifier.testTag("save_txn_button")
            ) {
                Text("Save Transaction")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SmsSimulatorDialog(
    onDismiss: () -> Unit,
    onSimulateSms: (String) -> Unit
) {
    var customSmsText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Sms, contentDescription = null, tint = AxioTealPrimary)
                Text(text = "Smart Bank SMS Parser", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Axio automatically detects bank & UPI transactions from SMS in Indian Rupees (₹). Tap any template below to test real-time parsing:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                // Sample SMS buttons
                DefaultSeedData.sampleSmsTemplates.forEachIndexed { index, sms ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onSimulateSms(sms)
                                onDismiss()
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Run",
                                tint = AxioTealPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = sms,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Or paste your own Bank / UPI SMS:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = customSmsText,
                    onValueChange = { customSmsText = it },
                    placeholder = { Text("e.g. Rs 540.00 debited from HDFC A/c XX4092 on 26-AUG-26 to ZOMATO...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (customSmsText.isNotBlank()) {
                        onSimulateSms(customSmsText.trim())
                        onDismiss()
                    }
                },
                enabled = customSmsText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AxioTealPrimary)
            ) {
                Text("Parse & Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun AddAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (bankName: String, type: AccountType, balance: Double, last4: String, limit: Double, due: String) -> Unit
) {
    var bankName by remember { mutableStateOf("") }
    var accountType by remember { mutableStateOf(AccountType.SAVINGS) }
    var balanceText by remember { mutableStateOf("") }
    var last4 by remember { mutableStateOf("") }
    var creditLimitText by remember { mutableStateOf("") }
    var billDueDate by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Bank / Card Account", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("Bank / Account Name") },
                    placeholder = { Text("e.g. Kotak Mahindra, Amazon Pay ICICI") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = accountType == AccountType.SAVINGS,
                        onClick = { accountType = AccountType.SAVINGS },
                        label = { Text("Savings") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = accountType == AccountType.CREDIT_CARD,
                        onClick = { accountType = AccountType.CREDIT_CARD },
                        label = { Text("Credit Card") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = balanceText,
                    onValueChange = { balanceText = it },
                    label = { Text(if (accountType == AccountType.CREDIT_CARD) "Current Outstanding Due (₹)" else "Initial Balance (₹)") },
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = last4,
                    onValueChange = { if (it.length <= 4) last4 = it },
                    label = { Text("Last 4 digits (optional)") },
                    placeholder = { Text("4092") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (accountType == AccountType.CREDIT_CARD) {
                    OutlinedTextField(
                        value = creditLimitText,
                        onValueChange = { creditLimitText = it },
                        label = { Text("Total Credit Limit (₹)") },
                        placeholder = { Text("e.g. 200000") },
                        leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = billDueDate,
                        onValueChange = { billDueDate = it },
                        label = { Text("Bill Due Date") },
                        placeholder = { Text("e.g. 05 Sep 2026") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bal = balanceText.toDoubleOrNull() ?: 0.0
                    val limit = creditLimitText.toDoubleOrNull() ?: 0.0
                    if (bankName.isNotBlank()) {
                        onConfirm(bankName.trim(), accountType, bal, last4, limit, billDueDate.trim())
                    }
                },
                enabled = bankName.isNotBlank() && balanceText.toDoubleOrNull() != null,
                colors = ButtonDefaults.buttonColors(containerColor = AxioTealPrimary)
            ) {
                Text("Add Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddBillDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, amount: Double, daysFromNow: Int, biller: String, category: ExpenseCategory) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var daysText by remember { mutableStateOf("5") }
    var biller by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Bill / EMI Reminder", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bill Name") },
                    placeholder = { Text("e.g. Electricity, Broadband, Credit Card") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (in ₹ Rupees)") },
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = biller,
                    onValueChange = { biller = it },
                    label = { Text("Biller / Service Provider") },
                    placeholder = { Text("e.g. Tata Power, Airtel, HDFC") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = daysText,
                    onValueChange = { daysText = it },
                    label = { Text("Due in (days from today)") },
                    placeholder = { Text("5") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amountText.toDoubleOrNull() ?: 0.0
                    val days = daysText.toIntOrNull() ?: 5
                    if (title.isNotBlank() && amt > 0) {
                        onConfirm(title.trim(), amt, days, biller.trim(), ExpenseCategory.BILLS)
                    }
                },
                enabled = title.isNotBlank() && amountText.toDoubleOrNull() != null,
                colors = ButtonDefaults.buttonColors(containerColor = AxioTealPrimary)
            ) {
                Text("Add Bill")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun AddSplitDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, totalAmount: Double, paidBy: String, members: String, myShare: Double) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var totalAmountText by remember { mutableStateOf("") }
    var paidBy by remember { mutableStateOf("You") }
    var members by remember { mutableStateOf("You, Rahul, Sneha") }

    val count = members.split(",").map { it.trim() }.filter { it.isNotEmpty() }.size.coerceAtLeast(1)
    val totalAmt = totalAmountText.toDoubleOrNull() ?: 0.0
    val calculatedShare = totalAmt / count

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Split Expense with Group", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Title") },
                    placeholder = { Text("e.g. Dinner at Olive, Weekend Trip") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = totalAmountText,
                    onValueChange = { totalAmountText = it },
                    label = { Text("Total Bill Amount (in ₹)") },
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = paidBy,
                    onValueChange = { paidBy = it },
                    label = { Text("Who Paid?") },
                    placeholder = { Text("You or Friend's Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = members,
                    onValueChange = { members = it },
                    label = { Text("Split Between (comma separated)") },
                    placeholder = { Text("You, Rahul, Sneha, Amit") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = AxioTealContainer.copy(alpha = 0.6f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Split summary: $count members",
                            style = MaterialTheme.typography.labelSmall,
                            color = AxioTealDark
                        )
                        Text(
                            text = "Your Share: ${RupeeFormatter.formatRupees(calculatedShare)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = AxioTealDark
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && totalAmt > 0) {
                        onConfirm(title.trim(), totalAmt, paidBy.trim(), members.trim(), calculatedShare)
                    }
                },
                enabled = title.isNotBlank() && totalAmt > 0,
                colors = ButtonDefaults.buttonColors(containerColor = AxioTealPrimary)
            ) {
                Text("Save Split")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetBudgetDialog(
    onDismiss: () -> Unit,
    onConfirm: (category: ExpenseCategory, limit: Double) -> Unit
) {
    var category by remember { mutableStateOf(ExpenseCategory.FOOD) }
    var limitText by remember { mutableStateOf("") }
    var expandedCat by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Category Budget (in ₹)", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Select Category", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))

                ExposedDropdownMenuBox(
                    expanded = expandedCat,
                    onExpandedChange = { expandedCat = !expandedCat },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = category.displayName,
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
                                    category = cat
                                    expandedCat = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = limitText,
                    onValueChange = { limitText = it },
                    label = { Text("Monthly Limit (in ₹ Rupees)") },
                    placeholder = { Text("e.g. 15000") },
                    leadingIcon = { Text("₹", fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 12.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val limit = limitText.toDoubleOrNull() ?: 0.0
                    if (limit > 0) {
                        onConfirm(category, limit)
                    }
                },
                enabled = limitText.toDoubleOrNull() != null && (limitText.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = AxioTealPrimary)
            ) {
                Text("Save Budget")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
