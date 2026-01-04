package ronsijm.templater.script.methods

import ronsijm.templater.script.evaluators.DateObject


object DateMethodExecutor {


    @Suppress("UnusedParameter")
    fun execute(date: DateObject, methodName: String, args: List<Any?> = emptyList()): Any? {
        return when (methodName) {
            "getHours" -> date.getHours()
            "getMinutes" -> date.getMinutes()
            "getSeconds" -> date.getSeconds()
            "getDate" -> date.getDate()
            "getMonth" -> date.getMonth()
            "getFullYear" -> date.getFullYear()
            "getDay" -> date.getDay()
            "toString" -> date.toString()
            else -> null
        }
    }
}

