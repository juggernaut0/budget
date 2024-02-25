package budget

import kui.Component
import kui.Props
import kui.classes
import kui.componentOf

class ExpenseList(private val service: BudgetService, private val model: MutableList<Expense>) : Component() {
    private fun addItem() {
        model.add(Expense("", Money(0)))
        service.save()
        render()
    }

    private fun removeItem(i: Int) {
        Modal.show(
            title = "Are you sure you want to delete this expense?",
            body = componentOf {
                it.div {
                    +"Name: ${model[i].name}"
                    br()
                    +"Amount: ${model[i].amount}"
                }
            },
            okText = "Delete",
            danger = true,
            ok = { confirmed ->
                if (confirmed) {
                    model.removeAt(i)
                    service.save()
                    render()
                }
            }
        )
    }

    override fun render() {
        markup().div {
            div(classes("row")) {
                button(Props(click = { addItem() })) { +"Add expense" }
            }
            div {
                div(classes("row", "header")) {
                    div(classes("col")) {
                        +"Name"
                    }
                    div(classes("col", "align-right")) {
                        +"Amount"
                    }
                    div(classes("col", "buttons")) { }
                }
                for ((i, expense) in model.withIndex()) {
                    val model = object {
                        var name by service.saving(expense::name)
                        var amount by service.saving(expense::amount)
                    }

                    val rowClasses = if (i % 2 == 0) {
                        classes("row")
                    } else {
                        classes("row", "odd")
                    }
                    div(rowClasses) {
                        inputText(classes("col"), model = model::name)
                        component(MoneyInput(model = model::amount))
                        div(classes("col", "buttons")) {
                            button(Props(classes = listOf("button-delete"), click = { removeItem(i) })) {
                                +DELETE
                            }
                        }
                    }
                }
            }
        }
    }
}