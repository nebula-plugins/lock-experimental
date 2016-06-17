package com.netflix.nebula.lock.groovy

import org.codehaus.groovy.ast.expr.MethodCallExpression

sealed class GroovyLockUpdate {
    abstract val method: MethodCallExpression

    class SingleDependencyLock(override val method: MethodCallExpression, val lock: String): GroovyLockUpdate()
    class CommaSeparatedDependenciesLock(override val method: MethodCallExpression, val locks: List<String>): GroovyLockUpdate()
}