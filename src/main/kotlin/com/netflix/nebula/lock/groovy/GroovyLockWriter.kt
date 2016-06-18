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

    fun updateLocks(project: Project, updates: Collection<GroovyLockUpdate>) {
        val updated = StringBuffer()

        val lines = project.buildFile.readLines()
        var i = 0
        while(i < lines.size) {
            if(i > 0) updated.append('\n')

            updates.find { it.method.lineNumber-1 == i }?.apply {
                when(method.arguments) {
                    is ArgumentListExpression -> {
                        val conf = method.methodAsString
                        val args = (method.arguments as ArgumentListExpression).expressions
                        args.forEachIndexed { j, arg ->
                            if(j > 0) updated.append('\n')
                            updated.append(conf.padStart(method.method.columnNumber + conf.length - 1, ' '))
                            updated.append("".padStart(args[0].columnNumber - method.method.lastColumnNumber, ' '))
                            updated.append(lines[arg.lineNumber-1].substring(arg.columnNumber-1, arg.lastColumnNumber-1))
                            if(locks[j] is String)
                                updated.append(" lock '${locks[j]}'")
                            i++
                        }
                    }
                    is TupleExpression -> {
                        if(method.lastLineNumber-1 > i)
                            updated.append(lines.subList(i, method.lastLineNumber-1).joinToString("\n") + "\n")
                        updated.append(lines[method.lastLineNumber-1].substring(0, method.lastColumnNumber-1))
                        if(locks[0] is String)
                            updated.append(" lock '${locks[0]}'")
                        i += method.lastLineNumber - method.lineNumber + 1
                    }
                }

            } ?: updated.append(lines[i++])
        }

        project.buildFile.writeText(updated.toString())
    }

}
