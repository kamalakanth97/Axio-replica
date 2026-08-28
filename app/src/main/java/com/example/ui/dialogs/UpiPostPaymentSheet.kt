package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseCategory
import com.example.data.model.PaymentMode
import com.example.data.model.TransactionType
import com.example.ui.theme.AxioTealContainer
import com.example.ui.theme.AxioTealDark
import com.example.ui.theme.AxioTealPrimary
import com.example.ui.theme.RupeeIncomeGreen
import com.example.util.RupeeFormatter
import com.example.util.UpiIntentHelper

data class UpiPostPaymentData(
    val merchantName: String,
    val vpa: String,
    val amount: Double,
    val initialNote: String = "",
    val suggestedCategory: ExpenseCategory = ExpenseCategory.FOOD,
    val appUsed: String = "UPI App"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpiPostPaymentSheet(
    data: UpiPostPaymentData,
    onDismiss: () -> Unit,
    onSaveTransaction: (
        title: String,
        amount: Double,
        type: TransactionType,
        category: ExpenseCategory,
        paymentMode: PaymentMode,
        accountName: String,
        notes: String,
        splitWithFriends: Boolean,
        splitMembers: String
    ) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var selectedCategory by remember {
        mutableStateOf(
            if (data.suggestedCategory != ExpenseCategory.OTHER) data.suggestedCategory
            else UpiIntentHelper.guessCategory(data.merchantName, data.vpa, data.initialNote)
        )
    }

    val defaultTags = listOf("#OfficeLunch", "#Personal", "#Reimbursable", "#Weekend", "#DinnerParty", "#Family", "#TaxDeductible", "#Vacation")
    val selectedTags = remember { mutableStateListOf<String>() }
    var customTagInput by remember { mutableStateOf("") }
    var showCustomTagField by remember { mutableStateOf(false) }

    var notesText by remember { mutableStateOf(data.initialNote) }
    var selectedAccount by remember { mutableStateOf("HDFC Bank - 4092 (UPI)") }

    var isSplitEnabled by remember { mutableStateOf(false) }
    var splitMembersText by remember { mutableStateOf("Rahul, Sneha, Kamal") }

    val accountsList = listOf(
        "HDFC Bank - 4092 (UPI)",
        "ICICI Bank - 8821 (UPI)",
        "SBI Savings - 1104 (UPI)",
        "Paytm Wallet",
        "Cash in Hand"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE6FFFA)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = AxioTealPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "Review & Categorize Spend",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Paid to ${data.merchantName.ifBlank { data.vpa }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Highlight Amount Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Amount Debited (₹)",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = RupeeFormatter.formatRupees(data.amount, true),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFE2E8F0)
                        ) {
                            Text(
                                text = data.appUsed,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = Color(0xFF334155)
                            )
                        }
                    }
                }

                // Category Selection
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Select Category:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    // Grid of categories
                    val categories = ExpenseCategory.values()
                    val rows = categories.toList().chunked(3)
                    rows.forEach { rowCats ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowCats.forEach { cat ->
                                val isSelected = selectedCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat.displayName, fontSize = 11.sp) },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = cat.getIcon(),
                                            contentDescription = null,
                                            tint = if (isSelected) Color.White else cat.color,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = AxioTealPrimary,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Fill remaining space if row is not full
                            if (rowCats.size < 3) {
                                repeat(3 - rowCats.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // Multi-select Quick Tags
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Add Tags (Multi-select):",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        TextButton(
                            onClick = { showCustomTagField = !showCustomTagField },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("+ Custom Tag", fontSize = 11.sp)
                        }
                    }

                    if (showCustomTagField) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = customTagInput,
                                onValueChange = { customTagInput = it },
                                placeholder = { Text("e.g. #ProjectX", fontSize = 12.sp) },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {
                                    if (customTagInput.isNotBlank()) {
                                        val tag = if (customTagInput.startsWith("#")) customTagInput.trim() else "#${customTagInput.trim()}"
                                        if (!selectedTags.contains(tag)) selectedTags.add(tag)
                                        customTagInput = ""
                                        showCustomTagField = false
                                    }
                                })
                            )
                            Button(
                                onClick = {
                                    if (customTagInput.isNotBlank()) {
                                        val tag = if (customTagInput.startsWith("#")) customTagInput.trim() else "#${customTagInput.trim()}"
                                        if (!selectedTags.contains(tag)) selectedTags.add(tag)
                                        customTagInput = ""
                                        showCustomTagField = false
                                    }
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp)
                            ) {
                                Text("Add", fontSize = 12.sp)
                            }
                        }
                    }

                    // Tag Chips Flow
                    val allDisplayTags = (defaultTags + selectedTags).distinct()
                    val tagRows = allDisplayTags.chunked(3)
                    tagRows.forEach { rowTags ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            rowTags.forEach { tag ->
                                val isTagSelected = selectedTags.contains(tag)
                                FilterChip(
                                    selected = isTagSelected,
                                    onClick = {
                                        if (isTagSelected) selectedTags.remove(tag) else selectedTags.add(tag)
                                    },
                                    label = { Text(tag, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF0F766E),
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowTags.size < 3) {
                                repeat(3 - rowTags.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                // Notes Field
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes / Item Description") },
                    placeholder = { Text("e.g. Lunch with team, Groceries list") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false,
                    maxLines = 2
                )

                // Debit Account Selector
                var accountExpanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = accountExpanded,
                    onExpandedChange = { accountExpanded = !accountExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedAccount,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Paid From Account") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accountExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = accountExpanded,
                        onDismissRequest = { accountExpanded = false }
                    ) {
                        accountsList.forEach { acct ->
                            DropdownMenuItem(
                                text = { Text(acct) },
                                onClick = {
                                    selectedAccount = acct
                                    accountExpanded = false
                                }
                            )
                        }
                    }
                }

                // Split with friends toggle
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = if (isSplitEnabled) Color(0xFFE6FFFA) else Color(0xFFF8FAFC))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = if (isSplitEnabled) AxioTealPrimary else Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Split this spend with friends?",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                            Switch(
                                checked = isSplitEnabled,
                                onCheckedChange = { isSplitEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = AxioTealPrimary)
                            )
                        }

                        if (isSplitEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = splitMembersText,
                                onValueChange = { splitMembersText = it },
                                label = { Text("Split With (Comma separated)") },
                                placeholder = { Text("Rahul, Sneha, Kamal") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            val memberCount = splitMembersText.split(",").filter { it.isNotBlank() }.size.coerceAtLeast(1)
                            val perPerson = data.amount / memberCount
                            Text(
                                text = "Your share: ${RupeeFormatter.formatRupees(perPerson, true)} across $memberCount people",
                                style = MaterialTheme.typography.labelSmall,
                                color = AxioTealDark,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val tagString = if (selectedTags.isNotEmpty()) selectedTags.joinToString(" ") else ""
                    val combinedNotes = listOf(tagString, notesText.trim())
                        .filter { it.isNotBlank() }
                        .joinToString(" • ")

                    val finalTitle = data.merchantName.ifBlank { data.vpa }

                    keyboardController?.hide()
                    focusManager.clearFocus()
                    onSaveTransaction(
                        finalTitle,
                        data.amount,
                        TransactionType.EXPENSE,
                        selectedCategory,
                        PaymentMode.UPI,
                        selectedAccount,
                        combinedNotes,
                        isSplitEnabled,
                        splitMembersText
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AxioTealPrimary),
                modifier = Modifier.testTag("save_categorized_spend_btn")
            ) {
                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save & Add to Spends")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    // Save with smart defaults anyway
                    val finalTitle = data.merchantName.ifBlank { data.vpa }
                    onSaveTransaction(
                        finalTitle,
                        data.amount,
                        TransactionType.EXPENSE,
                        selectedCategory,
                        PaymentMode.UPI,
                        selectedAccount,
                        notesText.trim(),
                        false,
                        ""
                    )
                    onDismiss()
                }
            ) {
                Text("Skip (Keep Defaults)")
            }
        }
    )
}
