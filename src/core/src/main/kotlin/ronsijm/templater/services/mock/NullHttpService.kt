package ronsijm.templater.services.mock

import ronsijm.templater.services.HttpService
import java.net.http.HttpRequest


object NullHttpService : HttpService {
    override fun send(request: HttpRequest) = ""
}

