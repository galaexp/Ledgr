package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.AccountEntity
import com.example.data.model.FriendEntity
import com.example.data.model.GroupEntity
import com.example.data.model.SettlementEntity
import com.example.ui.components.FriendAvatar
import com.example.ui.theme.EmeraldAccent
import com.example.ui.theme.ExpenseRose
import com.example.ui.theme.LocalCustomColors
import com.example.ui.theme.neoPopOnColor
import java.util.Locale
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettleUpBottomSheet(
    friends: List<FriendEntity>,
    groups: List<GroupEntity>,
    accounts: List<AccountEntity>,
    initialFriendId: Long? = null,
    initialBalance: Double = 0.0,
    onDismiss: () -> Unit,
    onConfirmSettlement: (SettlementEntity, Long?) -> Unit
) {
    val customColors = LocalCustomColors.current
    var selectedFriendId by remember { mutableStateOf(initialFriendId ?: friends.firstOrNull()?.id ?: 1L) }
    var youPaidFriend by remember { mutableStateOf(initialBalance < 0) } // true if you pay friend, false if friend pays you
    var amountInput by remember { mutableStateOf(if (abs(initialBalance) > 0) String.format(Locale.US, "%.2f", abs(initialBalance)) else "") }
    var selectedMethod by remember { mutableStateOf("Bank Transfer") }
    var noteInput by remember { mutableStateOf("") }
    var linkToAccount by remember { mutableStateOf(true) }
    var selectedAccountId by remember { mutableStateOf(accounts.firstOrNull()?.id) }

    val selectedFriend = friends.find { it.id == selectedFriendId }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = customColors.bentoCardBg,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MaterialTheme.colorScheme.outline) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Record Settlement",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Select Friend
            Text(
                text = "With Friend",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(friends) { friend ->
                    val isSelected = friend.id == selectedFriendId
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFriendId = friend.id },
                        label = { Text(friend.name) },
                        leadingIcon = {
                            FriendAvatar(name = friend.name, avatarColorHex = friend.avatarColorHex, size = 20.dp)
                        }
                    )
                }
            }

            // Direction: You paid Sara OR Sara paid You
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (youPaidFriend) ExpenseRose else Color.Transparent)
                        .clickable { youPaidFriend = true }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "You paid ${selectedFriend?.name ?: "Friend"}",
                        fontSize = 12.sp,
                        fontWeight = if (youPaidFriend) FontWeight.Bold else FontWeight.Medium,
                        color = if (youPaidFriend) ExpenseRose.neoPopOnColor() else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (!youPaidFriend) EmeraldAccent else Color.Transparent)
                        .clickable { youPaidFriend = false }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${selectedFriend?.name ?: "Friend"} paid You",
                        fontSize = 12.sp,
                        fontWeight = if (!youPaidFriend) FontWeight.Bold else FontWeight.Medium,
                        color = if (!youPaidFriend) EmeraldAccent.neoPopOnColor() else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Settlement Amount
            OutlinedTextField(
                value = amountInput,
                onValueChange = { amountInput = it },
                label = { Text("Settlement Amount ($)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            )

            // Payment Method
            Text(
                text = "Payment Method",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val paymentMethods = remember { listOf("Bank Transfer", "Cash", "UPI / Venmo / Revolut", "Card") }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(paymentMethods) { method ->
                    FilterChip(
                        selected = selectedMethod == method,
                        onClick = { selectedMethod = method },
                        label = { Text(method) }
                    )
                }
            }

            // Link to Bank/Wallet Account Checkbox
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Update Wallet Balance", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(
                                if (youPaidFriend) "Deduct payment from your account" else "Deposit funds into your account",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = linkToAccount, onCheckedChange = { linkToAccount = it })
                    }

                    if (linkToAccount) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(accounts) { acc ->
                                FilterChip(
                                    selected = selectedAccountId == acc.id,
                                    onClick = { selectedAccountId = acc.id },
                                    label = { Text("${acc.name} (${acc.currency})") }
                                )
                            }
                        }
                    }
                }
            }

            // Note
            OutlinedTextField(
                value = noteInput,
                onValueChange = { noteInput = it },
                label = { Text("Note / Memo (Optional)") },
                placeholder = { Text("e.g. Cleared Goa beach trip expenses") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Submit Button
            Button(
                onClick = {
                    val amount = amountInput.toDoubleOrNull() ?: 0.0
                    if (amount > 0 && selectedFriendId > 0) {
                        val settlement = SettlementEntity(
                            fromFriendId = if (youPaidFriend) 0L else selectedFriendId,
                            toFriendId = if (youPaidFriend) selectedFriendId else 0L,
                            amount = amount,
                            currency = "USD",
                            date = System.currentTimeMillis(),
                            paymentMethod = selectedMethod,
                            note = noteInput.ifEmpty { "Settlement" }
                        )
                        onConfirmSettlement(settlement, if (linkToAccount) selectedAccountId else null)
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldAccent),
                enabled = (amountInput.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Text(
                    text = "Confirm Settlement ($${String.format(Locale.US, "%.2f", amountInput.toDoubleOrNull() ?: 0.0)})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = EmeraldAccent.neoPopOnColor()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
