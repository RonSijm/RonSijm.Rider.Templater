package ronsijm.templater.debug.visualization

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class StateChangeDetectorTest {

    private val detector = StateChangeDetector()

    @Test
    fun `test detect swap in list`() {
        val before = DataStructureSnapshot(
            stepId = 1,
            variableName = "arr",
            dataType = DataStructureSnapshot.DataType.LIST,
            value = listOf(5, 1, 9, 2)
        )

        val after = DataStructureSnapshot(
            stepId = 2,
            variableName = "arr",
            dataType = DataStructureSnapshot.DataType.LIST,
            value = listOf(1, 5, 9, 2)
        )

        val changes = detector.detectChanges(before, after)

        assertEquals(1, changes.size)
        assertTrue(changes[0] is StateChange.ElementSwap)
        val swap = changes[0] as StateChange.ElementSwap
        assertEquals(0, swap.index1)
        assertEquals(1, swap.index2)
    }

    @Test
    fun `test detect multiple element updates`() {
        val before = DataStructureSnapshot(
            stepId = 1,
            variableName = "arr",
            dataType = DataStructureSnapshot.DataType.LIST,
            value = listOf(1, 2, 3)
        )

        val after = DataStructureSnapshot(
            stepId = 2,
            variableName = "arr",
            dataType = DataStructureSnapshot.DataType.LIST,
            value = listOf(1, 5, 3)
        )

        val changes = detector.detectChanges(before, after)

        assertEquals(1, changes.size)
        assertTrue(changes[0] is StateChange.ElementUpdate)
        val update = changes[0] as StateChange.ElementUpdate
        assertEquals(1, update.index)
        assertEquals(2, update.oldValue)
        assertEquals(5, update.newValue)
    }

    @Test
    fun `test detect element insertion`() {
        val before = DataStructureSnapshot(
            stepId = 1,
            variableName = "arr",
            dataType = DataStructureSnapshot.DataType.LIST,
            value = listOf(1, 2, 3)
        )

        val after = DataStructureSnapshot(
            stepId = 2,
            variableName = "arr",
            dataType = DataStructureSnapshot.DataType.LIST,
            value = listOf(1, 2, 3, 4)
        )

        val changes = detector.detectChanges(before, after)

        assertEquals(1, changes.size)
        assertTrue(changes[0] is StateChange.ElementInsert)
        val insert = changes[0] as StateChange.ElementInsert
        assertEquals(3, insert.index)
        assertEquals(4, insert.value)
    }

    @Test
    fun `test detect element removal`() {
        val before = DataStructureSnapshot(
            stepId = 1,
            variableName = "arr",
            dataType = DataStructureSnapshot.DataType.LIST,
            value = listOf(1, 2, 3, 4)
        )

        val after = DataStructureSnapshot(
            stepId = 2,
            variableName = "arr",
            dataType = DataStructureSnapshot.DataType.LIST,
            value = listOf(1, 2, 3)
        )

        val changes = detector.detectChanges(before, after)

        assertEquals(1, changes.size)
        assertTrue(changes[0] is StateChange.ElementRemove)
        val remove = changes[0] as StateChange.ElementRemove
        assertEquals(3, remove.index)
        assertEquals(4, remove.value)
    }

    @Test
    fun `test detect no change`() {
        val before = DataStructureSnapshot(
            stepId = 1,
            variableName = "arr",
            dataType = DataStructureSnapshot.DataType.LIST,
            value = listOf(1, 2, 3)
        )

        val after = DataStructureSnapshot(
            stepId = 2,
            variableName = "arr",
            dataType = DataStructureSnapshot.DataType.LIST,
            value = listOf(1, 2, 3)
        )

        val changes = detector.detectChanges(before, after)

        assertEquals(1, changes.size)
        assertTrue(changes[0] is StateChange.NoChange)
    }

    @Test
    fun `test detect comparison from statement`() {
        val statement = "if (arr[0] > arr[1])"
        val change = detector.detectComparisonFromStatement(statement, "arr")

        assertNotNull(change)
        assertTrue(change is StateChange.Comparison)
        val comparison = change as StateChange.Comparison
        assertEquals(0, comparison.index1)
        assertEquals(1, comparison.index2)
    }

    @Test
    fun `test detect comparison with different indices`() {
        val statement = "if (arr[2] < arr[5])"
        val change = detector.detectComparisonFromStatement(statement, "arr")

        assertNotNull(change)
        assertTrue(change is StateChange.Comparison)
        val comparison = change as StateChange.Comparison
        assertEquals(2, comparison.index1)
        assertEquals(5, comparison.index2)
    }

    @Test
    fun `test no comparison detected for non-array statement`() {
        val statement = "let x = 5"
        val change = detector.detectComparisonFromStatement(statement, "arr")

        assertNull(change)
    }
}

