package budget

import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.datetime.*
import kui.Component
import kui.Props
import kui.classes
import kui.componentOf
import multiplatform.api.safeJson
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import org.w3c.files.FileReader
import org.w3c.files.get
import kotlinx.datetime.Month as KtMonth

class OverviewScreen(private val service: BudgetService) : Component() {
    private val model = service.model

    private val collapseState: MutableSet<Int> = run {
        val stored = window.localStorage.getItem(COLLAPSE_STORAGE_KEY)
        stored?.split(",")?.mapNotNull { it.toIntOrNull() }?.toMutableSet() ?: mutableSetOf()
    }

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
                    val date = LocalDate(modalBody.year.toInt(), modalBody.month, 1)
                    val newMonth = service.addNewMonth(date)
                    BudgetApp.pushMonthDetails(newMonth)
                }
            }
        )
    }

    private fun deleteMonth(month: Month) {
        val m = month.date
        val displayMonth = "${m.month.name.lowercase().replaceFirstChar { it.uppercaseChar() }} ${m.year}"
        Modal.show(
            title = "Delete month",
            body = componentOf { it.p { +"Are you sure you want to delete the data for $displayMonth?" } },
            danger = true,
            okText = "Delete",
            ok = { confirmed ->
                if (confirmed) {
                    model.months.remove(month)
                    service.save()
                    render()
                }
            }
        )
    }

    private fun exportData() {
        val json = safeJson.encodeToString(budget.api.Budget.serializer(), model.toApiModel())
        val blob = Blob(arrayOf(json), BlobPropertyBag(type = "application/json"))
        val url = URL.createObjectURL(blob)
        val a = document.createElement("a") as HTMLAnchorElement
        a.href = url
        a.download = "budget.json"
        a.click()
    }

    private fun importData() {
        val input = document.createElement("input") as HTMLInputElement
        input.type = "file"
        input.accept = "application/json"
        input.onchange = {
            val file = input.files?.get(0)
            if (file != null) {
                val reader = FileReader()
                reader.onload = {
                    val json = reader.result as String
                    val newModel = safeJson.decodeFromString(budget.api.Budget.serializer(), json)
                    model.updateFromApiModel(newModel)
                    service.save()
                    render()
                }
                reader.readAsText(file)
            }
        }
        input.click()
    }

    private fun collapseYear(year: Int) {
        if (year in collapseState) {
            collapseState.remove(year)
        } else {
            collapseState.add(year)
        }
        window.localStorage.setItem(COLLAPSE_STORAGE_KEY, collapseState.joinToString(","))
        render()
    }

    private class OverviewYear(
        val year: Int,
        val collapsed: Boolean,
        val rows: List<OverviewRow>,
    )

    private class OverviewRow(
        val month: Month,
        val monthName: String,
        val income: Money,
        val incomeMoM: Money,
        val expenses: Money,
        val saved: Money,
        val savingsToDate: Money,
        val remaining: Money,
    )

    private fun generateRows(): List<OverviewYear> {
        if (model.months.isEmpty()) {
            return emptyList()
        }

        val byYear = model.months.groupBy { it.date.year }

        val res = mutableListOf<OverviewYear>()
        var lastMonthRemaining = 0
        var lastMonthIncome = Money(0)
        var totalSavings = Money(0)

        for ((year, months) in byYear) {
            val rows = mutableListOf<OverviewRow>()

            for (month in months) {
                val monthAbbr = month.date.month.name.substring(0, 3).lowercase().replaceFirstChar { it.uppercaseChar() }
                val monthName = "$monthAbbr ${month.date.year}"
                val income = month.income
                val incomeMoM = income - lastMonthIncome
                val expenses = month.expenses.sumOf { it.amount.cents }.let { Money(it) }
                val saved = month.savedFlat + Money(month.savedPct * income.cents / 100)
                totalSavings += saved
                val remaining = income - expenses - saved + Money((lastMonthRemaining * model.settings.debtMultiplier).toInt())

                rows.add(OverviewRow(month, monthName, income, incomeMoM, expenses, saved, totalSavings, remaining))

                lastMonthRemaining = remaining.cents.coerceAtMost(0)
                lastMonthIncome = income
            }

            res.add(OverviewYear(year, collapsed = collapseState.contains(year), rows))
        }
        return res
    }

    override fun render() {
        markup().div {
            button(Props(click = { addMonth() })) { +"Add month" }
            button(Props(click = { BudgetApp.pushSubscriptions() })) { +"Subscriptions" }
            button(Props(click = { BudgetApp.pushSettings() })) { +"Settings" }
            button(Props(click = { exportData() })) { +"Export data" }
            button(Props(click = { importData() })) { +"Import data" }
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
                    +"Saved (to date)"
                }
                div(classes("col", "align-right")) {
                    +"Remaining"
                }
                div(classes("col", "buttons")) {  }
            }
            for (year in generateRows()) {
                div(Props(classes = listOf("row", "clickable", "year"), click = { collapseYear(year.year) })) {
                    +"${year.year}"
                }
                if (year.collapsed) continue
                for ((i, month) in year.rows.withIndex()) {
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
                            val savingsClasses = if (month.savingsToDate.cents < 0) {
                                classes("negative")
                            } else {
                                Props.empty
                            }
                            +"${month.saved} ("
                            span(savingsClasses) { +month.savingsToDate.toString() }
                            +")"
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
                            button(Props(classes = listOf("button-delete"), click = { deleteMonth(month.month) })) {
                                +DELETE
                            }
                        }
                    }
                }
            }
        }
    }

    private companion object {
        private const val COLLAPSE_STORAGE_KEY = "budget-overview-collapse-state"
    }
}
