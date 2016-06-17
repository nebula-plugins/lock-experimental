package com.netflix.nebula.lock

import org.gradle.testkit.runner.BuildResult
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LockTest: TestKitTest() {

    @Before
    override fun before() {
        super.before()
        buildFile.writeText("""
            plugins {
                id 'java'
                id 'nebula.lock'
            }

            repositories {
                mavenCentral()
            }

            task listDependencies << {
                [configurations.compile, configurations.testCompile].each { conf ->
                    conf.resolvedConfiguration.firstLevelModuleDependencies.each {
                        println "${'$'}conf.name: ${'$'}it.module.id"
                    }
                }
            }
        """)
    }

    @Test
    fun lockingInOneConfigurationDoesNotAffectAnother() {
        buildFile.appendText("""
            dependencies {
                compile 'com.google.guava:guava:latest.release' lock '16.0'
                testCompile 'com.google.guava:guava:19.0'
            }
        """)

        val result = runTasksSuccessfully("listDependencies")
        result.assertDependency("com.google.guava:guava:16.0", conf = "compile", sourceSet = "main")
        result.assertDependency("com.google.guava:guava:19.0", conf = "testCompile", sourceSet = "test")
    }

    @Test
    fun lockingStringNotationWithClosure() {
        buildFile.appendText("""
            dependencies {
                compile('com.google.guava:guava:latest.release') { changing = true } lock '16.0'
            }
        """)

        val result = runTasksSuccessfully("listDependencies")
        result.assertDependency("com.google.guava:guava:16.0", conf = "compile", sourceSet = "main")
    }

    @Test
    fun lockingMapNotation() {
        buildFile.appendText("""
            dependencies {
                compile group: 'com.google.guava', name: 'guava', version: 'latest.release' lock '16.0'
            }
        """)

        val result = runTasksSuccessfully("listDependencies")
        result.assertDependency("com.google.guava:guava:16.0", conf = "compile", sourceSet = "main")
    }

    @Test
    fun lockingOnCommaSeparatedListOfDependenciesFails() {
        buildFile.appendText("""
            dependencies {
                compile(
                    'com.google.guava:guava:latest.release',
                    'commons-lang:commons-lang:latest.release'
                ) lock '16.0'
            }
        """)

        runTasksAndFail("listDependencies")
    }

    fun BuildResult.assertDependency(mvid: String, conf: String, sourceSet: String) {
        assertTrue(output.contains("$conf: $mvid"))
    }
}