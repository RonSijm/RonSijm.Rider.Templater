package ronsijm.templater.parser

import ronsijm.templater.services.ServiceContainer


data class TemplateContext(
    val frontmatter: Map<String, Any>,
    val frontmatterParser: FrontmatterParser,
    val fileName: String,
    val filePath: String? = null,
    val fileContent: String? = null,
    val services: ServiceContainer = ServiceContainer()
)