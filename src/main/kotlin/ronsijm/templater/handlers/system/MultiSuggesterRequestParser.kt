package ronsijm.templater.handlers.system

import ronsijm.templater.handlers.RequestParser
import ronsijm.templater.utils.ArgumentParser

class MultiSuggesterRequestParser : RequestParser<MultiSuggesterRequest> {
    override fun parse(args: List<Any?>): MultiSuggesterRequest {
        val textItems = ArgumentParser.getList<Any>(args, 0, emptyList())
        val items = ArgumentParser.getList<Any>(args, 1, textItems)
        val throwOnCancel = ArgumentParser.getBoolean(args, 2, false)
        val placeholder = ArgumentParser.getString(args, 3)
        val limit = ArgumentParser.getInt(args, 4)
        
        return MultiSuggesterRequest(
            textItems = textItems,
            items = items,
            throwOnCancel = throwOnCancel,
            placeholder = placeholder,
            limit = limit
        )
    }
}

