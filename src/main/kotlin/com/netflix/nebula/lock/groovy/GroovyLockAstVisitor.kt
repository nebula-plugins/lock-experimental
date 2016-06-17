package com.netflix.nebula.lock.groovy

import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.control.SourceUnit
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.internal.artifacts.DefaultModuleIdentifier
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier
import java.util.*

class GroovyLockAstVisitor(val project: Project): ClassCodeVisitorSupport() {
    val updates = ArrayList<GroovyLockUpdate>()
    private var inDependencies = false

    override fun getSourceUnit(): SourceUnit? = null

    override fun visitMethodCallExpression(call: MethodCallExpression) {
        if(inDependencies)
            visitMethodCallInDependencies(call)

        if(call.methodAsString == "dependencies") {
            inDependencies = true
            super.visitMethodCallExpression(call)
            inDependencies = false
        }
        else super.visitMethodCallExpression(call)
    }

    fun visitMethodCallInDependencies(call: MethodCallExpression) {
        // https://docs.gradle.org/current/javadoc/org/gradle/api/artifacts/dsl/DependencyHandler.html
        val conf = project.configurations.findByName(call.methodAsString)
        val args = when(call.arguments) {
            is ArgumentListExpression -> (call.arguments as ArgumentListExpression).expressions
            is TupleExpression -> (call.arguments as TupleExpression).expressions
            else -> emptyList()
        }

        if(conf is Configuration) {
            val locks = args.map { arg ->
                when(arg) {
                    is MapExpression -> {
                        val entries = collectEntryExpressions(args)
                        conf.find(entries["group"], entries["name"]!!, entries["version"])?.let { it.moduleVersion }
                    }
                    is ConstantExpression -> {
                        "([^:]*):([^:]+):([^@:]*).*".toRegex().matchEntire(arg.value as String)?.run {
                            val group = groupValues[1].let { if(it.isEmpty()) null else it }
                            val name = groupValues[2]
                            val version = groupValues[3].let { if(it.isEmpty()) null else it }
                            conf.find(group, name, version)?.let { it.moduleVersion }
                        }
                    }
                    else -> null
                }
            }.filterNotNull()

            if(locks.size > 1) {
                updates.add(GroovyLockUpdate.CommaSeparatedDependenciesLock(call, locks))
            }
            else if(locks.size == 1) {
                updates.add(GroovyLockUpdate.SingleDependencyLock(call, locks[0]))
            }
        }
    }

    private fun collectEntryExpressions(args: List<Expression>) =
            args.filterIsInstance(MapExpression::class.java)
                    .map { it.mapEntryExpressions }
                    .flatten()
                    .map { it.keyExpression.text to it.valueExpression.text }
                    .toMap()

    private fun Configuration.find(group: String?, name: String, version: String?): ResolvedDependency? {
        val mid = DefaultModuleIdentifier(group, name)
        return resolvedConfiguration.firstLevelModuleDependencies.find { it.module.id.module.equals(mid) }
    }
}