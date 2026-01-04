package ronsijm.templater.debug

import ronsijm.templater.settings.MermaidNodeStyles


class MermaidExporter {


    fun exportFlowchart(
        graph: ControlFlowGraph,
        title: String = "Template Execution Flow",
        includeExplanations: Boolean = true,
        nodeStyles: MermaidNodeStyles? = null
    ): String {
        if (graph.isEmpty) {
            return "flowchart TD\n    empty[No template blocks found]"
        }

        val styles = nodeStyles ?: MermaidNodeStyles()

        val sb = StringBuilder()
        sb.appendLine("flowchart TD")
        sb.appendLine("%% $title")
        sb.appendLine()


        sb.appendLine("%% Node styles")
        for (line in styles.toClassDefLines()) {
            sb.appendLine(line)
        }
        sb.appendLine()


        val nodesInScopes = graph.functionScopes.flatMap { it.nodeIds }.toSet()


        for (node in graph.nodes) {
            if (node.id !in nodesInScopes) {
                val nodeShape = getNodeShape(node)
                val styleClass = getStyleClass(node.type)

                sb.appendLine("${node.id}$nodeShape")
                if (styleClass != null) {
                    sb.appendLine("class ${node.id} $styleClass")
                }
            }
        }
        sb.appendLine()


        if (graph.functionScopes.isNotEmpty()) {
            sb.appendLine("%% Function subgraphs")
            for (scope in graph.functionScopes) {
                sb.appendLine("subgraph ${scope.id}[\"${escapeLabel(scope.name)}()\"]")
                sb.appendLine("    direction TB")


                for (nodeId in scope.nodeIds) {
                    val node = graph.nodes.find { it.id == nodeId }
                    if (node != null) {
                        val nodeShape = getNodeShape(node)
                        val styleClass = getStyleClass(node.type)

                        sb.appendLine("    ${node.id}$nodeShape")
                        if (styleClass != null) {
                            sb.appendLine("    class ${node.id} $styleClass")
                        }
                    }
                }
                sb.appendLine("end")
                sb.appendLine()
            }
        }


        sb.appendLine("%% Control flow")
        for (edge in graph.edges) {
            val edgeLine = formatEdge(edge)
            sb.appendLine(edgeLine)
        }


        if (includeExplanations && graph.parallelGroupExplanations.isNotEmpty()) {
            sb.appendLine()
            sb.appendLine("%% ========================================")
            sb.appendLine("%% PARALLEL EXECUTION ANALYSIS")
            sb.appendLine("%% ========================================")

            for ((index, explanation) in graph.parallelGroupExplanations.withIndex()) {
                sb.appendLine("%%")
                sb.appendLine("%% Parallel Group ${index + 1} (${explanation.forkNodeId}):")
                sb.appendLine("%% ${explanation.reason}")
                sb.appendLine("%%")
                sb.appendLine("%% Blocks in this group:")
                for (block in explanation.blockDescriptions) {
                    val reads = block.variablesRead.filter { it != "tR" }
                    val writes = block.variablesWritten.filter { it != "tR" }
                    sb.appendLine("%%   - ${block.label}")
                    if (reads.isNotEmpty() || writes.isNotEmpty()) {
                        if (reads.isNotEmpty()) {
                            sb.appendLine("%%     reads: ${reads.joinToString(", ")}")
                        }
                        if (writes.isNotEmpty()) {
                            sb.appendLine("%%     writes: ${writes.joinToString(", ")}")
                        }
                    } else {
                        sb.appendLine("%%     (no variable dependencies)")
                    }
                }
            }
        }

        return sb.toString()
    }


    fun exportSequenceDiagram(graph: ControlFlowGraph, title: String = "Template Execution"): String {
        if (graph.isEmpty) {
            return "sequenceDiagram\n    Note over Parser: No template blocks found"
        }

        val sb = StringBuilder()
        sb.appendLine("sequenceDiagram")
        sb.appendLine("    title $title")
        sb.appendLine()
        sb.appendLine("    participant T as Template")
        sb.appendLine("    participant P as Parser")
        sb.appendLine("    participant E as Evaluator")
        sb.appendLine("    participant M as Modules")
        sb.appendLine()

        for (node in graph.nodes) {
            when (node.type) {
                FlowNode.NodeType.START ->
                    sb.appendLine("    T->>P: Start parsing")
                FlowNode.NodeType.END ->
                    sb.appendLine("    P->>T: Return result")
                FlowNode.NodeType.INTERPOLATION ->
                    sb.appendLine("    P->>E: Interpolate: ${escapeLabel(node.label)}")
                FlowNode.NodeType.EXECUTION ->
                    sb.appendLine("    P->>E: Execute: ${escapeLabel(node.label)}")
                FlowNode.NodeType.CONDITION ->
                    sb.appendLine("    E->>E: Check: ${escapeLabel(node.label)}")
                FlowNode.NodeType.LOOP_START ->
                    sb.appendLine("    E->>E: Loop: ${escapeLabel(node.label)}")
                FlowNode.NodeType.FUNCTION_CALL ->
                    sb.appendLine("    E->>M: Call: ${escapeLabel(node.label)}")
                FlowNode.NodeType.FUNCTION_DECL ->
                    sb.appendLine("    Note over E: Define: ${escapeLabel(node.label)}")
                FlowNode.NodeType.VARIABLE_ASSIGN ->
                    sb.appendLine("    Note over E: Assign: ${escapeLabel(node.label)}")
                FlowNode.NodeType.RETURN ->
                    sb.appendLine("    E->>P: ${escapeLabel(node.label)}")
                FlowNode.NodeType.ERROR ->
                    sb.appendLine("    Note over E: ERROR: ${escapeLabel(node.label)}")
                else -> {}
            }
        }

        return sb.toString()
    }

    private fun getNodeShape(node: FlowNode): String {
        val label = escapeLabel(node.label)
        return when (node.type) {
            FlowNode.NodeType.START,
            FlowNode.NodeType.END -> "([\"$label\"])"
            FlowNode.NodeType.CONDITION -> "{\"$label\"}"
            FlowNode.NodeType.LOOP_START -> "{{\"$label\"}}"
            FlowNode.NodeType.LOOP_END -> "([\"$label\"])"
            FlowNode.NodeType.FUNCTION_DECL -> "[/\"$label\"/]"
            FlowNode.NodeType.RETURN -> ">\"$label\"]"
            FlowNode.NodeType.FORK -> "[[\"$label\"]]"
            FlowNode.NodeType.JOIN -> "[[\"$label\"]]"
            FlowNode.NodeType.ERROR -> "((\"$label\"))"
            else -> "[\"$label\"]"
        }
    }

    private fun getStyleClass(type: FlowNode.NodeType): String? = when (type) {
        FlowNode.NodeType.START, FlowNode.NodeType.END -> "startEnd"
        FlowNode.NodeType.CONDITION -> "condition"
        FlowNode.NodeType.LOOP_START -> "loop"
        FlowNode.NodeType.LOOP_END -> "loopEnd"
        FlowNode.NodeType.INTERPOLATION -> "interpolation"
        FlowNode.NodeType.EXECUTION -> "execution"
        FlowNode.NodeType.FUNCTION_DECL -> "funcDecl"
        FlowNode.NodeType.FUNCTION_CALL -> "funcCall"
        FlowNode.NodeType.VARIABLE_ASSIGN -> "variable"
        FlowNode.NodeType.RETURN -> "returnNode"
        FlowNode.NodeType.FORK -> "fork"
        FlowNode.NodeType.JOIN -> "join"
        FlowNode.NodeType.ERROR -> "error"
    }


    private fun formatEdge(edge: FlowEdge): String {
        val from = edge.from
        val to = edge.to

        return when (edge.type) {
            FlowEdge.EdgeType.NORMAL -> {
                if (edge.label != null) {
                    "$from -->|${edge.label}| $to"
                } else {
                    "$from --> $to"
                }
            }
            FlowEdge.EdgeType.TRUE_BRANCH -> "$from -->|yes| $to"
            FlowEdge.EdgeType.FALSE_BRANCH -> "$from -->|no| $to"
            FlowEdge.EdgeType.LOOP_BACK -> {
                val label = edge.label ?: "repeat"
                "$from -.->|$label| $to"
            }
            FlowEdge.EdgeType.LOOP_EXIT -> {
                val label = edge.label ?: "done"
                "$from -->|$label| $to"
            }
            FlowEdge.EdgeType.PARALLEL -> {

                "$from ==> $to"
            }
        }
    }

    private fun escapeLabel(text: String): String {
        return text
            .replace("\"", "'")
            .replace("\n", " ")
            .replace("\r", "")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .take(50)
    }




    fun exportFlowchart(trace: ExecutionTrace, title: String = "Template Execution Flow"): String {
        if (trace.isEmpty) {
            return "flowchart TD\n    empty[No execution steps recorded]"
        }

        val sb = StringBuilder()
        sb.appendLine("flowchart TD")
        sb.appendLine("%% $title")
        sb.appendLine()


        sb.appendLine("%% Node styles")
        sb.appendLine("classDef startEnd fill:#9f9,stroke:#333,stroke-width:2px")
        sb.appendLine("classDef block fill:#bbf,stroke:#333,stroke-width:1px")
        sb.appendLine("classDef expression fill:#fbb,stroke:#333,stroke-width:1px")
        sb.appendLine("classDef variable fill:#fbf,stroke:#333,stroke-width:1px")
        sb.appendLine("classDef function fill:#ff9,stroke:#333,stroke-width:1px")
        sb.appendLine("classDef error fill:#f66,stroke:#333,stroke-width:2px")
        sb.appendLine()


        val steps = trace.allSteps
        for (step in steps) {
            val nodeId = "step${step.id}"
            val label = escapeLabel(step.description)
            val nodeShape = getTraceNodeShape(step.type, label)
            val styleClass = getTraceStyleClass(step.type)

            sb.appendLine("$nodeId$nodeShape")
            if (styleClass != null) {
                sb.appendLine("class $nodeId $styleClass")
            }
        }
        sb.appendLine()


        sb.appendLine("%% Execution flow")
        for (i in 0 until steps.size - 1) {
            val current = steps[i]
            val next = steps[i + 1]
            val edgeLabel = getTraceEdgeLabel(current)

            if (edgeLabel != null) {
                sb.appendLine("step${current.id} -->|$edgeLabel| step${next.id}")
            } else {
                sb.appendLine("step${current.id} --> step${next.id}")
            }
        }

        return sb.toString()
    }


    fun exportSequenceDiagram(trace: ExecutionTrace, title: String = "Template Execution"): String {
        if (trace.isEmpty) {
            return "sequenceDiagram\n    Note over Parser: No execution steps recorded"
        }

        val sb = StringBuilder()
        sb.appendLine("sequenceDiagram")
        sb.appendLine("    title $title")
        sb.appendLine()
        sb.appendLine("    participant T as Template")
        sb.appendLine("    participant P as Parser")
        sb.appendLine("    participant E as Evaluator")
        sb.appendLine("    participant M as Modules")
        sb.appendLine()

        for (step in trace.allSteps) {
            when (step.type) {
                ExecutionStep.StepType.TEMPLATE_START ->
                    sb.appendLine("    T->>P: Start parsing")
                ExecutionStep.StepType.TEMPLATE_END ->
                    sb.appendLine("    P->>T: Return result")
                ExecutionStep.StepType.BLOCK_START ->
                    sb.appendLine("    P->>E: ${escapeLabel(step.description)}")
                ExecutionStep.StepType.BLOCK_END ->
                    sb.appendLine("    E->>P: ${escapeLabel(step.output ?: "done")}")
                ExecutionStep.StepType.EXPRESSION_EVAL ->
                    sb.appendLine("    E->>E: Eval: ${escapeLabel(step.description)}")
                ExecutionStep.StepType.FUNCTION_CALL ->
                    sb.appendLine("    E->>M: ${escapeLabel(step.description)}")
                ExecutionStep.StepType.VARIABLE_ASSIGN ->
                    sb.appendLine("    Note over E: ${escapeLabel(step.description)}")
                ExecutionStep.StepType.ERROR ->
                    sb.appendLine("    Note over E: ERROR: ${escapeLabel(step.description)}")
                else -> {}
            }
        }

        return sb.toString()
    }

    private fun getTraceNodeShape(type: ExecutionStep.StepType, label: String): String {
        return when (type) {
            ExecutionStep.StepType.TEMPLATE_START,
            ExecutionStep.StepType.TEMPLATE_END -> "([\"$label\"])"
            ExecutionStep.StepType.BLOCK_START,
            ExecutionStep.StepType.BLOCK_END -> "[\"$label\"]"
            ExecutionStep.StepType.CONDITION_EVAL -> "{\"$label\"}"
            ExecutionStep.StepType.LOOP_ITERATION -> "[[\"$label\"]]"
            ExecutionStep.StepType.ERROR -> "((\"$label\"))"
            else -> "(\"$label\")"
        }
    }

    private fun getTraceStyleClass(type: ExecutionStep.StepType): String? = when (type) {
        ExecutionStep.StepType.TEMPLATE_START, ExecutionStep.StepType.TEMPLATE_END -> "startEnd"
        ExecutionStep.StepType.BLOCK_START, ExecutionStep.StepType.BLOCK_END -> "block"
        ExecutionStep.StepType.EXPRESSION_EVAL -> "expression"
        ExecutionStep.StepType.VARIABLE_ASSIGN -> "variable"
        ExecutionStep.StepType.FUNCTION_CALL -> "function"
        ExecutionStep.StepType.ERROR -> "error"
        else -> null
    }

    private fun getTraceEdgeLabel(step: ExecutionStep): String? {
        if (step.output != null && step.output.length <= 20) {
            return escapeLabel(step.output)
        }
        return null
    }
}

