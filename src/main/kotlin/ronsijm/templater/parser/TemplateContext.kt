package ronsijm.templater.parser

import ronsijm.templater.services.ServiceContainer

/** Everything the template parser needs: file info, frontmatter, and services. */
data class TemplateContext(
    val frontmatter: Map<String, Any>,
    val frontmatterParser: FrontmatterParser,
    val fileName: String,
    val filePath: String? = null,
    val fileContent: String? = null,
    val services: ServiceContainer = ServiceContainer(),
    val executeDateCommand: ((String, List<String>) -> String?)? = null,
    val executeFrontmatterCommand: ((List<String>) -> Any?)? = null
)