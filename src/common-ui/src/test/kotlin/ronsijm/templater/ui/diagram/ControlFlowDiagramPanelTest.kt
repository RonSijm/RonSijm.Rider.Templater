package ronsijm.templater.ui.diagram

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ronsijm.templater.debug.ControlFlowGraph
import ronsijm.templater.debug.FlowEdge
import ronsijm.templater.debug.FlowNode
import ronsijm.templater.ui.theme.DefaultThemeColors
import java.awt.Dimension

class ControlFlowDiagramPanelTest {

    private lateinit var panel: ControlFlowDiagramPanel

    @BeforeEach
    fun setUp() {
        panel = ControlFlowDiagramPanel(DefaultThemeColors())
        panel.size = Dimension(800, 600)
    }

    @Test
    fun `panel initializes with null graph`() {
        assertNull(panel.getGraph())
    }

    @Test
    fun `setGraph stores the graph`() {
        val graph = createSimpleGraph()
        panel.setGraph(graph)
        assertEquals(graph, panel.getGraph())
    }

    @Test
    fun `setGraph with null clears the graph`() {
        val graph = createSimpleGraph()
        panel.setGraph(graph)
        panel.setGraph(null)
        assertNull(panel.getGraph())
    }

    @Test
    fun `resetView does not throw with null graph`() {
        assertDoesNotThrow { panel.resetView() }
    }

    @Test
    fun `resetView does not throw with valid graph`() {
        panel.setGraph(createSimpleGraph())
        assertDoesNotThrow { panel.resetView() }
    }

    @Test
    fun `panel handles empty graph`() {
        val emptyGraph = ControlFlowGraph(
            nodes = emptyList(),
            edges = emptyList(),
            functionScopes = emptyList()
        )
        assertDoesNotThrow { panel.setGraph(emptyGraph) }
    }

    @Test
    fun `panel handles graph with single node`() {
        val singleNodeGraph = ControlFlowGraph(
            nodes = listOf(FlowNode("1", FlowNode.NodeType.START, "Start")),
            edges = emptyList(),
            functionScopes = emptyList()
        )
        assertDoesNotThrow { panel.setGraph(singleNodeGraph) }
    }

    @Test
    fun `panel handles graph with cycle`() {
        val cyclicGraph = ControlFlowGraph(
            nodes = listOf(
                FlowNode("1", FlowNode.NodeType.START, "Start"),
                FlowNode("2", FlowNode.NodeType.LOOP_START, "Loop"),
                FlowNode("3", FlowNode.NodeType.END, "End")
            ),
            edges = listOf(
                FlowEdge("1", "2", null, FlowEdge.EdgeType.NORMAL),
                FlowEdge("2", "3", null, FlowEdge.EdgeType.LOOP_EXIT),
                FlowEdge("2", "2", null, FlowEdge.EdgeType.LOOP_BACK)
            ),
            functionScopes = emptyList()
        )
        assertDoesNotThrow { panel.setGraph(cyclicGraph) }
    }

    @Test
    fun `panel handles graph with multiple entry points`() {
        val multiEntryGraph = ControlFlowGraph(
            nodes = listOf(
                FlowNode("1", FlowNode.NodeType.START, "Entry1"),
                FlowNode("2", FlowNode.NodeType.START, "Entry2"),
                FlowNode("3", FlowNode.NodeType.EXECUTION, "Merge")
            ),
            edges = listOf(
                FlowEdge("1", "3"),
                FlowEdge("2", "3")
            ),
            functionScopes = emptyList()
        )
        assertDoesNotThrow { panel.setGraph(multiEntryGraph) }
    }

    @Test
    fun `panel handles all node types`() {
        val allTypesGraph = ControlFlowGraph(
            nodes = FlowNode.NodeType.entries.mapIndexed { index, type ->
                FlowNode("$index", type, type.name)
            },
            edges = emptyList(),
            functionScopes = emptyList()
        )
        assertDoesNotThrow { panel.setGraph(allTypesGraph) }
    }

    @Test
    fun `panel handles all edge types`() {
        val nodes = listOf(
            FlowNode("1", FlowNode.NodeType.START, "Start"),
            FlowNode("2", FlowNode.NodeType.EXECUTION, "Node2"),
            FlowNode("3", FlowNode.NodeType.EXECUTION, "Node3"),
            FlowNode("4", FlowNode.NodeType.EXECUTION, "Node4"),
            FlowNode("5", FlowNode.NodeType.EXECUTION, "Node5"),
            FlowNode("6", FlowNode.NodeType.END, "End")
        )
        val edges = listOf(
            FlowEdge("1", "2", null, FlowEdge.EdgeType.NORMAL),
            FlowEdge("2", "3", null, FlowEdge.EdgeType.TRUE_BRANCH),
            FlowEdge("2", "4", null, FlowEdge.EdgeType.FALSE_BRANCH),
            FlowEdge("3", "5", null, FlowEdge.EdgeType.LOOP_BACK),
            FlowEdge("4", "5", null, FlowEdge.EdgeType.LOOP_EXIT),
            FlowEdge("5", "6", null, FlowEdge.EdgeType.PARALLEL)
        )
        val graph = ControlFlowGraph(nodes, edges, emptyList())
        assertDoesNotThrow { panel.setGraph(graph) }
    }

    private fun createSimpleGraph(): ControlFlowGraph {
        return ControlFlowGraph(
            nodes = listOf(
                FlowNode("1", FlowNode.NodeType.START, "Start"),
                FlowNode("2", FlowNode.NodeType.EXECUTION, "Process"),
                FlowNode("3", FlowNode.NodeType.END, "End")
            ),
            edges = listOf(
                FlowEdge("1", "2"),
                FlowEdge("2", "3")
            ),
            functionScopes = emptyList()
        )
    }
}

