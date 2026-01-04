package ronsijm.templater.filesystem


interface FileSystem {

    fun getAllFiles(): List<FileHandle>


    fun createFile(path: String, content: String): FileHandle?


    fun findFile(path: String): FileHandle?


    fun getProjectRoot(): FileHandle?


    fun rename(file: FileHandle, newPath: String): FileHandle?
}

