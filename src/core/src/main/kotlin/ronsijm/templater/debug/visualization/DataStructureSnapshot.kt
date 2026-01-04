package ronsijm.templater.debug.visualization

import ronsijm.templater.script.ArrowFunction
import ronsijm.templater.script.UserFunction

data class DataStructureSnapshot(
    val stepId: Int,
    val variableName: String,
    val dataType: DataType,
    val value: Any?,
    val visualMetadata: VisualMetadata = VisualMetadata()
) {
    enum class DataType {
        ARRAY,
        LIST,
        MAP,
        SET,
        PRIMITIVE,
        OBJECT
    }

    companion object {
        fun fromVariable(stepId: Int, name: String, value: Any?): DataStructureSnapshot? {

            if (value is ArrowFunction || value is UserFunction) {
                return null
            }

            val dataType = when (value) {
                is Array<*> -> DataType.ARRAY
                is List<*> -> DataType.LIST
                is Map<*, *> -> DataType.MAP
                is Set<*> -> DataType.SET
                is Number, is String, is Boolean, null -> DataType.PRIMITIVE
                else -> DataType.OBJECT
            }


            if (dataType == DataType.PRIMITIVE || dataType == DataType.OBJECT) {
                return null
            }

            val copiedValue = when (value) {
                is Array<*> -> value.toList()
                is List<*> -> value.toList()
                is Map<*, *> -> value.toMap()
                is Set<*> -> value.toSet()
                else -> value
            }

            return DataStructureSnapshot(
                stepId = stepId,
                variableName = name,
                dataType = dataType,
                value = copiedValue,
                visualMetadata = VisualMetadata()
            )
        }

        fun captureAll(stepId: Int, variables: Map<String, Any?>): List<DataStructureSnapshot> {
            return variables.mapNotNull { (name, value) ->
                fromVariable(stepId, name, value)
            }
        }
    }
}

