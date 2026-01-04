package ronsijm.templater.handlers.date

import ronsijm.templater.handlers.RequestParser
import ronsijm.templater.utils.ArgumentParser
import ronsijm.templater.utils.DateFormats

class WeekdayRequestParser : RequestParser<WeekdayRequest> {
    override fun parse(args: List<Any?>): WeekdayRequest {
        return WeekdayRequest(
            format = ArgumentParser.getString(args, 0, DateFormats.DEFAULT_DATE),
            weekday = ArgumentParser.getInt(args, 1, 0) ?: 0,
            reference = ArgumentParser.getString(args, 2),
            referenceFormat = ArgumentParser.getString(args, 3)
        )
    }
}
