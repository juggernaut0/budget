package budget

import kui.Component

class SubscriptionsScreen(private val service: BudgetService) : Component() {
    override fun render() {
        markup().div {
            component(SubScreenHeader("Subscriptions"))
            component(ExpenseList(service, service.model.subscriptions))
        }
    }
}
