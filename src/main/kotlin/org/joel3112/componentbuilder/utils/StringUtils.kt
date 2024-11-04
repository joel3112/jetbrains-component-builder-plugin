package org.joel3112.componentbuilder.utils


import VariablesResolver
import ai.grazie.utils.capitalize
import com.intellij.psi.codeStyle.NameUtil
import com.intellij.webSymbols.utils.NameCaseUtils
import org.joel3112.componentbuilder.settings.data.Variable
import java.util.*
import java.util.regex.Pattern

fun String.isQuoted(): Boolean = this.startsWith("\"") && this.endsWith("\"")
private fun String.removeQuotes(): String = this.removeSurrounding("\"")
private fun String.addQuotes(): String = "\"$this\""
private fun checkIfQuoted(str: String, function: (String) -> String): String {
    if (str.isQuoted()) {
        return str.removeQuotes().let { function(it) }.addQuotes()
    }
    return function(str)
}

private val variableRegex = """\$(\w+)\$""".toRegex()

class StringUtils {
    companion object {
        fun toLowerCase(str: String): String = str.lowercase(Locale.US)
        fun toUpperCase(str: String): String = str.uppercase(Locale.US)
        fun toCapitalize(str: String): String = checkIfQuoted(str) { it.capitalize() }
        fun toCamelCase(str: String): String = checkIfQuoted(str) { NameCaseUtils.toCamelCase(it) }
        fun toPascalCase(str: String): String = checkIfQuoted(str) { NameCaseUtils.toPascalCase(it) }
        fun toSnakeCase(str: String): String = checkIfQuoted(str) { NameCaseUtils.toSnakeCase(it) }
        fun toKebabCase(str: String): String = checkIfQuoted(str) { NameCaseUtils.toKebabCase(it) }
        fun toFirstWord(str: String): String = str.split(" ").firstOrNull() ?: ""
        fun toSpaceSeparated(str: String): String = checkIfQuoted(str) { NameUtil.splitWords(it, ' ') { it } }
        fun toSpacesToUnderscores(str: String): String = str.replace(" ", "_")
        fun toConcat(srts: List<String>): String = srts.joinToString("")
        fun toRegularExpression(args: List<String>): String =
            if (args.size != 3) "" else args[0].replace(Regex(args[1].removeQuotes()), args[2])
    }
}

lateinit var currentComponentName: String
var currentTemplateVariables: Map<String, String> = emptyMap()
fun String.replaceVariables(cname: String, variables: MutableList<Variable>): String {

    currentComponentName = cname
    currentTemplateVariables = variables.map { it.name to it.expression }.toMap()
//    val project = ProjectManager.getInstance().openProjects.firstOrNull()
//    val service = project!!.service<SettingsService>()

    var result = this
    val resolvedVariables = mutableMapOf<String, String>()

    // Resolver cada variable y almacenar su resultado en un mapa
    variables.forEach { variable ->
        val expressionToEvaluate = variable.expression
        val replacement = VariablesResolver.evaluateExpression(expressionToEvaluate, resolvedVariables)
        resolvedVariables[variable.name] = replacement
    }

    // Reemplazar las variables en la plantilla con los valores resueltos, usando el formato ${NAME}
    // Si la variable no está definida, se reemplaza con una cadena vacía
    result = variableRegex.replace(result) { matchResult ->
        val resolvedValue = resolvedVariables[matchResult.groupValues[1]]
        resolvedValue?.replace("\"", "") ?: ""
    }

    return result
}

fun String.replaceName(cname: String): String {
    if (this.isEmpty()) {
        return ""
    }
    return variableRegex.replace(this) { cname }
}

fun String.convertRegexToPath(): String {
    // Manually replace escaped slashes and dots in the regex
    val cleanedRegex = this
        .replace("\\/", "/")   // Replace escaped slashes with normal slashes
        .replace("\\.", ".")   // Replace escaped dots with normal dots

    // This pattern will match full directory names and filenames with extensions
    val pattern = Pattern.compile("[\\w-]+(?:\\.[a-z]+)?")
    val matcher = pattern.matcher(cleanedRegex)
    val pathParts = mutableListOf<String>()

    // Collect valid components from the cleaned regex string
    while (matcher.find()) {
        pathParts.add(matcher.group())  // Add directory names and filenames with extensions
    }

    // Check if the regex contains directories (slashes) or just a file
    val hasDirectory = this.contains("/")

    // Join the valid parts with slashes; if there's a directory, add the leading "/"
    return if (hasDirectory && pathParts.size > 1) {
        "/" + pathParts.joinToString("/")
    } else {
        pathParts.joinToString("/")
    }
}
