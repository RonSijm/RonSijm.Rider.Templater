package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.RequestParser
import ronsijm.templater.utils.ArgumentParser

class FindTFileRequestParser : RequestParser<FindTFileRequest> {
    override fun parse(args: List<Any?>): FindTFileRequest {
        return FindTFileRequest(
            filename = ArgumentParser.getString(args, 0)
        )
    }
}

