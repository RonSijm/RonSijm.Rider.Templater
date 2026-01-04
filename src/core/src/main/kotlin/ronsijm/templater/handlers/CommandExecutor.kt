package ronsijm.templater.handlers

import ronsijm.templater.common.CommandResult
import ronsijm.templater.parser.TemplateContext


interface CommandExecutor {

    fun execute(args: List<Any?>, context: TemplateContext): CommandResult
}
