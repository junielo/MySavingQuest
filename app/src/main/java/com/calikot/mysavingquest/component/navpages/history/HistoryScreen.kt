package com.calikot.mysavingquest.component.navpages.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calikot.mysavingquest.component.setup.recurringbills.domain.models.RecurringBillItem
import androidx.navigation.NavController

@Composable
fun HistoryScreen(navController: NavController) {
    val defaultBills = listOf<RecurringBillItem>()
    val bills = remember { mutableStateListOf<RecurringBillItem>().apply { addAll(defaultBills) } }
    val listState = rememberLazyListState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        RecurringBillsList(bills = bills, listState = listState)
    }
}

@Composable
fun RecurringBillsList(bills: List<RecurringBillItem>, listState: LazyListState) {
    LazyColumn(state = listState, modifier = Modifier.fillMaxWidth()) {
        itemsIndexed(bills, key = { _, bill -> bill.hashCode() }) { i, bill ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                RecurringBillRow(bill)
            }
            if (i < bills.lastIndex) HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
        }
    }
}

@Composable
fun RecurringBillRow(bill: RecurringBillItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = bill.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = bill.date,
                fontSize = 13.sp,
                color = Color(0xFF757575)
            )
        }
        if (bill.isAuto) {
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFF2C2C2C),
                shadowElevation = 0.dp
            ) {
                Text(
                    text = "Auto",
                    color = Color.White,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .align(Alignment.CenterVertically)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = bill.amount.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.End
        )
    }
}
