package ronsijm.templater.debug

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.TestContextFactory


@Suppress("UnusedPrivateMember")
class DebugVisualizerNodeHighlightingTest {

    data class HighlightedNode(
        val stepId: Int,
        val stepType: ExecutionStep.StepType,
        val stepDescription: String,
        val lineNumber: Int?,
        val nodeId: String?,
        val nodeType: FlowNode.NodeType?
    )


    private fun findNodeForStep(
        step: ExecutionStep,
        graph: ControlFlowGraph,
        lineToNodeMap: Map<Int, String>
    ): String? {

        val statementId = step.statementNode?.id
        if (statementId != null) {

            return graph.nodes.find { it.id == statementId }?.id
        }


        val lineNumber = step.displayLineNumber
        val stepCode = step.input
        val stepType = step.type


        var nodeId = if (stepType == ExecutionStep.StepType.LOOP_ITERATION) {
            findLoopStartNode(lineNumber, graph)
        } else {

            lineNumber?.let { findNodeForLine(it, lineToNodeMap) }
        }


        if (nodeId == null && stepCode != null) {
            nodeId = findNodeByCode(stepCode, graph)
        }

        return nodeId
    }

    private fun findLoopStartNode(lineNumber: Int?, graph: ControlFlowGraph): String? {
        if (lineNumber == null) return null
        return graph.nodes.find { node ->
            node.type == FlowNode.NodeType.LOOP_START && node.lineNumber == lineNumber
        }?.id
    }

    private fun findNodeForLine(lineNumber: Int, lineToNodeMap: Map<Int, String>): String? {

        lineToNodeMap[lineNumber]?.let { return it }


        val closestLine = lineToNodeMap.keys
            .filter { it <= lineNumber }
            .maxOrNull()

        return closestLine?.let { lineToNodeMap[it] }
    }

    private fun findNodeByCode(code: String, graph: ControlFlowGraph): String? {
        val normalizedCode = code.trim()
        return graph.nodes.find { node ->
            node.code?.trim() == normalizedCode ||
            normalizedCode.startsWith(node.code?.trim() ?: "") ||
            (node.code?.trim() ?: "").startsWith(normalizedCode)
        }?.id
    }

    @Test
    fun `statement IDs - all execution steps have statement IDs and match control flow nodes`() {





        val debugParser = DebuggingTemplateParser(validateSyntax = false)

        val template = """<%*
let counter = 0;

for (let i = 0; i < 3; i++) {
    counter++;
}

if (counter > 0) {
    tR += "done";
}
%>"""

        val highlightedNodes = mutableListOf<HighlightedNode>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                highlightedNodes.add(
                    HighlightedNode(
                        stepId = breakpoint.step.id,
                        stepType = breakpoint.step.type,
                        stepDescription = breakpoint.step.description,
                        lineNumber = breakpoint.step.displayLineNumber,
                        nodeId = breakpoint.step.statementNode?.id,
                        nodeType = null
                    )
                )

                DebugAction.STEP_INTO
            },
            startInStepMode = true
        )

        debugParser.parse(template, TestContextFactory.create())


        val ast = debugParser.getTemplateAST()
        assertNotNull(ast, "AST should be built")

        val converter = ronsijm.templater.ast.ASTToControlFlowConverter()
        val graph = converter.convert(ast!!)


        val statementSteps = highlightedNodes.filter { it.stepType == ExecutionStep.StepType.STATEMENT }
        assertTrue(statementSteps.isNotEmpty(), "Should have statement steps")

        val stepsWithoutStatementId = statementSteps.filter { it.nodeId == null }
        if (stepsWithoutStatementId.isNotEmpty()) {
            println("Steps without statement IDs:")
            stepsWithoutStatementId.forEach { println("  - ${it.stepDescription}") }
        }
        assertTrue(stepsWithoutStatementId.isEmpty(),
            "All statement steps should have statement IDs. Found ${stepsWithoutStatementId.size} without IDs")


        val stepsWithMatchingNodes = statementSteps.mapNotNull { step ->
            val nodeId = step.nodeId
            if (nodeId != null) {
                val node = graph.nodes.find { it.id == nodeId }
                if (node != null) step to node else null
            } else {
                null
            }
        }

        assertEquals(statementSteps.size, stepsWithMatchingNodes.size,
            "All statement steps should have matching nodes in the control flow graph")


        val nodeTypes = stepsWithMatchingNodes.map { it.second.type }.toSet()
        assertTrue(nodeTypes.isNotEmpty(), "Should have various node types")

        println("Statement ID-based highlighting test results:")
        println("  Total steps: ${highlightedNodes.size}")
        println("  Statement steps: ${statementSteps.size}")
        println("  Steps with statement IDs: ${statementSteps.filter { it.nodeId != null }.size}")
        println("  Steps with matching nodes: ${stepsWithMatchingNodes.size}")
        println("  Node types found: $nodeTypes")
    }

    @Test
    fun `test for loop - loop iterations have correct line number`() {
        val debugParser = DebuggingTemplateParser(validateSyntax = false)

        val template = """<%*
let counter = 0;

for (let i = 0; i < 3; i++) {
    counter++;
}

tR += counter;
%>""".trimIndent()


        val highlightedNodes = mutableListOf<HighlightedNode>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                val step = breakpoint.step

                highlightedNodes.add(
                    HighlightedNode(
                        stepId = step.id,
                        stepType = step.type,
                        stepDescription = step.description,
                        lineNumber = step.displayLineNumber,
                        nodeId = null,
                        nodeType = null
                    )
                )

                DebugAction.STEP_INTO
            },
            startInStepMode = true
        )

        val result = debugParser.parse(template, TestContextFactory.create())


        println("\n=== Highlighted Nodes ===")
        highlightedNodes.forEach { node ->
            println("Step ${node.stepId}: [${node.stepType}] ${node.stepDescription}")
            println("  Line: ${node.lineNumber}")
        }
        println("=========================\n")



        val forLoopSteps = highlightedNodes.filter { it.stepDescription.contains("for (let i = 0; i < 3; i++)") }
        assertTrue(forLoopSteps.isNotEmpty(), "Should have at least one step for the for loop header")
        val forLoopHeaderLine = forLoopSteps.first().lineNumber
        assertNotNull(forLoopHeaderLine, "For loop header should have a line number")
        assertEquals(3, forLoopHeaderLine, "For loop header should be on line 3 (0-based, script-relative)")


        val loopIterationSteps = highlightedNodes.filter { it.stepType == ExecutionStep.StepType.LOOP_ITERATION }
        assertEquals(3, loopIterationSteps.size, "Should have 3 loop iteration steps")

        loopIterationSteps.forEach { iterationStep ->
            assertEquals(
                forLoopHeaderLine,
                iterationStep.lineNumber,
                "Loop iteration '${iterationStep.stepDescription}' should have the same line number " +
                    "as the loop header (line $forLoopHeaderLine), not the body line"
            )
        }




        val counterSteps = highlightedNodes.filter {
            it.stepType == ExecutionStep.StepType.STATEMENT && it.stepDescription.trim() == "counter++"
        }
        assertEquals(3, counterSteps.size, "Should have 3 counter++ STATEMENT steps (one per iteration)")


        counterSteps.forEach { counterStep ->
            assertNotNull(counterStep.lineNumber, "counter++ should have a line number")
        }


        assertEquals("3", result.result.trim())
    }

}
