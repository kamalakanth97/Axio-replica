package com.example.ui.dialogs

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.DefaultSeedData
import com.example.ui.theme.AxioTealContainer
import com.example.ui.theme.AxioTealDark
import com.example.ui.theme.AxioTealPrimary
import com.example.ui.theme.RupeeIncomeGreen

@Composable
fun SmsSyncDialog(
    onDismiss: () -> Unit,
    onSyncInbox: () -> Unit,
    onSimulateSms: (String) -> Unit,
    isSyncing: Boolean = false,
    syncSummary: String? = null
) {
    val context = LocalContext.current
    var hasSmsPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.READ_SMS] == true
        hasSmsPermission = granted
        if (granted) {
            onSyncInbox()
        }
    }

    var customSmsText by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Auto Sync & Status, 1 = Manual / Test

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Sms, contentDescription = null, tint = AxioTealPrimary)
                Text(text = "Automatic SMS Reader", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Tab switcher
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = AxioTealPrimary
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Auto Sync", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Test / Samples", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
                    )
                }

                if (selectedTab == 0) {
                    // Auto Sync & Live Receiver card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (hasSmsPermission) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (hasSmsPermission) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = if (hasSmsPermission) RupeeIncomeGreen else Color(0xFFD97706),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if (hasSmsPermission) "Automatic SMS Sync Active" else "SMS Permission Required",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (hasSmsPermission) Color(0xFF14532D) else Color(0xFF92400E)
                                )
                            }
                            Text(
                                text = if (hasSmsPermission)
                                    "KaKi Wallet is listening for incoming bank & UPI SMS in the background. Tap 'Sync Inbox Now' to import previous bank texts."
                                else
                                    "Grant SMS permission so KaKi Wallet can automatically read incoming bank debits/credits without manual typing.",
                                fontSize = 12.sp,
                                color = if (hasSmsPermission) Color(0xFF166534) else Color(0xFF78350F)
                            )
                        }
                    }

                    if (syncSummary != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = AxioTealContainer.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = AxioTealDark,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = syncSummary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = AxioTealDark
                                )
                            }
                        }
                    }

                    // Button to trigger permission or sync
                    Button(
                        onClick = {
                            if (!hasSmsPermission) {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.READ_SMS,
                                        Manifest.permission.RECEIVE_SMS
                                    )
                                )
                            } else {
                                onSyncInbox()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_sync_inbox_sms"),
                        colors = ButtonDefaults.buttonColors(containerColor = AxioTealPrimary),
                        enabled = !isSyncing,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scanning Inbox...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                imageVector = if (hasSmsPermission) Icons.Default.Sync else Icons.Default.Security,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (hasSmsPermission) "Sync SMS Inbox Now (Last 90 Days)" else "Grant SMS Permission & Sync",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Text(
                        text = "Supported Banks: HDFC, SBI, ICICI, Axis, Kotak, PNB, Canara, BoB, CRED, Paytm, PhonePe, Google Pay, Amazon Pay.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    // Manual simulation / sample testing
                    Text(
                        text = "Tap any template below to test parser:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    DefaultSeedData.sampleSmsTemplates.forEach { sms ->
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
                        text = "Or paste raw SMS text:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    OutlinedTextField(
                        value = customSmsText,
                        onValueChange = { customSmsText = it },
                        placeholder = { Text("e.g. Rs 540.00 debited from HDFC A/c XX4092 to ZOMATO...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                        maxLines = 4
                    )

                    Button(
                        onClick = {
                            if (customSmsText.isNotBlank()) {
                                onSimulateSms(customSmsText.trim())
                                onDismiss()
                            }
                        },
                        enabled = customSmsText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = AxioTealPrimary),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Parse & Add")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
