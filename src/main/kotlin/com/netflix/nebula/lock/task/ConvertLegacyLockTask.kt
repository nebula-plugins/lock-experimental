package com.netflix.nebula.lock.task

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.beust.klaxon.obj
import com.beust.klaxon.string
import com.netflix.nebula.lock.ConfigurationModuleIdentifier
import com.netflix.nebula.lock.UpdateLockService
import com.netflix.nebula.lock.withConf
import org.gradle.api.DefaultTask
import org.gradle.api.internal.artifacts.DefaultModuleIdentifier
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileNotFoundException
import java.util.*

open class ConvertLegacyLockTask: DefaultTask() {
    @TaskAction
    fun convert() {
        val lockFile = File(project.projectDir, "dependencies.lock")
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

        UpdateLockService(project).update(overrides)

        lockFile.delete()
    }
}