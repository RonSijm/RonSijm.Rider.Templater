package ronsijm.templater.parallel

import ronsijm.templater.handlers.generated.HandlerRegistry

/**
 * Analyzes template blocks to determine variable dependencies and barriers
 */
class DependencyAnalyzer {

    // Patterns for detecting variable operations
    private val letPattern = Regex("""(?:let|const|var)\s+(\w+)\s*=""")
    private val assignmentPattern = Regex("""^(\w+)\s*=""")
    private val variableRefPattern = Regex("""\b([a-zA-Z_]\w*)\b""")

    // Build pure and cancellable function sets from HandlerRegistry metadata
    private val pureFunctions: Set<String> by lazy {
        buildSet {
            HandlerRegistry.allHandlers.forEach { registration ->
                if (registration.metadata.pure) {
                    add("tp.${registration.metadata.module}.${registration.metadata.name}")
                }
            }
            // tp.frontmatter is always pure (it's a non-handler module)
            add("tp.frontmatter")
        }
    }

    // Barrier functions - handlers that require sequential execution
    // This includes:
    // - cancellable handlers (show dialogs, require user interaction)
    // - barrier handlers (have side effects like file operations, clipboard)
    private val barrierFunctions: Set<String> by lazy {
        buildSet {
            HandlerRegistry.allHandlers.forEach { registration ->
                if (registration.metadata.cancellable || registration.metadata.barrier) {
                    add("tp.${registration.metadata.module}.${registration.metadata.name}")
                }
            }
        }
    }
    
    // Reserved keywords that shouldn't be treated as variables
    private val reservedKeywords = setOf(
        "let", "const", "var", "if", "else", "for", "while", "do", "switch",
        "case", "break", "continue", "return", "function", "class", "new",
        "true", "false", "null", "undefined", "await", "async", "try", "catch",
        "finally", "throw", "typeof", "instanceof", "in", "of", "this", "super",
        "tp", "tR"  // tp is module prefix, tR is special
    )
    
    /**
     * Analyze a single template block
     */
    fun analyze(block: TemplateBlock): BlockAnalysis {
        val command = block.command

        val variablesWritten = mutableSetOf<String>()
        val variablesRead = mutableSetOf<String>()
        var isBarrier = false
        var hasTrWrite = false

        if (block.isExecution) {
            // Execution block - analyze for variable declarations and assignments

            // Find variable declarations (let x = ...)
            letPattern.findAll(command).forEach { match ->
                variablesWritten.add(match.groupValues[1])
            }

            // Find assignments (x = ...) - but not inside let/const/var
            val commandWithoutDeclarations = command.replace(letPattern, "")
            assignmentPattern.findAll(commandWithoutDeclarations).forEach { match ->
                val varName = match.groupValues[1]
                if (varName !in reservedKeywords) {
                    variablesWritten.add(varName)

                    // For assignments like "x = x + 1", also check the right side for reads
                    val rightSide = commandWithoutDeclarations.substringAfter("=", "")
                    variableRefPattern.findAll(rightSide).forEach { refMatch ->
                        val refVarName = refMatch.groupValues[1]
                        if (refVarName !in reservedKeywords) {
                            variablesRead.add(refVarName)
                        }
                    }
                }
            }

            // Check for tR writes
            if (command.contains("tR") && (command.contains("tR +=") || command.contains("tR="))) {
                hasTrWrite = true
            }

            // Check for barrier functions
            isBarrier = barrierFunctions.any { command.contains(it) }

            // Find all variable references (reads) - for non-assignment statements
            // Only add reads that aren't already captured from assignment right-hand sides
            if (!assignmentPattern.containsMatchIn(commandWithoutDeclarations)) {
                variableRefPattern.findAll(command).forEach { match ->
                    val varName = match.groupValues[1]
                    if (varName !in reservedKeywords && varName !in variablesWritten) {
                        variablesRead.add(varName)
                    }
                }
            }
        } else {
            // Interpolation block - just reads a variable or calls a function
            if (command == "tR") {
                variablesRead.add("tR")
            } else if (!command.startsWith("tp.")) {
                // It's a variable reference
                variablesRead.add(command)
            }
            // Pure function calls don't read variables (they use context)
        }
        
        return BlockAnalysis(
            block = block,
            variablesRead = variablesRead,
            variablesWritten = variablesWritten,
            isBarrier = isBarrier,
            hasTrWrite = hasTrWrite
        )
    }
    
    /**
     * Analyze all blocks in a template
     */
    fun analyzeAll(blocks: List<TemplateBlock>): List<BlockAnalysis> {
        return blocks.map { analyze(it) }
    }
}

