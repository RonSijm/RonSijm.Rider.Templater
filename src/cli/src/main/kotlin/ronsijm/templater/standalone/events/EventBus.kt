package ronsijm.templater.standalone.events

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import javax.swing.SwingUtilities
import kotlin.reflect.KClass

object EventBus {

    private val subscribers = ConcurrentHashMap<KClass<*>, CopyOnWriteArrayList<EventHandler<*>>>()

    fun <T : AppEvent> subscribe(
        eventType: KClass<T>,
        invokeOnEdt: Boolean = true,
        handler: (T) -> Unit
    ): Subscription {
        val eventHandler = EventHandler(handler, invokeOnEdt)
        subscribers.getOrPut(eventType) { CopyOnWriteArrayList() }.add(eventHandler)

        return object : Subscription {
            override fun unsubscribe() {
                subscribers[eventType]?.remove(eventHandler)
            }
        }
    }

    inline fun <reified T : AppEvent> subscribe(
        invokeOnEdt: Boolean = true,
        noinline handler: (T) -> Unit
    ): Subscription = subscribe(T::class, invokeOnEdt, handler)

    fun publish(event: AppEvent) {
        val handlers = subscribers[event::class] ?: return

        for (handler in handlers) {


            @Suppress("UNCHECKED_CAST")
            val typedHandler = handler as EventHandler<AppEvent>

            if (typedHandler.invokeOnEdt && !SwingUtilities.isEventDispatchThread()) {
                SwingUtilities.invokeLater { typedHandler.handler(event) }
            } else {
                typedHandler.handler(event)
            }
        }
    }

    fun clearAll() {
        subscribers.clear()
    }

    fun <T : AppEvent> subscriberCount(eventType: KClass<T>): Int {
        return subscribers[eventType]?.size ?: 0
    }

    private class EventHandler<T : AppEvent>(
        val handler: (T) -> Unit,
        val invokeOnEdt: Boolean
    )
}

interface Subscription {
    fun unsubscribe()
}

interface AppEvent

