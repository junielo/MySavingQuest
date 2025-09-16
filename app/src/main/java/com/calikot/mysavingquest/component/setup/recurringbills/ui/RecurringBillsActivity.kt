package com.calikot.mysavingquest.component.setup.recurringbills.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calikot.mysavingquest.R
import com.calikot.mysavingquest.component.setup.recurringbills.domain.models.RecurringBillItem
import com.calikot.mysavingquest.ui.theme.MySavingQuestTheme
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import com.calikot.mysavingquest.component.setup.accountbalance.ui.AccountBalanceActivity
import com.calikot.mysavingquest.ui.shared.ConfirmationDialog
import com.calikot.mysavingquest.ui.shared.LoadingDialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.calikot.mysavingquest.component.setup.recurringbills.domain.RecurringBillsVM
import com.calikot.mysavingquest.util.formatRecurringDay
import com.calikot.mysavingquest.util.formatWithCommas
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RecurringBillsActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MySavingQuestTheme(
                darkTheme = isSystemInDarkTheme(),
                dynamicColor = false
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = {
                                RecurringBillsTopBar()
                            },
                            navigationIcon = {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_launcher_foreground), // Use your icon resource
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            ),
                            modifier = Modifier
                                .shadow(8.dp, ambientColor = Color.Black, spotColor = Color.Black)
                        )
                    }
                ) { innerPadding ->
                    RecurringBillsScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun RecurringBillsScreen(modifier: Modifier = Modifier) {
    var showAddBillDialog by remember { mutableStateOf(false) }
    val viewModel: RecurringBillsVM = hiltViewModel()
    val bills by viewModel.recurringBills.collectAsState()
    val listState = rememberLazyListState()
    val fabOffsetX = remember { Animatable(0f) }
    val fabHiddenOffset = 300f // px to move FAB out of screen
    val fabVisibleOffset = 0f
    val isScrolling by remember { derivedStateOf { listState.isScrollInProgress } }

    LaunchedEffect(isScrolling) {
        fabOffsetX.animateTo(
            if (isScrolling) fabHiddenOffset else fabVisibleOffset,
            animationSpec = tween(durationMillis = 300)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val isLoading by viewModel.isLoading.collectAsState()
        LoadingDialog(show = isLoading)
        Column(modifier = Modifier.fillMaxSize()) {
            RecurringBillsList(bills = bills, listState = listState)
        }
        FloatingActionButton(
            onClick = { showAddBillDialog = true },
            shape = CircleShape,
            containerColor = Color(0xFF2C2C2C),
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(6.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(32.dp)
                .offset { IntOffset(fabOffsetX.value.toInt(), 0) }
        ) {
            Text("+", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
        }
        if (showAddBillDialog) {
            AddBillDialog(
                onDismiss = { showAddBillDialog = false },
                onCreate = { bill ->
                    showAddBillDialog = false
                    viewModel.showLoading()
                    viewModel.addRecurringBill(bill)
                    viewModel.dismissLoading()
                }
            )
        }
    }
}

@Composable
fun RecurringBillsTopBar() {
    val context = LocalContext.current
    val viewModel: RecurringBillsVM = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Recurring Bills",
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = {
                coroutineScope.launch {
                    viewModel.updateRecurringBillStatus()
                    val intent = Intent(context, AccountBalanceActivity::class.java)
                    context.startActivity(intent)
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C2C)),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            modifier = Modifier.height(40.dp)
        ) {
            Text("Next", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RecurringBillsList(bills: List<RecurringBillItem>, listState: LazyListState) {
    var showDialog by remember { mutableStateOf(false) }
    var billToDelete by remember { mutableStateOf<RecurringBillItem?>(null) }
    val viewModel: RecurringBillsVM = hiltViewModel()

    LazyColumn(state = listState, modifier = Modifier.fillMaxWidth()) {
        itemsIndexed(bills, key = { _, bill -> bill.hashCode() }) { i, bill ->
            var offsetX by remember { mutableFloatStateOf(0f) }
            val animatedOffsetX = remember { Animatable(0f) }
            val threshold = 200f
            val showDeleteIcon = offsetX < -40f
            val showDialogForItem = offsetX < -threshold

            LaunchedEffect(offsetX) {
                if (showDialogForItem && !showDialog) {
                    animatedOffsetX.animateTo(-threshold, animationSpec = tween(200))
                    showDialog = true
                    billToDelete = bill
                } else if (!showDialogForItem && offsetX == 0f) {
                    animatedOffsetX.animateTo(0f, animationSpec = tween(200))
                } else {
                    animatedOffsetX.snapTo(offsetX)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
            ) {
                // Delete icon background
                if (showDeleteIcon) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color(0xFFF44336)),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = "Delete",
                            tint = Color.White,
                            modifier = Modifier.padding(end = 32.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .offset { IntOffset(animatedOffsetX.value.toInt(), 0) }
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    if (!showDialogForItem) {
                                        offsetX = 0f
                                    }
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    offsetX = (offsetX + dragAmount).coerceAtMost(0f)
                                }
                            )
                        }
                ) {
                    RecurringBillRow(bill)
                }
            }
            if (i < bills.lastIndex) HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
        }
    }
    if (showDialog && billToDelete != null) {
        ConfirmationDialog(
            title = "Delete Bill",
            message = "Are you sure you want to delete this bill?",
            onConfirm = {
                billToDelete?.let { viewModel.removeRecurringBill(it) }
                showDialog = false
                billToDelete = null
            },
            onDismiss = {
                showDialog = false
                billToDelete = null
            }
        )
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
                fontSize = 18.sp, // smaller than before
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
            Text(
                text = formatRecurringDay(bill.date),
                fontSize = 13.sp, // smaller than before
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
            text = formatWithCommas(bill.amount),
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.End
        )
    }
}
