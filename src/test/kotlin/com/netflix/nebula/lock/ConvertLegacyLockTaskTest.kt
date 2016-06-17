package com.netflix.nebula.lock

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ConvertLegacyLockTaskTest: TestKitTest() {
    @Test
    fun convertLegacyLock() {
        buildFile.writeText("""
            plugins {
                id 'java'
                id 'nebula.lock'
                id 'nebula.dependency-lock' version '4.3.0'
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                compile 'com.google.guava:guava:latest.release'
            }
        """.trim('\n').trimIndent())

        runTasksSuccessfully("generateLock", "saveLock")
        val lock = File(projectDir, "dependencies.lock")
        lock.writeText(lock.readText().replace("\\d+\\.0".toRegex(), "16.0"))

        runTasksSuccessfully("convertLegacyLock")

        assertTrue(buildFile.readText().contains("""
            dependencies {
                compile 'com.google.guava:guava:latest.release' lock '16.0'
            }
        """.trim('\n').trimIndent()))
    }
}
