package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext

@RegisterHandler(
    module = "file",
    description = "Returns the folder name or path",
    example = "folder(true)"
)
class FolderHandler : CommandHandler<FolderRequest, String> {
    override fun handle(request: FolderRequest, context: TemplateContext): String {
        val path = context.filePath ?: return ""
        val lastSlash = path.lastIndexOf('/')
        val folderPath = if (lastSlash > 0) path.substring(0, lastSlash) else ""

        return if (request.relative) {
            folderPath
        } else {
            val lastFolderSlash = folderPath.lastIndexOf('/')
            if (lastFolderSlash >= 0) folderPath.substring(lastFolderSlash + 1) else folderPath
        }
    }
}

