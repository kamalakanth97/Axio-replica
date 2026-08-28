package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ExpenseCategory
import com.example.ui.AxioUiState
import com.example.ui.AxioViewModel
import com.example.ui.CategorySpend
import com.example.ui.theme.*
import com.example.util.RupeeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    uiState: AxioUiState,
    viewModel: AxioViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("analytics_screen")
    ) {
        TopAppBar(
            title = { Text("Spend Analytics & Budget", fontWeight = FontWeight.Bold) },
            actions = {
                IconButton(
                    onClick = { viewModel.openExportDialog(true) },
                    modifier = Modifier.testTag("export_analytics_icon_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Export Report",
                        tint = AxioTealPrimary
                    )
                }
                IconButton(onClick = { viewModel.openAddBudget(true) }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Set Budget",
                        tint = AxioTealPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp)
        ) {
            // Total Monthly Spend Banner
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "AUGUST 2026 EXPENSES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = RupeeFormatter.formatRupees(uiState.currentMonthSpend, true),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = AxioTealPrimary
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Category Donut Chart
                        if (uiState.categorySpends.isNotEmpty()) {
                            DonutChart(
                                categorySpends = uiState.categorySpends,
                                totalSpend = uiState.currentMonthSpend
                            )
                        }
                    }
                }
            }

            // Category Spend Breakdown
            item {
                SectionHeader(title = "Category Breakdown (in ₹)")
            }

            if (uiState.categorySpends.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No expense data recorded this month in ₹ Rupees.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                items(uiState.categorySpends, key = { it.category.name }) { item ->
                    val budget = uiState.budgets.find { it.category == item.category }
                    CategoryProgressCard(
                        item = item,
                        budgetLimit = budget?.monthlyLimit
                    )
                }
            }

            // Monthly Budget Manager
            item {
                SectionHeader(
                    title = "Monthly Category Budgets",
                    actionLabel = "+ Set Budget",
                    onActionClick = { viewModel.openAddBudget(true) }
                )
            }

            if (uiState.budgets.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Track limits to save money every month.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = { viewModel.openAddBudget(true) },
                                colors = ButtonDefaults.buttonColors(containerColor = AxioTealPrimary)
                            ) {
                                Text("Set Monthly Budget")
                            }
                        }
                    }
                }
            } else {
                items(uiState.budgets, key = { it.id }) { budget ->
                    val spend = uiState.categorySpends.find { it.category == budget.category }?.totalAmount ?: 0.0
                    val ratio = (spend / budget.monthlyLimit).coerceIn(0.0, 1.0).toFloat()
                    val isOver = spend > budget.monthlyLimit

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
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(budget.category.color.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = budget.category.getIcon(),
                                            contentDescription = null,
                                            tint = budget.category.color,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = budget.category.displayName,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                Text(
                                    text = "${RupeeFormatter.formatRupees(spend)} / ${RupeeFormatter.formatRupees(budget.monthlyLimit)}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isOver) RupeeExpenseRed else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (isOver) RupeeExpenseRed else if (ratio > 0.8f) RupeeWarningAmber else budget.category.color,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            if (isOver) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = "Warning",
                                        tint = RupeeExpenseRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Over budget by ${RupeeFormatter.formatRupees(spend - budget.monthlyLimit)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = RupeeExpenseRed
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DonutChart(
    categorySpends: List<CategorySpend>,
    totalSpend: Double
) {
    Box(
        modifier = Modifier
            .size(170.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(160.dp)) {
            var startAngle = -90f
            val strokeWidth = 24.dp.toPx()

            categorySpends.forEach { item ->
                val sweepAngle = (item.percentage / 100f) * 360f
                drawArc(
                    color = item.category.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                startAngle += sweepAngle
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.PieChart,
                contentDescription = null,
                tint = AxioTealPrimary,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "${categorySpends.size} Categories",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun CategoryProgressCard(
    item: CategorySpend,
    budgetLimit: Double?
) {
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(item.category.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.category.getIcon(),
                            contentDescription = null,
                            tint = item.category.color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = item.category.displayName,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${item.transactionCount} transactions",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = RupeeFormatter.formatRupees(item.totalAmount),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${String.format("%.1f", item.percentage)}% of total",
                        style = MaterialTheme.typography.labelSmall,
                        color = AxioTealPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { (item.percentage / 100f).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = item.category.color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
