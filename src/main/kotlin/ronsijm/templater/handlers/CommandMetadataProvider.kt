package ronsijm.templater.handlers

/**
 * Interface for providing command metadata
 * Separates documentation concerns from execution logic
 */
interface CommandMetadataProvider {
    /**
     * Metadata describing this command
     * Used for documentation, IntelliSense, and auto-discovery
     */
    val metadata: CommandMetadata
}

