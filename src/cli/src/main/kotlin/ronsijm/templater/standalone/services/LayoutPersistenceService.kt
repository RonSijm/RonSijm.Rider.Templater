package ronsijm.templater.standalone.services

import java.io.File
import java.util.Properties

object LayoutPersistenceService {

    private const val DEFAULT_LAYOUT_FILENAME = ".templater-layout.properties"

    private const val PROP_WINDOW_X = "window.x"
    private const val PROP_WINDOW_Y = "window.y"
    private const val PROP_WINDOW_WIDTH = "window.width"
    private const val PROP_WINDOW_HEIGHT = "window.height"
    private const val PROP_WINDOW_STATE = "window.state"

    fun getDefaultLayoutFile(): File {
        return File(System.getProperty("user.home"), DEFAULT_LAYOUT_FILENAME)
    }

    data class LayoutData(
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int,
        val extendedState: Int
    )

    fun saveLayout(file: File, layout: LayoutData) {
        val props = Properties()

        props.setProperty(PROP_WINDOW_X, layout.x.toString())
        props.setProperty(PROP_WINDOW_Y, layout.y.toString())
        props.setProperty(PROP_WINDOW_WIDTH, layout.width.toString())
        props.setProperty(PROP_WINDOW_HEIGHT, layout.height.toString())
        props.setProperty(PROP_WINDOW_STATE, layout.extendedState.toString())

        file.outputStream().use { output ->
            props.store(output, "Templater Layout Configuration")
        }
    }

    fun loadLayout(file: File, defaults: LayoutData): LayoutData {
        val props = Properties()
        file.inputStream().use { input ->
            props.load(input)
        }

        return LayoutData(
            x = props.getProperty(PROP_WINDOW_X)?.toIntOrNull() ?: defaults.x,
            y = props.getProperty(PROP_WINDOW_Y)?.toIntOrNull() ?: defaults.y,
            width = props.getProperty(PROP_WINDOW_WIDTH)?.toIntOrNull() ?: defaults.width,
            height = props.getProperty(PROP_WINDOW_HEIGHT)?.toIntOrNull() ?: defaults.height,
            extendedState = props.getProperty(PROP_WINDOW_STATE)?.toIntOrNull() ?: defaults.extendedState
        )
    }

    fun loadDefaultLayout(defaults: LayoutData): LayoutData {
        val file = getDefaultLayoutFile()
        return if (file.exists()) {
            try {
                loadLayout(file, defaults)
            } catch (e: Exception) {
                defaults
            }
        } else {
            defaults
        }
    }

    fun saveDefaultLayout(layout: LayoutData) {
        try {
            saveLayout(getDefaultLayoutFile(), layout)
        } catch (e: Exception) {

        }
    }
}

