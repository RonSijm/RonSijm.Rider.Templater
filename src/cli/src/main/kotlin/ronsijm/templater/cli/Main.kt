package ronsijm.templater.cli

import ronsijm.templater.parser.FrontmatterParser
import ronsijm.templater.parser.TemplateContext
import ronsijm.templater.parser.TemplateParser
import ronsijm.templater.services.ServiceContainer
import ronsijm.templater.services.SystemClipboardService
import ronsijm.templater.services.DefaultHttpService
import ronsijm.templater.services.mock.NullAppModuleProvider
import ronsijm.templater.settings.SimpleTemplaterSettings
import java.io.File

fun main(args: Array<String>) {
    if (args.isEmpty() || args.contains("-h") || args.contains("--help")) {
        printHelp()
        return
    }

    val options = parseArgs(args)

    if (options.inputFile == null) {
        System.err.println("Error: No input file specified")
        printHelp()
        kotlin.system.exitProcess(1)
    }

    val inputFile = File(options.inputFile)
    if (!inputFile.exists()) {
        System.err.println("Error: File not found: ${options.inputFile}")
        kotlin.system.exitProcess(1)
    }

    try {
        val result = processTemplate(inputFile, options)

        if (options.outputFile != null) {
            File(options.outputFile).writeText(result)
            if (options.verbose) {
                println("Output written to: ${options.outputFile}")
            }
        } else {
            println(result)
        }
    } catch (e: Exception) {
        System.err.println("Error processing template: ${e.message}")
        if (options.verbose) {
            e.printStackTrace()
        }
        kotlin.system.exitProcess(1)
    }
}

private fun processTemplate(inputFile: File, options: CliOptions): String {
    val content = inputFile.readText()
    val workingDir = inputFile.parentFile ?: File(".")

    if (options.dryRun) {
        if (options.verbose) {
            println("Dry run - parsing only")
        }
        return content
    }

    val fileOperationsService = CliFileOperationsService(inputFile, workingDir)
    val systemOperationsService = CliSystemOperationsService()
    val clipboardService = SystemClipboardService()
    val httpService = DefaultHttpService()
    val settings = SimpleTemplaterSettings()

    val services = ServiceContainer(
        clipboardService = clipboardService,
        httpService = httpService,
        fileOperationService = fileOperationsService,
        systemOperationsService = systemOperationsService,
        settings = settings
    )

    val frontmatterParser = FrontmatterParser()
    val (frontmatter, _) = frontmatterParser.parse(content)

    val context = TemplateContext(
        frontmatter = frontmatter,
        frontmatterParser = frontmatterParser,
        fileName = inputFile.nameWithoutExtension,
        filePath = inputFile.absolutePath,
        fileContent = content,
        services = services
    )

    val parser = TemplateParser(services = services)
    return parser.parse(content, context, NullAppModuleProvider)
}

private fun parseArgs(args: Array<String>): CliOptions {
    var inputFile: String? = null
    var outputFile: String? = null
    var verbose = false
    var dryRun = false

    var i = 0
    while (i < args.size) {
        when (args[i]) {
            "-o", "--output" -> {
                if (i + 1 < args.size) {
                    outputFile = args[++i]
                }
            }
            "-v", "--verbose" -> verbose = true
            "--dry-run" -> dryRun = true
            else -> {
                if (!args[i].startsWith("-")) {
                    inputFile = args[i]
                }
            }
        }
        i++
    }

    return CliOptions(inputFile, outputFile, verbose, dryRun)
}

private fun printHelp() {
    println("""
        Templater CLI - Process template files

        Usage: templater <input.md> [options]

        Options:
          -o, --output <file>    Output file (default: stdout)
          -v, --verbose          Show debug info
          --dry-run              Parse only, don't execute
          -h, --help             Show this help

        Examples:
          templater example.md
          templater example.md -o output.md
          templater example.md --verbose
    """.trimIndent())
}

private data class CliOptions(
    val inputFile: String?,
    val outputFile: String?,
    val verbose: Boolean,
    val dryRun: Boolean
)
