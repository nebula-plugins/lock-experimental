package com.netflix.nebula.lock.groovy

class NebulaLockExtension {
    def ignore(Closure dep) { dep.call() }
}
