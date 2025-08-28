package com.calikot.mysavingquest.setup

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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.calikot.mysavingquest.models.AccountItem
import com.calikot.mysavingquest.ui.theme.MySavingQuestTheme

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
    val defaultAccounts = listOf(
        AccountItem("BDO Debit", "Debit"),
        AccountItem("BDO Credit", "Credit"),
        AccountItem("GCash Credit", "Credit")
    )
    val accounts = remember { mutableStateListOf<AccountItem>().apply { addAll(defaultAccounts) } }

    val listState = rememberLazyListState()
    val fabRightOffset = remember { Animatable(0f) }
    val fabLeftOffset = remember { Animatable(0f) }
    val fabHiddenOffset = 300f
    val fabVisibleOffset = 0f
    val isScrolling by remember { derivedStateOf<Boolean> { listState.isScrollInProgress } }

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
        Column(modifier = Modifier.fillMaxSize()) {
            AccountBalanceList(accounts = accounts, listState = listState)
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
            onClick = { (context as? ComponentActivity)?.finish() },
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
            com.calikot.mysavingquest.setup.dialog.AddAccountBalanceDialog(
                onDismiss = { showAddDialog = false },
                onCreate = { type, name ->
                    if (name.isNotBlank()) {
                        accounts.add(AccountItem(name, type))
                        showAddDialog = false
                    }
                }
            )
        }
    }
}

@Composable
fun AccountBalanceTopBar() {
    val context = LocalContext.current
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
                val intent = Intent(context, NotificationSettingsActivity::class.java)
                context.startActivity(intent)
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
fun AccountBalanceList(accounts: MutableList<AccountItem>, listState: LazyListState) {
    var showDialog by remember { mutableStateOf(false) }
    var accountToDelete by remember { mutableStateOf<AccountItem?>(null) }

    LazyColumn(state = listState, modifier = Modifier.fillMaxWidth()) {
        itemsIndexed(accounts, key = { _, account -> account.hashCode() }) { i, account ->
            var offsetX by remember { mutableStateOf(0f) }
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
                    .background(Color.Transparent)
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
                    AccountBalanceRow(account)
                }
            }
            if (i < accounts.lastIndex) HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 1.dp)
        }
    }
    if (showDialog && accountToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                accountToDelete = null
            },
            title = { Text("Delete Account") },
            text = { Text("Are you sure you want to delete this account?") },
            confirmButton = {
                TextButton(onClick = {
                    accounts.remove(accountToDelete)
                    showDialog = false
                    accountToDelete = null
                }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDialog = false
                    accountToDelete = null
                }) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
fun AccountBalanceRow(account: AccountItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = account.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = account.type,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.End
        )
    }
}
