package ronsijm.templater.handlers.date

import ronsijm.templater.handlers.RequestParser
import ronsijm.templater.utils.ArgumentParser
import ronsijm.templater.utils.DateFormats

class TomorrowRequestParser : RequestParser<TomorrowRequest> {
    override fun parse(args: List<Any?>): TomorrowRequest {
        return TomorrowRequest(
            format = ArgumentParser.getString(args, 0, DateFormats.DEFAULT_DATE)
        )
    }
}
