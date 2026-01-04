package ronsijm.templater.ast

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue

class TemplateASTBuilderTest {

    private val builder = TemplateASTBuilder()

    @Test
    fun `should build AST with START and END nodes`() {
        val template = """
            <%* let x = 1 %>
        """.trimIndent()

        val ast = builder.build(template)


        assertTrue(ast.allStatements.size >= 3)
        assertEquals(StatementType.START, ast.allStatements.first().type)
        assertEquals(StatementType.END, ast.allStatements.last().type)
    }

    @Test
    fun `should assign stable IDs to statements`() {
        val template = """
            <%* let x = 1 %>
        """.trimIndent()

        val ast1 = builder.build(template)
        val ast2 = builder.build(template)



        ast1.allStatements.forEach { stmt ->
            assertNotNull(stmt.id)
            assertTrue(stmt.id.isNotEmpty())
        }

        ast2.allStatements.forEach { stmt ->
            assertNotNull(stmt.id)
            assertTrue(stmt.id.isNotEmpty())
        }
    }

    @Test
    fun `should parse for loop with children`() {
        val template = """
            <%*
            for (let i = 0; i < 10; i++) {
                let x = i;
            }
            %>
        """.trimIndent()

        val ast = builder.build(template)


        val forLoop = ast.allStatements.find { it.type == StatementType.FOR_LOOP }
        assertNotNull(forLoop)
        assertTrue(forLoop!!.children.isNotEmpty())


        val childStmt = forLoop.children.first()
        assertEquals(StatementType.VARIABLE_DECLARATION, childStmt.type)
    }

    @Test
    fun `should build control flow edges`() {
        val template = """
            <%*
            let x = 1;
            let y = 2;
            %>
        """.trimIndent()

        val ast = builder.build(template)


        assertTrue(ast.controlFlowEdges.isNotEmpty())


        val startNode = ast.allStatements.first { it.type == StatementType.START }
        val firstEdge = ast.controlFlowEdges.first { it.fromNodeId == startNode.id }
        assertNotNull(firstEdge)
    }

    @Test
    fun `should handle multiple template blocks`() {
        val template = """
            <%* let x = 1 %>
            Some text
            <%* let y = 2 %>
        """.trimIndent()

        val ast = builder.build(template)


        assertEquals(2, ast.blocks.size)


        val varDecls = ast.allStatements.filter { it.type == StatementType.VARIABLE_DECLARATION }
        assertEquals(2, varDecls.size)
    }

    @Test
    fun `should handle interpolation blocks`() {
        val template = """
            <%= x + y %>
        """.trimIndent()

        val ast = builder.build(template)


        assertEquals(1, ast.blocks.size)
        assertEquals(false, ast.blocks.first().isExecution)


        val interpolation = ast.allStatements.find { it.type == StatementType.INTERPOLATION }
        assertNotNull(interpolation)

        assertTrue(interpolation!!.code.trim().endsWith("x + y"))
    }

    @Test
    fun `should create loop back edges for for loops`() {
        val template = """
            <%*
            for (let i = 0; i < 10; i++) {
                let x = i;
            }
            %>
        """.trimIndent()

        val ast = builder.build(template)


        val forLoop = ast.allStatements.find { it.type == StatementType.FOR_LOOP }
        assertNotNull(forLoop)


        val loopBackEdge = ast.controlFlowEdges.find {
            it.edgeType == ControlFlowEdge.EdgeType.LOOP_BACK
        }
        assertNotNull(loopBackEdge)
    }

    @Test
    fun `should find statements by ID`() {
        val template = """
            <%* let x = 1 %>
        """.trimIndent()

        val ast = builder.build(template)


        val stmt = ast.allStatements.find { it.type == StatementType.VARIABLE_DECLARATION }
        assertNotNull(stmt)


        val found = ast.findStatement(stmt!!.id)
        assertNotNull(found)
        assertEquals(stmt.id, found!!.id)
        assertEquals(stmt.code, found.code)
    }
}

