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

val copySampleInitScripts = tasks.register<Sync>("copySampleInitScripts") {
    from("verified-plugins")
    include("*-init.gradle")
    into(samplesWorkingDir)
}

project.file("verified-plugins").listFiles(File::isDirectory)!!.forEach { sample ->
    tasks.register<com.gradle.pluginverifier.VerifyPluginTask>("verify_" + sample.name.replace('.', '_')) {
        dependsOn(copySampleInitScripts)
        sampleDir.set(sample)
        sampleWorkingDir.set(samplesWorkingDir.map { d -> d.dir(sample.name) })
        resultsFile.set(resultsDir.map { d -> d.file(sample.name + ".json") })
//        publishBuildScans.set(true)
    }
}

tasks.register("verifyPlugins") {
    dependsOn(tasks.withType(com.gradle.pluginverifier.VerifyPluginTask::class))
}
