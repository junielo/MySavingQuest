package com.calikot.mysavingquest.component.navpages.dashboard.ui

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dagger.hilt.android.AndroidEntryPoint
import androidx.hilt.navigation.compose.hiltViewModel
import com.calikot.mysavingquest.component.navpages.dashboard.domain.DashboardVM
import com.calikot.mysavingquest.util.formatCompact
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.calikot.mysavingquest.ui.shared.chart.ChartType
import com.calikot.mysavingquest.ui.shared.chart.MainChart
import com.calikot.mysavingquest.component.navpages.actionneeded.domain.ActionNeededVM
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.automirrored.filled.ArrowForward

@RequiresApi(Build.VERSION_CODES.O)
@AndroidEntryPoint
class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DashboardScreen()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardScreen(onActionNeededClick: () -> Unit = {}) {
    // get view model and observe actual savings record
    val viewModel: DashboardVM = hiltViewModel()
    val actualSavingsRecord by viewModel.actualSavingsRecord.collectAsState()

    // observe monthly chart data from ViewModel (SingleLineData per day)
    val monthlyChartData by viewModel.monthlySavingsChart.collectAsState()

    // use ViewModel monthly chart data directly
    val usedSingleLineData = monthlyChartData

    // Collect reactive actionable count from ActionNeededVM so UI recomposes when it changes
    val actionNeededVM: ActionNeededVM = hiltViewModel()
    val actionableCount by actionNeededVM.actionableCount.collectAsState(initial = 0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()) // make whole content scrollable
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF40C4FF),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.padding(vertical = 50.dp, horizontal = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(horizontal = 50.dp, vertical = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Determine formatted values and colors for account and net amounts
                        val accountAmountInt = actualSavingsRecord?.accountAmount ?: 0
                        val netAmountInt = actualSavingsRecord?.netAmount ?: 0

                        val accountAmountFloat = (accountAmountInt).toFloat()
                        val netAmountFloat = (netAmountInt).toFloat()

                        // Color: red when negative, green when positive, white otherwise (suitable on blue background)
                        val positiveGreen = Color(0xFF011E8C)
                        val negativeRed = Color(0xFFF44336)
                        val labelGray = Color(0xFF343434)

                        val accountColor = when {
                            accountAmountInt < 0 -> negativeRed
                            accountAmountInt > 0 -> positiveGreen
                            else -> Color.White
                        }

                        val netColor = when {
                            netAmountInt < 0 -> negativeRed
                            netAmountInt > 0 -> positiveGreen
                            else -> Color.White
                        }

                        // Arrange titles horizontally with a thin divider between them
                        Row(
                            modifier = Modifier.wrapContentSize(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Account Balance",
                                    fontSize = 12.sp,
                                    color = labelGray
                                )

                                Text(
                                    text = formatCompact(accountAmountFloat),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Thin,
                                    color = accountColor
                                )
                            }

                            // Thin vertical divider
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(48.dp)
                                    .background(positiveGreen)
                            )

                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Net Amount",
                                    fontSize = 12.sp,
                                    color = labelGray
                                )

                                Text(
                                    text = formatCompact(netAmountFloat),
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = netColor
                                )
                            }
                        }

                        // As Of {Month Year} text at bottom of card
                        Spacer(modifier = Modifier.height(12.dp))
                        val monthYear =
                            SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
                        Text(
                            text = "As of $monthYear",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = labelGray
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(vertical = 8.dp)
            ) {
                MainChart(
                    modifier = Modifier.fillMaxSize(),
                    chartType = ChartType.SINGLE_LINE,
                    data = usedSingleLineData,
                    title = "Current Month Actual Savings"
                )
            }

            // New card below the chart showing actionable items count
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF40C4FF),
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Only navigate when there are actionable items. Navigation is provided by the
                            // caller via the onActionNeededClick lambda (MainDrawerActivity passes a NavController action).
                            if (actionableCount > 0) {
                                onActionNeededClick()
                            }
                        }
                ) {
                    // Use SpaceBetween so we can place an optional arrow at the far right
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left side: icon + message
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            // Show a friendly up-to-date message when there are no actionable items,
                            // otherwise show the pending count and a bell icon.
                            val actionMessage = if (actionableCount == 0) {
                                "You are up to date"
                            } else {
                                "You have $actionableCount pending item(s)"
                            }

                            // Icon: check for zero, bell for pending
                            if (actionableCount == 0) {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = "Up to date",
                                    tint = Color(0xFF011E8C),
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.Notifications,
                                    contentDescription = "Pending items",
                                    tint = Color(0xFF011E8C),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = actionMessage,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF343434)
                            )
                        }

                        // Right side: arrow only when actionable items exist
                        if (actionableCount > 0) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Open action needed",
                                tint = Color(0xFF343434),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp)) // replace weight spacer with a fixed spacer when scrollable
        }
    }
}