package ronsijm.templater.services.mock

import ronsijm.templater.modules.AppModuleProvider


object NullAppModuleProvider : AppModuleProvider {
    override fun getAppModule(): Any? = null
    override fun hasAppModule(): Boolean = false
}

