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

import com.netflix.nebula.lock.groovy.GroovyLockAstVisitor
import com.netflix.nebula.lock.groovy.GroovyLockWriter
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.gradle.api.Project

class UpdateLockService(val project: Project) {
    val groovyLockWriter = GroovyLockWriter()

    fun update(overrides: Map<ConfigurationModuleIdentifier, String> = emptyMap()) {
        project.configurations.all {
            it.resolutionStrategy.apply {
                cacheDynamicVersionsFor(0, "seconds")
                cacheChangingModulesFor(0, "seconds")
            }
        }

        when {
            project.buildFile.name.endsWith("gradle") -> updateLockGroovy(overrides)
            project.buildFile.name.endsWith("kts") -> updateLockKotlin(overrides)
            else -> { /* do nothing */ }
        }
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