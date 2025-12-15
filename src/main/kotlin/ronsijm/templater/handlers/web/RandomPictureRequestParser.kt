package ronsijm.templater.handlers.web

import ronsijm.templater.handlers.RequestParser
import ronsijm.templater.utils.ArgumentParser

class RandomPictureRequestParser : RequestParser<RandomPictureRequest> {
    override fun parse(args: List<Any?>): RandomPictureRequest {
        return RandomPictureRequest(
            size = ArgumentParser.getString(args, 0, "1600x900"),
            query = ArgumentParser.getString(args, 1),
            includeSize = ArgumentParser.getBoolean(args, 2, false)
        )
    }
}

