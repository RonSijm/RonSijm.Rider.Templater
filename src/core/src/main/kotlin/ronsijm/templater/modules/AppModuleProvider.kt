package ronsijm.templater.modules


interface AppModuleProvider {

    fun getAppModule(): Any?


    fun hasAppModule(): Boolean = getAppModule() != null
}
