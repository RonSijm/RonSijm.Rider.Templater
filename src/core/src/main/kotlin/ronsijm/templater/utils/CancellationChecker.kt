package ronsijm.templater.utils


interface CancellationChecker {

    fun checkCancelled()


    fun isCancelled(): Boolean
}


class CancellationException(message: String = "Operation was cancelled") : RuntimeException(message)


object NoCancellationChecker : CancellationChecker {
    override fun checkCancelled() {

    }

    override fun isCancelled(): Boolean = false
}
