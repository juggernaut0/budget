package budget

import kui.Component

class MonthDetailsScreen(private val service: BudgetService, private val month: Month) : Component() {
    override fun render() {
        markup().div {
            component(SubScreenHeader("${month.date.month.name.lowercase().replaceFirstChar { it.uppercaseChar() }} ${month.date.year}"))
            component(IncomeSavingsEditor(service, month))
            component(ExpenseList(service, month.expenses))
        }
    }
}
