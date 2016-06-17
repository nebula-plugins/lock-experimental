package com.netflix.nebula.lock

import com.netflix.nebula.lock.groovy.GroovyLockExtensions
import com.netflix.nebula.lock.task.ConvertLegacyLockTask
import com.netflix.nebula.lock.task.UpdateLockTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class NebulaLockPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.create("updateLocks", UpdateLockTask::class.java)
        project.tasks.create("convertLegacyLocks", ConvertLegacyLockTask::class.java)
        GroovyLockExtensions.enhanceDependencySyntax(project)
    }
}