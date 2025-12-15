package ronsijm.templater.modules.date

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Utility functions for date operations
 * Shared across all date commands
 */
internal object DateUtils {

    // Cache for DateTimeFormatter instances to improve performance
    private val formatterCache = mutableMapOf<String, DateTimeFormatter>()

    /**
     * Get or create a cached DateTimeFormatter for the given format
     */
    private fun getFormatter(format: String): DateTimeFormatter {
        val javaFormat = convertMomentFormat(format)
        return formatterCache.getOrPut(javaFormat) {
            DateTimeFormatter.ofPattern(javaFormat)
        }
    }

    /**
     * Parse a date string with the given format
     */
    fun parseDate(dateString: String, format: String): LocalDate {
        val formatter = getFormatter(format)
        return LocalDate.parse(dateString, formatter)
    }

    /**
     * Parse a datetime string with the given format
     */
    fun parseDateTime(dateTimeString: String, format: String): LocalDateTime {
        val formatter = getFormatter(format)
        return LocalDateTime.parse(dateTimeString, formatter)
    }
    
    /**
     * Apply offset to a date
     * Supports both number (days) and ISO 8601 duration format (P1Y, P1M, P1D, etc.)
     */
    fun applyOffset(date: LocalDate, offset: String): LocalDate {
        // Try to parse as number first (days)
        val days = offset.toIntOrNull()
        if (days != null) {
            return date.plusDays(days.toLong())
        }

        // Try to parse as ISO 8601 duration (simplified)
        if (offset.startsWith("P") || offset.startsWith("p")) {
            return parseIsoDuration(date, offset)
        }

        return date
    }

    /**
     * Apply offset to a datetime
     * Supports both number (days) and ISO 8601 duration format (P1Y, P1M, P1D, etc.)
     */
    fun applyDateTimeOffset(dateTime: LocalDateTime, offset: String): LocalDateTime {
        // Try to parse as number first (days)
        val days = offset.toIntOrNull()
        if (days != null) {
            return dateTime.plusDays(days.toLong())
        }

        // Try to parse as ISO 8601 duration (simplified)
        if (offset.startsWith("P") || offset.startsWith("p")) {
            return parseDateTimeIsoDuration(dateTime, offset)
        }

        return dateTime
    }
    
    /**
     * Parse ISO 8601 duration format (simplified)
     * Examples: P1Y (1 year), P1M (1 month), P1D (1 day), P-1M (minus 1 month)
     */
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

    /**
     * Parse ISO 8601 duration format for datetime (simplified)
     * Examples: P1Y (1 year), P1M (1 month), P1D (1 day), P-1M (minus 1 month)
     */
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
    
    /**
     * Convert moment.js format to Java DateTimeFormatter format
     * Comprehensive mapping of Moment.js tokens to Java DateTimeFormatter tokens
     *
     * Reference: https://momentjs.com/docs/#/displaying/format/
     *
     * Note: Replacements must be done in order from longest to shortest to avoid conflicts
     */
    fun convertMomentFormat(format: String): String {
        var result = format

        // Use numeric placeholders to avoid conflicts during replacement
        // Only replace Moment.js-specific patterns that differ from Java DateTimeFormatter
        // Replace longer patterns first, then shorter ones

        // Year - Moment uses YYYY/YY, Java uses yyyy/yy
        result = result.replace("YYYY", "{{1}}")
        result = result.replace("YY", "{{2}}")

        // Month - MMMM/MMM/MM are same in both, but M needs care
        // (These are actually the same in Java and Moment, so no replacement needed)
        // result = result.replace("MMMM", "{{3}}")
        // result = result.replace("MMM", "{{4}}")
        // result = result.replace("MM", "{{5}}")
        // result = result.replace("M", "{{6}}")

        // Day of Month - Moment uses DD/D, Java uses dd/d
        // Only replace uppercase DD/D (Moment) to lowercase dd/d (Java)
        result = result.replace("Do", "{{10}}")  // Day with ordinal (Moment only)
        result = result.replace("DD", "{{12}}")
        result = result.replace("D", "{{13}}")

        // Day of Week - Moment uses dddd/ddd/dd/d, Java uses EEEE/EEE/EE/e
        // Only replace lowercase dddd/ddd/dd/d (Moment weekday) to EEEE/EEE/EE/e (Java weekday)
        // BUT: lowercase dd/d are also valid Java patterns for day of month!
        // So we only replace them if they appear AFTER we've replaced DD/D
        // This way, if user writes "DD" it becomes "dd", and if they write "dd" it stays "dd"
        result = result.replace("dddd", "{{7}}")
        result = result.replace("ddd", "{{8}}")
        // Do NOT replace "dd" or "d" - they're ambiguous and likely Java format

        // Hour - HH/H/hh/h are same in both, no replacement needed
        // Minute - mm/m are same in both, no replacement needed
        // Second - ss/s are same in both, no replacement needed
        // Millisecond - SSS/SS/S are same in both, no replacement needed

        // AM/PM - Moment uses A/a, Java uses a (always lowercase)
        result = result.replace("A", "{{25}}")
        // lowercase 'a' is already correct for Java

        // Timezone - Moment uses Z/ZZ, Java uses XXX/XX
        result = result.replace("ZZ", "{{27}}")
        result = result.replace("Z", "{{28}}")

        // Now replace placeholders with Java format tokens
        result = result.replace("{{1}}", "yyyy")  // YYYY -> yyyy
        result = result.replace("{{2}}", "yy")    // YY -> yy
        result = result.replace("{{7}}", "EEEE")  // dddd -> EEEE (weekday full)
        result = result.replace("{{8}}", "EEE")   // ddd -> EEE (weekday short)
        result = result.replace("{{10}}", "d")    // Do -> d (ordinal not supported in Java)
        result = result.replace("{{12}}", "dd")   // DD -> dd (day of month 2 digits)
        result = result.replace("{{13}}", "d")    // D -> d (day of month 1 digit)
        result = result.replace("{{25}}", "a")    // A -> a (AM/PM)
        result = result.replace("{{28}}", "XXX")  // Z -> XXX (timezone with colon)
        result = result.replace("{{27}}", "XX")   // ZZ -> XX (timezone compact)

        return result
    }
    
    /**
     * Format date with the given pattern
     * Converts moment.js-style formats to Java DateTimeFormatter
     */
    fun formatDate(date: LocalDate, format: String): String {
        val formatter = getFormatter(format)
        return date.format(formatter)
    }

    /**
     * Format datetime with the given pattern
     * Converts moment.js-style formats to Java DateTimeFormatter
     */
    fun formatDateTime(dateTime: LocalDateTime, format: String): String {
        val formatter = getFormatter(format)
        return dateTime.format(formatter)
    }
}

