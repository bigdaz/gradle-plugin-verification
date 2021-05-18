plugins {
    java
    groovy
    `java-gradle-plugin`
}
group = "com.gradle"
version = "0.1"

repositories {
    mavenCentral()
}
dependencies {
    implementation(gradleTestKit())
    implementation("com.google.code.gson:gson:2.8.6")
}

gradlePlugin {
    plugins {
        create("verifyPlugins") {
            id = "com.gradle.plugin-verifier"
            implementationClass = "com.gradle.pluginverifier.VerifyPluginsPlugin"
        }
    }
}