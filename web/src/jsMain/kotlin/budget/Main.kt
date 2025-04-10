package budget

import asynclite.async
import auth.AuthPanel
import auth.AuthorizedClient
import auth.api.v1.LookupParams
import kotlinx.browser.document
import kotlinx.browser.window
import kui.Component
import multiplatform.api.FetchClient
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

fun main() {
    if (auth.isSignedIn()) {
        async {
            val user = runCatching {
                AuthorizedClient(FetchClient()).callApi(auth.api.v1.lookup, LookupParams())
            }.getOrNull()
            if (user == null) {
                auth.signOut()
                window.location.reload()
                return@async
            }
            kui.mountComponent(document.body!!, BudgetApp)
        }
    } else {
        AuthPanel.Styles.apply()
        kui.mountComponent(document.body!!, AuthPanel())
    }
}

const val DELETE = "\u2716"

fun <T,V> Component.rendering(backing: ReadWriteProperty<T, V>): ReadWriteProperty<T, V> {
    return object : ReadWriteProperty<T, V> {
        override fun getValue(thisRef: T, property: KProperty<*>): V {
            return backing.getValue(thisRef, property)
        }

        override fun setValue(thisRef: T, property: KProperty<*>, value: V) {
            backing.setValue(thisRef, property, value)
            render()
        }
    }
}

fun <T,V> Component.rendering(backing: KMutableProperty0<V>): ReadWriteProperty<T, V> {
    return rendering(backing.asReadWriteProperty())
}

fun <T, V> BudgetService.saving(backing: ReadWriteProperty<T, V>): ReadWriteProperty<T, V> {
    return object : ReadWriteProperty<T, V> {
        override fun getValue(thisRef: T, property: KProperty<*>): V {
            return backing.getValue(thisRef, property)
        }

        override fun setValue(thisRef: T, property: KProperty<*>, value: V) {
            backing.setValue(thisRef, property, value)
            save()
        }
    }
}

fun <T, V> BudgetService.saving(backing: KMutableProperty0<V>): ReadWriteProperty<T, V> {
    return saving(backing.asReadWriteProperty())
}

fun <T, V> KMutableProperty0<V>.asReadWriteProperty(): ReadWriteProperty<T, V> {
    return object : ReadWriteProperty<T, V> {
        override fun getValue(thisRef: T, property: KProperty<*>): V {
            return this@asReadWriteProperty.getValue(thisRef, property)
        }

        override fun setValue(thisRef: T, property: KProperty<*>, value: V) {
            this@asReadWriteProperty.setValue(thisRef, property, value)
        }
    }
}
