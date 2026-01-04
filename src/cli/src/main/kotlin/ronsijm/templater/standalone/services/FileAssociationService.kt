package ronsijm.templater.standalone.services

import java.io.File

object FileAssociationService {

    private const val APP_NAME = "Templater"
    private const val PROG_ID = "Templater.MarkdownFile"

    fun isWindows(): Boolean {
        return System.getProperty("os.name").lowercase().contains("windows")
    }

    private fun getExecutablePath(): String? {
        return try {

            val jarPath = File(
                FileAssociationService::class.java.protectionDomain.codeSource.location.toURI()
            ).absolutePath


            if (jarPath.endsWith(".jar")) {
                val javaHome = System.getProperty("java.home")
                val javaw = File(javaHome, "bin/javaw.exe")
                if (javaw.exists()) {
                    "\"${javaw.absolutePath}\" -jar \"$jarPath\""
                } else {
                    "java -jar \"$jarPath\""
                }
            } else {

                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getRunCommand(): String? {
        return try {
            val jarPath = File(
                FileAssociationService::class.java.protectionDomain.codeSource.location.toURI()
            ).absolutePath

            if (jarPath.endsWith(".jar")) {
                val javaHome = System.getProperty("java.home")
                val javaw = File(javaHome, "bin/javaw.exe")
                if (javaw.exists()) {
                    "\"${javaw.absolutePath}\" -jar \"$jarPath\" --run"
                } else {
                    "java -jar \"$jarPath\" --run"
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun registerFileAssociation(): Result<String> {
        if (!isWindows()) {
            return Result.failure(Exception("File association is only supported on Windows"))
        }

        val execPath = getExecutablePath()
            ?: return Result.failure(Exception("Cannot determine executable path. File association only works when running from JAR."))

        val runPath = getRunCommand()
            ?: return Result.failure(Exception("Cannot determine run command path."))

        return try {




            executeRegCommand("add", "HKCU\\Software\\Classes\\$PROG_ID", "/ve", "/d", "Templater Markdown File", "/f")


            executeRegCommand("add", "HKCU\\Software\\Classes\\$PROG_ID\\shell\\open\\command", "/ve", "/d", "$execPath \"%1\"", "/f")


            executeRegCommand("add", "HKCU\\Software\\Classes\\.md\\OpenWithProgids", "/v", PROG_ID, "/t", "REG_NONE", "/f")


            executeRegCommand("add", "HKCU\\Software\\Classes\\.md\\shell\\OpenWithTemplater", "/ve", "/d", "Open with $APP_NAME", "/f")
            executeRegCommand("add", "HKCU\\Software\\Classes\\.md\\shell\\OpenWithTemplater\\command", "/ve", "/d", "$execPath \"%1\"", "/f")


            val runKey = "HKCU\\Software\\Classes\\.md\\shell\\RunWithTemplater"
            executeRegCommand("add", runKey, "/ve", "/d", "Run with $APP_NAME", "/f")
            executeRegCommand("add", "$runKey\\command", "/ve", "/d", "$runPath \"%1\"", "/f")

            val successMsg = "File association registered successfully!\n\n" +
                "You can now right-click on .md files and select:\n" +
                "- 'Open with Templater' to edit\n" +
                "- 'Run with Templater' to execute"
            Result.success(successMsg)
        } catch (e: Exception) {
            Result.failure(Exception("Failed to register file association: ${e.message}"))
        }
    }

    fun unregisterFileAssociation(): Result<String> {
        if (!isWindows()) {
            return Result.failure(Exception("File association is only supported on Windows"))
        }

        return try {

            executeRegCommand("delete", "HKCU\\Software\\Classes\\$PROG_ID", "/f")
            executeRegCommand("delete", "HKCU\\Software\\Classes\\.md\\OpenWithProgids", "/v", PROG_ID, "/f")
            executeRegCommand("delete", "HKCU\\Software\\Classes\\.md\\shell\\OpenWithTemplater", "/f")
            executeRegCommand("delete", "HKCU\\Software\\Classes\\.md\\shell\\RunWithTemplater", "/f")

            Result.success("File association removed successfully!")
        } catch (e: Exception) {

            Result.success("File association removed (some entries may not have existed).")
        }
    }

    fun isRegistered(): Boolean {
        if (!isWindows()) return false

        return try {
            val process = ProcessBuilder("reg", "query", "HKCU\\Software\\Classes\\$PROG_ID")
                .redirectErrorStream(true)
                .start()
            process.waitFor() == 0
        } catch (e: Exception) {
            false
        }
    }

    private fun executeRegCommand(vararg args: String) {
        val command = listOf("reg") + args.toList()
        val process = ProcessBuilder(command)
            .redirectErrorStream(true)
            .start()

        val exitCode = process.waitFor()
        if (exitCode != 0) {
            val output = process.inputStream.bufferedReader().readText()
            throw Exception("Registry command failed (exit code $exitCode): $output")
        }
    }
}

