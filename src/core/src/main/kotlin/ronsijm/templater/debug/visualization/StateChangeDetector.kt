package ronsijm.templater.debug.visualization

class StateChangeDetector {

    fun detectChanges(
        previous: DataStructureSnapshot?,
        current: DataStructureSnapshot
    ): List<StateChange> {
        if (previous == null) {
            return listOf(StateChange.NoChange)
        }

        if (previous.variableName != current.variableName) {
            return listOf(StateChange.NoChange)
        }

        return when {
            previous.dataType == DataStructureSnapshot.DataType.LIST &&
            current.dataType == DataStructureSnapshot.DataType.LIST ->
                detectListChanges(previous.value as? List<*>, current.value as? List<*>)

            previous.dataType == DataStructureSnapshot.DataType.ARRAY &&
            current.dataType == DataStructureSnapshot.DataType.ARRAY ->
                detectListChanges(previous.value as? List<*>, current.value as? List<*>)

            previous.dataType == DataStructureSnapshot.DataType.MAP &&
            current.dataType == DataStructureSnapshot.DataType.MAP ->
                detectMapChanges(previous.value as? Map<*, *>, current.value as? Map<*, *>)

            else -> listOf(StateChange.NoChange)
        }
    }

    private fun detectListChanges(previous: List<*>?, current: List<*>?): List<StateChange> {
        if (previous == null || current == null) {
            return listOf(StateChange.NoChange)
        }

        val changes = mutableListOf<StateChange>()

        if (previous.size != current.size) {
            when {
                current.size > previous.size -> {
                    for (i in previous.size until current.size) {
                        changes.add(StateChange.ElementInsert(i, current[i]))
                    }
                }
                current.size < previous.size -> {
                    for (i in current.size until previous.size) {
                        changes.add(StateChange.ElementRemove(i, previous[i]))
                    }
                }
            }
            return changes
        }

        val swapDetected = detectSwap(previous, current)
        if (swapDetected != null) {
            changes.add(swapDetected)
            return changes
        }

        for (i in previous.indices) {
            if (previous[i] != current[i]) {
                changes.add(StateChange.ElementUpdate(i, previous[i], current[i]))
            }
        }

        return if (changes.isEmpty()) listOf(StateChange.NoChange) else changes
    }

    private fun detectSwap(previous: List<*>, current: List<*>): StateChange.ElementSwap? {
        if (previous.size != current.size) return null

        val differences = mutableListOf<Int>()
        for (i in previous.indices) {
            if (previous[i] != current[i]) {
                differences.add(i)
            }
        }

        if (differences.size == 2) {
            val i = differences[0]
            val j = differences[1]
            if (previous[i] == current[j] && previous[j] == current[i]) {
                return StateChange.ElementSwap(i, j)
            }
        }

        return null
    }

    private fun detectMapChanges(previous: Map<*, *>?, current: Map<*, *>?): List<StateChange> {
        if (previous == null || current == null) {
            return listOf(StateChange.NoChange)
        }

        return listOf(StateChange.NoChange)
    }

    fun detectComparisonFromStatement(statement: String, variableName: String): StateChange? {
        val arrayAccessPattern = Regex("""$variableName\[(\d+)\].*$variableName\[(\d+)\]""")
        val match = arrayAccessPattern.find(statement)

        return match?.let {
            val index1 = it.groupValues[1].toIntOrNull()
            val index2 = it.groupValues[2].toIntOrNull()
            if (index1 != null && index2 != null) {
                StateChange.Comparison(index1, index2)
            } else {
                null
            }
        }
    }
}

