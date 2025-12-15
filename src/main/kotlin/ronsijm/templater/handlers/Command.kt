package ronsijm.templater.handlers

/** A command knows how to describe itself (metadata) and run itself (execute) */
interface Command : CommandMetadataProvider, CommandExecutor

