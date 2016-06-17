package com.netflix.nebula.lock

import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleIdentifier
import org.gradle.api.internal.artifacts.DefaultModuleVersionIdentifier

// From: https://docs.gradle.org/current/javadoc/org/gradle/api/artifacts/dsl/DependencyHandler.html
// "In both notations, all properties, except name, are optional."
data class GradleDependency(val group: String?,
                            val name: String,
                            val version: String?) {
    val id = DefaultModuleVersionIdentifier(group, name, version)
}

data class ConfigurationModuleIdentifier(val conf: Configuration, val mid: ModuleIdentifier)

fun ModuleIdentifier.withConf(conf: Configuration) = ConfigurationModuleIdentifier(conf, this)