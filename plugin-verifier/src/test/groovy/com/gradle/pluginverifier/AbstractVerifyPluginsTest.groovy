package com.gradle.pluginverifier

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import spock.lang.Specification

abstract class AbstractVerifyPluginsTest extends Specification {
    def "validate plugin properties"() {
        expect:
        runBuild("validateExternalPlugins")
    }

    def "validate configuration cache"() {
        expect:
        runBuild("--configuration-cache", "clean", task)
    }

    def "validate build cache"() {
        if (!incremental) {
            // TODO: This should be SKIPPED (not PASSED) in this case
            return
        }
        given:
        // Run an initial clean build
        runBuild("--no-scan", "clean", task)

        // Without clean, task should be UP-TO-DATE
        when:
        def result = runBuild(task)
        then:
        result.task(task).outcome == TaskOutcome.UP_TO_DATE

        // With clean, task should be FROM_CACHE
        when:
        result = runBuild("clean", task)
        then:
        result.task(task).outcome == TaskOutcome.FROM_CACHE
    }

    def runBuild(String... arguments) {
        return GradleRunner.create()
                .forwardOutput()
                .withProjectDir(projectDir)
                .withArguments(
                        ["-I", "../../init.gradle", "--build-cache"] + arguments.toList()
                )
                .build()
    }

    private File getProjectDir() {
        return new File("build/verified-plugins", getExampleBuild())
    }

    protected abstract String getExampleBuild()

    protected abstract String getTask()

    protected static boolean isIncremental() {
        return true
    }
}
