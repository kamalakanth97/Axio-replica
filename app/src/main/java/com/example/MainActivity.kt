package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.AxioViewModel
import com.example.ui.dialogs.*
import com.example.ui.screens.*
import com.example.ui.theme.AxioTealContainer
import com.example.ui.theme.AxioTealDark
import com.example.ui.theme.AxioTealPrimary
import com.example.ui.theme.AxioTheme

enum class AxioScreen(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    TRANSACTIONS("Spends", Icons.Default.ReceiptLong),
    ANALYTICS("Analytics", Icons.Default.PieChart),
    ACCOUNTS("Accounts", Icons.Default.AccountBalanceWallet),
    SPLIT("Splits", Icons.Default.Group)
}

class MainActivity : ComponentActivity() {

    private val viewModel: AxioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AxioTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                var currentScreen by remember { mutableStateOf(AxioScreen.HOME) }

                val isAddTxnOpen by viewModel.isAddTransactionOpen.collectAsStateWithLifecycle()
                val isSmsSimOpen by viewModel.isSmsSimulatorOpen.collectAsStateWithLifecycle()
                val isSyncingSms by viewModel.isSyncingSms.collectAsStateWithLifecycle()
                val smsSyncSummary by viewModel.smsSyncSummary.collectAsStateWithLifecycle()
                val isAddAccountOpen by viewModel.isAddAccountOpen.collectAsStateWithLifecycle()
                val isAddBillOpen by viewModel.isAddBillOpen.collectAsStateWithLifecycle()
                val isAddSplitOpen by viewModel.isAddSplitOpen.collectAsStateWithLifecycle()
                val isAddBudgetOpen by viewModel.isAddBudgetOpen.collectAsStateWithLifecycle()
                val isQrScannerOpen by viewModel.isQrScannerOpen.collectAsStateWithLifecycle()
                val isExportDialogOpen by viewModel.isExportDialogOpen.collectAsStateWithLifecycle()
                val postPaymentData by viewModel.postPaymentData.collectAsStateWithLifecycle()
                val upiPrompt by viewModel.upiPromptState.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            AxioScreen.values().forEach { screen ->
                                NavigationBarItem(
                                    selected = currentScreen == screen,
                                    onClick = { currentScreen = screen },
                                    icon = {
                                        Icon(
                                            imageVector = screen.icon,
                                            contentDescription = screen.label,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    label = { Text(screen.label) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = AxioTealDark,
                                        selectedTextColor = AxioTealPrimary,
                                        indicatorColor = AxioTealContainer
                                    ),
                                    modifier = Modifier.testTag("nav_${screen.name.lowercase()}")
                                )
                            }
                        }
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { viewModel.openAddTransaction(true) },
                            containerColor = AxioTealPrimary,
                            contentColor = Color.White,
                            modifier = Modifier.testTag("fab_add_expense")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Expense")
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentScreen) {
                            AxioScreen.HOME -> HomeScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                                onNavigateToTransactions = { currentScreen = AxioScreen.TRANSACTIONS },
                                onNavigateToAnalytics = { currentScreen = AxioScreen.ANALYTICS },
                                onNavigateToAccounts = { currentScreen = AxioScreen.ACCOUNTS }
                            )
                            AxioScreen.TRANSACTIONS -> TransactionsScreen(
                                uiState = uiState,
                                viewModel = viewModel
                            )
                            AxioScreen.ANALYTICS -> AnalyticsScreen(
                                uiState = uiState,
                                viewModel = viewModel
                            )
                            AxioScreen.ACCOUNTS -> AccountsScreen(
                                uiState = uiState,
                                viewModel = viewModel
                            )
                            AxioScreen.SPLIT -> SplitScreen(
                                uiState = uiState,
                                viewModel = viewModel
                            )
                        }

                        // Dialog Overlays
                        if (isAddTxnOpen) {
                            AddTransactionDialog(
                                onDismiss = { viewModel.openAddTransaction(false) },
                                onConfirm = { title, amount, type, category, mode, acct, notes ->
                                    viewModel.addTransaction(title, amount, type, category, mode, acct, notes)
                                }
                            )
                        }

                        if (isSmsSimOpen) {
                            SmsSyncDialog(
                                onDismiss = { viewModel.openSmsSimulator(false) },
                                onSyncInbox = { viewModel.syncInboxMessages(daysBack = 90) },
                                onSimulateSms = { sms ->
                                    viewModel.parseAndProcessSms(sms)
                                },
                                isSyncing = isSyncingSms,
                                syncSummary = smsSyncSummary
                            )
                        }

                        if (isAddAccountOpen) {
                            AddAccountDialog(
                                onDismiss = { viewModel.openAddAccount(false) },
                                onConfirm = { name, type, bal, last4, limit, due ->
                                    viewModel.addAccount(name, type, bal, last4, limit, due)
                                }
                            )
                        }

                        if (isAddBillOpen) {
                            AddBillDialog(
                                onDismiss = { viewModel.openAddBill(false) },
                                onConfirm = { title, amount, days, biller, cat ->
                                    viewModel.addBill(title, amount, days, biller, cat)
                                }
                            )
                        }

                        if (isAddSplitOpen) {
                            AddSplitDialog(
                                onDismiss = { viewModel.openAddSplit(false) },
                                onConfirm = { title, total, paidBy, members, share ->
                                    viewModel.addSplitExpense(title, total, paidBy, members, share)
                                }
                            )
                        }

                        if (isAddBudgetOpen) {
                            SetBudgetDialog(
                                onDismiss = { viewModel.openAddBudget(false) },
                                onConfirm = { cat, limit ->
                                    viewModel.setBudgetLimit(cat, limit)
                                }
                            )
                        }

                        if (upiPrompt.isOpen) {
                            UpiPaymentDialog(
                                initialVpa = upiPrompt.initialVpa,
                                initialName = upiPrompt.initialName,
                                initialAmount = upiPrompt.initialAmount,
                                initialNote = upiPrompt.initialNote,
                                initialCategory = upiPrompt.initialCategory,
                                onDismiss = { viewModel.closeUpiPayment() },
                                onPaymentInitiated = { title, amount, type, category, mode, acct, notes ->
                                    viewModel.addTransaction(title, amount, type, category, mode, acct, notes)
                                },
                                onOpenPostPaymentReview = { postData ->
                                    viewModel.openPostPaymentReview(postData)
                                }
                            )
                        }

                        if (isQrScannerOpen) {
                            CameraQrScannerDialog(
                                onDismiss = { viewModel.openQrScanner(false) },
                                onQrScanned = { details ->
                                    viewModel.openQrScanner(false)
                                    val suggestedCat = com.example.util.UpiIntentHelper.guessCategory(
                                        details.name + " " + details.note + " " + details.vpa
                                    )
                                    viewModel.openUpiPayment(
                                        vpa = details.vpa,
                                        name = details.name,
                                        amount = details.amount,
                                        note = details.note,
                                        category = suggestedCat
                                    )
                                }
                            )
                        }

                        postPaymentData?.let { data ->
                            UpiPostPaymentSheet(
                                data = data,
                                onDismiss = { viewModel.dismissPostPaymentReview() },
                                onSaveTransaction = { title, amount, type, cat, mode, acct, notes, splitWithFriends, splitMembers ->
                                    viewModel.savePostPaymentTransaction(
                                        title = title,
                                        amount = amount,
                                        type = type,
                                        category = cat,
                                        paymentMode = mode,
                                        accountName = acct,
                                        notes = notes,
                                        splitWithFriends = splitWithFriends,
                                        splitMembers = splitMembers
                                    )
                                    viewModel.dismissPostPaymentReview()
                                }
                            )
                        }

                        if (isExportDialogOpen) {
                            ExportReportDialog(
                                transactions = uiState.transactions,
                                onDismiss = { viewModel.openExportDialog(false) }
                            )
                        }
                    }
                }
            }
        }
    }
}
