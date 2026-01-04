package ronsijm.templater.standalone.modules

import ronsijm.templater.cli.filesystem.JavaFileSystem
import ronsijm.templater.filesystem.FileHandle
import ronsijm.templater.filesystem.FileSystem
import ronsijm.templater.modules.AppModuleProvider
import ronsijm.templater.parser.TemplateContext
import java.io.File

class CliAppModuleProvider(
    private val context: TemplateContext,
    private val rootDir: File,
    private val activeFileProvider: () -> FileHandle? = { null }
) : AppModuleProvider {

    private val fileSystem: FileSystem by lazy { JavaFileSystem(rootDir) }
    private val appModule: CliAppModule by lazy {
        CliAppModule(context, fileSystem, activeFileProvider)
    }

    override fun getAppModule(): Any = appModule

    override fun hasAppModule(): Boolean = true

    fun getTypedAppModule(): CliAppModule = appModule

    companion object {
        fun fromPath(context: TemplateContext, path: String): CliAppModuleProvider {
            val file = File(path)
            val rootDir = if (file.isDirectory) file else file.parentFile ?: File(".")
            return CliAppModuleProvider(context, rootDir)
        }

        fun withActiveFile(
            context: TemplateContext,
            rootDir: File,
            activeFileProvider: () -> FileHandle?
        ): CliAppModuleProvider {
            return CliAppModuleProvider(context, rootDir, activeFileProvider)
        }
    }
}

