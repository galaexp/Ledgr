package com.example.domain

import com.example.data.model.FriendEntity
import com.example.data.model.SettlementEntity
import com.example.data.model.SplitExpenseEntity
import com.example.data.model.SplitParticipantEntity
import kotlin.math.abs
import kotlin.math.min

data class SimplifiedDebt(
    val fromFriendId: Long, // 0 = User
    val fromName: String,
    val toFriendId: Long,   // 0 = User
    val toName: String,
    val amount: Double,
    val currency: String
)

data class FriendBalanceSummary(
    val friend: FriendEntity,
    val netBalance: Double, // >0: friend owes user, <0: user owes friend
    val totalPaidByFriend: Double,
    val totalShareOfFriend: Double
)

object DebtSimplificationEngine {

    /**
     * Calculates net balances for all friends vs the primary user (id = 0)
     * and between all friends.
     */
    fun computeFriendBalances(
        friends: List<FriendEntity>,
        splitExpenses: List<SplitExpenseEntity>,
        participants: List<SplitParticipantEntity>,
        settlements: List<SettlementEntity>
    ): List<FriendBalanceSummary> {
        val friendMap = friends.associateBy { it.id }
        val netBalanceMap = mutableMapOf<Long, Double>()
        val paidMap = mutableMapOf<Long, Double>()
        val shareMap = mutableMapOf<Long, Double>()

        friends.forEach { friend ->
            netBalanceMap[friend.id] = 0.0
            paidMap[friend.id] = 0.0
            shareMap[friend.id] = 0.0
        }

        val participantsByExpense = participants.groupBy { it.splitExpenseId }

        // Process Split Expenses
        for (expense in splitExpenses) {
            val payerId = expense.payerFriendId
            val expenseParts = participantsByExpense[expense.id] ?: emptyList()

            for (part in expenseParts) {
                if (payerId == 0L && part.friendId != 0L) {
                    // User paid for friend -> friend owes user (positive for friend in netBalanceMap)
                    netBalanceMap[part.friendId] = (netBalanceMap[part.friendId] ?: 0.0) + part.shareAmount
                    shareMap[part.friendId] = (shareMap[part.friendId] ?: 0.0) + part.shareAmount
                } else if (payerId != 0L && part.friendId == 0L) {
                    // Friend paid for user -> user owes friend (negative for friend in netBalanceMap)
                    netBalanceMap[payerId] = (netBalanceMap[payerId] ?: 0.0) - part.shareAmount
                    paidMap[payerId] = (paidMap[payerId] ?: 0.0) + part.shareAmount
                }
            }
        }

        // Process Settlements
        for (settlement in settlements) {
            if (settlement.fromFriendId != 0L && settlement.toFriendId == 0L) {
                // Friend paid user (settled friend's debt to user)
                netBalanceMap[settlement.fromFriendId] = (netBalanceMap[settlement.fromFriendId] ?: 0.0) - settlement.amount
            } else if (settlement.fromFriendId == 0L && settlement.toFriendId != 0L) {
                // User paid friend (settled user's debt to friend)
                netBalanceMap[settlement.toFriendId] = (netBalanceMap[settlement.toFriendId] ?: 0.0) + settlement.amount
            }
        }

        return friends.map { friend ->
            FriendBalanceSummary(
                friend = friend,
                netBalance = netBalanceMap[friend.id] ?: 0.0,
                totalPaidByFriend = paidMap[friend.id] ?: 0.0,
                totalShareOfFriend = shareMap[friend.id] ?: 0.0
            )
        }
    }

    /**
     * Graph-based Debt Simplification Algorithm
     * Minimizes the total number of cash transfers needed to settle all group debts.
     */
    fun simplifyGroupDebts(
        friends: List<FriendEntity>,
        splitExpenses: List<SplitExpenseEntity>,
        participants: List<SplitParticipantEntity>,
        currency: String = "USD"
    ): List<SimplifiedDebt> {
        val nameMap = mutableMapOf<Long, String>()
        nameMap[0L] = "You"
        friends.forEach { nameMap[it.id] = it.name }

        // Compute net balance for each person in the group
        val net = mutableMapOf<Long, Double>()
        net[0L] = 0.0
        friends.forEach { net[it.id] = 0.0 }

        val partsByExpense = participants.groupBy { it.splitExpenseId }

        for (expense in splitExpenses) {
            val payer = expense.payerFriendId
            val expenseParts = partsByExpense[expense.id] ?: emptyList()

            for (part in expenseParts) {
                if (part.friendId != payer) {
                    // Payer is owed money (+)
                    net[payer] = (net[payer] ?: 0.0) + part.shareAmount
                    // Participant owes money (-)
                    net[part.friendId] = (net[part.friendId] ?: 0.0) - part.shareAmount
                }
            }
        }

        // Separate debtors (net < -0.01) and creditors (net > 0.01)
        val debtors = mutableListOf<Pair<Long, Double>>()
        val creditors = mutableListOf<Pair<Long, Double>>()

        net.forEach { (id, balance) ->
            if (balance < -0.01) {
                debtors.add(id to -balance)
            } else if (balance > 0.01) {
                creditors.add(id to balance)
            }
        }

        debtors.sortByDescending { it.second }
        creditors.sortByDescending { it.second }

        val results = mutableListOf<SimplifiedDebt>()
        var dIdx = 0
        var cIdx = 0

        while (dIdx < debtors.size && cIdx < creditors.size) {
            val debtor = debtors[dIdx]
            val creditor = creditors[cIdx]

            val settleAmount = min(debtor.second, creditor.second)
            if (settleAmount > 0.01) {
                results.add(
                    SimplifiedDebt(
                        fromFriendId = debtor.first,
                        fromName = nameMap[debtor.first] ?: "Friend #${debtor.first}",
                        toFriendId = creditor.first,
                        toName = nameMap[creditor.first] ?: "Friend #${creditor.first}",
                        amount = Math.round(settleAmount * 100.0) / 100.0,
                        currency = currency
                    )
                )
            }

            debtors[dIdx] = debtor.copy(second = debtor.second - settleAmount)
            creditors[cIdx] = creditor.copy(second = creditor.second - settleAmount)

            if (debtors[dIdx].second <= 0.01) dIdx++
            if (creditors[cIdx].second <= 0.01) cIdx++
        }

        return results
    }
}
