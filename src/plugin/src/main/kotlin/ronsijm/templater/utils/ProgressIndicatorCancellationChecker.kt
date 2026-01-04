package ronsijm.templater.utils

import com.intellij.openapi.progress.ProcessCanceledException
import com.intellij.openapi.progress.ProgressIndicator


class ProgressIndicatorCancellationChecker(
    private val indicator: ProgressIndicator
) : CancellationChecker {

    override fun checkCancelled() {

        indicator.checkCanceled()
    }

    override fun isCancelled(): Boolean {
        return indicator.isCanceled
    }
}
