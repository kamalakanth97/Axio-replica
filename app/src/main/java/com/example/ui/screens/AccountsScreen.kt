package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.BillReminderEntity
import com.example.data.model.AccountType
import com.example.ui.AxioUiState
import com.example.ui.AxioViewModel
import com.example.ui.theme.*
import com.example.util.RupeeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    uiState: AxioUiState,
    viewModel: AxioViewModel
) {
    val bankAccounts = uiState.accounts.filter { it.accountType == AccountType.SAVINGS || it.accountType == AccountType.CASH || it.accountType == AccountType.WALLET }
    val creditCards = uiState.accounts.filter { it.accountType == AccountType.CREDIT_CARD }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("accounts_screen")
    ) {
        TopAppBar(
            title = { Text("Accounts & Bill Reminders", fontWeight = FontWeight.Bold) },
            actions = {
                IconButton(onClick = { viewModel.openAddAccount(true) }) {
                    Icon(imageVector = Icons.Default.AddCard, contentDescription = "Add Account", tint = AxioTealPrimary)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Net Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL BANK BALANCE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = RupeeFormatter.formatRupees(uiState.totalBankBalance, true),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = RupeeIncomeGreen
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "CREDIT CARD DUES",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = RupeeFormatter.formatRupees(uiState.totalCreditCardDue, true),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                                color = RupeeWarningAmber
                            )
                        }
                    }
                }
            }

            // 1. Bank Accounts Section
            item {
                SectionHeader(
                    title = "Bank Accounts & Cash (${bankAccounts.size})",
                    actionLabel = "+ Add Bank",
                    onActionClick = { viewModel.openAddAccount(true) }
                )
            }

            items(bankAccounts, key = { it.id }) { account ->
                BankAccountCard(account = account)
            }

            // 2. Credit Cards Section
            item {
                SectionHeader(
                    title = "Credit Cards (${creditCards.size})",
                    actionLabel = "+ Add Card",
                    onActionClick = { viewModel.openAddAccount(true) }
                )
            }

            items(creditCards, key = { it.id }) { card ->
                CreditCardItem(card = card)
            }

            // 3. Bill & EMI Reminders Section
            item {
                SectionHeader(
                    title = "Bill & EMI Reminders (${uiState.bills.size})",
                    actionLabel = "+ Add Bill",
                    onActionClick = { viewModel.openAddBill(true) }
                )
            }

            if (uiState.bills.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No pending bills or EMIs.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                items(uiState.bills, key = { it.id }) { bill ->
                    BillReminderItem(
                        bill = bill,
                        onPayWithUpi = {
                            val vpa = "${bill.billerName.lowercase().replace(" ", "")}@icici"
                            viewModel.openUpiPayment(
                                vpa = vpa,
                                name = bill.billerName,
                                amount = bill.amount,
                                note = "Bill: ${bill.title}",
                                category = bill.category
                            )
                        },
                        onTogglePaid = { viewModel.toggleBillPaid(bill) },
                        onDelete = { viewModel.deleteBill(bill) }
                    )
                }
            }
        }
    }
}

@Composable
fun BankAccountCard(account: AccountEntity) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(account.cardColorHex).copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (account.accountType == AccountType.CASH) Icons.Default.Payments else Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = Color(account.cardColorHex),
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = account.bankName,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (account.accountNumberLast4.isNotEmpty() && account.accountNumberLast4 != "CASH")
                            "A/c ending in •• ${account.accountNumberLast4}" else account.accountType.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = RupeeFormatter.formatRupees(account.balance, true),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Available Bal",
                    style = MaterialTheme.typography.labelSmall,
                    color = RupeeIncomeGreen
                )
            }
        }
    }
}

@Composable
fun CreditCardItem(card: AccountEntity) {
    val usedRatio = if (card.creditLimit > 0) (card.balance / card.creditLimit).toFloat() else 0f
    val availableLimit = (card.creditLimit - card.balance).coerceAtLeast(0.0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(card.cardColorHex).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CreditCard,
                            contentDescription = null,
                            tint = Color(card.cardColorHex),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = card.bankName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Card ending in •• ${card.accountNumberLast4}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = RupeeFormatter.formatRupees(card.balance, true),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = RupeeWarningAmber
                    )
                    Text(
                        text = "Current Due",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { usedRatio.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (usedRatio > 0.6f) RupeeExpenseRed else AxioTealPrimary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Avail Limit: ${RupeeFormatter.formatRupees(availableLimit)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                if (card.billDueDate.isNotEmpty()) {
                    Text(
                        text = "Due Date: ${card.billDueDate}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = RupeeWarningAmber
                    )
                }
            }
        }
    }
}

@Composable
fun BillReminderItem(
    bill: BillReminderEntity,
    onPayWithUpi: () -> Unit,
    onTogglePaid: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (bill.isPaid) MaterialTheme.colorScheme.surface.copy(alpha = 0.7f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = bill.isPaid,
                    onCheckedChange = { onTogglePaid() },
                    colors = CheckboxDefaults.colors(checkedColor = RupeeIncomeGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = bill.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (bill.isPaid) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = "Due: ${RupeeFormatter.formatDate(bill.dueDateTimestamp)} • ${bill.billerName}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = RupeeFormatter.formatRupees(bill.amount, true),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (bill.isPaid) RupeeIncomeGreen else RupeeExpenseRed
                        )
                    )
                    Text(
                        text = if (bill.isPaid) "PAID" else "PENDING",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = if (bill.isPaid) RupeeIncomeGreen else RupeeWarningAmber
                    )
                }

                if (!bill.isPaid) {
                    FilledTonalButton(
                        onClick = onPayWithUpi,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFE6FFFA),
                            contentColor = Color(0xFF0D9488)
                        )
                    ) {
                        Text("Pay UPI", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
