package ronsijm.templater.parallel

import ronsijm.templater.common.ModuleNames
import ronsijm.templater.common.Prefixes
import ronsijm.templater.handlers.generated.HandlerRegistry

class DependencyAnalyzer {

    private val letPattern = Regex("""(?:let|const|var)\s+(\w+)\s*=""")
    private val assignmentPattern = Regex("""^(\w+)\s*=""")
    private val variableRefPattern = Regex("""\b([a-zA-Z_]\w*)\b""")

    private val pureFunctions: Set<String> by lazy {
        buildSet {
            HandlerRegistry.allHandlers.forEach { registration ->
                if (registration.metadata.pure) {
                    add("${Prefixes.TP}${registration.metadata.module}.${registration.metadata.name}")
                }
            }
            add("${Prefixes.TP}${ModuleNames.FRONTMATTER}")
        }
    }


    private val barrierFunctions: Set<String> by lazy {
        buildSet {
            HandlerRegistry.allHandlers.forEach { registration ->


                if (registration.metadata.barrier || registration.metadata.cancellable) {
                    add("${Prefixes.TP}${registration.metadata.module}.${registration.metadata.name}")
                }
            }
        }
    }

    private val reservedKeywords = setOf(
        "let", "const", "var", "if", "else", "for", "while", "do", "switch",
        "case", "break", "continue", "return", "function", "class", "new",
        "true", "false", "null", "undefined", "await", "async", "try", "catch",
        "finally", "throw", "typeof", "instanceof", "in", "of", "this", "super",
        "tp", "tR"
    )
    fun analyze(block: TemplateBlock): BlockAnalysis {
        val command = block.command

        val variablesWritten = mutableSetOf<String>()
        val variablesRead = mutableSetOf<String>()
        var isBarrier = false
        var hasTrWrite = false

        if (block.isExecution) {
            letPattern.findAll(command).forEach { match ->
                variablesWritten.add(match.groupValues[1])
            }

            val commandWithoutDeclarations = command.replace(letPattern, "")
            assignmentPattern.findAll(commandWithoutDeclarations).forEach { match ->
                val varName = match.groupValues[1]
                if (varName !in reservedKeywords) {
                    variablesWritten.add(varName)


                    val rightSide = commandWithoutDeclarations.substringAfter("=", "")
                    variableRefPattern.findAll(rightSide).forEach { refMatch ->
                        val refVarName = refMatch.groupValues[1]
                        if (refVarName !in reservedKeywords) {
                            variablesRead.add(refVarName)
                        }
                    }
                }
            }

            if (command.contains("tR") && (command.contains("tR +=") || command.contains("tR="))) {
                hasTrWrite = true
            }

            isBarrier = barrierFunctions.any { command.contains(it) }

            if (!assignmentPattern.containsMatchIn(commandWithoutDeclarations)) {
                variableRefPattern.findAll(command).forEach { match ->
                    val varName = match.groupValues[1]
                    if (varName !in reservedKeywords && varName !in variablesWritten) {
                        variablesRead.add(varName)
                    }
                }
            }
        } else {
            if (command == Prefixes.TR) {
                variablesRead.add(Prefixes.TR)
            } else if (!command.startsWith(Prefixes.TP)) {
                variablesRead.add(command)
            }
        }

        return BlockAnalysis(
            block = block,
            variablesRead = variablesRead,
            variablesWritten = variablesWritten,
            isBarrier = isBarrier,
            hasTrWrite = hasTrWrite
        )
    }


    fun analyzeAll(blocks: List<TemplateBlock>): List<BlockAnalysis> {
        return blocks.map { analyze(it) }
    }
}
