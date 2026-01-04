package ronsijm.templater.config

import java.util.Properties

data class ProductConfig(
    val productName: String,
    val pluginId: String,
    val description: String,
    val vendor: String
) {
    companion object {

        fun load(): ProductConfig {
            val props = Properties()
            val stream = ProductConfig::class.java.classLoader
                .getResourceAsStream("product.properties")

            stream?.use { props.load(it) }

            return ProductConfig(
                productName = props.getProperty("product.name", "Templater"),
                pluginId = props.getProperty("plugin.id", "ronsijm.templater"),
                description = props.getProperty("plugin.description", "Template engine for JetBrains IDEs"),
                vendor = props.getProperty("vendor", "RonSijm")
            )
        }

        fun createDefault(): ProductConfig {
            return ProductConfig(
                productName = "Templater",
                pluginId = "ronsijm.templater",
                description = "Template engine for JetBrains IDEs",
                vendor = "RonSijm"
            )
        }
    }
}
