package budget

import kotlinx.datetime.LocalDate
import kotlin.math.absoluteValue
import budget.api.Budget as ApiBudget
import budget.api.Expense as ApiExpense
import budget.api.Month as ApiMonth

data class Budget(
    val months: MutableList<Month>,
    val subscriptions: MutableList<Expense>,
    val settings: Settings,
) {
    fun toApiModel(): ApiBudget {
        return ApiBudget(
            months = months.map { it.toApiModel() },
            subscriptions = subscriptions.map { it.toApiModel() },
            settings = settings.toApiModel(),
        )
    }

    fun updateFromApiModel(model: ApiBudget) {
        months.clear()
        months.addAll(model.months.map { it.toMutableModel() })
        months.sortBy { it.date }
        subscriptions.clear()
        subscriptions.addAll(model.subscriptions.map { it.toMutableModel() })
        settings.savingsPctDefault = model.settings.savingsPctDefault
        settings.savingsFlatDefault = Money(model.settings.savingsFlatDefault)
        settings.debtMultiplier = model.settings.debtMultiplier
    }
}

private fun ApiMonth.toMutableModel(): Month {
    return Month(
        date = date,
        income = Money(income),
        expenses = expenses.mapTo(mutableListOf()) { it.toMutableModel() },
        savedPct = savedPct,
        savedFlat = Money(savedFlat),
    )
}

private fun ApiExpense.toMutableModel(): Expense {
    return Expense(
        name = name,
        amount = Money(amount),
    )
}

value class Money(val cents: Int) {
    constructor(dollars: Double) : this((dollars * 100).toInt())

    override fun toString(): String {
        val absCents = cents.absoluteValue
        val str = "$${absCents / 100}.${(absCents % 100).toString().padStart(2, '0')}"
        return if (cents < 0) {
            "-$str"
        } else {
            str
        }
    }

    operator fun plus(other: Money) = Money(cents + other.cents)
    operator fun minus(other: Money) = Money(cents - other.cents)
}

data class Month(
    var date: LocalDate,
    var income: Money,
    val expenses: MutableList<Expense>,
    var savedPct: Int,
    var savedFlat: Money,
) {
    fun toApiModel(): ApiMonth {
        return ApiMonth(
            date = date,
            income = income.cents,
            expenses = expenses.map { it.toApiModel() },
            savedPct = savedPct,
            savedFlat = savedFlat.cents,
        )
    }
}

data class Expense(
    var name: String,
    var amount: Money,
) {
    fun toApiModel(): ApiExpense {
        return ApiExpense(
            name = name,
            amount = amount.cents,
        )
    }
}

data class Settings(
    var savingsPctDefault: Int,
    var savingsFlatDefault: Money,
    var debtMultiplier: Double,
) {
    fun toApiModel(): budget.api.Settings {
        return budget.api.Settings(
            savingsPctDefault = savingsPctDefault,
            savingsFlatDefault = savingsFlatDefault.cents,
            debtMultiplier = debtMultiplier,
        )
    }
}
