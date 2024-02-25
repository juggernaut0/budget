package budget

import kui.Component
import kui.Props

class SettingsScreen(private val service: BudgetService) : Component() {
    private val settings = service.model.settings

    private var savingsPctDefault: Double
        get() = settings.savingsPctDefault.toDouble()
        set(value) {
            settings.savingsPctDefault = value.toInt()
            service.save()
        }
    private var savingsFlatDefault by service.saving(settings::savingsFlatDefault)
    private var debtMultiplier by service.saving(settings::debtMultiplier)

    override fun render() {
        markup().div {
            component(SubScreenHeader("Settings"))
            label(Props(title = "Savings percentage to set when creating a new month")) {
                +"Default savings percentage"
                inputNumber(model = ::savingsPctDefault)
            }
            label(Props(title = "Savings flat amount to set when creating a new month")) {
                +"Default savings flat"
                component(MoneyInput(model = ::savingsFlatDefault))
            }
            label(Props(title = "When rolling over a deficit from a previous month, multiply it by this factor before subtracting from available")) {
                +"Debt multiplier"
                inputNumber(model = ::debtMultiplier)
            }
        }
    }
}
