package ronsijm.templater.debug.visualization

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

class DataStructureSnapshotTest {

    @Test
    fun `test fromVariable creates snapshot for list`() {
        val list = listOf(1, 2, 3)
        val snapshot = DataStructureSnapshot.fromVariable(1, "myList", list)

        assertNotNull(snapshot)
        assertEquals(1, snapshot?.stepId)
        assertEquals("myList", snapshot?.variableName)
        assertEquals(DataStructureSnapshot.DataType.LIST, snapshot?.dataType)
        assertEquals(listOf(1, 2, 3), snapshot?.value)
    }

    @Test
    fun `test fromVariable creates snapshot for array`() {
        val array = arrayOf(5, 1, 9, 2)
        val snapshot = DataStructureSnapshot.fromVariable(1, "arr", array)

        assertNotNull(snapshot)
        assertEquals("arr", snapshot?.variableName)
        assertEquals(DataStructureSnapshot.DataType.ARRAY, snapshot?.dataType)
        assertEquals(listOf(5, 1, 9, 2), snapshot?.value)
    }

    @Test
    fun `test fromVariable creates snapshot for map`() {
        val map = mapOf("a" to 1, "b" to 2)
        val snapshot = DataStructureSnapshot.fromVariable(1, "myMap", map)

        assertNotNull(snapshot)
        assertEquals(DataStructureSnapshot.DataType.MAP, snapshot?.dataType)
        assertEquals(map, snapshot?.value)
    }

    @Test
    fun `test fromVariable returns null for primitives`() {
        val snapshot1 = DataStructureSnapshot.fromVariable(1, "num", 42)
        val snapshot2 = DataStructureSnapshot.fromVariable(1, "str", "hello")
        val snapshot3 = DataStructureSnapshot.fromVariable(1, "bool", true)
        val snapshot4 = DataStructureSnapshot.fromVariable(1, "nul", null)

        assertNull(snapshot1)
        assertNull(snapshot2)
        assertNull(snapshot3)
        assertNull(snapshot4)
    }

    @Test
    fun `test captureAll filters primitives and captures data structures`() {
        val variables = mapOf(
            "arr" to listOf(1, 2, 3),
            "count" to 42,
            "name" to "test",
            "map" to mapOf("x" to 10)
        )

        val snapshots = DataStructureSnapshot.captureAll(1, variables)

        assertEquals(2, snapshots.size)
        assertTrue(snapshots.any { it.variableName == "arr" })
        assertTrue(snapshots.any { it.variableName == "map" })
        assertFalse(snapshots.any { it.variableName == "count" })
        assertFalse(snapshots.any { it.variableName == "name" })
    }

    @Test
    fun `test snapshot creates deep copy of list`() {
        val originalList = mutableListOf(1, 2, 3)
        val snapshot = DataStructureSnapshot.fromVariable(1, "list", originalList)

        originalList[0] = 999

        assertEquals(listOf(1, 2, 3), snapshot?.value)
    }

    @Test
    fun `test snapshot creates deep copy of map`() {
        val originalMap = mutableMapOf("a" to 1, "b" to 2)
        val snapshot = DataStructureSnapshot.fromVariable(1, "map", originalMap)

        originalMap["a"] = 999

        val snapshotMap = snapshot?.value as? Map<*, *>
        assertEquals(1, snapshotMap?.get("a"))
    }

    @Test
    fun `test empty list creates snapshot`() {
        val emptyList = emptyList<Int>()
        val snapshot = DataStructureSnapshot.fromVariable(1, "empty", emptyList)

        assertNotNull(snapshot)
        assertEquals(DataStructureSnapshot.DataType.LIST, snapshot?.dataType)
        assertEquals(emptyList<Int>(), snapshot?.value)
    }

    @Test
    fun `test set creates snapshot`() {
        val set = setOf(1, 2, 3)
        val snapshot = DataStructureSnapshot.fromVariable(1, "mySet", set)

        assertNotNull(snapshot)
        assertEquals(DataStructureSnapshot.DataType.SET, snapshot?.dataType)
        assertEquals(set, snapshot?.value)
    }
}

