package com.netflix.nebula.lock.groovy

import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency

class GroovyLockExtensions {
    /**
     * Because somehow project is sticky inside the lock closure, even if we remove the metaClass on Dependency.
     * This is only really a problem during test execution, but the solution doesn't harm normal operation.
     */
    static Project cachedProject

    static void enhanceDependencySyntax(Project project) {
        cachedProject = project

        Dependency.metaClass.lock = { lockedVersion ->
            if(delegate instanceof ExternalModuleDependency) {
                ExternalModuleDependency dep = delegate

                def containingConf = cachedProject.configurations.find { it.dependencies.any { it.is(dep) } }
                containingConf.dependencies.remove(dep)

                def locked = new DefaultExternalModuleDependency(dep.group, dep.name, lockedVersion, dep.configuration)
                locked.setChanging(dep.changing)
                locked.setForce(dep.force)

                containingConf.dependencies.add(locked)
            }
            else {
                // TODO locks do not apply to anything but external dependencies
            }
        }
    }
}
