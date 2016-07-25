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

package com.netflix.nebula.lock.groovy

import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.gradle.api.Project

class GroovyLockWriter() {
    private val lockRegex = " lock '.*'\\s*$".toRegex()

    fun updateLocks(project: Project, updates: Collection<GroovyLockUpdate>) {
        val updated = StringBuffer()

        val lines = project.buildFile.readLines()
        var i = 0
        while(i < lines.size) {
            if(i > 0) updated.append('\n')

            updates.find { it.method.lastLineNumber-1 == i }?.apply {
                when(method.arguments) {
                    is ArgumentListExpression -> {
                        val trimmedLine = lines[i++].replace(lockRegex, "")
                        updated.append(trimmedLine)
                        if(lock != null) {
                            updated.append(" lock '$lock'")
                        }
                    }
                    is TupleExpression -> {
                        val trimmedLine = lines[i++].replace(lockRegex, "")
                        updated.append(trimmedLine)
                        if(lock != null) {
                            updated.append(" lock '$lock'")
                        }
                    }
                }

            } ?: updated.append(lines[i++])
        }

        project.buildFile.writeText(updated.toString())
    }

}
