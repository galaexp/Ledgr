package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.domain.DebtSimplificationEngine
import com.example.domain.FriendBalanceSummary
import com.example.domain.PdfReportGenerator
import com.example.ui.components.BentoCard
import com.example.ui.components.FriendAvatar
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.ExpenseRose
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.neoPopOnColor
import com.example.viewmodel.LedgrViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

enum class SplitTab {
    FRIENDS, GROUPS, SIMPLIFIED_DEBTS, ACTIVITY
}

@Composable
fun SplitsScreen(
    viewModel: LedgrViewModel,
    onOpenSplitBill: () -> Unit,
    onOpenSettleUp: (Long?, Double) -> Unit,
    onOpenAddFriend: () -> Unit,
    onOpenAddGroup: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(SplitTab.FRIENDS) }

    val friends by viewModel.friends.collectAsState(initial = emptyList())
    val groups by viewModel.groups.collectAsState(initial = emptyList())
    val friendBalances by viewModel.friendBalances.collectAsState()
    val (youAreOwed, youOwe) = viewModel.netSplitSummary.collectAsState().value
    val splitExpenses by viewModel.splitExpenses.collectAsState(initial = emptyList())
    val splitParticipants by viewModel.splitParticipants.collectAsState(initial = emptyList())
    val settlements by viewModel.settlements.collectAsState(initial = emptyList())
    val currencySymbol by viewModel.currencySymbol.collectAsState()

    val simplifiedDebts = remember(friends, splitExpenses, splitParticipants) {
        DebtSimplificationEngine.simplifyGroupDebts(friends, splitExpenses, splitParticipants)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // 1. Net Position Hero Banner
        BentoCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "TOTAL NET BALANCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    val net = youAreOwed - youOwe
                    val formattedNet = String.format(Locale.US, "%.2f", abs(net))
                    Text(
                        text = "${if (net >= 0) "+" else "-"}$currencySymbol$formattedNet",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (net >= 0) IncomeGreen else ExpenseRose
                    )
                    Text(
                        text = if (net >= 0) "Overall people owe you money" else "Overall you owe money to friends",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onOpenSplitBill,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Split Bill", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 2. Tab Navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf(
                SplitTab.FRIENDS to "Friends",
                SplitTab.GROUPS to "Groups",
                SplitTab.SIMPLIFIED_DEBTS to "Debts Graph",
                SplitTab.ACTIVITY to "Activity"
            ).forEach { (tab, title) ->
                val isSelected = selectedTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) EmeraldAccent else Color.Transparent)
                        .clickable { selectedTab = tab }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) EmeraldAccent.neoPopOnColor() else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // 3. Tab Contents
        when (selectedTab) {
            SplitTab.FRIENDS -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Friend Ledger Accounts", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onOpenAddFriend) {
                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Friend", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (friendBalances.isEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenAddFriend() },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Group, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No friends added yet", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Tap here to add your roommates, travel buddies, or colleagues", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 96.dp)
                    ) {
                        items(friendBalances) { summary ->
                            FriendBalanceCard(
                                summary = summary,
                                currencySymbol = currencySymbol,
                                onSettleClick = {
                                    onOpenSettleUp(summary.friend.id, summary.netBalance)
                                }
                            )
                        }
                    }
                }
            }

            SplitTab.GROUPS -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Expense Groups", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onOpenAddGroup) {
                        Icon(imageVector = Icons.Default.GroupAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Group", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (groups.isEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenAddGroup() },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Luggage, contentDescription = null, tint = EmeraldAccent, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No shared groups created yet", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Tap here to create a group for Trips, Apartment Rent, or Events", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 96.dp)
                    ) {
                        items(groups) { group ->
                            val groupExpenses = splitExpenses.filter { it.groupId == group.id }
                            val totalGroupSpend = groupExpenses.sumOf { it.totalAmount }

                            GroupCard(
                                group = group,
                                totalSpend = totalGroupSpend,
                                currencySymbol = currencySymbol,
                                expenseCount = groupExpenses.size,
                                onGeneratePdf = {
                                    val pdfFile = PdfReportGenerator.generateAndShareGroupReport(
                                        context = context,
                                        group = group,
                                        friends = friends,
                                        expenses = groupExpenses,
                                        participants = splitParticipants
                                    )
                                    if (pdfFile != null) {
                                        PdfReportGenerator.sharePdfFile(context, pdfFile, "Ledgr Statement: ${group.name}")
                                    } else {
                                        Toast.makeText(context, "PDF generation failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }

            SplitTab.SIMPLIFIED_DEBTS -> {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Hub, contentDescription = null, tint = EmeraldAccent)
                            Column {
                                Text(
                                    text = "Cash Flow Minimization Algorithm",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Resolves circular group debts into the absolute minimum number of payments.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (simplifiedDebts.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("All group debts are completely settled! ✨", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("Split a new bill or invite friends to track shared expenses.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 96.dp)
                        ) {
                            items(simplifiedDebts) { debt ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            FriendAvatar(name = debt.fromName, size = 34.dp)
                                            Icon(
                                                imageVector = Icons.Default.ArrowForward,
                                                contentDescription = null,
                                                tint = EmeraldAccent,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            FriendAvatar(name = debt.toName, size = 34.dp)

                                            Column {
                                                Text(
                                                    text = "${debt.fromName} pays ${debt.toName}",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                Text("Optimal route", fontSize = 10.sp, color = EmeraldAccent)
                                            }
                                        }

                                        Text(
                                            text = "$currencySymbol${String.format(Locale.US, "%.2f", debt.amount)}",
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = EmeraldAccent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            SplitTab.ACTIVITY -> {
                Text("Settlements & Shared Expenses Log", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                val sdf = remember { SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.US) }

                if (settlements.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No settlement activity yet", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Completed repayments and settle-ups will appear here.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 96.dp)
                    ) {
                        items(settlements) { item ->
                            val fromName = if (item.fromFriendId == 0L) "You" else friends.find { it.id == item.fromFriendId }?.name ?: "Friend"
                            val toName = if (item.toFriendId == 0L) "You" else friends.find { it.id == item.toFriendId }?.name ?: "Friend"

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = EmeraldAccent,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Column {
                                            Text("$fromName settled with $toName", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text("${item.paymentMethod} · ${sdf.format(Date(item.date))}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    Text(
                                        text = "$currencySymbol${String.format(Locale.US, "%.2f", item.amount)}",
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldAccent
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
fun FriendBalanceCard(
    summary: FriendBalanceSummary,
    currencySymbol: String,
    onSettleClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FriendAvatar(
                    name = summary.friend.name,
                    avatarColorHex = summary.friend.avatarColorHex,
                    size = 40.dp
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = summary.friend.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = summary.friend.paymentHandle.ifEmpty { summary.friend.phone.ifEmpty { "Member" } },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    val isZero = abs(summary.netBalance) < 0.01
                    val text = when {
                        isZero -> "Settled up"
                        summary.netBalance > 0 -> "owes you"
                        else -> "you owe"
                    }
                    val color = when {
                        isZero -> MaterialTheme.colorScheme.onSurfaceVariant
                        summary.netBalance > 0 -> IncomeGreen
                        else -> ExpenseRose
                    }

                    Text(text = text, fontSize = 11.sp, color = color)
                    if (!isZero) {
                        Text(
                            text = "$currencySymbol${String.format(Locale.US, "%.2f", abs(summary.netBalance))}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = color
                        )
                    }
                }

                if (abs(summary.netBalance) >= 0.01) {
                    Button(
                        onClick = onSettleClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Settle", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun GroupCard(
    group: GroupEntity,
    totalSpend: Double,
    currencySymbol: String,
    expenseCount: Int,
    onGeneratePdf: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = group.iconEmoji, fontSize = 22.sp)
                    Column {
                        Text(
                            text = group.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = group.description.ifEmpty { "$expenseCount recorded shared transactions" },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FilledTonalButton(
                    onClick = onGeneratePdf,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("PDF Report", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Group Spend", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    text = "$currencySymbol${String.format(Locale.US, "%,.2f", totalSpend)}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = EmeraldAccent
                )
            }
        }
    }
}
