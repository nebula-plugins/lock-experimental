package com.netflix.nebula.lock

import com.netflix.nebula.lock.groovy.GroovyLockExtensions
import org.gradle.api.Plugin
import org.gradle.api.Project

class NebulaLockPlugin: Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.create("updateLocks", UpdateLockTask::class.java)
        project.extensions.add("lockedDependencies", arrayListOf<Any>())
        GroovyLockExtensions.enhanceDependencySyntax(project)
    }
}