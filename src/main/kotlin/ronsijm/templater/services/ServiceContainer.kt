package ronsijm.templater.services

import ronsijm.templater.settings.CancelBehavior
import ronsijm.templater.settings.SimpleTemplaterSettings
import ronsijm.templater.settings.TemplaterSettingsData

/** Holds all service dependencies. Uses null-object defaults so callers don't need null checks. */
data class ServiceContainer(
    val clipboardService: ClipboardService = SystemClipboardService(),
    val httpService: HttpService = DefaultHttpService(),
    val fileOperationService: FileOperationService = NullFileOperationService,
    val systemOperationsService: SystemOperationsService = NullSystemOperationsService,
    val settings: TemplaterSettingsData = SimpleTemplaterSettings()
) {
    companion object {
        fun createDefault() = ServiceContainer()

        fun createForTesting(
            clipboardService: ClipboardService? = null,
            httpService: HttpService? = null,
            fileOperationService: FileOperationService? = null,
            systemOperationsService: SystemOperationsService? = null,
            settings: TemplaterSettingsData? = null
        ) = ServiceContainer(
            clipboardService = clipboardService ?: MockClipboardService(),
            httpService = httpService ?: MockHttpService(),
            fileOperationService = fileOperationService ?: MockFileOperationsService(),
            systemOperationsService = systemOperationsService ?: MockSystemOperationsService(),
            settings = settings ?: SimpleTemplaterSettings()
        )
    }
}
