package com.netflix.nebula.lock

import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class UpdateLockTaskTest : TestKitTest() {
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

        """.trim('\n').trimIndent())
    }

    @Test
    fun updateLocks() {
        buildFile.appendText("""
            dependencies {
                compile 'com.google.guava:guava:18.+'
                compile group: 'commons-lang',
                    name: 'commons-lang',
                    version: '2.+'
            }
        """.trim('\n').trimIndent())

        runTasksSuccessfully("updateLocks")

        assertTrue(buildFile.readText().contains("""
            dependencies {
                compile 'com.google.guava:guava:18.+' lock '18.0'
                compile group: 'commons-lang',
                    name: 'commons-lang',
                    version: '2.+' lock '2.6'
            }
        """.trim('\n').trimIndent()))
    }

    @Test
    fun lockStatementsOnDependenciesWithStaticVersionsAreRemoved() {
    }

    @Test
    fun multipleDependenciesAddedToConfigurationCommaSeparated() {
        buildFile.appendText("""
            dependencies {
                compile 'com.google.guava:guava:18.+',
                        'commons-lang:commons-lang:2.+'
            }
        """.trim('\n').trimIndent())

        runTasksSuccessfully("updateLocks")

        assertTrue(buildFile.readText().contains("""
            dependencies {
                compile 'com.google.guava:guava:18.+' lock '18.0'
                compile 'commons-lang:commons-lang:2.+' lock '2.6'
            }
        """.trim('\n').trimIndent()))
    }
}