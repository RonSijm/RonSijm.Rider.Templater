package ronsijm.templater.handlers.date

import ronsijm.templater.handlers.RequestParser
import ronsijm.templater.utils.ArgumentParser
import ronsijm.templater.utils.DateFormats

class NowRequestParser : RequestParser<NowRequest> {
    override fun parse(args: List<Any?>): NowRequest {
        return NowRequest(
            format = ArgumentParser.getString(args, 0, DateFormats.DEFAULT_DATETIME),
            offset = ArgumentParser.getString(args, 1),
            reference = ArgumentParser.getString(args, 2),
            referenceFormat = ArgumentParser.getString(args, 3)
        )
    }
}

