package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.RequestParser
import ronsijm.templater.utils.ArgumentParser

class LastModifiedDateRequestParser : RequestParser<LastModifiedDateRequest> {
    override fun parse(args: List<Any?>): LastModifiedDateRequest {
        return LastModifiedDateRequest(
            format = ArgumentParser.getString(args, 0, "yyyy-MM-dd HH:mm"),
            path = ArgumentParser.getString(args, 1).ifEmpty { null }
        )
    }
}

