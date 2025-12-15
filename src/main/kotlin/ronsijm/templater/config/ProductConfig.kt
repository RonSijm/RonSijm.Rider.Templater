package ronsijm.templater.config

import java.util.Properties

/**
 * Product configuration for IDE-specific branding
 * Loads product-specific metadata from properties file at runtime
 */
data class ProductConfig(
    val productName: String,
    val pluginId: String,
    val description: String,
    val vendor: String
) {
    companion object {
        /**
         * Load product configuration from product.properties file
         * This file is generated at build time based on the variant being built
         */
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
        
        /**
         * Create a default configuration for testing
         */
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

