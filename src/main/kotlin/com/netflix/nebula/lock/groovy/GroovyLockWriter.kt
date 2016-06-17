package com.netflix.nebula.lock.groovy

import org.gradle.api.Project

class GroovyLockWriter() {

    fun updateLocks(project: Project, updates: Collection<GroovyLockUpdate>) {
        val updated = StringBuffer()

        val lines = project.buildFile.readLines()
        var i = 0
        while(i < lines.size) {
            if(i > 0) updated.append('\n')

            updates.find { it.method.lineNumber-1 == i }?.apply {
                when(this) {
                    is GroovyLockUpdate.SingleDependencyLock -> {
                        if(method.lastLineNumber-1 > i)
                            updated.append(lines.subList(i, method.lastLineNumber-1).joinToString("\n") + "\n")
                        updated.append(lines[method.lastLineNumber-1].substring(0, method.lastColumnNumber-1))
                        updated.append(" lock '$lock'")
                        i += method.lastLineNumber - method.lineNumber + 1
                    }
                    is GroovyLockUpdate.CommaSeparatedDependenciesLock -> {
                        // TODO split ast into several
                    }
                }
            } ?: updated.append(lines[i++])
        }

        project.buildFile.writeText(updated.toString())
    }

}
