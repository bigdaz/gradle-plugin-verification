plugins {
    java
    groovy
}
repositories {
    mavenCentral()
}
dependencies {
    testImplementation(gradleTestKit())

    testImplementation("org.codehaus.groovy:groovy:3.0.7")
    testImplementation("org.spockframework:spock-core:2.0-M4-groovy-3.0")
    testImplementation("junit:junit:4.13.1")
}

tasks.register<Copy>("copyPluginProjects") {
    from("../verified-plugins")
    into("build/verified-plugins")
}

tasks.test {
    dependsOn("copyPluginProjects")
    useJUnitPlatform()
}
