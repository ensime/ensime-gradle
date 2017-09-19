/**
 *  Copyright 2017 ENSIME Gradle Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ensime.gradle

import org.ensime.gradle.extensions.findScalaOrg
import org.ensime.gradle.extensions.findScalaVersion
import org.ensime.gradle.extensions.scalaDependencies
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * Created by bcarlson on 7/2/17.
 */
open class EnsimeTask : DefaultTask() {
    val ext: EnsimePluginExtension = project.extensions.getByType(EnsimePluginExtension::class.java)

    fun resolveLibraries(configurationName: String, dependencies: List<String>): List<File> {
        val config = project.configurations.create(configurationName)

        project.dependencies.apply {
            dependencies.forEach {
                add(config.name, it)
            }
        }

        val dependencyFiles = config.resolve().toList()

        logger.debug("Found ${config.name} jars: ${dependencies.joinToString(",")}")

        return dependencyFiles
    }

    fun resolveScalaJars(scalaOrg: String, scalaVersion: String): List<File> =
            resolveLibraries("ensimeScala", listOf(
                    "$scalaOrg:scalap:$scalaVersion@jar",
                    "$scalaOrg:scala-compiler:$scalaVersion@jar",
                    "$scalaOrg:scala-reflect:$scalaVersion@jar",
                    "$scalaOrg:scala-library:$scalaVersion@jar"))

    fun resolveEnsimeJars(scalaVersion: String): List<File> {
        val scalaMinorVersion = scalaVersion.substringBeforeLast('.')
        return resolveLibraries("ensimeServer", listOf(
                "org.ensime:server_$scalaMinorVersion:${ext.ensimeServerVersion}"
        ))
    }

    @TaskAction
    fun generateConfig() {
        val scalaDependencies = project.scalaDependencies()

        val scalaOrg = ext.scalaOrg ?:
                scalaDependencies.findScalaOrg() ?: EnsimePlugin.DEFAULT_SCALA_ORG
        logger.debug("Using Scala org $scalaOrg")
        val scalaVersion = ext.scalaVersion ?:
                scalaDependencies.findScalaVersion() ?: EnsimePlugin.DEFAULT_SCALA_VERSION
        logger.debug("Using Scala version $scalaVersion")

        val scalaJars: List<File> = resolveScalaJars(scalaOrg, scalaVersion)

        val ensimeJars = resolveEnsimeJars(scalaVersion)
    }
}
