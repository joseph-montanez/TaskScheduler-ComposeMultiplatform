import libs.CommandResult

actual fun runTaskCommand(command: String): CommandResult {
    return CommandResult("", "InterruptedException occurred", -1)
}