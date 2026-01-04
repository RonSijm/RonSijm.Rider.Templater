package ronsijm.templater.handlers.system

import ronsijm.templater.handlers.RequestParser
import ronsijm.templater.utils.ArgumentParser

class PromptRequestParser : RequestParser<PromptRequest> {
    override fun parse(args: List<Any?>): PromptRequest {
        val promptText = ArgumentParser.getString(args, 0, "Enter value")
        val defaultValue = ArgumentParser.getString(args, 1).ifEmpty { null }
        val throwOnCancel = ArgumentParser.getBoolean(args, 2, false)
        val multiline = ArgumentParser.getBoolean(args, 3, false)

        return PromptRequest(
            promptText = promptText,
            defaultValue = defaultValue,
            throwOnCancel = throwOnCancel,
            multiline = multiline
        )
    }
}
