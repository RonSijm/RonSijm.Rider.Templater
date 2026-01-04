package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.RequestParser
import ronsijm.templater.utils.ArgumentParser

class CreateNewRequestParser : RequestParser<CreateNewRequest> {
    override fun parse(args: List<Any?>): CreateNewRequest {
        val template = ArgumentParser.getString(args, 0)
        val filename = ArgumentParser.getString(args, 1).ifEmpty { null }
        val openNew = ArgumentParser.getBoolean(args, 2, false)
        val folder = ArgumentParser.getString(args, 3).ifEmpty { null }

        return CreateNewRequest(
            template = template,
            filename = filename,
            openNew = openNew,
            folder = folder
        )
    }
}
