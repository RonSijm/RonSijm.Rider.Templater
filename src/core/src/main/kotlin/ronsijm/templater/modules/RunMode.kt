package ronsijm.templater.modules


enum class RunMode {
    CREATE_NEW_FROM_TEMPLATE,
    APPEND_ACTIVE_FILE,
    OVERWRITE_FILE,
    OVERWRITE_ACTIVE_FILE,
    DYNAMIC_PROCESSOR,
    STARTUP_TEMPLATE
}
