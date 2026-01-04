package ronsijm.templater.services

import ronsijm.templater.services.mock.MockClipboardService
import ronsijm.templater.services.mock.MockFileOperationsService
import ronsijm.templater.services.mock.MockHttpService
import ronsijm.templater.services.mock.MockSystemOperationsService
import ronsijm.templater.services.mock.NullClipboardService
import ronsijm.templater.services.mock.NullFileOperationService
import ronsijm.templater.services.mock.NullHttpService
import ronsijm.templater.services.mock.NullSystemOperationsService
import ronsijm.templater.settings.SimpleTemplaterSettings
import ronsijm.templater.settings.TemplaterSettingsData


data class ServiceContainer(
    val clipboardService: ClipboardService = NullClipboardService,
    val httpService: HttpService = NullHttpService,
    val fileOperationService: FileOperationService = NullFileOperationService,
    val systemOperationsService: SystemOperationsService = NullSystemOperationsService,
    val settings: TemplaterSettingsData = SimpleTemplaterSettings()
) {
    companion object {

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


        internal fun createWithDefaults(
            clipboardService: ClipboardService = SystemClipboardService(),
            httpService: HttpService = DefaultHttpService(),
            fileOperationService: FileOperationService = NullFileOperationService,
            systemOperationsService: SystemOperationsService = NullSystemOperationsService,
            settings: TemplaterSettingsData = SimpleTemplaterSettings()
        ) = ServiceContainer(
            clipboardService = clipboardService,
            httpService = httpService,
            fileOperationService = fileOperationService,
            systemOperationsService = systemOperationsService,
            settings = settings
        )
    }
}
