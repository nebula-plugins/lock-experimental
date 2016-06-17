package com.netflix.nebula.lock

import com.netflix.nebula.lock.groovy.GroovyLockAstVisitor
import com.netflix.nebula.lock.groovy.GroovyLockWriter
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.gradle.api.Project

class UpdateLockService(val project: Project) {
    val groovyLockWriter = GroovyLockWriter()

    fun update(overrides: Map<ConfigurationModuleIdentifier, String> = emptyMap()) = when {
        project.buildFile.name.endsWith("gradle") -> updateLockGroovy(overrides)
        project.buildFile.name.endsWith("kts") -> updateLockKotlin(overrides)
        else -> { /* do nothing */ }
    }

    fun updateLockGroovy(overrides: Map<ConfigurationModuleIdentifier, String>) {
        val ast = AstBuilder().buildFromString(project.buildFile.readText())
        val stmt = ast.find { it is BlockStatement }
        if(stmt is BlockStatement) {
            val visitor = GroovyLockAstVisitor(project, overrides)
            visitor.visitBlockStatement(stmt)
            groovyLockWriter.updateLocks(project, visitor.updates)
        }
    }

    fun updateLockKotlin(overrides: Map<ConfigurationModuleIdentifier, String>) {
        // TODO implement me
    }
}