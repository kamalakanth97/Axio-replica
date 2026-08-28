package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.ExpenseCategory
import com.example.data.model.TransactionType
import com.example.ui.AxioUiState
import com.example.ui.AxioViewModel
import com.example.ui.theme.*
import com.example.util.RupeeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsScreen(
    uiState: AxioUiState,
    viewModel: AxioViewModel
) {
    var selectedTxnForDetail by remember { mutableStateOf<TransactionEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("transactions_screen")
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Transactions & SMS Log",
                    fontWeight = FontWeight.Bold
                )
            },
            actions = {
                IconButton(
                    onClick = { viewModel.openQrScanner(true) },
                    modifier = Modifier.testTag("scan_qr_txn_icon_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "Scan UPI QR",
                        tint = AxioTealPrimary
                    )
                }
                IconButton(
                    onClick = { viewModel.openExportDialog(true) },
                    modifier = Modifier.testTag("export_txn_icon_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Export Transactions CSV",
                        tint = AxioTealPrimary
                    )
                }
                IconButton(
                    onClick = { viewModel.openSmsSimulator(true) },
                    modifier = Modifier.testTag("sms_sync_icon_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sms,
                        contentDescription = "Test SMS Parser",
                        tint = AxioTealPrimary
                    )
                }
                IconButton(
                    onClick = { viewModel.openAddTransaction(true) },
                    modifier = Modifier.testTag("add_txn_icon_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Add Transaction",
                        tint = AxioTealPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        // Search Bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("search_transactions_input"),
            placeholder = { Text("Search by merchant, note, or account...") },
            leadingIcon = {
                Icon(imageVector = Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AxioTealPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        // Category Filter Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = uiState.selectedCategoryFilter == null,
                    onClick = { viewModel.setCategoryFilter(null) },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AxioTealPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
            items(ExpenseCategory.values()) { category ->
                FilterChip(
                    selected = uiState.selectedCategoryFilter == category,
                    onClick = {
                        viewModel.setCategoryFilter(
                            if (uiState.selectedCategoryFilter == category) null else category
                        )
                    },
                    label = { Text(category.displayName) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AxioTealPrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Summary Bar for filtered list
        val filteredExpenseTotal = uiState.transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        val filteredIncomeTotal = uiState.transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${uiState.transactions.size} records",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Total Spend: ${RupeeFormatter.formatRupees(filteredExpenseTotal)}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = RupeeExpenseRed
                    )
                    if (filteredIncomeTotal > 0) {
                        Text(
                            text = "Income: ${RupeeFormatter.formatRupees(filteredIncomeTotal)}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = RupeeIncomeGreen
                        )
                    }
                }
            }
        }

        // Group transactions by date
        val groupedTransactions = remember(uiState.transactions) {
            uiState.transactions.groupBy { RupeeFormatter.formatDate(it.timestamp, "EEEE, dd MMMM yyyy") }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            if (uiState.transactions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No transactions found",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Add a transaction or simulate an SMS message.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            } else {
                groupedTransactions.forEach { (dateHeader, txns) ->
                    val daySpend = txns.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = dateHeader,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            if (daySpend > 0) {
                                Text(
                                    text = "₹${RupeeFormatter.formatRupees(daySpend, false)}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = RupeeExpenseRed
                                )
                            }
                        }
                    }

                    items(txns, key = { it.id }) { txn ->
                        Box(modifier = Modifier.clickable { selectedTxnForDetail = txn }) {
                            TransactionListItem(
                                transaction = txn,
                                onDelete = { viewModel.deleteTransaction(txn) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Detail Dialog
    selectedTxnForDetail?.let { txn ->
        TransactionDetailDialog(
            transaction = txn,
            onDismiss = { selectedTxnForDetail = null },
            onDelete = {
                viewModel.deleteTransaction(txn)
                selectedTxnForDetail = null
            }
        )
    }
}

@Composable
fun TransactionDetailDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(transaction.category.color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = transaction.category.getIcon(),
                        contentDescription = null,
                        tint = transaction.category.color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(text = transaction.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow("Amount", RupeeFormatter.formatRupees(transaction.amount, true), isHighlight = true)
                DetailRow("Type", transaction.type.name)
                DetailRow("Category", transaction.category.displayName)
                DetailRow("Account", transaction.accountName)
                DetailRow("Payment Mode", transaction.paymentMode.displayName)
                DetailRow("Date & Time", "${RupeeFormatter.formatDate(transaction.timestamp)} at ${RupeeFormatter.formatTime(transaction.timestamp)}")
                if (transaction.notes.isNotEmpty()) {
                    DetailRow("Notes", transaction.notes)
                }
                if (transaction.isSmsParsed && !transaction.rawSms.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Original Bank SMS:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = AxioTealPrimary
                    )
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = transaction.rawSms,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(8.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDelete,
                colors = ButtonDefaults.textButtonColors(contentColor = RupeeExpenseRed)
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Delete")
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        Text(
            text = value,
            style = if (isHighlight) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = AxioTealPrimary)
            else MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
