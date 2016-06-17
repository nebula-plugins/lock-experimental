package com.netflix.nebula.lock

import com.netflix.nebula.lock.groovy.GroovyLockAstVisitor
import com.netflix.nebula.lock.groovy.GroovyLockWriter
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class UpdateLockTask: DefaultTask() {
    val groovyLockWriter = GroovyLockWriter()

    @TaskAction
    fun updateLock() = when {
        project.buildFile.name.endsWith("gradle") -> updateLockGroovy()
        project.buildFile.name.endsWith("kts") -> updateLockKotlin()
        else -> { /* do nothing */ }
    }

    fun updateLockGroovy() {
        val ast = AstBuilder().buildFromString(project.buildFile.readText())
        val stmt = ast.find { it is BlockStatement }
        if(stmt is BlockStatement) {
            val visitor = GroovyLockAstVisitor(project)
            visitor.visitBlockStatement(stmt)
            groovyLockWriter.updateLocks(project, visitor.updates)
        }
    }

    fun updateLockKotlin() {
        // TODO implement me
    }
}