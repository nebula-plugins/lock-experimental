package com.netflix.nebula.lock.groovy

import org.codehaus.groovy.ast.expr.MethodCallExpression

data class GroovyLockUpdate(val method: MethodCallExpression, val locks: List<String>)