package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.TransactionType
import com.example.ui.theme.AxioTealPrimary
import com.example.ui.theme.RupeeExpenseRed
import com.example.ui.theme.RupeeIncomeGreen
import com.example.util.CsvExportHelper
import com.example.util.ExportDateRange
import com.example.util.RupeeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportReportDialog(
    transactions: List<TransactionEntity>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf(ExportDateRange.ALL_TIME) }
    var selectedCategoryFilter by remember { mutableStateOf<ExpenseCategory?>(null) }
    var expandedCatDropdown by remember { mutableStateOf(false) }

    val filteredTransactions = remember(transactions, selectedFilter, selectedCategoryFilter) {
        transactions.filter { txn ->
            val matchesCategory = selectedCategoryFilter == null || txn.category == selectedCategoryFilter
            val matchesFilter = when (selectedFilter) {
                ExportDateRange.ALL_TIME -> true
                ExportDateRange.THIS_MONTH -> {
                    // Filter within last 30 days
                    val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 3600 * 1000)
                    txn.timestamp >= thirtyDaysAgo
                }
                ExportDateRange.LAST_30_DAYS -> {
                    val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 3600 * 1000)
                    txn.timestamp >= thirtyDaysAgo
                }
                ExportDateRange.EXPENSES_ONLY -> txn.type == TransactionType.EXPENSE
                ExportDateRange.INCOME_ONLY -> txn.type == TransactionType.INCOME
            }
            matchesCategory && matchesFilter
        }
    }

    val totalIncome = filteredTransactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val totalExpense = filteredTransactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val netSavings = totalIncome - totalExpense

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE6FFFA)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        tint = AxioTealPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Export Statement & Reports",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Compatible with Excel, Google Sheets, Numbers",
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Filter presets
                Text(
                    text = "Filter Transactions:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ExportDateRange.values().forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter.displayName, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AxioTealPrimary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Category Filter dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedCatDropdown,
                    onExpandedChange = { expandedCatDropdown = !expandedCatDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedCategoryFilter?.displayName ?: "All Categories",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Filter by Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCatDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCatDropdown,
                        onDismissRequest = { expandedCatDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Categories") },
                            onClick = {
                                selectedCategoryFilter = null
                                expandedCatDropdown = false
                            }
                        )
                        ExpenseCategory.values().forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.displayName) },
                                leadingIcon = {
                                    Icon(imageVector = cat.getIcon(), contentDescription = null, tint = cat.color)
                                },
                                onClick = {
                                    selectedCategoryFilter = cat
                                    expandedCatDropdown = false
                                }
                            )
                        }
                    }
                }

                // Summary Financial Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Records to Export:", style = MaterialTheme.typography.bodySmall)
                            Text("${filteredTransactions.size} entries", fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Expenses:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                RupeeFormatter.formatRupees(totalExpense, true),
                                fontWeight = FontWeight.Bold,
                                color = RupeeExpenseRed
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Incomes:", style = MaterialTheme.typography.bodySmall)
                            Text(
                                RupeeFormatter.formatRupees(totalIncome, true),
                                fontWeight = FontWeight.Bold,
                                color = RupeeIncomeGreen
                            )
                        }

                        HorizontalDivider(color = Color(0xFFCBD5E1), thickness = 0.8.dp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Net Savings:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text(
                                RupeeFormatter.formatRupees(netSavings, true),
                                fontWeight = FontWeight.Bold,
                                color = if (netSavings >= 0) RupeeIncomeGreen else RupeeExpenseRed
                            )
                        }
                    }
                }

                // Preview Sample Box
                Text(
                    text = "CSV Preview:",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0F172A))
                        .padding(10.dp)
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState())
                ) {
                    val previewCsv = remember(filteredTransactions) {
                        CsvExportHelper.generateCsvString(filteredTransactions.take(4), "Sample Preview")
                    }
                    Text(
                        text = previewCsv,
                        color = Color(0xFF38BDF8),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    CsvExportHelper.shareCsvReport(context, filteredTransactions, selectedFilter.displayName)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = AxioTealPrimary),
                modifier = Modifier.testTag("share_csv_button")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Share CSV File")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    CsvExportHelper.copyCsvToClipboard(context, filteredTransactions)
                }
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy CSV")
            }
        }
    )
}
