package ronsijm.templater.parallel

/**
 * Analyzes template blocks to determine variable dependencies and barriers
 */
class DependencyAnalyzer {
    
    // Patterns for detecting variable operations
    private val letPattern = Regex("""(?:let|const|var)\s+(\w+)\s*=""")
    private val assignmentPattern = Regex("""^(\w+)\s*=""")
    private val variableRefPattern = Regex("""\b([a-zA-Z_]\w*)\b""")
    
    // Barrier functions - these require sequential execution
    private val barrierFunctions = setOf(
        "tp.system.prompt",
        "tp.system.suggester",
        "tp.system.clipboard",  // clipboard operations should be sequential
        "tp.file.create_new",
        "tp.file.move",
        "tp.file.rename"
    )
    
    // Pure functions - these have no side effects and can be parallelized
    private val pureFunctions = setOf(
        "tp.date.now",
        "tp.date.tomorrow",
        "tp.date.yesterday",
        "tp.date.weekday",
        "tp.file.title",
        "tp.file.folder",
        "tp.file.path",
        "tp.file.content",
        "tp.file.selection",
        "tp.file.cursor",
        "tp.file.cursor_append",
        "tp.file.last_modified_date",
        "tp.file.creation_date",
        "tp.file.tags",
        "tp.file.include",
        "tp.file.exists",
        "tp.file.find_tfile",
        "tp.frontmatter"
    )
    
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

