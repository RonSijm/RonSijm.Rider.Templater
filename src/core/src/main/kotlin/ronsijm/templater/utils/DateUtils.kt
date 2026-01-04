package ronsijm.templater.utils

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


object DateUtils {


    private val formatterCache = mutableMapOf<String, DateTimeFormatter>()


    private fun getFormatter(format: String): DateTimeFormatter {
        val javaFormat = convertMomentFormat(format)
        return formatterCache.getOrPut(javaFormat) {
            DateTimeFormatter.ofPattern(javaFormat)
        }
    }


    fun parseDate(dateString: String, format: String): LocalDate {
        val formatter = getFormatter(format)
        return LocalDate.parse(dateString, formatter)
    }


    fun parseDateTime(dateTimeString: String, format: String): LocalDateTime {
        val formatter = getFormatter(format)
        return LocalDateTime.parse(dateTimeString, formatter)
    }


    fun applyOffset(date: LocalDate, offset: String): LocalDate {

        val days = offset.toIntOrNull()
        if (days != null) {
            return date.plusDays(days.toLong())
        }


        if (offset.startsWith("P") || offset.startsWith("p")) {
            return parseIsoDuration(date, offset)
        }

        return date
    }


    fun applyDateTimeOffset(dateTime: LocalDateTime, offset: String): LocalDateTime {

        val days = offset.toIntOrNull()
        if (days != null) {
            return dateTime.plusDays(days.toLong())
        }


        if (offset.startsWith("P") || offset.startsWith("p")) {
            return parseDateTimeIsoDuration(dateTime, offset)
        }

        return dateTime
    }


    private fun parseIsoDuration(date: LocalDate, duration: String): LocalDate {
        var result = date
        val pattern = Regex("""P(-?\d+)([YMD])""", RegexOption.IGNORE_CASE)
        val matches = pattern.findAll(duration)

        for (match in matches) {
            val amount = match.groupValues[1].toLongOrNull() ?: continue
            val unit = match.groupValues[2].uppercase()

            result = when (unit) {
                "Y" -> result.plusYears(amount)
                "M" -> result.plusMonths(amount)
                "D" -> result.plusDays(amount)
                else -> result
            }
        }

        return result
    }


    private fun parseDateTimeIsoDuration(dateTime: LocalDateTime, duration: String): LocalDateTime {
        var result = dateTime
        val pattern = Regex("""P(-?\d+)([YMD])""", RegexOption.IGNORE_CASE)
        val matches = pattern.findAll(duration)

        for (match in matches) {
            val amount = match.groupValues[1].toLongOrNull() ?: continue
            val unit = match.groupValues[2].uppercase()

            result = when (unit) {
                "Y" -> result.plusYears(amount)
                "M" -> result.plusMonths(amount)
                "D" -> result.plusDays(amount)
                else -> result
            }
        }

        return result
    }


    fun convertMomentFormat(format: String): String {
        var result = format






        result = result.replace("YYYY", "{{1}}")
        result = result.replace("YY", "{{2}}")


        result = result.replace("Do", "{{10}}")
        result = result.replace("DD", "{{12}}")
        result = result.replace("D", "{{13}}")


        result = result.replace("dddd", "{{7}}")
        result = result.replace("ddd", "{{8}}")


        result = result.replace("A", "{{25}}")


        result = result.replace("ZZ", "{{27}}")
        result = result.replace("Z", "{{28}}")


        result = result.replace("{{1}}", "yyyy")
        result = result.replace("{{2}}", "yy")
        result = result.replace("{{7}}", "EEEE")
        result = result.replace("{{8}}", "EEE")
        result = result.replace("{{10}}", "d")
        result = result.replace("{{12}}", "dd")
        result = result.replace("{{13}}", "d")
        result = result.replace("{{25}}", "a")
        result = result.replace("{{28}}", "XXX")
        result = result.replace("{{27}}", "XX")

        return result
    }


    fun formatDate(date: LocalDate, format: String): String {
        val formatter = getFormatter(format)
        return date.format(formatter)
    }


    fun formatDateTime(dateTime: LocalDateTime, format: String): String {
        val formatter = getFormatter(format)
        return dateTime.format(formatter)
    }
}
