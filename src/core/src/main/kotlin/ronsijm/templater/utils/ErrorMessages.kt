package ronsijm.templater.utils


object ErrorMessages {

    fun commandError(message: String) = "<!-- Error: $message -->"
    fun clipboardError(message: String) = "<!-- Error reading clipboard: $message -->"
    fun pictureError(message: String) = "<!-- Failed to generate random picture: $message -->"
    fun requestError(message: String) = "<!-- HTTP request failed: $message -->"
    fun fileError(message: String) = "<!-- File error: $message -->"
    fun dateError(message: String) = "<!-- Date error: $message -->"
    fun validationError(message: String) = "<!-- Validation error: $message -->"


    fun quoteError(message: String) = "> [!quote] Daily Quote\n> Failed to fetch quote: $message"


    fun inlineError(message: String) = "[Error: $message]"
    fun urlRequiredError(module: String) = "[Error: URL required for $module]"
    fun missingOptionError(option: String, module: String) = "[Error: Missing '$option' in $module options]"
    fun invalidArgumentError(module: String) = "[Error: Invalid argument for $module]"


    fun invalidCommandFormat(command: String) = "Invalid command format: $command. Commands should be in format: tp.module.function()"
    fun scriptExecutionError(message: String?) = "ERROR: ${message ?: "Unknown error"}"
}
