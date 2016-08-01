package com.netflix.nebula.lock.groovy

import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.expr.BinaryExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.VariableExpression

class GroovyVariableExtractionVisitor : ClassCodeVisitorSupport() {
    override fun getSourceUnit() = null
    val variables = mutableMapOf<String, String>()

    override fun visitBinaryExpression(expression: BinaryExpression?) {
        val left = expression?.leftExpression
        val right = expression?.rightExpression
        if (left != null && right != null) {
            when (left) {
                is PropertyExpression -> {
                    val key = "${left.objectExpression.text}.${left.propertyAsString}"
                    variables.put(key, right.text)
                }
                is VariableExpression -> variables.put(left.text, right.text)
            }
        }
        super.visitBinaryExpression(expression)
    }
}
