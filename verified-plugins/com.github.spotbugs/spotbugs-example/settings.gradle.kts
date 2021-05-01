pluginManagement {
    val pluginVersion: String by settings
    plugins {
        id("com.github.spotbugs") version pluginVersion
    }
}
rootProject.name = "spotbugs-example"
