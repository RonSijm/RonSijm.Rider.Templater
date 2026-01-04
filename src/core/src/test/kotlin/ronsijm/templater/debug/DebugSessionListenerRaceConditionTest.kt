package ronsijm.templater.debug

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference


class DebugSessionListenerRaceConditionTest {


    class MockActiveDebugSession {
        private val stateListeners = CopyOnWriteArrayList<MockSessionStateListener>()

        @Volatile
        var isPaused: Boolean = false
            private set

        fun addStateListener(listener: MockSessionStateListener) {
            stateListeners.add(listener)
        }


        fun onBreakpointHit(breakpointInfo: String): String {
            isPaused = true
            stateListeners.forEach { it.onPaused(breakpointInfo) }
            return "continue"
        }
    }

    interface MockSessionStateListener {
        fun onPaused(breakpointInfo: String)
    }


    class MockDebugService {
        private val listeners = CopyOnWriteArrayList<MockDebugEventListener>()

        fun addListener(listener: MockDebugEventListener) {
            listeners.add(listener)
        }

        fun startSession(): MockActiveDebugSession {
            val session = MockActiveDebugSession()
            listeners.forEach { it.onSessionStarted(session) }
            return session
        }
    }

    interface MockDebugEventListener {
        fun onSessionStarted(session: MockActiveDebugSession)
    }


    class MockInvokeLater {
        private val executor = Executors.newSingleThreadExecutor()
        private val pendingTasks = CopyOnWriteArrayList<Runnable>()

        fun invokeLater(task: Runnable) {
            pendingTasks.add(task)
            executor.submit {

                Thread.sleep(10)
                task.run()
            }
        }

        fun shutdown() {
            executor.shutdown()
            executor.awaitTermination(5, TimeUnit.SECONDS)
        }
    }

    @Test
    fun `race condition - listener registered via invokeLater misses early breakpoint`() {

        val debugService = MockDebugService()
        val invokeLater = MockInvokeLater()
        val receivedPauses = CopyOnWriteArrayList<String>()
        val breakpointHitLatch = CountDownLatch(1)


        debugService.addListener(object : MockDebugEventListener {
            override fun onSessionStarted(session: MockActiveDebugSession) {

                invokeLater.invokeLater {
                    session.addStateListener(object : MockSessionStateListener {
                        override fun onPaused(breakpointInfo: String) {
                            receivedPauses.add(breakpointInfo)
                        }
                    })
                }
            }
        })


        val session = debugService.startSession()



        session.onBreakpointHit("first breakpoint")
        breakpointHitLatch.countDown()


        Thread.sleep(50)
        invokeLater.shutdown()


        assertTrue(receivedPauses.isEmpty(),
            "With the buggy implementation, the listener misses the first breakpoint")
    }

    @Test
    fun `fix - listener registered synchronously catches all breakpoints`() {

        val debugService = MockDebugService()
        val invokeLater = MockInvokeLater()
        val receivedPauses = CopyOnWriteArrayList<String>()


        debugService.addListener(object : MockDebugEventListener {
            override fun onSessionStarted(session: MockActiveDebugSession) {

                session.addStateListener(object : MockSessionStateListener {
                    override fun onPaused(breakpointInfo: String) {

                        invokeLater.invokeLater {
                            receivedPauses.add(breakpointInfo)
                        }
                    }
                })
            }
        })


        val session = debugService.startSession()


        session.onBreakpointHit("first breakpoint")


        Thread.sleep(50)
        invokeLater.shutdown()


        assertEquals(1, receivedPauses.size,
            "With the fixed implementation, the listener catches the first breakpoint")
        assertEquals("first breakpoint", receivedPauses[0])
    }

    @Test
    fun `concurrent scenario - multiple rapid breakpoints with sync listener`() {
        val debugService = MockDebugService()
        val receivedPauses = CopyOnWriteArrayList<String>()
        val allBreakpointsHit = CountDownLatch(3)


        debugService.addListener(object : MockDebugEventListener {
            override fun onSessionStarted(session: MockActiveDebugSession) {
                session.addStateListener(object : MockSessionStateListener {
                    override fun onPaused(breakpointInfo: String) {
                        receivedPauses.add(breakpointInfo)
                        allBreakpointsHit.countDown()
                    }
                })
            }
        })

        val session = debugService.startSession()


        val executor = Executors.newSingleThreadExecutor()
        executor.submit {
            session.onBreakpointHit("breakpoint 1")
            session.onBreakpointHit("breakpoint 2")
            session.onBreakpointHit("breakpoint 3")
        }


        assertTrue(allBreakpointsHit.await(5, TimeUnit.SECONDS))
        executor.shutdown()

        assertEquals(3, receivedPauses.size)
        assertEquals(listOf("breakpoint 1", "breakpoint 2", "breakpoint 3"), receivedPauses)
    }
}

