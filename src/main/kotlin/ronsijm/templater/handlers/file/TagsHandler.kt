package ronsijm.templater.handlers.file

import ronsijm.templater.handlers.CommandHandler
import ronsijm.templater.handlers.RegisterHandler
import ronsijm.templater.parser.TemplateContext

@RegisterHandler(
    module = "file",
    description = "Returns the file tags",
    example = "tags()"
)
class TagsHandler : CommandHandler<TagsRequest, String> {
    override fun handle(request: TagsRequest, context: TemplateContext): String {
        val serviceTags = context.services.fileOperationService.getTags()
        val tags = if (serviceTags.isNotEmpty()) {
            serviceTags
        } else {
            extractTagsFromContent(context.fileContent ?: "")
        }
        return tags.joinToString(", ")
    }

    private fun extractTagsFromContent(content: String): List<String> {
        val tagRegex = Regex("#([a-zA-Z0-9_-]+)")
        return tagRegex.findAll(content)
            .map { it.groupValues[1] }
            .toList()
    }
}

