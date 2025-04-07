package budget

import kui.Component
import kui.Props
import kui.classes
import kui.componentOf

class IncomeSavingsEditor(private val service: BudgetService, private val month: Month) : Component() {
    private var savedPct: Double
        get() = month.savedPct.toDouble()
        set(value) {
            month.savedPct = value.toInt()
            service.save()
            render()
        }
    private var savedFlat: Money
        get() = month.savedFlat
        set(value) {
            month.savedFlat = value
            service.save()
            render()
        }

    private fun addIncome() {
        month.incomes.add(Income("", Money(0)))
        service.save()
        render()
    }

    private fun removeIncome(i: Int) {
        Modal.show(
            title = "Are you sure you want to delete this income source?",
            body = componentOf {
                it.div {
                    +"Description: ${month.incomes[i].description}"
                    br()
                    +"Amount: ${month.incomes[i].amount}"
                }
            },
            okText = "Delete",
            danger = true,
            ok = { confirmed ->
                if (confirmed) {
                    month.incomes.removeAt(i)
                    service.save()
                    render()
                }
            }
        )
    }

    private fun createIncomeModel(income: Income) = object {
        var description: String
            get() = income.description
            set(value) {
                income.description = value
                service.save()
                render()
            }
        var amount: Money
            get() = income.amount
            set(value) {
                income.amount = value
                service.save()
                render()
            }
    }

    override fun render() {
        markup().div {
            div(classes("row")) {
                button(Props(click = { addIncome() })) { +"Add income source" }
            }
            div {
                div(classes("row", "header")) {
                    div(classes("col")) {
                        +"Description"
                    }
                    div(classes("col", "align-right")) {
                        +"Amount"
                    }
                    div(classes("col", "buttons")) { }
                }
                for ((i, income) in month.incomes.withIndex()) {
                    val model = createIncomeModel(income)
                    val rowClasses = if (i % 2 == 0) classes("row") else classes("row", "odd")
                    div(rowClasses) {
                        inputText(classes("col"), model = model::description)
                        component(MoneyInput(model = model::amount))
                        div(classes("col", "buttons")) {
                            button(Props(classes = listOf("button-delete"), click = { removeIncome(i) })) {
                                +DELETE
                            }
                        }
                    }
                }
            }
            div(classes("row", "header", "margin-bottom")) {
                div(classes("col")) {
                    +"Total Income"
                }
                div(classes("col", "align-right")) {
                    +month.totalIncome.toString()
                }
                div(classes("col", "buttons")) { }
            }
            div(classes("row", "margin-bottom")) {
                span { +"Savings" }
                label(classes("col")) {
                    +"%"
                    inputNumber(classes("col"), model = ::savedPct)
                }
                span { +"+" }
                component(MoneyInput(classes("col"), model = ::savedFlat))
                span { +"=" }
                span { +month.totalSaved.toString() }
            }
        }
    }
}