package ronsijm.templater.debug.visualization

sealed class StateChange {
    data class ElementSwap(val index1: Int, val index2: Int) : StateChange()
    data class ElementUpdate(val index: Int, val oldValue: Any?, val newValue: Any?) : StateChange()
    data class ElementInsert(val index: Int, val value: Any?) : StateChange()
    data class ElementRemove(val index: Int, val value: Any?) : StateChange()
    data class Comparison(val index1: Int, val index2: Int) : StateChange()
    data object NoChange : StateChange()
}

