package ronsijm.templater.settings


enum class PopupBehavior {
    ALWAYS,
    ONLY_ON_ERROR,
    NEVER
}


enum class CancelBehavior {

    REMOVE_EXPRESSION,

    KEEP_EXPRESSION
}


enum class MermaidOutputLocation {

    SAME_AS_SCRIPT,

    DEDICATED_FOLDER
}
