package budget

import kui.Component
import kui.classes

class IncomeSavingsEditor(private val service: BudgetService, private val month: Month) : Component() {
    private var income: Money
        get() = month.income
        set(value) {
            month.income = value
            service.save()
            render()
        }
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

    override fun render() {
        markup().div(classes("row")) {
            label(classes("col")) {
                +"Income"
                component(MoneyInput(classes("col"), model = ::income))
            }
            span { +"Savings" }
            label(classes("col")) {
                +"%"
                inputNumber(classes("col"), model = ::savedPct)
            }
            span { +"+" }
            component(MoneyInput(classes("col"), model = ::savedFlat))
            span { +"=" }
            span {
                val saved = Money(month.savedFlat.cents + month.savedPct * month.income.cents / 100)
                +"$saved"
            }
        }
    }
}