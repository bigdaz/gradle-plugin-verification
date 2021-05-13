buildscript {
    repositories {
        mavenLocal()
    }
    dependencies {
        classpath("com.gradle:plugin-verifier:0.1")
    }
}

plugins {
    base
}

val samplesWorkingDir = project.layout.buildDirectory.dir("verified-plugins")
val resultsDir = project.layout.buildDirectory.dir("verification-results")
val copyPluginProjects = tasks.register<Copy>("copyPluginProjects") {
    from("verified-plugins")
    into(samplesWorkingDir)
}

project.file("verified-plugins").listFiles(File::isDirectory)!!.forEach { sample ->
    val sampleWorkingDir = samplesWorkingDir.map { s -> s.dir(sample.name) }
    val sampleResultsFile = resultsDir.map { d -> d.file(sample.name + ".txt") }

    tasks.register<com.gradle.pluginverifier.VerifyPluginTask>("verify_" + sample.name.replace('.', '_')) {
        dependsOn(copyPluginProjects)
        sampleDir.set(sampleWorkingDir)
        resultsFile.set(sampleResultsFile)
    }
}

tasks.register("verifyPlugins") {
    dependsOn(tasks.withType(com.gradle.pluginverifier.VerifyPluginTask::class))
}
