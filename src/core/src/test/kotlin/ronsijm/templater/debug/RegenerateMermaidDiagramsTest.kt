package ronsijm.templater.debug

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import ronsijm.templater.script.ScriptParser
import java.io.File


@Tag("slow")
class RegenerateMermaidDiagramsTest {

    private val scriptParser = ScriptParser()
    private val builder = ControlFlowBuilder(scriptParser)
    private val exporter = MermaidExporter()

    @Test
    fun `regenerate all mermaid diagrams`() {

        var projectRoot = File(System.getProperty("user.dir"))
        if (projectRoot.name == "core") {
            projectRoot = projectRoot.parentFile.parentFile
        }

        val templatesDir = File(projectRoot, "docs/Examples/Templates")
        val mermaidDir = File(projectRoot, "docs/Examples_Mermaid/Templates")

        regenerateDirectory(templatesDir, mermaidDir)

        val functionsDir = File(projectRoot, "docs/Examples/Functions")
        val functionsMermaidDir = File(projectRoot, "docs/Examples_Mermaid/Functions")

        regenerateDirectory(functionsDir, functionsMermaidDir)
    }

    private fun regenerateDirectory(sourceDir: File, targetDir: File) {
        if (!sourceDir.exists()) {
            println("Source directory does not exist: ${sourceDir.absolutePath}")
            return
        }

        sourceDir.walkTopDown().forEach { file ->
            if (file.isFile && file.extension == "md") {
                val relativePath = file.relativeTo(sourceDir)
                val targetFile = File(targetDir, relativePath.path.replace(".md", ".mmd"))

                println("Processing: ${file.name}")

                val template = file.readText()
                val graph = builder.buildFromTemplate(template)
                val mermaid = exporter.exportFlowchart(graph, title = file.nameWithoutExtension)

                targetFile.parentFile.mkdirs()
                targetFile.writeText(mermaid)

                println("  Generated: ${targetFile.relativeTo(File(System.getProperty("user.dir")))}")
                println("  Nodes: ${graph.nodes.size}, Parallel groups: ${graph.parallelGroupExplanations.size}")
            }
        }
    }
}

