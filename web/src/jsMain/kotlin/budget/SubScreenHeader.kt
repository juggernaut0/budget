package budget

import kui.Component
import kui.Props
import kui.classes

class SubScreenHeader(private val text: String) : Component() {
    override fun render() {
        markup().div(classes("row")) {
            button(Props(click = { BudgetApp.pushOverview() })) { +"Back" }
            h3(classes("col")) {
                +text
            }
        }
    }
}