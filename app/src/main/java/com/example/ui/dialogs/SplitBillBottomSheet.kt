package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.domain.ParsedReceiptItem
import com.example.ui.components.FriendAvatar
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.ExpenseRose
import com.example.ui.theme.LocalCustomColors
import com.example.ui.theme.neoPopOnColor
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SplitBillBottomSheet(
    friends: List<FriendEntity>,
    groups: List<GroupEntity>,
    accounts: List<AccountEntity>,
    initialAmount: Double = 0.0,
    initialTitle: String = "",
    receiptItems: List<ParsedReceiptItem> = emptyList(),
    onDismiss: () -> Unit,
    onSaveSplit: (SplitExpenseEntity, List<SplitParticipantEntity>, Long?) -> Unit
) {
    val customColors = LocalCustomColors.current
    var billTitle by remember { mutableStateOf(initialTitle.ifEmpty { "Dinner & Drinks" }) }
    var totalAmountInput by remember { mutableStateOf(if (initialAmount > 0) String.format(Locale.US, "%.2f", initialAmount) else "") }
    var selectedGroupId by remember { mutableStateOf<Long?>(null) }
    var payerFriendId by remember { mutableStateOf(0L) } // 0 = You
    var splitType by remember { mutableStateOf(if (receiptItems.isNotEmpty()) SplitType.ITEMIZED else SplitType.EQUAL) }
    var linkedAccountId by remember { mutableStateOf<Long?>(accounts.firstOrNull()?.id) }

    // Selected participant friend IDs (always includes 0L for User)
    val selectedParticipants = remember { mutableStateListOf<Long>(0L) }

    // Custom exact amounts, percentages, and shares maps
    val exactAmounts = remember { mutableStateMapOf<Long, String>() }
    val percentages = remember { mutableStateMapOf<Long, String>() }
    val shares = remember { mutableStateMapOf<Long, String>() }

    // Itemized assignments: item index -> friendId
    val itemAssignments = remember { mutableStateMapOf<Int, Long>() }

    // Initialize participants if friends available
    LaunchedEffect(friends) {
        if (selectedParticipants.size == 1 && friends.isNotEmpty()) {
            selectedParticipants.addAll(friends.take(3).map { it.id })
            selectedParticipants.forEach {
                shares[it] = "1"
            }
        }
    }

    // Auto-update participants when a group is selected
    LaunchedEffect(selectedGroupId) {
        if (selectedGroupId != null) {
            val group = groups.find { it.id == selectedGroupId }
            if (group != null) {
                val memberIds = group.memberFriendIds.split(",").mapNotNull { it.trim().toLongOrNull() }
                selectedParticipants.clear()
                selectedParticipants.add(0L)
                selectedParticipants.addAll(memberIds)
                selectedParticipants.forEach { shares[it] = "1" }
            }
        }
    }

    val totalAmount = totalAmountInput.toDoubleOrNull() ?: 0.0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = customColors.bentoCardBg,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outline) }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Split a Bill",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Title & Total Amount Fields
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = billTitle,
                        onValueChange = { billTitle = it },
                        label = { Text("Expense Title") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.5f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = totalAmountInput,
                        onValueChange = { totalAmountInput = it },
                        label = { Text("Total ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        textStyle = LocalTextStyle.current.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Group Picker (Optional)
            if (groups.isNotEmpty()) {
                item {
                    Text(
                        text = "Assign to Group (Optional)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedGroupId == null,
                                onClick = { selectedGroupId = null },
                                label = { Text("No Group (One-off)") }
                            )
                        }
                        items(groups) { group ->
                            FilterChip(
                                selected = selectedGroupId == group.id,
                                onClick = { selectedGroupId = group.id },
                                label = { Text("${group.iconEmoji} ${group.name}") }
                            )
                        }
                    }
                }
            }

            // Payer Selection: Who Paid?
            item {
                Text(
                    text = "Who Paid the Bill?",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    // You
                    item {
                        FilterChip(
                            selected = payerFriendId == 0L,
                            onClick = { payerFriendId = 0L },
                            label = { Text("You (Paid)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = EmeraldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        )
                    }
                    items(friends) { friend ->
                        FilterChip(
                            selected = payerFriendId == friend.id,
                            onClick = { payerFriendId = friend.id },
                            label = { Text(friend.name) },
                            leadingIcon = {
                                FriendAvatar(name = friend.name, avatarColorHex = friend.avatarColorHex, size = 20.dp)
                            }
                        )
                    }
                }
            }

            // Split Mode Tabs (Equal, Exact, Percentage, Shares, Itemized)
            item {
                Text(
                    text = "Split Method",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(
                        SplitType.EQUAL to "=",
                        SplitType.EXACT to "$",
                        SplitType.PERCENTAGE to "%",
                        SplitType.SHARES to "1/x",
                        SplitType.ITEMIZED to "Items"
                    ).forEach { (type, label) ->
                        val isSelected = splitType == type
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) EmeraldAccent else Color.Transparent)
                                .clickable { splitType = type }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) EmeraldAccent.neoPopOnColor() else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Participant Selector and Custom Split Input
            item {
                Text(
                    text = "Select Participants & Shares",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Itemized line items split mode (if items available)
            if (splitType == SplitType.ITEMIZED && receiptItems.isNotEmpty()) {
                items(receiptItems.size) { index ->
                    val item = receiptItems[index]
                    val assignedTo = itemAssignments[index] ?: 0L
                    val assignedName = if (assignedTo == 0L) "You" else friends.find { it.id == assignedTo }?.name ?: "Friend"

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("$${String.format(Locale.US, "%.2f", item.price)}", fontSize = 12.sp, color = EmeraldAccent)
                            }

                            // Cycle participant on click
                            FilledTonalButton(
                                onClick = {
                                    val allP = listOf(0L) + friends.map { it.id }
                                    val currentIdx = allP.indexOf(assignedTo)
                                    val nextId = allP[(currentIdx + 1) % allP.size]
                                    itemAssignments[index] = nextId
                                    if (!selectedParticipants.contains(nextId)) selectedParticipants.add(nextId)
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Assign: $assignedName", fontSize = 11.sp)
                            }
                        }
                    }
                }
            } else {
                // Participant rows for Equal, Exact, Percentage, and Shares
                val allCandidateParticipants = listOf(0L) + friends.map { it.id }

                items(allCandidateParticipants.size) { idx ->
                    val pId = allCandidateParticipants[idx]
                    val isIncluded = selectedParticipants.contains(pId)
                    val pName = if (pId == 0L) "You" else friends.find { it.id == pId }?.name ?: "Friend"

                    val participantCount = selectedParticipants.size.coerceAtLeast(1)
                    val equalShare = if (isIncluded) totalAmount / participantCount else 0.0

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isIncluded) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isIncluded) EmeraldAccent.copy(alpha = 0.5f) else Color.Transparent
                        ),
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
                                Checkbox(
                                    checked = isIncluded,
                                    onCheckedChange = { checked ->
                                        if (checked) {
                                            if (!selectedParticipants.contains(pId)) selectedParticipants.add(pId)
                                            shares[pId] = "1"
                                        } else {
                                            if (selectedParticipants.size > 1) {
                                                selectedParticipants.remove(pId)
                                            }
                                        }
                                    }
                                )
                                FriendAvatar(
                                    name = pName,
                                    avatarColorHex = friends.find { it.id == pId }?.avatarColorHex ?: 0xFF00B594,
                                    size = 34.dp
                                )
                                Column {
                                    Text(text = pName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    if (pId == payerFriendId) {
                                        Text("Payer", fontSize = 10.sp, color = EmeraldAccent, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }

                            // Input based on split method
                            if (isIncluded) {
                                when (splitType) {
                                    SplitType.EQUAL -> {
                                        Text(
                                            text = "$${String.format(Locale.US, "%.2f", equalShare)}",
                                            fontFamily = FontFamily.Monospace,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = EmeraldAccent
                                        )
                                    }
                                    SplitType.EXACT -> {
                                        OutlinedTextField(
                                            value = exactAmounts[pId] ?: "",
                                            onValueChange = { exactAmounts[pId] = it },
                                            placeholder = { Text("0.00") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                            singleLine = true,
                                            modifier = Modifier.width(90.dp),
                                            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                                        )
                                    }
                                    SplitType.PERCENTAGE -> {
                                        OutlinedTextField(
                                            value = percentages[pId] ?: "",
                                            onValueChange = { percentages[pId] = it },
                                            placeholder = { Text("%") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.width(80.dp),
                                            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                                        )
                                    }
                                    SplitType.SHARES -> {
                                        OutlinedTextField(
                                            value = shares[pId] ?: "1",
                                            onValueChange = { shares[pId] = it },
                                            placeholder = { Text("Shares") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.width(75.dp),
                                            textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace, fontSize = 13.sp)
                                        )
                                    }
                                    SplitType.ITEMIZED -> {}
                                }
                            }
                        }
                    }
                }
            }

            // Linked Account (If user is payer, deduct from account)
            if (payerFriendId == 0L && accounts.isNotEmpty()) {
                item {
                    Text(
                        text = "Deduct from Account",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        items(accounts) { acc ->
                            FilterChip(
                                selected = linkedAccountId == acc.id,
                                onClick = { linkedAccountId = acc.id },
                                label = { Text(acc.name) }
                            )
                        }
                    }
                }
            }

            // Save Split Expense Button
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (totalAmount > 0 && selectedParticipants.isNotEmpty()) {
                            val splitExpense = SplitExpenseEntity(
                                title = billTitle,
                                totalAmount = totalAmount,
                                currency = "USD",
                                payerFriendId = payerFriendId,
                                groupId = selectedGroupId,
                                date = System.currentTimeMillis(),
                                splitType = splitType,
                                notes = "Split between ${selectedParticipants.size} people"
                            )

                            // Compute per-person share amounts
                            val participantEntities = mutableListOf<SplitParticipantEntity>()
                            when (splitType) {
                                SplitType.EQUAL -> {
                                    val eachShare = totalAmount / selectedParticipants.size
                                    selectedParticipants.forEach { pId ->
                                        participantEntities.add(
                                            SplitParticipantEntity(
                                                splitExpenseId = 0,
                                                friendId = pId,
                                                shareAmount = Math.round(eachShare * 100.0) / 100.0,
                                                sharePercentage = 100.0 / selectedParticipants.size
                                            )
                                        )
                                    }
                                }
                                SplitType.EXACT -> {
                                    selectedParticipants.forEach { pId ->
                                        val amt = exactAmounts[pId]?.toDoubleOrNull() ?: (totalAmount / selectedParticipants.size)
                                        participantEntities.add(
                                            SplitParticipantEntity(
                                                splitExpenseId = 0,
                                                friendId = pId,
                                                shareAmount = amt
                                            )
                                        )
                                    }
                                }
                                SplitType.PERCENTAGE -> {
                                    selectedParticipants.forEach { pId ->
                                        val pct = percentages[pId]?.toDoubleOrNull() ?: (100.0 / selectedParticipants.size)
                                        val amt = totalAmount * (pct / 100.0)
                                        participantEntities.add(
                                            SplitParticipantEntity(
                                                splitExpenseId = 0,
                                                friendId = pId,
                                                shareAmount = amt,
                                                sharePercentage = pct
                                            )
                                        )
                                    }
                                }
                                SplitType.SHARES -> {
                                    val totalShares = selectedParticipants.sumOf { shares[it]?.toDoubleOrNull() ?: 1.0 }.coerceAtLeast(1.0)
                                    selectedParticipants.forEach { pId ->
                                        val sh = shares[pId]?.toDoubleOrNull() ?: 1.0
                                        val amt = totalAmount * (sh / totalShares)
                                        participantEntities.add(
                                            SplitParticipantEntity(
                                                splitExpenseId = 0,
                                                friendId = pId,
                                                shareAmount = Math.round(amt * 100.0) / 100.0,
                                                shareUnits = sh
                                            )
                                        )
                                    }
                                }
                                SplitType.ITEMIZED -> {
                                    val groupedItems = receiptItems.mapIndexed { idx, item -> item to (itemAssignments[idx] ?: 0L) }
                                    selectedParticipants.forEach { pId ->
                                        val userItems = groupedItems.filter { it.second == pId }.map { it.first }
                                        val userSubtotal = userItems.sumOf { it.price }
                                        participantEntities.add(
                                            SplitParticipantEntity(
                                                splitExpenseId = 0,
                                                friendId = pId,
                                                shareAmount = Math.round(userSubtotal * 100.0) / 100.0
                                            )
                                        )
                                    }
                                }
                            }

                            onSaveSplit(splitExpense, participantEntities, if (payerFriendId == 0L) linkedAccountId else null)
                            onDismiss()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                    enabled = totalAmount > 0 && selectedParticipants.isNotEmpty()
                ) {
                    Text(
                        text = "Confirm & Split Bill ($${String.format(Locale.US, "%.2f", totalAmount)})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldAccent.neoPopOnColor()
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
