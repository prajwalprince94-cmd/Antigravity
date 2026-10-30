package com.prajwalprince94.expensetracker.data.analytics

import com.prajwalprince94.expensetracker.data.model.DebtTransfer
import com.prajwalprince94.expensetracker.data.model.GroupBalanceSummary
import com.prajwalprince94.expensetracker.data.model.SplitBillItem
import com.prajwalprince94.expensetracker.data.model.SplitGroup
import kotlin.math.abs
import kotlin.math.min

/**
 * High-performance settlement engine for group bill splitting.
 * Implements greedy debt minimization to reduce total transfer transactions.
 */
object SplitDebtCalculator {

    const val CURRENT_USER = "You"

    /**
     * Calculates the balance summary and simplified debt transfers for a specific group.
     */
    fun calculateGroupSummary(group: SplitGroup, bills: List<SplitBillItem>): GroupBalanceSummary {
        val groupBills = bills.filter { it.groupId == group.id && !it.isSettled }
        val allMembers = group.members.toSet()

        val netBalances = mutableMapOf<String, Long>()
        allMembers.forEach { netBalances[it] = 0L }

        var totalSpent = 0L

        for (bill in groupBills) {
            totalSpent += bill.totalAmountPaise

            // Credit the payer
            val payer = bill.paidBy
            netBalances[payer] = (netBalances[payer] ?: 0L) + bill.totalAmountPaise

            // Debit each member their share
            bill.memberShares.forEach { (member, share) ->
                netBalances[member] = (netBalances[member] ?: 0L) - share
            }
        }

        val simplifiedDebts = simplifyDebts(netBalances)

        // Calculate specific amounts for CURRENT_USER ("You")
        var youAreOwed = 0L
        var youOwe = 0L

        for (debt in simplifiedDebts) {
            if (debt.toMember == CURRENT_USER) {
                youAreOwed += debt.amountPaise
            } else if (debt.fromMember == CURRENT_USER) {
                youOwe += debt.amountPaise
            }
        }

        val userNetBalance = (netBalances[CURRENT_USER] ?: 0L)

        return GroupBalanceSummary(
            totalSpentPaise = totalSpent,
            youAreOwedPaise = youAreOwed,
            youOwePaise = youOwe,
            netBalancePaise = userNetBalance,
            memberBalances = netBalances,
            simplifiedDebts = simplifiedDebts
        )
    }

    /**
     * Aggregates balances across all active groups.
     */
    fun calculateAllGroupsSummary(groups: List<SplitGroup>, bills: List<SplitBillItem>): GroupBalanceSummary {
        var totalSpent = 0L
        var totalYouAreOwed = 0L
        var totalYouOwe = 0L
        val allSimplifiedDebts = mutableListOf<DebtTransfer>()
        val combinedBalances = mutableMapOf<String, Long>()

        for (group in groups) {
            val summary = calculateGroupSummary(group, bills)
            totalSpent += summary.totalSpentPaise
            totalYouAreOwed += summary.youAreOwedPaise
            totalYouOwe += summary.youOwePaise
            allSimplifiedDebts.addAll(summary.simplifiedDebts)

            summary.memberBalances.forEach { (member, bal) ->
                combinedBalances[member] = (combinedBalances[member] ?: 0L) + bal
            }
        }

        return GroupBalanceSummary(
            totalSpentPaise = totalSpent,
            youAreOwedPaise = totalYouAreOwed,
            youOwePaise = totalYouOwe,
            netBalancePaise = totalYouAreOwed - totalYouOwe,
            memberBalances = combinedBalances,
            simplifiedDebts = allSimplifiedDebts
        )
    }

    /**
     * Greedy algorithm to minimize debt transfers among group members.
     * Takes net balances (credits positive, debits negative) and resolves them with minimum steps.
     */
    fun simplifyDebts(balances: Map<String, Long>): List<DebtTransfer> {
        val debtors = mutableListOf<Pair<String, Long>>()
        val creditors = mutableListOf<Pair<String, Long>>()

        balances.forEach { (person, balance) ->
            if (balance < -50L) { // Ignore minor fractional paise (<0.50 rupees)
                debtors.add(person to abs(balance))
            } else if (balance > 50L) {
                creditors.add(person to balance)
            }
        }

        debtors.sortByDescending { it.second }
        creditors.sortByDescending { it.second }

        val transfers = mutableListOf<DebtTransfer>()

        var dIdx = 0
        var cIdx = 0

        while (dIdx < debtors.size && cIdx < creditors.size) {
            val (debtor, debt) = debtors[dIdx]
            val (creditor, credit) = creditors[cIdx]

            val settledAmount = min(debt, credit)
            if (settledAmount > 0L) {
                transfers.add(
                    DebtTransfer(
                        fromMember = debtor,
                        toMember = creditor,
                        amountPaise = settledAmount
                    )
                )
            }

            val remainingDebt = debt - settledAmount
            val remainingCredit = credit - settledAmount

            if (remainingDebt <= 50L) {
                dIdx++
            } else {
                debtors[dIdx] = debtor to remainingDebt
            }

            if (remainingCredit <= 50L) {
                cIdx++
            } else {
                creditors[cIdx] = creditor to remainingCredit
            }
        }

        return transfers
    }
}
