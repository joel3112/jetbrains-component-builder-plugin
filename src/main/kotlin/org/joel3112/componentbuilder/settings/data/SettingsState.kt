package org.joel3112.componentbuilder.settings.data

import com.intellij.util.xmlb.annotations.OptionTag
import java.util.*

class SettingsState {
    @get:OptionTag("ITEMS")
    var items: MutableList<Item> = mutableListOf(Item("component", "Component"), Item("styles", "Styles"))

    fun copy(): SettingsState {
        val copy = SettingsState()
        copy.items = items
        return copy
    }

    override fun equals(other: Any?): Boolean {
        val that = other as SettingsState
        return items.toTypedArray().contentEquals(that.items.toTypedArray())
    }

    override fun hashCode(): Int {
        var result = Objects.hash(items)
        result = 31 * result + items.toTypedArray().contentHashCode()
        return result
    }

    override fun toString(): String = """
        SettingsState(
            items='${items.toTypedArray().contentToString()}'
        )
    """.trimIndent()
}
