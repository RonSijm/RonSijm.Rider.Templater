package ronsijm.templater.handlers.date

import ronsijm.templater.handlers.RequestParser
import ronsijm.templater.utils.ArgumentParser
import ronsijm.templater.utils.DateFormats

class YesterdayRequestParser : RequestParser<YesterdayRequest> {
    override fun parse(args: List<Any?>): YesterdayRequest {
        return YesterdayRequest(
            format = ArgumentParser.getString(args, 0, DateFormats.DEFAULT_DATE)
        )
    }
}
