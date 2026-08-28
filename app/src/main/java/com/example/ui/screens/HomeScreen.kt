package com.example.ui.screens

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.TransactionEntity
import com.example.data.model.AccountType
import com.example.data.model.TransactionType
import com.example.ui.AxioUiState
import com.example.ui.AxioViewModel
import com.example.ui.theme.*
import com.example.util.RupeeFormatter

@Composable
fun HomeScreen(
    uiState: AxioUiState,
    viewModel: AxioViewModel,
    onNavigateToTransactions: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToAccounts: () -> Unit
) {
    val netWorth = (uiState.totalBankBalance + uiState.totalCashInHand) - uiState.totalCreditCardDue

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen_container"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // 1. Top Header Bar
        item {
            HomeTopHeader(
                onOpenSmsSimulator = { viewModel.openSmsSimulator(true) },
                onOpenQrScanner = { viewModel.openQrScanner(true) },
                onOpenExportDialog = { viewModel.openExportDialog(true) }
            )
        }

        // SMS notification banner if present
        if (uiState.smsBannerMessage != null) {
            item {
                SmsNotificationBanner(
                    message = uiState.smsBannerMessage,
                    onDismiss = { viewModel.dismissSmsBanner() }
                )
            }
        }

        // 2. Net Balance & Financial Summary Card
        item {
            NetBalanceCard(
                netWorth = netWorth,
                totalBankBalance = uiState.totalBankBalance,
                totalCash = uiState.totalCashInHand,
                totalCcDue = uiState.totalCreditCardDue,
                currentMonthSpend = uiState.currentMonthSpend,
                currentMonthIncome = uiState.currentMonthIncome
            )
        }

        // 3. Quick Action Buttons
        item {
            QuickActionsRow(
                onScanQr = { viewModel.openQrScanner(true) },
                onPayUpi = { viewModel.openUpiPayment() },
                onAddExpense = { viewModel.openAddTransaction(true) },
                onSimulateSms = { viewModel.openSmsSimulator(true) },
                onExportReports = { viewModel.openExportDialog(true) }
            )
        }

        // UPI Intent Feature Banner Card with QR Scan & Quick Pay
        item {
            UpiQuickPayBanner(
                onOpenUpi = { vpa, name, amt, cat ->
                    viewModel.openUpiPayment(vpa = vpa, name = name, amount = amt, category = cat)
                },
                onScanQr = { viewModel.openQrScanner(true) }
            )
        }

        // 4. Accounts & Cards Carousel
        item {
            SectionHeader(
                title = "Your Accounts & Cards",
                actionLabel = "View All",
                onActionClick = onNavigateToAccounts
            )
            AccountsCarousel(accounts = uiState.accounts)
        }

        // 5. Smart Insight Banner
        item {
            SmartInsightCard(
                monthlySpend = uiState.currentMonthSpend,
                ccDue = uiState.totalCreditCardDue
            )
        }

        // 6. Recent Transactions
        item {
            SectionHeader(
                title = "Recent Spends (SMS & UPI)",
                actionLabel = "See All",
                onActionClick = onNavigateToTransactions
            )
        }

        if (uiState.transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No transactions recorded yet in ₹ Rupees.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            items(uiState.transactions.take(6), key = { it.id }) { txn ->
                TransactionListItem(
                    transaction = txn,
                    onDelete = { viewModel.deleteTransaction(txn) }
                )
            }
        }
    }
}

@Composable
fun HomeTopHeader(
    onOpenSmsSimulator: () -> Unit,
    onOpenQrScanner: () -> Unit,
    onOpenExportDialog: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AxioTealPrimary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "₹",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "KaKi Wallet",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = AxioTealPrimary
                )
                Text(
                    text = "Smart Money & Expense Manager",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconButton(
                onClick = onOpenQrScanner,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE6FFFA))
                    .testTag("scan_qr_header_button")
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Scan UPI QR",
                    tint = AxioTealPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onOpenExportDialog,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF1F5F9))
                    .testTag("export_csv_header_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Export Reports",
                    tint = Color(0xFF334155),
                    modifier = Modifier.size(18.dp)
                )
            }

            FilledTonalButton(
                onClick = onOpenSmsSimulator,
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = AxioTealContainer,
                    contentColor = AxioTealDark
                ),
                modifier = Modifier.testTag("sms_sync_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Sync SMS",
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Sync SMS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SmsNotificationBanner(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7))
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
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Success",
                    tint = RupeeIncomeGreen,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = Color(0xFF14532D)
                )
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color(0xFF14532D),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun NetBalanceCard(
    netWorth: Double,
    totalBankBalance: Double,
    totalCash: Double,
    totalCcDue: Double,
    currentMonthSpend: Double,
    currentMonthIncome: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .testTag("net_balance_card"),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = AxioNavy)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A))
                    )
                )
                .padding(20.dp)
        ) {
            Text(
                text = "TOTAL NET BALANCE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = Color(0xFF94A3B8)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = RupeeFormatter.formatRupees(netWorth, showDecimals = true),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = Color(0xFF334155), thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(14.dp))

            // Income & Expense Breakdown in Rupees
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // August Spend
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(RupeeExpenseRed)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Aug Spend",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = RupeeFormatter.formatRupees(currentMonthSpend),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFFCA5A5)
                    )
                }

                // August Income
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(RupeeIncomeGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Aug Income",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = RupeeFormatter.formatRupees(currentMonthIncome),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF86EFAC)
                    )
                }

                // Credit Card Outstanding Due
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(RupeeWarningAmber)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "CC Dues",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = RupeeFormatter.formatRupees(totalCcDue),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFFDE047)
                    )
                }
            }
        }
    }
}

@Composable
fun QuickActionsRow(
    onScanQr: () -> Unit,
    onPayUpi: () -> Unit,
    onAddExpense: () -> Unit,
    onSimulateSms: () -> Unit,
    onExportReports: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        QuickActionButton(
            title = "Scan QR",
            icon = Icons.Default.QrCodeScanner,
            bgColor = Color(0xFFCCFBF1),
            iconTint = AxioTealDark,
            onClick = onScanQr,
            tag = "scan_qr_quick"
        )
        QuickActionButton(
            title = "UPI Pay",
            icon = Icons.Default.Send,
            bgColor = Color(0xFFE6FFFA),
            iconTint = Color(0xFF0D9488),
            onClick = onPayUpi,
            tag = "pay_upi_quick"
        )
        QuickActionButton(
            title = "+ Expense",
            icon = Icons.Default.Add,
            bgColor = AxioTealContainer,
            iconTint = AxioTealDark,
            onClick = onAddExpense,
            tag = "add_expense_quick"
        )
        QuickActionButton(
            title = "Test SMS",
            icon = Icons.Default.MarkChatRead,
            bgColor = Color(0xFFEDE9FE),
            iconTint = RupeePurple,
            onClick = onSimulateSms,
            tag = "test_sms_quick"
        )
        QuickActionButton(
            title = "Export",
            icon = Icons.Default.Share,
            bgColor = Color(0xFFF1F5F9),
            iconTint = Color(0xFF334155),
            onClick = onExportReports,
            tag = "export_quick"
        )
    }
}

@Composable
fun UpiQuickPayBanner(
    onOpenUpi: (vpa: String, name: String, amount: Double, category: com.example.data.model.ExpenseCategory) -> Unit,
    onScanQr: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0D9488)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "UPI",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "UPI Scan & Pay Hub",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "Scan BharatPe / GPay QR or Pay VPA",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(
                        onClick = onScanQr,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF0D9488),
                            contentColor = Color.White
                        ),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Scan QR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { onOpenUpi("", "", 0.0, com.example.data.model.ExpenseCategory.FOOD) },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text("Pay ID", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFF334155), thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Quick UPI 1-Tap actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = false,
                    onClick = {
                        onOpenUpi("swiggy@icici", "Swiggy", 350.0, com.example.data.model.ExpenseCategory.FOOD)
                    },
                    label = { Text("Swiggy ₹350", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(12.dp)) },
                    colors = FilterChipDefaults.filterChipColors(containerColor = Color(0xFF1E293B), labelColor = Color(0xFFCBD5E1)),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = false,
                    onClick = {
                        onOpenUpi("zomato@axis", "Zomato", 420.0, com.example.data.model.ExpenseCategory.FOOD)
                    },
                    label = { Text("Zomato ₹420", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Fastfood, contentDescription = null, modifier = Modifier.size(12.dp)) },
                    colors = FilterChipDefaults.filterChipColors(containerColor = Color(0xFF1E293B), labelColor = Color(0xFFCBD5E1)),
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = false,
                    onClick = {
                        onOpenUpi("tatapower@icici", "Tata Power", 1450.0, com.example.data.model.ExpenseCategory.BILLS)
                    },
                    label = { Text("Power ₹1.4k", fontSize = 11.sp) },
                    leadingIcon = { Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(12.dp)) },
                    colors = FilterChipDefaults.filterChipColors(containerColor = Color(0xFF1E293B), labelColor = Color(0xFFCBD5E1)),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    bgColor: Color,
    iconTint: Color,
    onClick: () -> Unit,
    tag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(4.dp)
            .testTag(tag)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )
        if (actionLabel != null && onActionClick != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = AxioTealPrimary
                ),
                modifier = Modifier.clickable(onClick = onActionClick)
            )
        }
    }
}

@Composable
fun AccountsCarousel(accounts: List<AccountEntity>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(accounts, key = { it.id }) { account ->
            AccountCardItem(account = account)
        }
    }
}

@Composable
fun AccountCardItem(account: AccountEntity) {
    val isCreditCard = account.accountType == AccountType.CREDIT_CARD
    val cardBg = Color(account.cardColorHex)

    Card(
        modifier = Modifier
            .width(220.dp)
            .height(130.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = account.bankName,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (account.accountNumberLast4.isNotEmpty() && account.accountNumberLast4 != "CASH") {
                    Text(
                        text = "•• ${account.accountNumberLast4}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Column {
                Text(
                    text = if (isCreditCard) "OUTSTANDING DUE" else "AVAILABLE BALANCE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = RupeeFormatter.formatRupees(account.balance, showDecimals = true),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = account.accountType.displayName,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color.White.copy(alpha = 0.75f)
                )
                if (isCreditCard && account.billDueDate.isNotEmpty()) {
                    Text(
                        text = "Due: ${account.billDueDate}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFFFDE047)
                    )
                }
            }
        }
    }
}

@Composable
fun SmartInsightCard(monthlySpend: Double, ccDue: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AxioTealContainer.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(AxioTealPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Smart Insight",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Axio Smart Spend Insight",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = AxioTealDark
                )
                Text(
                    text = "Your Dining & Groceries spend this week is within your ₹12,000 monthly budget limit. Great job managing expenses!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun TransactionListItem(
    transaction: TransactionEntity,
    onDelete: () -> Unit
) {
    val isExpense = transaction.type == TransactionType.EXPENSE
    val amountPrefix = if (isExpense) "-" else "+"
    val amountColor = if (isExpense) RupeeExpenseRed else RupeeIncomeGreen

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 4.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
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
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(transaction.category.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = transaction.category.getIcon(),
                        contentDescription = transaction.category.displayName,
                        tint = transaction.category.color,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = transaction.title,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (transaction.isSmsParsed) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(AxioTealContainer)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "SMS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AxioTealDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${transaction.accountName} • ${RupeeFormatter.formatDate(transaction.timestamp, "dd MMM")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Amount in Rupees
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$amountPrefix${RupeeFormatter.formatRupees(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = amountColor
                )
                Text(
                    text = transaction.category.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }
    }
}
