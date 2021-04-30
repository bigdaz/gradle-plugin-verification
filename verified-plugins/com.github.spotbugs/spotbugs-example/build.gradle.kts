plugins {
    java
    id("com.github.spotbugs") version "4.6.0"
}
repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use JUnit test framework.
    testImplementation("junit:junit:4.13.1")
}
