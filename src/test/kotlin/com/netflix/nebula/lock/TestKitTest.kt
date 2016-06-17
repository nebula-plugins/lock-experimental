package com.netflix.nebula.lock

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File

/**
 * Things that really belong in the unfinished Gradle Testkit
 */
open class TestKitTest {
    @JvmField @Rule
    val temp = TemporaryFolder()

    lateinit var projectDir: File
    lateinit var buildFile: File
    lateinit var settingsFile: File

    open fun before() {
        projectDir = temp.root
        buildFile = File(projectDir, "build.gradle")
        settingsFile = File(projectDir, "settings.gradle")
    }

    fun runner(tasks: Array<out String>) =
        GradleRunner.create()
                .withDebug(true)
                .withProjectDir(projectDir)
                .withArguments(tasks.toList().plus("--stacktrace"))
                .withPluginClasspath()

    fun runTasksSuccessfully(vararg tasks: String) = runner(tasks).build()
    fun runTasksAndFail(vararg tasks: String) = runner(tasks).buildAndFail()

    fun addSubproject(name: String): File {
        val subprojectDir = File(projectDir, name)
        subprojectDir.mkdirs()
        settingsFile.writeText("include '$name'\n")
        return subprojectDir
    }

    fun addSubproject(name: String, buildGradleContents: String): File {
        val subprojectDir = addSubproject(name)
        File(subprojectDir, "build.gradle").writeText(buildGradleContents)
        return subprojectDir
    }

    fun dependencies(vararg confs: String = arrayOf("compile", "testCompile")) =
        buildFile.readLines()
            .map { it.trim() }
            .filter { line -> confs.any { line.startsWith(it) } }
            .map { it.split("\\s+".toRegex())[1].replace("'".toRegex(), "") }
            .sorted()
}
