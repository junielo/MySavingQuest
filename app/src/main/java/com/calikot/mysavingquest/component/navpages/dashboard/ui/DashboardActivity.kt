package com.calikot.mysavingquest.component.navpages.dashboard.ui

import android.os.Bundle
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
import java.util.Calendar
import com.calikot.mysavingquest.ui.shared.chart.ChartType
import com.calikot.mysavingquest.ui.shared.chart.MainChart
import com.calikot.mysavingquest.ui.shared.chart.data.BarChartData
import com.calikot.mysavingquest.ui.shared.chart.data.SingleLineData

@AndroidEntryPoint
class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DashboardScreen()
        }
    }
}

@Composable
fun DashboardScreen() {
    // get view model and observe actual savings record
    val viewModel: DashboardVM = hiltViewModel()
    val actualSavingsRecord by viewModel.actualSavingsRecord.collectAsState()

    // Temporary single-line chart data: Jan 1 to Jan 30, values from 15,000 to 30,000
    val singleLineDataList = run {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        (0 until 30).map { idx ->
            val day = idx + 1
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, Calendar.JANUARY)
                set(Calendar.DAY_OF_MONTH, day)
            }
            val label = SimpleDateFormat("MMM d", Locale.getDefault()).format(cal.time)
            val value = 15000 + ((30000 - 15000) * idx) / 29 // linear interpolation inclusive
            SingleLineData(key = label, value = value)
        }
    }

    val barChartDataList = run {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        (0 until 30).map { idx ->
            val day = idx + 1
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, Calendar.JANUARY)
                set(Calendar.DAY_OF_MONTH, day)
            }
            val label = SimpleDateFormat("MMM d", Locale.getDefault()).format(cal.time)
            val value = 15000 + ((30000 - 15000) * idx) / 29 // linear interpolation inclusive
            BarChartData(key = label, value = value)
        }
    }

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
                    .fillMaxWidth()
                    .background(Color(0xFF232323)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .padding(vertical = 50.dp, horizontal = 24.dp)
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

                        // Color: red when negative, green when positive, black otherwise
                        val positiveGreen = Color(0xFF4CAF50)
                        val negativeRed = Color(0xFFF44336)
                        val labelGray = Color(0xFF666666)

                        val accountColor = when {
                            accountAmountInt < 0 -> negativeRed
                            accountAmountInt > 0 -> positiveGreen
                            else -> Color.Black
                        }

                        val netColor = when {
                            netAmountInt < 0 -> negativeRed
                            netAmountInt > 0 -> positiveGreen
                            else -> Color.Black
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
                                    fontWeight = FontWeight.Bold,
                                    color = accountColor
                                )
                            }

                            // Thin vertical divider
                            Box(
                                modifier = Modifier
                                    .width(1.dp)
                                    .height(48.dp)
                                    .background(Color(0xFFDDDDDD))
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
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = netColor
                                )
                            }
                        }

                        // As Of {Month Year} text at bottom of card
                        Spacer(modifier = Modifier.height(12.dp))
                        val monthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
                        Text(
                            text = "As Of $monthYear",
                            fontSize = 12.sp,
                            color = labelGray
                        )
                    }
                }
            }

            // Render three chart types in order: DoubleLine, SingleLine, Bar
            // 1) Double Line Chart
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(200.dp)
//                    .padding(vertical = 8.dp)
//            ) {
//                MainChart(modifier = Modifier.fillMaxSize(), chartType = ChartType.DOUBLE_LINE)
//            }

            // 2) Single Line Chart
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .padding(vertical = 8.dp)
            ) {
                MainChart(modifier = Modifier.fillMaxSize(), chartType = ChartType.SINGLE_LINE, data = singleLineDataList)
            }

            // 3) Bar Chart
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(200.dp)
//                    .padding(vertical = 8.dp)
//            ) {
//                MainChart(modifier = Modifier.fillMaxSize(), chartType = ChartType.BAR, data = barChartDataList)
//            }

            Spacer(modifier = Modifier.height(24.dp)) // replace weight spacer with a fixed spacer when scrollable
        }
    }
}