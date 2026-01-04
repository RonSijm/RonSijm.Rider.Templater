package ronsijm.templater.standalone.ui.util

import javax.swing.SwingUtilities
import javax.swing.SwingWorker

object BackgroundTask {
    fun execute(
        task: () -> Unit,
        onError: ((Exception) -> Unit)? = null,
        onComplete: (() -> Unit)? = null
    ) {
        object : SwingWorker<Unit, Unit>() {
            private var error: Exception? = null

            override fun doInBackground() {
                try {
                    task()
                } catch (e: Exception) {
                    error = e
                }
            }

            override fun done() {
                val e = error
                if (e != null) {
                    onError?.invoke(e)
                } else {
                    onComplete?.invoke()
                }
            }
        }.execute()
    }

    fun <T> executeWithResult(
        task: () -> T,
        onResult: (T) -> Unit,
        onError: ((Exception) -> Unit)? = null
    ) {
        object : SwingWorker<T, Unit>() {
            override fun doInBackground(): T {
                return task()
            }

            override fun done() {
                try {
                    val result = get()
                    onResult(result)
                } catch (e: Exception) {
                    onError?.invoke(e)
                }
            }
        }.execute()
    }
}

