package ronsijm.templater.diagram

import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import ronsijm.templater.debug.ControlFlowGraph
import ronsijm.templater.debug.FlowEdge
import ronsijm.templater.debug.FlowNode
import ronsijm.templater.debug.FunctionScope
import ronsijm.templater.settings.MermaidNodeStyles
import java.awt.*
import java.awt.event.*
import java.awt.geom.AffineTransform
import java.awt.geom.Path2D
import java.awt.geom.RoundRectangle2D
import javax.swing.JPanel
import kotlin.math.max
import kotlin.math.min


class ControlFlowDiagramPanel : JPanel() {

    private var graph: ControlFlowGraph? = null
    private var nodeStyles: MermaidNodeStyles? = null


    private val nodePositions = mutableMapOf<String, Point>()
    private val nodeSizes = mutableMapOf<String, Dimension>()
    private val scopeBounds = mutableMapOf<String, Rectangle>()


    var activeNodeId: String? = null
        private set
    private val visitedNodeIds = mutableSetOf<String>()
    private var runtimeVisualizationEnabled = false


    private var scale = 1.0
    private var translateX = 0.0
    private var translateY = 0.0
    private var lastMousePoint: Point? = null


    private val nodeWidth = 160
    private val nodeHeight = 40
    private val horizontalSpacing = 80
    private val verticalSpacing = 60


    private val scopePadding = 15
    private val scopeTitleHeight = 20

    init {
        background = JBColor.background()
        isDoubleBuffered = true
        setupMouseListeners()
        setupKeyboardListeners()
    }

    private fun setupMouseListeners() {




        addMouseWheelListener { e ->
            val scrollAmount = e.wheelRotation * 30

            when {
                e.isControlDown -> {

                    val oldScale = scale
                    scale = if (e.wheelRotation < 0) {
                        min(scale * 1.1, 3.0)
                    } else {
                        max(scale / 1.1, 0.3)
                    }


                    val mouseX = e.x
                    val mouseY = e.y
                    translateX = mouseX - (mouseX - translateX) * (scale / oldScale)
                    translateY = mouseY - (mouseY - translateY) * (scale / oldScale)
                }
                e.isShiftDown -> {

                    translateX -= scrollAmount
                }
                else -> {

                    translateY -= scrollAmount
                }
            }

            e.consume()
            repaint()
        }


        addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                lastMousePoint = e.point

                requestFocusInWindow()
            }
            override fun mouseReleased(e: MouseEvent) {
                lastMousePoint = null
            }
        })

        addMouseMotionListener(object : MouseMotionAdapter() {
            override fun mouseDragged(e: MouseEvent) {
                lastMousePoint?.let { last ->
                    translateX += e.x - last.x
                    translateY += e.y - last.y
                    lastMousePoint = e.point
                    repaint()
                }
            }
        })


        isFocusable = true
    }

    private fun setupKeyboardListeners() {

        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                val panAmount = 30.0
                when (e.keyCode) {
                    KeyEvent.VK_UP -> {
                        translateY += panAmount
                        repaint()
                        e.consume()
                    }
                    KeyEvent.VK_DOWN -> {
                        translateY -= panAmount
                        repaint()
                        e.consume()
                    }
                    KeyEvent.VK_LEFT -> {
                        translateX += panAmount
                        repaint()
                        e.consume()
                    }
                    KeyEvent.VK_RIGHT -> {
                        translateX -= panAmount
                        repaint()
                        e.consume()
                    }
                    KeyEvent.VK_PLUS, KeyEvent.VK_EQUALS, KeyEvent.VK_ADD -> {

                        val centerX = width / 2.0
                        val centerY = height / 2.0
                        val oldScale = scale
                        scale = min(scale * 1.1, 3.0)
                        translateX = centerX - (centerX - translateX) * (scale / oldScale)
                        translateY = centerY - (centerY - translateY) * (scale / oldScale)
                        repaint()
                        e.consume()
                    }
                    KeyEvent.VK_MINUS, KeyEvent.VK_SUBTRACT -> {

                        val centerX = width / 2.0
                        val centerY = height / 2.0
                        val oldScale = scale
                        scale = max(scale / 1.1, 0.3)
                        translateX = centerX - (centerX - translateX) * (scale / oldScale)
                        translateY = centerY - (centerY - translateY) * (scale / oldScale)
                        repaint()
                        e.consume()
                    }
                    KeyEvent.VK_0 -> {

                        scale = 1.0
                        centerGraph()
                        repaint()
                        e.consume()
                    }
                }
            }
        })
    }

    fun setGraph(graph: ControlFlowGraph?, styles: MermaidNodeStyles? = null) {
        this.graph = graph
        this.nodeStyles = styles
        layoutGraph()
        centerGraph()
        repaint()
    }

    fun resetView() {
        scale = 1.0
        centerGraph()
        repaint()
    }


    fun setRuntimeVisualizationEnabled(enabled: Boolean) {
        runtimeVisualizationEnabled = enabled
        if (!enabled) {
            clearRuntimeState()
        }
        repaint()
    }


    fun setActiveNode(nodeId: String?) {
        val previousActive = activeNodeId
        activeNodeId = nodeId


        if (previousActive != null && previousActive != nodeId) {
            visitedNodeIds.add(previousActive)
        }


        if (nodeId != null) {
            scrollToNode(nodeId)
        }

        repaint()
    }


    fun markNodeVisited(nodeId: String) {
        visitedNodeIds.add(nodeId)
        repaint()
    }


    fun clearRuntimeState() {
        activeNodeId = null
        visitedNodeIds.clear()
        repaint()
    }


    fun scrollToNode(nodeId: String) {
        val pos = nodePositions[nodeId] ?: return
        val size = nodeSizes[nodeId] ?: Dimension(nodeWidth, nodeHeight)


        val nodeCenterX = pos.x + size.width / 2
        val nodeCenterY = pos.y + size.height / 2


        val targetTranslateX = width / 2.0 - nodeCenterX * scale
        val targetTranslateY = height / 2.0 - nodeCenterY * scale


        translateX = targetTranslateX
        translateY = targetTranslateY
    }

    private fun centerGraph() {
        if (nodePositions.isEmpty()) {
            translateX = 50.0
            translateY = 50.0
            return
        }

        val minX = nodePositions.values.minOfOrNull { it.x } ?: 0
        val minY = nodePositions.values.minOfOrNull { it.y } ?: 0
        val maxX = nodePositions.values.maxOfOrNull { it.x + nodeWidth } ?: 0
        val maxY = nodePositions.values.maxOfOrNull { it.y + nodeHeight } ?: 0

        val graphWidth = maxX - minX
        val graphHeight = maxY - minY

        translateX = (width - graphWidth * scale) / 2 - minX * scale
        translateY = (height - graphHeight * scale) / 2 - minY * scale
    }

    private fun layoutGraph() {
        nodePositions.clear()
        nodeSizes.clear()
        scopeBounds.clear()

        val g = graph ?: return
        if (g.isEmpty) return


        val layers = assignLayers(g)

        var y = 20
        for ((layerIndex, layer) in layers.withIndex()) {
            val layerWidth = layer.size * (nodeWidth + horizontalSpacing) - horizontalSpacing
            var x = 20

            for (node in layer) {
                nodePositions[node.id] = Point(x, y)
                nodeSizes[node.id] = Dimension(nodeWidth, nodeHeight)
                x += nodeWidth + horizontalSpacing
            }

            y += nodeHeight + verticalSpacing
        }


        calculateScopeBounds(g)
    }

    private fun calculateScopeBounds(graph: ControlFlowGraph) {
        for (scope in graph.functionScopes) {
            if (scope.nodeIds.isEmpty()) continue


            var minX = Int.MAX_VALUE
            var minY = Int.MAX_VALUE
            var maxX = Int.MIN_VALUE
            var maxY = Int.MIN_VALUE

            for (nodeId in scope.nodeIds) {
                val pos = nodePositions[nodeId] ?: continue
                val size = nodeSizes[nodeId] ?: Dimension(nodeWidth, nodeHeight)

                minX = min(minX, pos.x)
                minY = min(minY, pos.y)
                maxX = max(maxX, pos.x + size.width)
                maxY = max(maxY, pos.y + size.height)
            }

            if (minX != Int.MAX_VALUE) {

                scopeBounds[scope.id] = Rectangle(
                    minX - scopePadding,
                    minY - scopePadding - scopeTitleHeight,
                    maxX - minX + 2 * scopePadding,
                    maxY - minY + 2 * scopePadding + scopeTitleHeight
                )
            }
        }
    }

    private fun assignLayers(graph: ControlFlowGraph): List<List<FlowNode>> {
        val layers = mutableListOf<MutableList<FlowNode>>()
        val nodeToLayer = mutableMapOf<String, Int>()
        val visited = mutableSetOf<String>()


        val outgoing = mutableMapOf<String, MutableList<String>>()
        val incoming = mutableMapOf<String, MutableList<String>>()

        for (node in graph.nodes) {
            outgoing[node.id] = mutableListOf()
            incoming[node.id] = mutableListOf()
        }

        for (edge in graph.edges) {
            outgoing[edge.from]?.add(edge.to)
            incoming[edge.to]?.add(edge.from)
        }


        val queue = ArrayDeque<FlowNode>()


        for (node in graph.nodes) {
            if (incoming[node.id]?.isEmpty() == true) {
                queue.add(node)
                nodeToLayer[node.id] = 0
            }
        }


        if (queue.isEmpty() && graph.nodes.isNotEmpty()) {
            val first = graph.nodes.first()
            queue.add(first)
            nodeToLayer[first.id] = 0
        }

        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            if (node.id in visited) continue
            visited.add(node.id)

            val layer = nodeToLayer[node.id] ?: 0


            while (layers.size <= layer) {
                layers.add(mutableListOf())
            }
            layers[layer].add(node)


            for (targetId in outgoing[node.id] ?: emptyList()) {
                if (targetId !in visited) {
                    val targetLayer = max(layer + 1, nodeToLayer[targetId] ?: 0)
                    nodeToLayer[targetId] = targetLayer
                    val targetNode = graph.nodes.find { it.id == targetId }
                    if (targetNode != null) {
                        queue.add(targetNode)
                    }
                }
            }
        }


        for (node in graph.nodes) {
            if (node.id !in visited) {
                if (layers.isEmpty()) layers.add(mutableListOf())
                layers.last().add(node)
            }
        }

        return layers
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)


        val oldTransform = g2d.transform
        g2d.translate(translateX, translateY)
        g2d.scale(scale, scale)

        val currentGraph = graph
        if (currentGraph == null || currentGraph.isEmpty) {
            g2d.transform = oldTransform
            g2d.color = JBColor.foreground()
            g2d.drawString("No control flow to display", 20, 30)
            return
        }


        drawFunctionScopes(g2d, currentGraph)


        drawEdges(g2d, currentGraph)


        drawNodes(g2d, currentGraph)

        g2d.transform = oldTransform
    }

    private fun drawFunctionScopes(g2d: Graphics2D, graph: ControlFlowGraph) {
        for (scope in graph.functionScopes) {
            val bounds = scopeBounds[scope.id] ?: continue


            val scopeFillColor = JBColor(
                Color(200, 180, 220, 40),
                Color(100, 80, 120, 60)
            )
            val scopeStrokeColor = JBColor(
                Color(150, 100, 180),
                Color(180, 140, 200)
            )


            g2d.color = scopeFillColor
            val roundRect = RoundRectangle2D.Double(
                bounds.x.toDouble(),
                bounds.y.toDouble(),
                bounds.width.toDouble(),
                bounds.height.toDouble(),
                12.0, 12.0
            )
            g2d.fill(roundRect)


            g2d.color = scopeStrokeColor
            g2d.stroke = BasicStroke(1.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0f, floatArrayOf(5f, 3f), 0f)
            g2d.draw(roundRect)


            g2d.color = scopeStrokeColor
            g2d.font = JBUI.Fonts.label().deriveFont(Font.ITALIC)
            val title = "${scope.name}()"
            val fm = g2d.fontMetrics
            val titleX = bounds.x + scopePadding
            val titleY = bounds.y + fm.ascent + 2
            g2d.drawString(title, titleX, titleY)
        }


        g2d.stroke = BasicStroke(1.5f)
    }

    private fun drawEdges(g2d: Graphics2D, graph: ControlFlowGraph) {
        for (edge in graph.edges) {
            val fromPos = nodePositions[edge.from] ?: continue
            val toPos = nodePositions[edge.to] ?: continue

            val fromCenterX = fromPos.x + nodeWidth / 2
            val fromBottomY = fromPos.y + nodeHeight
            val toCenterX = toPos.x + nodeWidth / 2
            val toTopY = toPos.y


            g2d.color = when (edge.type) {
                FlowEdge.EdgeType.TRUE_BRANCH -> JBColor(Color(0, 150, 0), Color(100, 200, 100))
                FlowEdge.EdgeType.FALSE_BRANCH -> JBColor(Color(200, 0, 0), Color(255, 100, 100))
                FlowEdge.EdgeType.LOOP_BACK -> JBColor(Color(0, 100, 200), Color(100, 150, 255))
                FlowEdge.EdgeType.LOOP_EXIT -> JBColor(Color(150, 100, 0), Color(200, 150, 50))
                FlowEdge.EdgeType.PARALLEL -> JBColor(Color(150, 0, 150), Color(200, 100, 200))
                else -> JBColor.foreground()
            }

            g2d.stroke = BasicStroke(1.5f)


            if (edge.type == FlowEdge.EdgeType.LOOP_BACK || fromPos.y >= toPos.y) {

                val path = Path2D.Double()
                path.moveTo(fromCenterX.toDouble(), fromBottomY.toDouble())
                val controlX = max(fromCenterX, toCenterX) + 60.0
                path.curveTo(
                    controlX, fromBottomY.toDouble(),
                    controlX, toTopY.toDouble(),
                    toCenterX.toDouble(), toTopY.toDouble()
                )
                g2d.draw(path)
            } else {

                g2d.drawLine(fromCenterX, fromBottomY, toCenterX, toTopY)
            }


            drawArrowhead(g2d, fromCenterX, fromBottomY, toCenterX, toTopY)
        }
    }

    private fun drawArrowhead(g2d: Graphics2D, x1: Int, y1: Int, x2: Int, y2: Int) {
        val arrowSize = 8
        val dx = x2 - x1
        val dy = y2 - y1
        val len = kotlin.math.sqrt((dx * dx + dy * dy).toDouble())
        if (len == 0.0) return

        val unitDx = dx / len
        val unitDy = dy / len

        val arrowX1 = (x2 - arrowSize * unitDx - arrowSize * unitDy * 0.5).toInt()
        val arrowY1 = (y2 - arrowSize * unitDy + arrowSize * unitDx * 0.5).toInt()
        val arrowX2 = (x2 - arrowSize * unitDx + arrowSize * unitDy * 0.5).toInt()
        val arrowY2 = (y2 - arrowSize * unitDy - arrowSize * unitDx * 0.5).toInt()

        g2d.fillPolygon(intArrayOf(x2, arrowX1, arrowX2), intArrayOf(y2, arrowY1, arrowY2), 3)
    }

    private fun drawNodes(g2d: Graphics2D, graph: ControlFlowGraph) {
        for (node in graph.nodes) {
            val pos = nodePositions[node.id] ?: continue
            val size = nodeSizes[node.id] ?: Dimension(nodeWidth, nodeHeight)

            val isActive = runtimeVisualizationEnabled && node.id == activeNodeId
            val isVisited = runtimeVisualizationEnabled && node.id in visitedNodeIds


            val (baseFillColor, baseStrokeColor) = getNodeColors(node.type)


            val (fillColor, strokeColor) = when {
                isActive -> Pair(getActiveNodeColor(), getActiveNodeStrokeColor())
                isVisited -> Pair(getVisitedNodeColor(baseFillColor), getVisitedNodeStrokeColor(baseStrokeColor))
                else -> Pair(baseFillColor, baseStrokeColor)
            }


            if (isActive) {
                drawActiveNodeGlow(g2d, pos, size)
            }


            g2d.color = fillColor
            val strokeWidth = if (isActive) 3f else 2f
            when (node.type) {
                FlowNode.NodeType.START, FlowNode.NodeType.END -> {

                    g2d.fillRoundRect(pos.x, pos.y, size.width, size.height, 20, 20)
                    g2d.color = strokeColor
                    g2d.stroke = BasicStroke(strokeWidth)
                    g2d.drawRoundRect(pos.x, pos.y, size.width, size.height, 20, 20)
                }
                FlowNode.NodeType.CONDITION -> {

                    val diamond = createDiamond(pos.x, pos.y, size.width, size.height)
                    g2d.fill(diamond)
                    g2d.color = strokeColor
                    g2d.stroke = BasicStroke(strokeWidth)
                    g2d.draw(diamond)
                }
                else -> {

                    g2d.fillRect(pos.x, pos.y, size.width, size.height)
                    g2d.color = strokeColor
                    g2d.stroke = BasicStroke(if (isActive) strokeWidth else 1.5f)
                    g2d.drawRect(pos.x, pos.y, size.width, size.height)
                }
            }


            g2d.color = JBColor.foreground()
            g2d.font = JBUI.Fonts.label()
            val fm = g2d.fontMetrics
            val label = truncateLabel(node.label, fm, size.width - 10)
            val labelX = pos.x + (size.width - fm.stringWidth(label)) / 2
            val labelY = pos.y + (size.height + fm.ascent - fm.descent) / 2
            g2d.drawString(label, labelX, labelY)
        }
    }

    private fun drawActiveNodeGlow(g2d: Graphics2D, pos: Point, size: Dimension) {
        val oldComposite = g2d.composite
        val glowColor = getActiveNodeGlowColor()


        for (i in 3 downTo 1) {
            val alpha = (0.15f * i).coerceAtMost(1f)
            g2d.composite = java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha)
            g2d.color = glowColor
            g2d.stroke = BasicStroke((4 + i * 3).toFloat())
            g2d.drawRoundRect(pos.x - i * 2, pos.y - i * 2, size.width + i * 4, size.height + i * 4, 10, 10)
        }

        g2d.composite = oldComposite
    }

    private fun getActiveNodeColor(): Color {
        return JBColor(Color(255, 200, 100), Color(200, 150, 50))
    }

    private fun getActiveNodeStrokeColor(): Color {
        return JBColor(Color(255, 140, 0), Color(255, 180, 50))
    }

    private fun getActiveNodeGlowColor(): Color {
        return JBColor(Color(255, 200, 0), Color(255, 220, 100))
    }

    private fun getVisitedNodeColor(baseColor: Color): Color {

        val r = ((baseColor.red * 0.7 + 100 * 0.3).toInt()).coerceIn(0, 255)
        val g = ((baseColor.green * 0.7 + 180 * 0.3).toInt()).coerceIn(0, 255)
        val b = ((baseColor.blue * 0.7 + 100 * 0.3).toInt()).coerceIn(0, 255)
        return Color(r, g, b)
    }

    private fun getVisitedNodeStrokeColor(baseColor: Color): Color {

        val r = ((baseColor.red * 0.6 + 50 * 0.4).toInt()).coerceIn(0, 255)
        val g = ((baseColor.green * 0.6 + 150 * 0.4).toInt()).coerceIn(0, 255)
        val b = ((baseColor.blue * 0.6 + 50 * 0.4).toInt()).coerceIn(0, 255)
        return Color(r, g, b)
    }

    private fun createDiamond(x: Int, y: Int, width: Int, height: Int): Polygon {
        val centerX = x + width / 2
        val centerY = y + height / 2
        return Polygon(
            intArrayOf(centerX, x + width, centerX, x),
            intArrayOf(y, centerY, y + height, centerY),
            4
        )
    }

    private fun getNodeColors(type: FlowNode.NodeType): Pair<Color, Color> {

        val styles = nodeStyles

        return when (type) {
            FlowNode.NodeType.START, FlowNode.NodeType.END ->
                parseStyleColors(styles?.startEnd) ?: Pair(
                    JBColor(Color(159, 255, 159), Color(80, 150, 80)),
                    JBColor(Color(51, 51, 51), Color(200, 200, 200))
                )
            FlowNode.NodeType.CONDITION ->
                parseStyleColors(styles?.condition) ?: Pair(
                    JBColor(Color(255, 215, 0), Color(180, 150, 0)),
                    JBColor(Color(51, 51, 51), Color(200, 200, 200))
                )
            FlowNode.NodeType.LOOP_START ->
                parseStyleColors(styles?.loop) ?: Pair(
                    JBColor(Color(135, 206, 235), Color(70, 130, 180)),
                    JBColor(Color(51, 51, 51), Color(200, 200, 200))
                )
            FlowNode.NodeType.LOOP_END ->
                parseStyleColors(styles?.loopEnd) ?: Pair(
                    JBColor(Color(176, 224, 230), Color(100, 150, 160)),
                    JBColor(Color(51, 51, 51), Color(200, 200, 200))
                )
            FlowNode.NodeType.INTERPOLATION ->
                parseStyleColors(styles?.interpolation) ?: Pair(
                    JBColor(Color(152, 251, 152), Color(80, 180, 80)),
                    JBColor(Color(51, 51, 51), Color(200, 200, 200))
                )
            FlowNode.NodeType.EXECUTION ->
                parseStyleColors(styles?.execution) ?: Pair(
                    JBColor(Color(187, 187, 255), Color(100, 100, 180)),
                    JBColor(Color(51, 51, 51), Color(200, 200, 200))
                )
            FlowNode.NodeType.FUNCTION_DECL ->
                parseStyleColors(styles?.funcDecl) ?: Pair(
                    JBColor(Color(221, 160, 221), Color(150, 100, 150)),
                    JBColor(Color(51, 51, 51), Color(200, 200, 200))
                )
            FlowNode.NodeType.FUNCTION_CALL ->
                parseStyleColors(styles?.funcCall) ?: Pair(
                    JBColor(Color(255, 255, 153), Color(180, 180, 80)),
                    JBColor(Color(51, 51, 51), Color(200, 200, 200))
                )
            FlowNode.NodeType.VARIABLE_ASSIGN ->
                parseStyleColors(styles?.variable) ?: Pair(
                    JBColor(Color(255, 187, 255), Color(180, 100, 180)),
                    JBColor(Color(51, 51, 51), Color(200, 200, 200))
                )
            FlowNode.NodeType.RETURN ->
                parseStyleColors(styles?.returnNode) ?: Pair(
                    JBColor(Color(255, 160, 122), Color(180, 100, 80)),
                    JBColor(Color(51, 51, 51), Color(200, 200, 200))
                )
            FlowNode.NodeType.FORK ->
                parseStyleColors(styles?.fork) ?: Pair(
                    JBColor(Color(255, 107, 107), Color(180, 70, 70)),
                    JBColor(Color(51, 51, 51), Color(200, 200, 200))
                )
            FlowNode.NodeType.JOIN ->
                parseStyleColors(styles?.join) ?: Pair(
                    JBColor(Color(78, 205, 196), Color(50, 150, 140)),
                    JBColor(Color(51, 51, 51), Color(200, 200, 200))
                )
            FlowNode.NodeType.ERROR ->
                parseStyleColors(styles?.error) ?: Pair(
                    JBColor(Color(255, 102, 102), Color(200, 70, 70)),
                    JBColor(Color(51, 51, 51), Color(200, 200, 200))
                )
        }
    }

    private fun parseStyleColors(style: ronsijm.templater.settings.MermaidNodeStyle?): Pair<Color, Color>? {
        if (style == null) return null

        val fill = parseColor(style.fill) ?: return null
        val stroke = parseColor(style.stroke) ?: return null

        return Pair(
            JBColor(fill, darkenColor(fill)),
            JBColor(stroke, lightenColor(stroke))
        )
    }

    private fun parseColor(colorStr: String): Color? {
        return try {
            if (colorStr.startsWith("#")) {
                Color.decode(colorStr)
            } else {

                val field = Color::class.java.getField(colorStr.lowercase())
                field.get(null) as? Color
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun darkenColor(color: Color): Color {
        return Color(
            max(0, color.red - 50),
            max(0, color.green - 50),
            max(0, color.blue - 50)
        )
    }

    private fun lightenColor(color: Color): Color {
        return Color(
            min(255, color.red + 50),
            min(255, color.green + 50),
            min(255, color.blue + 50)
        )
    }

    private fun truncateLabel(label: String, fm: FontMetrics, maxWidth: Int): String {
        if (fm.stringWidth(label) <= maxWidth) return label

        var truncated = label
        while (truncated.isNotEmpty() && fm.stringWidth("$truncated...") > maxWidth) {
            truncated = truncated.dropLast(1)
        }
        return if (truncated.isEmpty()) "..." else "$truncated..."
    }
}
