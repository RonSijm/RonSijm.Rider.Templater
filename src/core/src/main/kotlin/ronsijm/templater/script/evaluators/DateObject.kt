package ronsijm.templater.script.evaluators


class DateObject {
    private val dateTime = java.time.LocalDateTime.now()

    fun getHours(): Int = dateTime.hour
    fun getMinutes(): Int = dateTime.minute
    fun getSeconds(): Int = dateTime.second
    fun getDate(): Int = dateTime.dayOfMonth
    fun getMonth(): Int = dateTime.monthValue - 1
    fun getFullYear(): Int = dateTime.year
    fun getDay(): Int = dateTime.dayOfWeek.value % 7

    override fun toString(): String {
        return dateTime.format(java.time.format.DateTimeFormatter.ofPattern("EEE MMM dd yyyy HH:mm:ss"))
    }
}

