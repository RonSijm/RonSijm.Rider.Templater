package ronsijm.templater.modules

import com.intellij.openapi.project.Project
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.services.ServiceContainer

/**
 * Creates module instances lazily. Only non-command modules live here -
 * date/file/system/web are handled by HandlerRegistry.
 */
class ModuleFactory(
    private val context: TemplateContext,
    private val services: ServiceContainer,
    private val project: Project? = null
) {
    private val _frontmatterModule by lazy { FrontmatterModule(context) }
    private val _hooksModule by lazy { HooksModule(context) }
    private val _configModule by lazy { ConfigModule(context) }
    private val _appModule by lazy { project?.let { AppModule(context, it) } }

    fun getFrontmatterModule() = _frontmatterModule
    fun getHooksModule() = _hooksModule
    fun getConfigModule() = _configModule
    fun getAppModule() = _appModule
    fun hasAppModule() = _appModule != null
}

