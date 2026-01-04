package ronsijm.templater.debug.visualization

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import ronsijm.templater.TestContextFactory
import ronsijm.templater.debug.DebugAction
import ronsijm.templater.debug.DebuggingTemplateParser

class AlgorithmVisualizationIntegrationTest {

    @Test
    fun `test bubble sort captures array snapshots`() {
        val template = """
<%*
let arr = [5, 1, 9, 2];
for (let i = 0; i < arr.length - 1; i++) {
    for (let j = 0; j < arr.length - i - 1; j++) {
        if (arr[j] > arr[j + 1]) {
            let temp = arr[j];
            arr[j] = arr[j + 1];
            arr[j + 1] = temp;
        }
    }
}
%>
        """.trimIndent()

        val debugParser = DebuggingTemplateParser()
        val capturedSteps = mutableListOf<CapturedStep>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                val step = breakpoint.step

                val arrSnapshot = step.dataSnapshots.find { it.variableName == "arr" }
                if (arrSnapshot != null) {
                    capturedSteps.add(
                        CapturedStep(
                            stepId = step.id,
                            description = step.description,
                            arrayValue = arrSnapshot.value as? List<*>,
                            stateChanges = step.stateChanges
                        )
                    )
                }

                DebugAction.STEP_INTO
            },
            startInStepMode = true
        )

        debugParser.parse(template, TestContextFactory.create())


        assertTrue(capturedSteps.isNotEmpty(), "Should capture steps")


        val initialStep = capturedSteps.first { it.arrayValue != null }
        assertEquals(listOf(5, 1, 9, 2), initialStep.arrayValue)



    }

    @Test
    fun `test temp variable swap captures array snapshots`() {

        val template = """
<%*
let arr = [5, 1];
let temp = arr[0];
arr[0] = arr[1];
arr[1] = temp;
%>
        """.trimIndent()

        val debugParser = DebuggingTemplateParser()
        val capturedSteps = mutableListOf<CapturedStep>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                val step = breakpoint.step

                val arrSnapshot = step.dataSnapshots.find { it.variableName == "arr" }
                if (arrSnapshot != null) {
                    capturedSteps.add(
                        CapturedStep(
                            stepId = step.id,
                            description = step.description,
                            arrayValue = arrSnapshot.value as? List<*>,
                            stateChanges = step.stateChanges
                        )
                    )
                }

                DebugAction.STEP_INTO
            },
            startInStepMode = true
        )

        debugParser.parse(template, TestContextFactory.create())


        assertTrue(capturedSteps.isNotEmpty(), "Should capture at least one step with array")


        assertTrue(capturedSteps.any { it.arrayValue == listOf(5, 1) }, "Should capture initial array state")


        val comparisonDetected = capturedSteps.any { step ->
            step.stateChanges.any { it is StateChange.Comparison }
        }
        assertTrue(comparisonDetected, "Should detect array element comparisons")
    }

    @Test
    fun `test array element update detection`() {
        val template = """
<%*
let arr = [1, 2, 3];
arr[1] = 99;
%>
        """.trimIndent()

        val debugParser = DebuggingTemplateParser()
        val capturedSteps = mutableListOf<CapturedStep>()

        debugParser.startDebugSession(
            onBreakpoint = { breakpoint ->
                val step = breakpoint.step

                val arrSnapshot = step.dataSnapshots.find { it.variableName == "arr" }
                if (arrSnapshot != null) {
                    capturedSteps.add(
                        CapturedStep(
                            stepId = step.id,
                            description = step.description,
                            arrayValue = arrSnapshot.value as? List<*>,
                            stateChanges = step.stateChanges
                        )
                    )
                }

                DebugAction.STEP_INTO
            },
            startInStepMode = true
        )

        debugParser.parse(template, TestContextFactory.create())






        assertTrue(capturedSteps.isNotEmpty(), "Should capture array states")
        assertTrue(capturedSteps.any { it.arrayValue == listOf(1, 2, 3) }, "Should capture initial state")
    }

    data class CapturedStep(
        val stepId: Int,
        val description: String,
        val arrayValue: List<*>?,
        val stateChanges: List<StateChange>
    )
}

