package com.netflix.nebula.lock.groovy

import com.netflix.nebula.lock.ConfigurationModuleIdentifier
import com.netflix.nebula.lock.withConf
import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.control.SourceUnit
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleIdentifier
import org.gradle.api.artifacts.ResolvedDependency
import org.gradle.api.internal.artifacts.DefaultModuleIdentifier
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier
import java.util.*

class GroovyLockAstVisitor(val project: Project,
                           val overrides: Map<ConfigurationModuleIdentifier, String>): ClassCodeVisitorSupport() {
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
                        conf.lockedVersion(entries["group"], entries["name"]!!)
                    }
                    is ConstantExpression -> {
                        "([^:]*):([^:]+):([^@:]*).*".toRegex().matchEntire(arg.value as String)?.run {
                            val group = groupValues[1].let { if(it.isEmpty()) null else it }
                            val name = groupValues[2]
                            conf.lockedVersion(group, name)
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

    private fun Configuration.lockedVersion(group: String?, name: String): String? {
        val mid = DefaultModuleIdentifier(group, name)
        return overrides[mid.withConf(this)] ?: resolvedConfiguration.firstLevelModuleDependencies.find { it.module.id.module.equals(mid) }?.moduleVersion
    }
}