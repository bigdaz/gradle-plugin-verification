plugins {
    `java`
}
repositories {
    mavenCentral()
}
dependencies {
    testImplementation(gradleTestKit())
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
