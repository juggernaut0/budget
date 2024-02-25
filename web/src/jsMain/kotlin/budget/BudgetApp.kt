package budget

import asynclite.async
import kui.Component
import kui.componentOf
import kotlinx.browser.window

object BudgetApp : Component() {
    private val emptyComponent = componentOf { it.div {  } }
    private var current: Component = emptyComponent

    val budgetService = BudgetService()

    init {
        /*current = componentFromRoute(window.location.hash)
        window.onpopstate = { e ->
            current = componentFromRoute(window.location.hash)
            render()
        }*/
        async {
            budgetService.init()
            pushOverview()
        }
    }

    fun pushOverview() {
        //pushState("#/")
        current = OverviewScreen(budgetService)
        render()
    }

    fun pushSubscriptions() {
        //pushState("#/subscriptions")
        current = SubscriptionsScreen(budgetService)
        render()
    }

    fun pushSettings() {
        //pushState("#/settings")
        current = SettingsScreen(budgetService)
        render()
    }

    fun pushMonthDetails(month: Month) {
        current = MonthDetailsScreen(budgetService, month)
        render()
    }

    /*private fun parseUrlParams(fragment: String): List<String> {
        return fragment
            .trimStart('#', '/')
            .split('/')
            .filter { it.isNotEmpty() }
            .map { decodeURIComponent(it) }
    }

    private fun pushState(url: String) {
        window.history.pushState(null, "", url)
        current = componentFromRoute(url)
        render()
    }*/

    override fun render() {
        markup().component(current)
    }
}

external fun decodeURIComponent(s: String): String
