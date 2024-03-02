package org.joel3112.componentbuilder.settings.data

import java.util.*

class SettingsState {

    var name: String = "my-component"
    var items: Array<String> = arrayOf("my-component", "my-component2")

    fun copy(): SettingsState {
        val copy = SettingsState()
        copy.name = name
        copy.items = items
        return copy
    }

    override fun equals(other: Any?): Boolean {
        val that = other as SettingsState
        return name == that.name && items.contentEquals(that.items)
    }

    override fun hashCode(): Int {
        var result = Objects.hash(name)
        result = 31 * result + name.hashCode()
        return result
    }

    override fun toString(): String = """
        SettingsState(
            name='$name'
            items=${items.contentToString()}
        )
    """.trimIndent()
}
