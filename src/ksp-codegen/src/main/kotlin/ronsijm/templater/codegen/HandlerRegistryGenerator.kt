package ronsijm.templater.codegen

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import java.nio.file.FileAlreadyExistsException

class HandlerRegistryGenerator(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger
) : SymbolProcessor {

    private data class HandlerInfo(
        val classDecl: KSClassDeclaration,
        val module: String,
        val commandName: String,
        val description: String,
        val example: String,
        val parameters: List<ParameterInfo>
    )

    private data class ParameterInfo(
        val name: String,
        val type: String,
        val hasDefault: Boolean,
        val description: String
    )

    override fun process(resolver: Resolver): List<KSAnnotated> {
        logger.warn("HandlerRegistryGenerator: Starting processing")

        val symbols = resolver.getSymbolsWithAnnotation("ronsijm.templater.handlers.RegisterHandler")
            .filterIsInstance<KSClassDeclaration>()
            .filter { it.validate() }
            .toList()

        logger.warn("HandlerRegistryGenerator: Found ${symbols.size} annotated classes")

        if (symbols.isEmpty()) {
            logger.warn("HandlerRegistryGenerator: No symbols found, returning empty")
            return emptyList()
        }

        val handlerInfos = symbols.mapNotNull { extractHandlerInfo(it, resolver) }
        val handlersByModule = handlerInfos.groupBy { it.module }

        generateRegistry(handlersByModule)
        return emptyList()
    }

    private fun extractHandlerInfo(classDecl: KSClassDeclaration, resolver: Resolver): HandlerInfo? {
        val annotation = classDecl.annotations.first { it.shortName.asString() == "RegisterHandler" }

        val module = annotation.arguments.firstOrNull { it.name?.asString() == "module" }?.value as? String ?: return null
        val description = annotation.arguments.firstOrNull { it.name?.asString() == "description" }?.value as? String ?: ""
        val example = annotation.arguments.firstOrNull { it.name?.asString() == "example" }?.value as? String ?: ""

        val handlerName = classDecl.simpleName.asString()
        val baseName = handlerName.removeSuffix("Handler")
        val commandName = camelToSnakeCase(baseName)

        val packageName = classDecl.packageName.asString()
        val requestClassName = "${baseName}Request"
        val requestClass = resolver.getClassDeclarationByName(
            resolver.getKSNameFromString("$packageName.$requestClassName")
        )

        val parameters = requestClass?.let { extractParameters(it) } ?: emptyList()

        return HandlerInfo(classDecl, module, commandName, description, example, parameters)
    }

    private fun extractParameters(requestClass: KSClassDeclaration): List<ParameterInfo> {
        val primaryConstructor = requestClass.primaryConstructor ?: return emptyList()

        return primaryConstructor.parameters.mapNotNull { param ->
            val name = param.name?.asString() ?: return@mapNotNull null
            if (name == "dummy") return@mapNotNull null

            val type = mapKotlinTypeToTypeScript(param.type.resolve())
            val hasDefault = param.hasDefault

            val property = requestClass.getAllProperties().find { it.simpleName.asString() == name }
            val descAnnotation = property?.annotations?.find { it.shortName.asString() == "ParamDescription" }
            val description = descAnnotation?.arguments?.firstOrNull()?.value as? String ?: ""

            ParameterInfo(camelToSnakeCase(name), type, hasDefault, description)
        }
    }

    private fun mapKotlinTypeToTypeScript(type: KSType): String {
        val typeName = type.declaration.simpleName.asString()
        val isNullable = type.isMarkedNullable

        val baseType = when (typeName) {
            "String" -> "string"
            "Int", "Long", "Short", "Byte" -> "number"
            "Float", "Double" -> "number"
            "Boolean" -> "boolean"
            "List", "Array" -> "array"
            else -> typeName.lowercase()
        }

        return if (isNullable) "$baseType?" else baseType
    }

    private fun camelToSnakeCase(str: String): String {
        return str.replace(Regex("([a-z])([A-Z])")) { "${it.groupValues[1]}_${it.groupValues[2]}" }.lowercase()
    }

    private fun formatParametersString(parameters: List<ParameterInfo>): String {
        if (parameters.isEmpty()) return ""
        return parameters.joinToString(", ") { p ->
            val optional = if (p.hasDefault) "?" else ""
            "${p.name}$optional: ${p.type}"
        }
    }

    private fun generateExample(commandName: String): String = "$commandName()"

    private fun escapeString(str: String): String {
        return str.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
    }

    private fun generateRegistry(handlersByModule: Map<String, List<HandlerInfo>>) {
        val file = try {
            codeGenerator.createNewFile(
                dependencies = Dependencies(aggregating = true),
                packageName = "ronsijm.templater.handlers.generated",
                fileName = "HandlerRegistry"
            )
        } catch (e: FileAlreadyExistsException) {
            logger.warn("HandlerRegistryGenerator: HandlerRegistry.kt already exists, skipping generation")
            return
        }

        file.bufferedWriter().use { writer -> writeRegistryFile(writer, handlersByModule) }
    }

    private fun writeRegistryFile(writer: java.io.BufferedWriter, handlersByModule: Map<String, List<HandlerInfo>>) {
        writeHeader(writer)
        writeImports(writer, handlersByModule)
        writeMetadataObjects(writer, handlersByModule)
        writeRegistryObject(writer, handlersByModule)
    }

    private fun writeHeader(writer: java.io.BufferedWriter) {
        writer.appendLine("// Generated by HandlerRegistryGenerator - DO NOT EDIT")
        writer.appendLine("package ronsijm.templater.handlers.generated")
        writer.appendLine()
        writer.appendLine("import ronsijm.templater.handlers.HandlerRegistration")
        writer.appendLine("import ronsijm.templater.handlers.HandlerMetadata")
        writer.appendLine("import ronsijm.templater.handlers.CommandAdapter")
        writer.appendLine("import ronsijm.templater.handlers.Command")
        writer.appendLine()
    }

    private fun writeImports(writer: java.io.BufferedWriter, handlersByModule: Map<String, List<HandlerInfo>>) {
        handlersByModule.values.flatten().forEach { info ->
            val packageName = info.classDecl.packageName.asString()
            val handlerName = info.classDecl.simpleName.asString()
            val baseName = handlerName.removeSuffix("Handler")
            writer.appendLine("import ${packageName}.${handlerName}")
            writer.appendLine("import ${packageName}.${baseName}RequestParser")
        }
        writer.appendLine()
    }

    private fun writeMetadataObjects(writer: java.io.BufferedWriter, handlersByModule: Map<String, List<HandlerInfo>>) {
        writer.appendLine("// Generated metadata objects")
        handlersByModule.values.flatten().forEach { info ->
            val handlerName = info.classDecl.simpleName.asString()
            val baseName = handlerName.removeSuffix("Handler")
            val metadataName = "${baseName}HandlerMetadata"
            val parametersStr = formatParametersString(info.parameters)
            val example = if (info.example.isNotEmpty()) info.example else generateExample(info.commandName)

            writer.appendLine("private val $metadataName = HandlerMetadata(")
            writer.appendLine("    module = \"${info.module}\",")
            writer.appendLine("    name = \"${info.commandName}\",")
            writer.appendLine("    description = \"${escapeString(info.description)}\",")
            writer.appendLine("    example = \"${escapeString(example)}\",")
            writer.appendLine("    parameters = \"${escapeString(parametersStr)}\"")
            writer.appendLine(")")
            writer.appendLine()
        }
    }

    private fun writeRegistryObject(writer: java.io.BufferedWriter, handlersByModule: Map<String, List<HandlerInfo>>) {
        writer.appendLine("object HandlerRegistry {")
        writer.appendLine()

        // Generate registrations per module
        handlersByModule.forEach { (module, handlers) ->
            writer.appendLine("    val ${module}Handlers: List<HandlerRegistration<*, *>> = listOf(")
            handlers.forEachIndexed { index, info ->
                val handlerName = info.classDecl.simpleName.asString()
                val baseName = handlerName.removeSuffix("Handler")
                val comma = if (index < handlers.size - 1) "," else ""
                writer.appendLine("        HandlerRegistration(${handlerName}(), ${baseName}RequestParser(), ${baseName}HandlerMetadata)$comma")
            }
            writer.appendLine("    )")
            writer.appendLine()
        }

        // Generate commands per module
        handlersByModule.keys.forEach { module ->
            writer.appendLine("    val ${module}Commands: List<Command> = ${module}Handlers.map { CommandAdapter(it) }")
        }
        writer.appendLine()

        // Generate command lookup maps
        handlersByModule.keys.forEach { module ->
            writer.appendLine("    val ${module}CommandsByName: Map<String, Command> = ${module}Commands.associateBy { it.metadata.name }")
        }
        writer.appendLine()

        // Generate all modules map
        writer.appendLine("    val allModules: Map<String, List<Command>> = mapOf(")
        handlersByModule.keys.forEachIndexed { index, module ->
            val comma = if (index < handlersByModule.size - 1) "," else ""
            writer.appendLine("        \"$module\" to ${module}Commands$comma")
        }
        writer.appendLine("    )")
        writer.appendLine()

        // Generate commandsByModule map
        writer.appendLine("    val commandsByModule: Map<String, Map<String, Command>> = mapOf(")
        handlersByModule.keys.forEachIndexed { index, module ->
            val comma = if (index < handlersByModule.size - 1) "," else ""
            writer.appendLine("        \"$module\" to ${module}CommandsByName$comma")
        }
        writer.appendLine("    )")
        writer.appendLine()

        // Generate all handlers list
        writer.appendLine("    val allHandlers: List<HandlerRegistration<*, *>> = listOf(")
        handlersByModule.keys.forEachIndexed { index, module ->
            val comma = if (index < handlersByModule.size - 1) "," else ""
            writer.appendLine("        ${module}Handlers$comma")
        }
        writer.appendLine("    ).flatten()")
        writer.appendLine()

        // Generate executeCommand function
        writer.appendLine("    fun executeCommand(module: String, function: String, args: List<Any?>, context: ronsijm.templater.parser.TemplateContext): String? {")
        writer.appendLine("        return commandsByModule[module]?.get(function)?.execute(args, context)")
        writer.appendLine("    }")
        writer.appendLine("}")
    }
}

class HandlerRegistryGeneratorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return HandlerRegistryGenerator(environment.codeGenerator, environment.logger)
    }
}
