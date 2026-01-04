package ronsijm.templater.parallel

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference


interface CancellationToken {

    val isCancellationRequested: Boolean


    fun throwIfCancellationRequested()


    fun onCancellation(callback: () -> Unit): CancellationRegistration


    fun link(other: CancellationToken): CancellationToken
}


interface CancellationRegistration {
    fun unregister()
}


class CancellationTokenSource : CancellationToken {
    private val _isCancelled = AtomicBoolean(false)
    private val _callbacks = mutableListOf<() -> Unit>()
    private val _reason = AtomicReference<String?>(null)

    override val isCancellationRequested: Boolean
        get() = _isCancelled.get()

    val cancellationReason: String?
        get() = _reason.get()


    fun cancel(reason: String? = null) {
        if (_isCancelled.compareAndSet(false, true)) {
            _reason.set(reason)
            synchronized(_callbacks) {
                _callbacks.forEach { it.invoke() }
                _callbacks.clear()
            }
        }
    }

    override fun throwIfCancellationRequested() {
        if (_isCancelled.get()) {
            throw TemplateCancellationException(_reason.get() ?: "Operation cancelled")
        }
    }

    override fun onCancellation(callback: () -> Unit): CancellationRegistration {
        synchronized(_callbacks) {
            if (_isCancelled.get()) {
                callback()
                return NoOpRegistration
            }
            _callbacks.add(callback)
            return object : CancellationRegistration {
                override fun unregister() {
                    synchronized(_callbacks) {
                        _callbacks.remove(callback)
                    }
                }
            }
        }
    }

    override fun link(other: CancellationToken): CancellationToken {
        return LinkedCancellationToken(this, other)
    }


    val token: CancellationToken get() = this
}


object NoneCancellationToken : CancellationToken {
    override val isCancellationRequested: Boolean = false
    override fun throwIfCancellationRequested() {}
    override fun onCancellation(callback: () -> Unit): CancellationRegistration = NoOpRegistration
    override fun link(other: CancellationToken): CancellationToken = other
}

private object NoOpRegistration : CancellationRegistration {
    override fun unregister() {}
}


private class LinkedCancellationToken(
    private val first: CancellationToken,
    private val second: CancellationToken
) : CancellationToken {
    override val isCancellationRequested: Boolean
        get() = first.isCancellationRequested || second.isCancellationRequested

    override fun throwIfCancellationRequested() {
        first.throwIfCancellationRequested()
        second.throwIfCancellationRequested()
    }

    override fun onCancellation(callback: () -> Unit): CancellationRegistration {
        val reg1 = first.onCancellation(callback)
        val reg2 = second.onCancellation(callback)
        return object : CancellationRegistration {
            override fun unregister() {
                reg1.unregister()
                reg2.unregister()
            }
        }
    }

    override fun link(other: CancellationToken): CancellationToken {
        return LinkedCancellationToken(this, other)
    }
}


class TemplateCancellationException(
    message: String,
    cause: Throwable? = null
) : CancellationException(message) {
    init {
        if (cause != null) initCause(cause)
    }
}


suspend fun CancellationToken.ensureActive() {
    currentCoroutineContext()[Job]?.ensureActive()
    throwIfCancellationRequested()
}

