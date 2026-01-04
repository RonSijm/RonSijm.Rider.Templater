package ronsijm.templater.debug

import org.junit.jupiter.api.Test
import ronsijm.templater.ast.TemplateASTBuilder
import org.junit.jupiter.api.Assertions.assertEquals

class LineNumberDebugTest {

    @Test
    fun `debug line number assignment for simple template`() {
        val template = """
<%*
const x = 5
const y = 10
const z = x + y
%>
        """.trimIndent()

        val builder = TemplateASTBuilder()
        val ast = builder.build(template)

        println("=== AST Statements ===")
        ast.allStatements.forEach { stmt ->
            println("Line ${stmt.lineNumber}: ${stmt.code.take(50)}")
        }







        val statements = ast.allStatements.filter { it.code.startsWith("const") }
        assertEquals(3, statements.size, "Should have 3 const statements")


        val xStmt = statements.find { it.code.contains("x = 5") }
        val yStmt = statements.find { it.code.contains("y = 10") }
        val zStmt = statements.find { it.code.contains("z = x + y") }

        println("\nExpected line numbers:")
        println("  const x = 5 should be at line 2")
        println("  const y = 10 should be at line 3")
        println("  const z = x + y should be at line 4")

        println("\nActual line numbers:")
        println("  const x = 5 is at line ${xStmt?.lineNumber}")
        println("  const y = 10 is at line ${yStmt?.lineNumber}")
        println("  const z = x + y is at line ${zStmt?.lineNumber}")
    }

    @Test
    fun `debug line number assignment for nested loop`() {
        val template = """
<%*
for (let i = 0; i < 3; i++) {
    const x = i
}
%>
        """.trimIndent()

        val builder = TemplateASTBuilder()
        val ast = builder.build(template)

        println("\n=== Nested Loop AST ===")
        ast.allStatements.forEach { stmt ->
            println("Line ${stmt.lineNumber}: ${stmt.type} - ${stmt.code.take(50)}")
            stmt.children.forEach { child ->
                println("  Child Line ${child.lineNumber}: ${child.type} - ${child.code.take(50)}")
            }
        }







        val forLoop = ast.allStatements.find { it.code.startsWith("for") }
        println("\nFor loop at line: ${forLoop?.lineNumber} (expected: 2)")

        val constInLoop = forLoop?.children?.find { it.code.contains("const x") }
        println("const x = i at line: ${constInLoop?.lineNumber} (expected: 3)")
    }

    @Test
    fun `debug line numbers for deeply nested structure`() {
        val template = """
<%*
let arr = [5, 2, 8, 1, 9];
let n = arr.length;
let swaps = 0;

for (let i = 0; i < n - 1; i++) {
    for (let j = 0; j < n - i - 1; j++) {
        if (arr[j] > arr[j + 1]) {
            let temp = arr[j];
            arr[j] = arr[j + 1];
            arr[j + 1] = temp;
            swaps++;
        }
    }
}

tR += "Done";
%>
        """.trimIndent()

        val builder = TemplateASTBuilder()
        val ast = builder.build(template)

        println("\n=== Deeply Nested Structure ===")
        fun printNode(node: ronsijm.templater.ast.StatementNode, indent: String = "") {
            println("$indent Line ${node.lineNumber}: ${node.type} - ${node.code.take(40)}")
            node.children.forEach { printNode(it, "$indent  ") }
            node.elseBranches.forEach { (cond, nodes) ->
                println("$indent  Else branch: $cond")
                nodes.forEach { printNode(it, "$indent    ") }
            }
        }

        ast.allStatements.filter { it.lineNumber != null }.forEach { printNode(it) }


        val swapsStmt = ast.allStatements.find { it.code.contains("swaps++") }
        println("\nswaps++ statement:")
        println("  Found: ${swapsStmt != null}")
        println("  Line: ${swapsStmt?.lineNumber} (expected: 12)")
        println("  Code: ${swapsStmt?.code}")
    }
}

