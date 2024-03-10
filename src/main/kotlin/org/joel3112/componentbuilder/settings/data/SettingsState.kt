package org.joel3112.componentbuilder.settings.data

import com.intellij.util.xmlb.annotations.OptionTag
import java.util.*

class SettingsState {
    @get:OptionTag("NAME")
    var name: String = "my-component"

    @get:OptionTag("ITEMS")
    var items: MutableList<Item> = mutableListOf(Item("Component"))

    fun copy(): SettingsState {
        val copy = SettingsState()
        copy.name = name
        copy.items = items
        return copy
    }

    override fun equals(other: Any?): Boolean {
        val that = other as SettingsState
        val toTypedArray = items.toTypedArray()
        return name == that.name && toTypedArray.contentEquals(that.items.toTypedArray())
    }

    override fun hashCode(): Int {
        var result = Objects.hash(name)
        result = 31 * result + name.hashCode() + items.toTypedArray().contentHashCode()
        return result
    }

    override fun toString(): String = """
        SettingsState(
            name='$name'
            items='${items.toTypedArray().contentToString()}'
        )
    """.trimIndent()
}
