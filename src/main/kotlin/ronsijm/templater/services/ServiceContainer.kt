package ronsijm.templater.services

/** Holds all service dependencies. Uses null-object defaults so callers don't need null checks. */
data class ServiceContainer(
    val clipboardService: ClipboardService = SystemClipboardService(),
    val httpService: HttpService = DefaultHttpService(),
    val fileOperationService: FileOperationService = NullFileOperationService,
    val systemOperationsService: SystemOperationsService = NullSystemOperationsService
) {
    companion object {
        fun createDefault() = ServiceContainer()

        fun createForTesting(
            clipboardService: ClipboardService? = null,
            httpService: HttpService? = null,
            fileOperationService: FileOperationService? = null,
            systemOperationsService: SystemOperationsService? = null
        ) = ServiceContainer(
            clipboardService = clipboardService ?: MockClipboardService(),
            httpService = httpService ?: MockHttpService(),
            fileOperationService = fileOperationService ?: MockFileOperationsService(),
            systemOperationsService = systemOperationsService ?: MockSystemOperationsService()
        )
    }
}

class MockClipboardService(private var content: String = "") : ClipboardService {
    override fun getClipboardText() = content
    override fun setClipboardText(text: String) { content = text }
}

class MockHttpService(private val responses: MutableMap<String, String> = mutableMapOf()) : HttpService {
    override fun send(request: java.net.http.HttpRequest) = responses[request.uri().toString()] ?: "{}"
    fun addResponse(url: String, response: String) { responses[url] = response }
}

