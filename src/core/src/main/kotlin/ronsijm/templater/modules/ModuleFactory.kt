package ronsijm.templater.modules

import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.services.mock.NullAppModuleProvider


class ModuleFactory(
    private val context: TemplateContext,
    private val appModuleProvider: AppModuleProvider = NullAppModuleProvider
) {
    private val _frontmatterModule by lazy { FrontmatterModule(context) }
    private val _hooksModule by lazy { HooksModule(context) }
    private val _configModule by lazy { ConfigModule(context) }

    fun getFrontmatterModule() = _frontmatterModule
    fun getHooksModule() = _hooksModule
    fun getConfigModule() = _configModule
    fun getAppModule() = appModuleProvider.getAppModule()
    fun hasAppModule() = appModuleProvider.hasAppModule()
}
