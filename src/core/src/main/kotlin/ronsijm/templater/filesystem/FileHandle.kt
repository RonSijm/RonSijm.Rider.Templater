package ronsijm.templater.filesystem


interface FileHandle {

    val path: String


    val name: String


    val extension: String


    val isDirectory: Boolean


    val parent: FileHandle?


    fun read(): String


    fun write(content: String)


    fun delete()


    fun exists(): Boolean


    fun getBaseName(): String {
        return if (extension.isNotEmpty()) {
            name.substringBeforeLast(".$extension")
        } else {
            name
        }
    }
}

