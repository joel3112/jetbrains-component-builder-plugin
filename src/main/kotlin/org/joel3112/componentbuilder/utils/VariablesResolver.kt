import org.joel3112.componentbuilder.settings.data.Variable
import org.joel3112.componentbuilder.utils.StringUtils
import org.joel3112.componentbuilder.utils.currentComponentName
import org.joel3112.componentbuilder.utils.currentTemplateVariables
import org.joel3112.componentbuilder.utils.isQuoted


class VariablesResolver {

    companion object {
        private const val EMPTY_RESULT = ""
        val defaultVariable = Variable("NAME", "fileNameWithoutExtension()", true)

        private val expressionFunctionMap: Map<String, (Any) -> String> = mapOf(
            "fileNameWithoutExtension()" to { "\"$currentComponentName\"" },
            "lowercase(String)" to { arg -> StringUtils.toLowerCase(arg as String) },
            "uppercase(String)" to { arg -> StringUtils.toUpperCase(arg as String) },
            "capitalize(String)" to { arg -> StringUtils.toCapitalize(arg as String) },
            "camelCase(String)" to { arg -> StringUtils.toCamelCase(arg as String) },
            "pascalCase(String)" to { arg -> StringUtils.toPascalCase(arg as String) },
            "snakeCase(String)" to { arg -> StringUtils.toSnakeCase(arg as String) },
            "kebabCase(String)" to { arg -> StringUtils.toKebabCase(arg as String) },
            "firstWord(String)" to { arg -> StringUtils.toFirstWord(arg as String) },
            "spaceSeparated(String)" to { arg -> StringUtils.toSpaceSeparated(arg as String) },
            "spacesToUnderscores(String)" to { arg -> StringUtils.toSpacesToUnderscores(arg as String) },
            "concat(expressions...)" to { args -> StringUtils.toConcat(args as List<String>) },
            "regularExpression(String, Pattern, Replacement)" to { args -> StringUtils.toRegularExpression(args as List<String>) },
        )
        var expressionList: List<String> = expressionFunctionMap.keys.sorted()

        // Función para evaluar expresiones, incluidas funciones y variables anidadas
        fun evaluateExpression(expression: String, variables: MutableMap<String, String>): String {
            val regex = Regex("(\\w+)\\(([^()]*)\\)")
            var parsedExpression = expression

            if (expression.isEmpty()) {
                return EMPTY_RESULT
            }
            if (expression.isQuoted()) {
                return parsedExpression
            }

            variables.forEach { (key, value) ->
                parsedExpression = parsedExpression.replace(key, value)
            }
            if (!regex.containsMatchIn(parsedExpression)) {
                if (variables[parsedExpression] == null) {
                    // Resolver si en la expression tiene una variable que aun no se ha evaluado
                    val expressionFromParsedExpression = currentTemplateVariables[parsedExpression]
                    if (expressionFromParsedExpression != null) {
                        return evaluateExpression(expressionFromParsedExpression, variables)
                    }
                }
                return EMPTY_RESULT
            }

            // Bucle para coincidir y reemplazar llamadas a funciones con los resultados evaluados
            while (regex.containsMatchIn(parsedExpression)) {
                val matchResult = regex.find(parsedExpression)

                if (matchResult != null) {
                    val functionName = matchResult.groupValues[1]
                    val argumentExpression = matchResult.groupValues[2]

                    // Separar los múltiples argumentos por coma y eliminar las comillas si están presentes
                    val arguments = argumentExpression.split(",").map { it.trim() }
                    // Evaluar cada argumento de manera recursiva (esto permite evaluar expresiones como uppercase(NAME))
                    val evaluatedArguments = arguments.map { evaluateExpression(it, variables) }

                    // Nueva lógica para encontrar la función exacta en el functionMap utilizando regex
                    val funcKeyRegex = Regex("$functionName\\(([^)]*)\\)")
                    val func = expressionFunctionMap.filterKeys { funcKeyRegex.matches(it) }.values.firstOrNull()

                    var result = EMPTY_RESULT
                    try {
                        if (func != null) {
                            // Si la función espera una lista de argumentos o una cadena
                            result = if (evaluatedArguments.size == 1) {
                                func.invoke(evaluatedArguments[0])
                            } else {
                                func.invoke(evaluatedArguments)
                            }
                        }
                    } catch (e: Exception) {
                        result = EMPTY_RESULT
                    }
                    parsedExpression = parsedExpression.replace(matchResult.value, result)
                }
            }

            return parsedExpression
        }
    }
}
