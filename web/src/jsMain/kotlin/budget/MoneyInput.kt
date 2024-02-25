package budget

import kui.Component
import kui.Props
import kui.classes
import kotlin.reflect.KMutableProperty0

class MoneyInput(private val props: Props = Props.empty, private val model: KMutableProperty0<Money>) : Component() {
    private var value: Double
        get() = model.get().cents / 100.0
        set(value) {
            val newValue = Money(value)
            if (newValue != model.get()) {
                model.set(newValue)
                render()
            }
        }

    override fun render() {
        markup().span(props) {
            span(classes("row")) {
                +"$"
                inputNumber(classes("col"), step = 0.01, model = ::value)
            }
        }
    }
}
