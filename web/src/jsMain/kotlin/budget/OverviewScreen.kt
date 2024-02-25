package budget

import kotlinx.datetime.*
import kui.Component
import kui.Props
import kui.classes
import kui.componentOf
import kotlinx.datetime.Month as KtMonth

class OverviewScreen(private val service: BudgetService) : Component() {
    private val model = service.model

    private fun addMonth() {
        val modalBody = object : Component() {
            var month: KtMonth
            var year: Double

            init {
                val newMonth = model.months.lastOrNull()?.date?.plus(1, DateTimeUnit.MONTH)
                    ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                month = newMonth.month
                year = newMonth.year.toDouble()
            }

            override fun render() {
                markup().div {
                    label { +"Month" }
                    select(options = KtMonth.entries, model = ::month)
                    label { +"Year" }
                    inputNumber(model = ::year)
                }
            }
        }

        Modal.show(
            title = "Add month",
            body = modalBody,
            ok = { confirmed ->
                if (confirmed) {
                    val newMonth = Month(
                        date = LocalDate(modalBody.year.toInt(), modalBody.month, 1),
                        income = Money(0),
                        expenses = model.subscriptions.toMutableList(),
                        savedPct = model.settings.savingsPctDefault,
                        savedFlat = model.settings.savingsFlatDefault,
                    )
                    model.months.add(newMonth)
                    service.save()
                    BudgetApp.pushMonthDetails(newMonth)
                }
            }
        )
    }

    fun deleteMonth(index: Int) {
        val m = model.months[index].date
        val displayMonth = "${m.month.name.lowercase().replaceFirstChar { it.uppercaseChar() }} ${m.year}"
        Modal.show(
            title = "Delete month",
            body = componentOf { it.p { +"Are you sure you want to delete the data for $displayMonth?" } },
            danger = true,
            okText = "Delete",
            ok = { confirmed ->
                if (confirmed) {
                    model.months.removeAt(index)
                    service.save()
                }
            }
        )
    }

    private class OverviewRow(
        val month: Month,
        val monthName: String,
        val income: Money,
        val incomeMoM: Money,
        val expenses: Money,
        val saved: Money,
        val remaining: Money,
    )

    private fun generateRows(): List<OverviewRow> {
        if (model.months.isEmpty()) {
            return emptyList()
        }

        val res = mutableListOf<OverviewRow>()

        var lastMonthRemaining = 0
        var lastMonthIncome = Money(0)
        for (month in model.months) {
            val monthAbbr = month.date.month.name.substring(0, 3).lowercase().replaceFirstChar { it.uppercaseChar() }
            val monthName = "$monthAbbr ${month.date.year}"
            val income = month.income
            val incomeMoM = income - lastMonthIncome
            val expenses = month.expenses.sumOf { it.amount.cents }.let { Money(it) }
            val saved = month.savedFlat + Money(month.savedPct * income.cents / 100)
            val remaining = income - expenses - saved + Money((lastMonthRemaining * model.settings.debtMultiplier).toInt())

            res.add(OverviewRow(month, monthName, income, incomeMoM, expenses, saved, remaining))

            lastMonthRemaining = remaining.cents.coerceAtMost(0)
            lastMonthIncome = income
        }
        return res
    }

    override fun render() {
        markup().div {
            button(Props(click = { addMonth() })) { +"Add month" }
            button(Props(click = { BudgetApp.pushSubscriptions() })) { +"Subscriptions" }
            button(Props(click = { BudgetApp.pushSettings() })) { +"Settings" }
            div(classes("row", "header")) {
                div(classes("col")) {
                    +"Month"
                }
                div(classes("col", "align-right")) {
                    +"Income (MoM)"
                }
                div(classes("col", "align-right")) {
                    +"Expenses"
                }
                div(classes("col", "align-right")) {
                    +"Saved"
                }
                div(classes("col", "align-right")) {
                    +"Remaining"
                }
                div(classes("col", "buttons")) {  }
            }
            for ((i, month) in generateRows().withIndex()) {
                val rowClasses = if (i % 2 == 0) {
                    listOf("row", "clickable")
                } else {
                    listOf("row", "clickable", "odd")
                }
                div(Props(classes = rowClasses, click = { BudgetApp.pushMonthDetails(month.month) })) {
                    div(classes("col")) {
                        +month.monthName
                    }
                    div(classes("col", "align-right")) {
                        +"${month.income} (${month.incomeMoM})"
                    }
                    div(classes("col", "align-right")) {
                        +month.expenses.toString()
                    }
                    div(classes("col", "align-right")) {
                        +month.saved.toString()
                    }
                    val remainingClasses = if (month.remaining.cents < 0) {
                        classes("col", "align-right", "negative")
                    } else {
                        classes("col", "align-right")
                    }
                    div(remainingClasses) {
                        +month.remaining.toString()
                    }
                    div(classes("col", "buttons")) {
                        button(Props(classes = listOf("button-delete"), click = { deleteMonth(i) })) {
                            +DELETE
                        }
                    }
                }
            }
        }
    }
}