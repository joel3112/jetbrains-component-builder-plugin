package org.joel3112.componentbuilder.utils

class StringUtils {
    companion object {
        private val regexSpecialChars = "[^a-zA-Z0-9-]".toRegex()

        private fun removeSpecialChars(value: String): String {
            return value.replace(regexSpecialChars, "")
        }

        fun toPascaleCase(value: String): String {
            val words = value.split("-")
            val result = StringBuilder()
            for (word in words) {
                val wordWithoutSpecialChars = removeSpecialChars(word)
                result.append(wordWithoutSpecialChars.substring(0, 1).uppercase())
                result.append(wordWithoutSpecialChars.substring(1))
            }
            return result.toString()
        }

        fun toKebabCase(value: String): String {
            val words = value.split("(?=[A-Z])".toRegex())
            val result = StringBuilder()
            for (word in words) {
                result.append(word.lowercase())
                result.append("-")
            }
            return result.toString().removeSuffix("-").removePrefix("-")
        }


        fun haveSpecialChars(value: String): Boolean {
            return regexSpecialChars.containsMatchIn(value)
        }

        fun replaceVariables(content: String, value: String): String {
            if (content.isEmpty()) {
                return value
            }
            return content.replace("${"$"}cname${"$"}", value)
        }
    }
}
