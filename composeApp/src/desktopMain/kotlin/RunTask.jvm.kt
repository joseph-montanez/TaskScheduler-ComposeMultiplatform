import libs.CommandResult
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

actual fun runTaskCommand(command: String): CommandResult {
    try {
        val processBuilder = ProcessBuilder()
        if (System.getProperty("os.name").lowercase(Locale.getDefault()).contains("windows")) {
            processBuilder.command("cmd.exe", "/c", command)
        } else {
            processBuilder.command("sh", "-c", command)
        }

        val process = processBuilder.start()
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        val errorReader = BufferedReader(InputStreamReader(process.errorStream))

        val output = reader.readText()
        val errorOutput = errorReader.readText()

        val exitVal = process.waitFor()

        if (exitVal == 0) {
            println("Success!")
            println(output)
        } else {
            println("Error!")
            println(errorOutput)
        }
        return CommandResult(output, errorOutput, exitVal)
    } catch (e: Exception) {
        e.printStackTrace()
        return CommandResult("", e.message ?: "IOException occurred", -1)
    }
}
