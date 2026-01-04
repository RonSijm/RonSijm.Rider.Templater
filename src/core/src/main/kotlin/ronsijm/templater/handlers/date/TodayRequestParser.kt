package ronsijm.templater.handlers.date

import ronsijm.templater.handlers.RequestParser
import ronsijm.templater.utils.ArgumentParser
import ronsijm.templater.utils.DateFormats

class TodayRequestParser : RequestParser<TodayRequest> {
    override fun parse(args: List<Any?>): TodayRequest {
        return TodayRequest(
            format = ArgumentParser.getString(args, 0, DateFormats.DEFAULT_DATE)
        )
    }
}
