package ronsijm.templater.debug

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.TestContextFactory

class ExecutionTraceTest {

    @Test
    fun `test ExecutionTrace records steps`() {
        val trace = ExecutionTrace()

        val id1 = trace.recordStep(
            type = ExecutionStep.StepType.TEMPLATE_START,
            description = "Start"
        )

        val id2 = trace.recordStep(
            type = ExecutionStep.StepType.BLOCK_START,
            description = "Block 1",
            input = "tp.file.name()"
        )

        assertEquals(2, trace.size)
        assertEquals(1, id1)
        assertEquals(2, id2)

        val step1 = trace.getStep(id1)
        assertNotNull(step1)
        assertEquals("Start", step1?.description)
        assertEquals(ExecutionStep.StepType.TEMPLATE_START, step1?.type)
    }

    @Test
    fun `test ExecutionTrace scope management`() {
        val trace = ExecutionTrace()

        val parentId = trace.recordStep(
            type = ExecutionStep.StepType.BLOCK_START,
            description = "Parent"
        )

        trace.enterScope(parentId)

        val childId = trace.recordStep(
            type = ExecutionStep.StepType.EXPRESSION_EVAL,
            description = "Child"
        )

        trace.exitScope()

        val siblingId = trace.recordStep(
            type = ExecutionStep.StepType.BLOCK_START,
            description = "Sibling"
        )

        val child = trace.getStep(childId)
        val sibling = trace.getStep(siblingId)

        assertEquals(parentId, child?.parentId)
        assertNull(sibling?.parentId)

        val children = trace.getChildSteps(parentId)
        assertEquals(1, children.size)
        assertEquals("Child", children[0].description)
    }

    @Test
    fun `test ExecutionTrace clear`() {
        val trace = ExecutionTrace()

        trace.recordStep(ExecutionStep.StepType.TEMPLATE_START, "Start")
        trace.recordStep(ExecutionStep.StepType.TEMPLATE_END, "End")

        assertEquals(2, trace.size)

        trace.clear()

        assertTrue(trace.isEmpty)
        assertEquals(0, trace.size)
    }

    @Test
    fun `test MermaidExporter generates flowchart`() {
        val trace = ExecutionTrace()
        val exporter = MermaidExporter()

        trace.recordStep(ExecutionStep.StepType.TEMPLATE_START, "Start parsing")
        trace.recordStep(ExecutionStep.StepType.BLOCK_START, "Block 1: tp.file.name()")
        trace.recordStep(ExecutionStep.StepType.BLOCK_END, "Block 1 complete", output = "test.md")
        trace.recordStep(ExecutionStep.StepType.TEMPLATE_END, "Parsing complete")

        val flowchart = exporter.exportFlowchart(trace)

        assertTrue(flowchart.startsWith("flowchart TD"))
        assertTrue(flowchart.contains("step1"))
        assertTrue(flowchart.contains("step2"))
        assertTrue(flowchart.contains("-->"))
    }

    @Test
    fun `test MermaidExporter generates sequence diagram`() {
        val trace = ExecutionTrace()
        val exporter = MermaidExporter()

        trace.recordStep(ExecutionStep.StepType.TEMPLATE_START, "Start")
        trace.recordStep(ExecutionStep.StepType.FUNCTION_CALL, "tp.file.name()")
        trace.recordStep(ExecutionStep.StepType.TEMPLATE_END, "End")

        val sequence = exporter.exportSequenceDiagram(trace)

        assertTrue(sequence.startsWith("sequenceDiagram"))
        assertTrue(sequence.contains("participant"))
        assertTrue(sequence.contains("->>"))
    }

    @Test
    fun `test TracingTemplateParser builds control flow graph`() {
        val parser = TracingTemplateParser(validateSyntax = false)
        val context = TestContextFactory.create()

        val template = "Hello <% \"World\" %>!"
        val result = parser.parse(template, context)

        assertEquals("Hello World!", result)

        val graph = parser.getControlFlowGraph()
        assertNotNull(graph)
        assertFalse(graph!!.isEmpty)


        val nodes = graph.nodes
        assertTrue(nodes.any { it.type == FlowNode.NodeType.START })
        assertTrue(nodes.any { it.type == FlowNode.NodeType.END })
    }

    @Test
    fun `test TracingTemplateParser exports Mermaid`() {
        val parser = TracingTemplateParser(validateSyntax = false)
        val context = TestContextFactory.create()

        parser.parse("Test <% 1 + 1 %> done", context)

        val flowchart = parser.exportMermaidFlowchart("Test Flow")
        assertTrue(flowchart.contains("Test Flow"))
        assertTrue(flowchart.contains("flowchart TD"))

        val sequence = parser.exportMermaidSequence("Test Sequence")
        assertTrue(sequence.contains("Test Sequence"))
        assertTrue(sequence.contains("sequenceDiagram"))
    }

    @Test
    fun `test DebugSession records steps and triggers breakpoints`() {
        val breakpointHits = mutableListOf<DebugBreakpoint>()

        val session = DebugSession(
            onBreakpoint = { bp ->
                breakpointHits.add(bp)
                DebugAction.CONTINUE
            }
        )


        val testNode = ronsijm.templater.ast.StatementNode(
            id = "node_5",
            type = ronsijm.templater.ast.StatementType.VARIABLE_DECLARATION,
            code = "Test statement",
            lineNumber = 5
        )


        session.addBreakpoint(testNode)



        session.recordStep(
            type = ExecutionStep.StepType.STATEMENT,
            description = "Test statement",
            statementNode = testNode
        )

        assertEquals(1, breakpointHits.size)
        assertEquals("Test statement", breakpointHits[0].step.description)
    }

    @Test
    fun `test DebugSession step into mode`() {
        var stepCount = 0

        val session = DebugSession(
            onBreakpoint = { _ ->
                stepCount++
                if (stepCount < 3) DebugAction.STEP_INTO else DebugAction.CONTINUE
            }
        )


        val node1 = ronsijm.templater.ast.StatementNode(id = "node_1", type = ronsijm.templater.ast.StatementType.VARIABLE_DECLARATION, code = "Step 1", lineNumber = 1)
        session.addBreakpoint(node1)
        session.recordStep(ExecutionStep.StepType.STATEMENT, "Step 1", statementNode = node1)


        val node2 = ronsijm.templater.ast.StatementNode(id = "node_2", type = ronsijm.templater.ast.StatementType.VARIABLE_DECLARATION, code = "Step 2", lineNumber = 2)
        session.recordStep(ExecutionStep.StepType.STATEMENT, "Step 2", statementNode = node2)
        val node3 = ronsijm.templater.ast.StatementNode(id = "node_3", type = ronsijm.templater.ast.StatementType.VARIABLE_DECLARATION, code = "Step 3", lineNumber = 3)
        session.recordStep(ExecutionStep.StepType.STATEMENT, "Step 3", statementNode = node3)


        val node4 = ronsijm.templater.ast.StatementNode(id = "node_4", type = ronsijm.templater.ast.StatementType.VARIABLE_DECLARATION, code = "Step 4", lineNumber = 4)
        session.recordStep(ExecutionStep.StepType.STATEMENT, "Step 4", statementNode = node4)

        assertEquals(3, stepCount)
    }

    @Test
    fun `test DebuggingTemplateParser with breakpoints`() {
        val breakpointHits = mutableListOf<DebugBreakpoint>()

        val parser = DebuggingTemplateParser(validateSyntax = false)
        val context = TestContextFactory.create()

        parser.startDebugSession(
            onBreakpoint = { bp ->
                breakpointHits.add(bp)
                DebugAction.CONTINUE
            }
        )


        parser.addBreakpoint(1)

        val result = parser.parse("Hello <% \"World\" %>!", context)

        assertEquals("Hello World!", result.result)
        assertFalse(result.wasStopped)


        assertTrue(result.trace.allSteps.isNotEmpty())
    }

    @Test
    fun `test DebuggingTemplateParser stop action`() {
        val parser = DebuggingTemplateParser(validateSyntax = false)
        val context = TestContextFactory.create()

        var breakpointHit = false
        parser.startDebugSession(
            onBreakpoint = { _ ->
                breakpointHit = true
                DebugAction.STOP
            }
        )



        parser.addBreakpoint(1)

        val result = parser.parse("Hello <% \"World\" %>!", context)


        if (breakpointHit) {
            assertTrue(result.wasStopped)
        }

        assertTrue(result.trace.allSteps.isNotEmpty())
    }

    @Test
    fun `test DebuggingTemplateParser exports Mermaid after debug`() {
        val parser = DebuggingTemplateParser(validateSyntax = false)
        val context = TestContextFactory.create()

        parser.startDebugSession(
            onBreakpoint = { _ -> DebugAction.CONTINUE }
        )

        parser.parse("Test <% 1 + 1 %> done", context)

        val flowchart = parser.exportMermaidFlowchart("Debug Flow")
        assertTrue(flowchart.contains("Debug Flow"))
        assertTrue(flowchart.contains("flowchart TD"))
    }

    @Test
    fun `test DebugSession step over mode sets mode correctly`() {
        var stepCount = 0
        val stepsHit = mutableListOf<String>()

        val session = DebugSession(
            onBreakpoint = { bp ->
                stepCount++
                stepsHit.add(bp.step.description)


                DebugAction.STEP_OVER
            }
        )


        val node1 = ronsijm.templater.ast.StatementNode(id = "node_1", type = ronsijm.templater.ast.StatementType.VARIABLE_DECLARATION, code = "Statement 1", lineNumber = 1)
        session.addBreakpoint(node1)
        session.recordStep(ExecutionStep.StepType.STATEMENT, "Statement 1", statementNode = node1)


        assertEquals(1, stepCount)
        assertEquals(listOf("Statement 1"), stepsHit)
    }

    @Test
    fun `test DebugSession step out mode sets mode correctly`() {
        var stepCount = 0
        val stepsHit = mutableListOf<String>()

        val session = DebugSession(
            onBreakpoint = { bp ->
                stepCount++
                stepsHit.add(bp.step.description)


                DebugAction.STEP_OUT
            }
        )


        val node1 = ronsijm.templater.ast.StatementNode(id = "node_1", type = ronsijm.templater.ast.StatementType.VARIABLE_DECLARATION, code = "Statement 1", lineNumber = 1)
        session.addBreakpoint(node1)
        session.recordStep(ExecutionStep.StepType.STATEMENT, "Statement 1", statementNode = node1)


        assertEquals(1, stepCount)
        assertEquals(listOf("Statement 1"), stepsHit)
    }

    @Test
    fun `test DebugSession multiple breakpoints`() {
        val breakpointLines = mutableListOf<Int>()

        val session = DebugSession(
            onBreakpoint = { bp ->
                breakpointLines.add(bp.step.displayLineNumber ?: -1)
                DebugAction.CONTINUE
            }
        )

        val node1 = ronsijm.templater.ast.StatementNode(id = "node_1", type = ronsijm.templater.ast.StatementType.VARIABLE_DECLARATION, code = "Statement 1", lineNumber = 1)
        val node5 = ronsijm.templater.ast.StatementNode(id = "node_5", type = ronsijm.templater.ast.StatementType.VARIABLE_DECLARATION, code = "Statement 3", lineNumber = 5)
        val node10 = ronsijm.templater.ast.StatementNode(id = "node_10", type = ronsijm.templater.ast.StatementType.VARIABLE_DECLARATION, code = "Statement 5", lineNumber = 10)

        session.addBreakpoint(node1)
        session.addBreakpoint(node5)
        session.addBreakpoint(node10)

        session.recordStep(ExecutionStep.StepType.STATEMENT, "Statement 1", statementNode = node1)
        val node3 = ronsijm.templater.ast.StatementNode(id = "node_3", type = ronsijm.templater.ast.StatementType.VARIABLE_DECLARATION, code = "Statement 2", lineNumber = 3)
        session.recordStep(ExecutionStep.StepType.STATEMENT, "Statement 2", statementNode = node3)
        session.recordStep(ExecutionStep.StepType.STATEMENT, "Statement 3", statementNode = node5)
        val node7 = ronsijm.templater.ast.StatementNode(id = "node_7", type = ronsijm.templater.ast.StatementType.VARIABLE_DECLARATION, code = "Statement 4", lineNumber = 7)
        session.recordStep(ExecutionStep.StepType.STATEMENT, "Statement 4", statementNode = node7)
        session.recordStep(ExecutionStep.StepType.STATEMENT, "Statement 5", statementNode = node10)

        assertEquals(listOf(1, 5, 10), breakpointLines)
    }

    @Test
    fun `test DebugSession remove breakpoint`() {
        val breakpointLines = mutableListOf<Int>()

        val session = DebugSession(
            onBreakpoint = { bp ->
                breakpointLines.add(bp.step.displayLineNumber ?: -1)
                DebugAction.CONTINUE
            }
        )

        val node1 = ronsijm.templater.ast.StatementNode(id = "node_1", type = ronsijm.templater.ast.StatementType.VARIABLE_DECLARATION, code = "Statement 1", lineNumber = 1)
        val node5 = ronsijm.templater.ast.StatementNode(id = "node_5", type = ronsijm.templater.ast.StatementType.VARIABLE_DECLARATION, code = "Statement 2", lineNumber = 5)

        session.addBreakpoint(node1)
        session.addBreakpoint(node5)

        session.recordStep(ExecutionStep.StepType.STATEMENT, "Statement 1", statementNode = node1)


        session.removeBreakpoint(node5)

        session.recordStep(ExecutionStep.StepType.STATEMENT, "Statement 2", statementNode = node5)

        assertEquals(listOf(1), breakpointLines)
    }

    @Test
    fun `test DebugSession has breakpoint`() {
        val session = DebugSession(
            onBreakpoint = { _ -> DebugAction.CONTINUE }
        )

        val node1 = ronsijm.templater.ast.StatementNode(id = "node_1", type = ronsijm.templater.ast.StatementType.VARIABLE_DECLARATION, code = "Statement 1", lineNumber = 1)
        val node5 = ronsijm.templater.ast.StatementNode(id = "node_5", type = ronsijm.templater.ast.StatementType.VARIABLE_DECLARATION, code = "Statement 5", lineNumber = 5)
        val node10 = ronsijm.templater.ast.StatementNode(id = "node_10", type = ronsijm.templater.ast.StatementType.VARIABLE_DECLARATION, code = "Statement 10", lineNumber = 10)

        session.addBreakpoint(node1)
        session.addBreakpoint(node10)

        assertTrue(session.hasBreakpoint(node1))
        assertFalse(session.hasBreakpoint(node5))
        assertTrue(session.hasBreakpoint(node10))
    }

    @Test
    fun `test DebugSession trace contains all steps`() {
        val session = DebugSession(
            onBreakpoint = { _ -> DebugAction.CONTINUE }
        )

        session.recordStep(ExecutionStep.StepType.TEMPLATE_START, "Start")
        session.recordStep(ExecutionStep.StepType.BLOCK_START, "Block 1")
        session.recordStep(ExecutionStep.StepType.BLOCK_END, "Block 1 End")
        session.recordStep(ExecutionStep.StepType.TEMPLATE_END, "End")

        val trace = session.getTrace()
        assertEquals(4, trace.size)
        assertEquals("Start", trace.allSteps[0].description)
        assertEquals("End", trace.allSteps[3].description)
    }

    @Test
    fun `test DebugSession onComplete callback`() {
        var completedTrace: ExecutionTrace? = null

        val session = DebugSession(
            onBreakpoint = { _ -> DebugAction.CONTINUE },
            onComplete = { trace -> completedTrace = trace }
        )

        session.recordStep(ExecutionStep.StepType.TEMPLATE_START, "Start")
        session.recordStep(ExecutionStep.StepType.TEMPLATE_END, "End")

        session.complete()

        assertNotNull(completedTrace)
        assertEquals(2, completedTrace!!.size)
    }

    @Test
    fun `test DebuggingTemplateParser with multiple blocks`() {
        val stepsRecorded = mutableListOf<String>()

        val parser = DebuggingTemplateParser(validateSyntax = false)
        val context = TestContextFactory.create()

        parser.startDebugSession(
            onBreakpoint = { bp ->
                stepsRecorded.add(bp.step.description)
                DebugAction.CONTINUE
            }
        )



        val template = """<%* tR = "First" %>
<%* tR = "Second" %>"""

        val result = parser.parse(template, context)


        assertEquals("First\nSecond", result.result)
        assertFalse(result.wasStopped)


        val statementSteps = result.trace.allSteps.filter { it.type == ExecutionStep.StepType.STATEMENT }
        assertTrue(statementSteps.isNotEmpty(), "Should have recorded statement steps")
    }

    @Test
    fun `test DebuggingTemplateParser without debug session`() {
        val parser = DebuggingTemplateParser(validateSyntax = false)
        val context = TestContextFactory.create()


        val result = parser.parse("Hello <% \"World\" %>!", context)

        assertEquals("Hello World!", result.result)
        assertFalse(result.wasStopped)
        assertTrue(result.trace.isEmpty)
    }

    @Test
    fun `test DebuggingTemplateParser step into all blocks`() {
        val parser = DebuggingTemplateParser(validateSyntax = false)
        val context = TestContextFactory.create()

        parser.startDebugSession(
            onBreakpoint = { bp ->
                DebugAction.STEP_INTO
            }
        )



        val result = parser.parse("<%* tR = '1' %><%* tR = '2' %><%* tR = '3' %>", context)


        assertEquals("123", result.result)

        val statementSteps = result.trace.allSteps.filter { it.type == ExecutionStep.StepType.STATEMENT }
        assertTrue(statementSteps.size >= 3, "Should have recorded at least 3 statement steps (one per block)")
    }

    @Test
    fun `test ExecutionStep variables capture`() {
        val trace = ExecutionTrace()

        val variables = mapOf(
            "name" to "test",
            "count" to 42,
            "items" to listOf("a", "b", "c")
        )

        trace.recordStep(
            type = ExecutionStep.StepType.BLOCK_START,
            description = "Test block",
            variables = variables
        )

        val step = trace.allSteps.first()
        assertEquals("test", step.variables["name"])
        assertEquals(42, step.variables["count"])
        assertEquals(listOf("a", "b", "c"), step.variables["items"])
    }

    @Test
    fun `test ExecutionStep position tracking`() {
        val trace = ExecutionTrace()

        val testNode = ronsijm.templater.ast.StatementNode(
            id = "test_node",
            type = ronsijm.templater.ast.StatementType.VARIABLE_DECLARATION,
            code = "Block at position",
            lineNumber = 5
        )
        trace.recordStep(
            type = ExecutionStep.StepType.BLOCK_START,
            description = "Block at position",
            statementNode = testNode,
            startPosition = 100,
            endPosition = 150
        )

        val step = trace.allSteps.first()
        assertEquals(5, step.displayLineNumber)
        assertEquals(100, step.startPosition)
        assertEquals(150, step.endPosition)
    }

    @Test
    fun `test DebugBreakpoint contains trace context`() {
        var capturedBreakpoint: DebugBreakpoint? = null

        val session = DebugSession(
            onBreakpoint = { bp ->
                capturedBreakpoint = bp
                DebugAction.CONTINUE
            }
        )

        val node1 = ronsijm.templater.ast.StatementNode(id = "node_1", type = ronsijm.templater.ast.StatementType.VARIABLE_DECLARATION, code = "Statement", lineNumber = 1)
        session.addBreakpoint(node1)

        session.recordStep(ExecutionStep.StepType.TEMPLATE_START, "Start")
        session.recordStep(ExecutionStep.StepType.STATEMENT, "Statement", statementNode = node1)

        assertNotNull(capturedBreakpoint)
        assertEquals("Statement", capturedBreakpoint!!.step.description)
        assertEquals(2, capturedBreakpoint!!.trace.size)
    }
}

