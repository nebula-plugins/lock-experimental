package com.netflix.nebula.lock.task

import com.netflix.nebula.lock.UpdateLockService
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class UpdateLockTask: DefaultTask() {
    @TaskAction
    fun updateLock() = UpdateLockService(project).update()
}