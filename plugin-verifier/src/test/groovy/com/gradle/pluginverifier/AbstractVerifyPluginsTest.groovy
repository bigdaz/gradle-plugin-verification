package com.gradle.pluginverifier

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification
import spock.lang.Unroll

abstract class AbstractVerifyPluginsTest extends Specification {
    static List<String> getGradleVersions() {
        ["5.6.4", "6.8.3", "7.0"]
    }

    def "plugin properties are correctly annotated"() {
        expect:
        runBuild("-I", "../validate-plugin-init.gradle", "validateExternalPlugins")
    }

    def "works with configuration cache"() {
        expect:
        runBuild("--configuration-cache", "clean", task)
    }

    def "works with build cache"() {
        if (!incremental) {
            // TODO: This should be SKIPPED (not PASSED) in this case
            return
        }
        given:
        // Run an initial clean build
        runBuild("--no-scan", "clean", task)

        // Without clean, task should be UP-TO-DATE
        when:
        BuildResult result = runBuild(task)
        then:
        result.task(task).outcome == TaskOutcome.UP_TO_DATE

        // With clean, task should be FROM_CACHE
        when:
        result = runBuild("clean", task)
        then:
        result.task(task).outcome == TaskOutcome.FROM_CACHE
    }

    @Unroll
    def "version #pluginVersion works with Gradle #gradleVersion"() {
        when:
        def args = ["-PpluginVersion=" + pluginVersion, "clean", task]
        def gradleRunner = gradleRunner(args)
                .withGradleVersion(gradleVersion)

        then:
        gradleRunner.build()

        where:
        [pluginVersion, gradleVersion] << [pluginVersions, gradleVersions].combinations()
    }

    def runBuild(String... arguments) {
        def args = ["-PpluginVersion=" + latestPluginVersion()] + arguments.toList()
        return gradleRunner(args).build()
    }

    def gradleRunner(List<String> arguments) {
//        def args = ["-I", "../build-scan-init.gradle", "--build-cache"] + arguments.toList()
        def args = ["--build-cache"] + arguments

        return GradleRunner.create()
                .forwardOutput()
                .withProjectDir(getProjectDir())
                .withArguments(args)
    }

    private File getProjectDir() {
        return new File("build/verified-plugins", getPluginId())
    }

    private String latestPluginVersion() {
        getPluginVersions().sort().last()
    }

    protected abstract String getPluginId()

    protected abstract List<String> getPluginVersions()

    protected abstract String getTask()

    protected static boolean isIncremental() {
        return true
    }
}
