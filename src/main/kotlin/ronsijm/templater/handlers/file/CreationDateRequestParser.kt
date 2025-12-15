package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.RequestParser
import ronsijm.templater.utils.ArgumentParser

/**
 * Parser for CreationDateRequest
 * 
 * Converts raw template arguments to typed CreationDateRequest
 */
class CreationDateRequestParser : RequestParser<CreationDateRequest> {
    override fun parse(args: List<Any?>): CreationDateRequest {
        return CreationDateRequest(
            format = ArgumentParser.getString(args, 0, "yyyy-MM-dd HH:mm"),
            path = ArgumentParser.getString(args, 1).ifEmpty { null }
        )
    }
}



