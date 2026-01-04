package ronsijm.templater.script.ast





sealed interface Statement : AstNode


data class BlockStatement(
    val statements: List<Statement>,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Statement


data class ExpressionStatement(
    val expression: Expression,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Statement



enum class VariableKind { LET, CONST, VAR }

data class VariableDeclaration(
    val kind: VariableKind,
    val name: String,
    val initializer: Expression?,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Statement



data class IfStatement(
    val condition: Expression,
    val thenBranch: Statement,
    val elseBranch: Statement?,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Statement

data class ForStatement(
    val init: Statement?,
    val condition: Expression?,
    val update: Expression?,
    val body: Statement,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Statement

data class ForOfStatement(
    val variableName: String,
    val iterable: Expression,
    val body: Statement,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Statement

data class WhileStatement(
    val condition: Expression,
    val body: Statement,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Statement

data class ReturnStatement(
    val value: Expression?,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Statement

data class BreakStatement(
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Statement

data class ContinueStatement(
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Statement



data class FunctionDeclaration(
    val name: String,
    val parameters: List<String>,
    val body: BlockStatement,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Statement



data class TryStatement(
    val tryBlock: BlockStatement,
    val catchParam: String?,
    val catchBlock: BlockStatement?,
    val finallyBlock: BlockStatement?,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Statement

data class ThrowStatement(
    val expression: Expression,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Statement




data class EmptyStatement(
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Statement


data class ResultAccumulatorStatement(
    val operator: AssignmentOperator,
    val value: Expression,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Statement



data class UpdateStatement(
    val variable: String,
    val operator: UpdateOperator,
    val prefix: Boolean = false,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : Statement

enum class UpdateOperator {
    INCREMENT, DECREMENT
}






data class Program(
    val statements: List<Statement>,
    override val location: SourceLocation = SourceLocation.UNKNOWN
) : AstNode

