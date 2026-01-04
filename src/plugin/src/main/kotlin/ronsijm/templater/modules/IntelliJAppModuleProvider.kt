package ronsijm.templater.modules

import com.intellij.openapi.project.Project
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.filesystem.IntelliJFileSystem


class IntelliJAppModuleProvider(
    private val context: TemplateContext,
    private val project: Project
) : AppModuleProvider {

    private val fileSystem by lazy { IntelliJFileSystem(project) }
    private val appModule by lazy { AppModule(context, fileSystem, project) }

    override fun getAppModule(): Any = appModule
}
