package budget

import asynclite.Cancellation
import asynclite.async
import asynclite.delay
import auth.AuthorizedClient
import budget.api.GetBudgetParams
import budget.api.UpdateBudgetParams
import budget.api.getBudget
import budget.api.updateBudget
import kotlinx.datetime.LocalDate
import multiplatform.api.FetchClient
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

class BudgetService {
    val model: Budget = Budget(
        mutableListOf(),
        mutableListOf(),
        Settings(0, Money(0), 1.0),
    )
    private val apiClient = AuthorizedClient(FetchClient())

    private val saveFn = run {
        var cancellation: Cancellation? = null

        {
            async {
                cancellation?.cancel()
                delay(5000) { cancellation = it }
                cancellation = null
                apiClient.callApi(updateBudget, UpdateBudgetParams("default"), model.toApiModel())
            }
        }
    }

    suspend fun init() {
        apiClient.callApi(getBudget, GetBudgetParams("default")).let {
            model.updateFromApiModel(it)
        }
        async {
            while (true) {
                delay(5*60*1000)
                save()
            }
        }
    }

    fun save() {
        saveFn()
    }

    fun <T> saving(backing: KMutableProperty0<T>): ReadWriteProperty<Any?, T> {
        return object : ReadWriteProperty<Any?, T> {
            override fun getValue(thisRef: Any?, property: KProperty<*>): T {
                return backing.get()
            }

            override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
                backing.set(value)
                save()
            }
        }
    }

    fun addNewMonth(date: LocalDate): Month {
        val newMonth = Month(
            date = date,
            income = Money(0),
            expenses = model.subscriptions.toMutableList(),
            savedPct = model.settings.savingsPctDefault,
            savedFlat = model.settings.savingsFlatDefault,
        )
        val addPos = model.months.indexOfFirst { it.date > date }
        if (addPos == -1) {
            model.months.add(newMonth)
        } else {
            model.months.add(addPos, newMonth)
        }
        save()
        return newMonth
    }
}
