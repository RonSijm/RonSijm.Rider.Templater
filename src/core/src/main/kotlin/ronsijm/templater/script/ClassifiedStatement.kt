package ronsijm.templater.script


sealed class ClassifiedStatement {

    abstract val statementText: String

    data class Empty(val original: String) : ClassifiedStatement() {
        override val statementText: String get() = original
    }
    data class Brace(val original: String) : ClassifiedStatement() {
        override val statementText: String get() = original
    }
    data class ReturnVoid(val original: String) : ClassifiedStatement() {
        override val statementText: String get() = original
    }
    data class ReturnValue(val valueExpr: String) : ClassifiedStatement() {
        override val statementText: String get() = "return $valueExpr"
    }
    data class ForLoop(val header: String, val body: List<String>) : ClassifiedStatement() {
        override val statementText: String get() = "for ($header)"
    }
    data class WhileLoop(val condition: String, val body: List<String>) : ClassifiedStatement() {
        override val statementText: String get() = "while ($condition)"
    }
    data class IfStatement(val header: String, val body: List<String>, val elseBranches: List<Pair<String?, List<String>>>) : ClassifiedStatement() {
        override val statementText: String get() = "if ($header)"
    }
    data class FunctionDecl(val original: String) : ClassifiedStatement() {
        override val statementText: String get() = original
    }
    data class VarDecl(val original: String) : ClassifiedStatement() {
        override val statementText: String get() = original
    }
    data class TrAccumulator(val original: String) : ClassifiedStatement() {
        override val statementText: String get() = original
    }
    data class Increment(val varName: String) : ClassifiedStatement() {
        override val statementText: String get() = "$varName++"
    }
    data class Decrement(val varName: String) : ClassifiedStatement() {
        override val statementText: String get() = "$varName--"
    }
    data class CompoundAssign(val original: String) : ClassifiedStatement() {
        override val statementText: String get() = original
    }
    data class ArrayAssign(val arrayName: String, val indexExpr: String, val valueExpr: String) : ClassifiedStatement() {
        override val statementText: String get() = "$arrayName[$indexExpr] = $valueExpr"
    }
    data class SimpleAssign(val varName: String, val valueExpr: String) : ClassifiedStatement() {
        override val statementText: String get() = "$varName = $valueExpr"
    }
    data class Assignment(val original: String) : ClassifiedStatement() {
        override val statementText: String get() = original
    }
    data class FunctionCall(val original: String) : ClassifiedStatement() {
        override val statementText: String get() = original
    }
    data class Other(val original: String) : ClassifiedStatement() {
        override val statementText: String get() = original
    }
}
