/*
 * Copyright 2016-2017 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

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
        lock.writeText(lock.readText().replace(""""locked":(\s+)"[^"]+"""".toRegex(),
                """"locked":$1"16.0""""))

        runTasksSuccessfully("convertLegacyLock")

        assertTrue(buildFile.readText().contains("""
            dependencies {
                compile 'com.google.guava:guava:latest.release' lock '16.0'
            }
        """.trim('\n').trimIndent()))
    }
}
