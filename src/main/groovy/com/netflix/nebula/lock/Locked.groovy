package com.netflix.nebula.lock

import groovy.transform.Canonical
import org.gradle.api.artifacts.Dependency

@Canonical
class Locked {
    Dependency locked
    Dependency original
}
