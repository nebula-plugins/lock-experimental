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

package com.netflix.nebula.lock.task

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.beust.klaxon.obj
import com.beust.klaxon.string
import com.netflix.nebula.lock.ConfigurationModuleIdentifier
import com.netflix.nebula.lock.LockService
import com.netflix.nebula.lock.withConf
import org.gradle.api.DefaultTask
import org.gradle.api.internal.artifacts.DefaultModuleIdentifier
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileNotFoundException
import java.util.*

open class ConvertLegacyLockTask: DefaultTask() {
    lateinit var lockService: LockService

    @TaskAction
    fun convert() {
        val lockExt = project.extensions.findByName("dependencyLock")
        val lockFile = File(project.projectDir, lockExt?.javaClass?.getMethod("getLockFile")
                ?.invoke(lockExt)?.let { it as String } ?: "dependencies.lock")

        if(!lockFile.exists()) {
            throw FileNotFoundException("No dependency lock file in this project")
        }

        val json = (Parser().parse(lockFile.inputStream()) as JsonObject)

        val overrides = project.configurations.fold(HashMap<ConfigurationModuleIdentifier, String>()) { acc, conf ->
            conf.dependencies.forEach { dep ->
                val mid = DefaultModuleIdentifier(dep.group, dep.name)
                json.obj(conf.name)?.obj(mid.toString())?.string("locked")?.let { locked ->
                    acc[mid.withConf(conf)] = locked
                }
            }
            acc
        }

        lockService?.updateLocks(overrides)

        lockFile.delete()
    }
}