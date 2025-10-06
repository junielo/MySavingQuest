package com.calikot.mysavingquest.component.setup.accountbalance.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calikot.mysavingquest.R
import com.calikot.mysavingquest.component.setup.notification.NotificationSettingsActivity
import com.calikot.mysavingquest.component.setup.accountbalance.domain.models.AccountBalanceItem
import com.calikot.mysavingquest.ui.theme.MySavingQuestTheme
import androidx.hilt.navigation.compose.hiltViewModel
import com.calikot.mysavingquest.component.setup.accountbalance.domain.AccountBalanceVM
import com.calikot.mysavingquest.component.setup.recurringbills.ui.RecurringBillsActivity
import com.calikot.mysavingquest.ui.shared.LoadingDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AccountBalanceActivity : ComponentActivity() {
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
                                AccountBalanceTopBar()
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
                    AccountBalanceScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AccountBalanceScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }
    val viewModel: AccountBalanceVM = hiltViewModel()
    val accountBalances by viewModel.accountBalances.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val listState = rememberLazyListState()
    val fabRightOffset = remember { Animatable(0f) }
    val fabLeftOffset = remember { Animatable(0f) }
    val fabHiddenOffset = 300f
    val fabVisibleOffset = 0f
    val isScrolling by remember { derivedStateOf { listState.isScrollInProgress } }

    AccountBalanceSetupStatus(list = accountBalances, viewModel = viewModel)

    LaunchedEffect(isScrolling) {
        fabRightOffset.animateTo(
            if (isScrolling) fabHiddenOffset else fabVisibleOffset,
            animationSpec = tween(durationMillis = 300)
        )
        fabLeftOffset.animateTo(
            if (isScrolling) -fabHiddenOffset else fabVisibleOffset,
            animationSpec = tween(durationMillis = 300)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LoadingDialog(show = isLoading)
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Swipe left to delete",
                fontSize = 11.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 2.dp)
            )
            AccountBalanceList(accounts = accountBalances, listState = listState, viewModel = viewModel)
        }
        FloatingActionButton(
            onClick = { showAddDialog = true },
            shape = CircleShape,
            containerColor = Color(0xFF2C2C2C),
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(6.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(32.dp)
                .offset { IntOffset(fabRightOffset.value.toInt(), 0) }
        ) {
            Icon(Icons.Filled.Add, contentDescription = "Add", modifier = Modifier.size(32.dp))
        }
        FloatingActionButton(
            onClick = {
                val intent = Intent(context, RecurringBillsActivity::class.java)
                context.startActivity(intent)
                (context as? ComponentActivity)?.finish()
            },
            shape = CircleShape,
            containerColor = Color(0xFF2C2C2C),
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(6.dp),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(32.dp)
                .offset { IntOffset(fabLeftOffset.value.toInt(), 0) }
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", modifier = Modifier.size(32.dp))
        }
        if (showAddDialog) {
            AddAccountBalanceDialog(
                onDismiss = { showAddDialog = false },
                onCreate = { type, name ->
                    if (name.isNotBlank()) {
                        showAddDialog = false
                        viewModel.addAccountBalance(AccountBalanceItem(accType = type, accName = name)) { result ->
                            if (result.isSuccess) {
                                Toast.makeText(context, "Account added.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, result.exceptionOrNull()?.message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun AccountBalanceTopBar() {
    val context = LocalContext.current
    val viewModel: AccountBalanceVM = hiltViewModel()
    val coroutineScope = rememberCoroutineScope()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Account Balance",
            modifier = Modifier.weight(1f)
        )
        Button(
            onClick = {
                coroutineScope.launch {
                    if (viewModel.accountBalances.value.isNotEmpty()) {
                        viewModel.updateAccountBalanceStatus(true)
                        val intent = Intent(context, NotificationSettingsActivity::class.java)
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "Please add at least one account", Toast.LENGTH_SHORT).show()
                    }
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
fun AccountBalanceList(accounts: List<AccountBalanceItem>, listState: LazyListState, viewModel: AccountBalanceVM) {
    var showDialog by remember { mutableStateOf(false) }
    var accountToDelete by remember { mutableStateOf<AccountBalanceItem?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<AccountBalanceItem?>(null) }
    val context = LocalContext.current

    LazyColumn(state = listState, modifier = Modifier.fillMaxWidth()) {
        itemsIndexed(accounts, key = { _, account -> account.hashCode() }) { i, account ->
            var offsetX by remember { mutableFloatStateOf(0f) }
            val animatedOffsetX = remember { Animatable(0f) }
            val threshold = 200f
            val showDeleteIcon = offsetX < -40f
            val showDialogForItem = offsetX < -threshold

            LaunchedEffect(offsetX) {
                if (showDialogForItem && !showDialog) {
                    animatedOffsetX.animateTo(-threshold, animationSpec = tween(200))
                    showDialog = true
                    accountToDelete = account
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
                    AccountBalanceRow(account, onEdit = {
                        accountToEdit = account
                        showEditDialog = true
                    })
                }
            }
            if (i < accounts.lastIndex) HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
        }
    }
    if (showDialog && accountToDelete != null) {
        com.calikot.mysavingquest.ui.shared.ConfirmationDialog(
            title = "Delete Account",
            message = "Are you sure you want to delete this account?",
            onConfirm = {
                accountToDelete?.let {
                    viewModel.removeAccountBalance(it) { result ->
                        if (result.isSuccess) {
                            Toast.makeText(context, "Account deleted.", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, result.exceptionOrNull()?.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                showDialog = false
                accountToDelete = null
            },
            onDismiss = {
                showDialog = false
                accountToDelete = null
            }
        )
    }
    if (showEditDialog && accountToEdit != null) {
        EditAccountBalanceDialog(
            account = accountToEdit!!,
            onDismiss = {
                showEditDialog = false
                accountToEdit = null
            },
            onUpdate = { updatedAccount ->
                showEditDialog = false
                accountToEdit = null
                viewModel.updateAccountBalance(updatedAccount) { result ->
                    if (result.isSuccess) {
                        Toast.makeText(context, "Account updated.", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, result.exceptionOrNull()?.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}

@Composable
fun AccountBalanceRow(account: AccountBalanceItem, onEdit: (() -> Unit)) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = account.accName,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = account.accType,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.End
        )
        // Edit button
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp,
            modifier = Modifier.padding(start = 12.dp).size(32.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit account",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AccountBalanceSetupStatus(list: List<AccountBalanceItem>, viewModel: AccountBalanceVM){
    LaunchedEffect(list.size) {
        if (list.isEmpty()) {
            viewModel.updateAccountBalanceStatus(false)
        }
    }
}