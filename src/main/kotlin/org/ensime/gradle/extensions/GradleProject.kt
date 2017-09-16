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
package org.ensime.gradle.extensions

import org.ensime.gradle.EnsimePlugin
import org.ensime.gradle.EnsimePluginExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.UnknownConfigurationException

fun Project.findScalaVersion(): String? {
    val extClass: Class<EnsimePluginExtension> = EnsimePluginExtension::class.java
    val extension = extensions.getByType(extClass)
    logger.debug("Found extension $extension")
    val version = extractScalaVersion(this)
    return version
}

fun extractScalaVersion(project: Project): String? =
        if (project.gradle.gradleVersion > "4") scalaVersion4(project)
        else scalaVersionOld(project)

fun scalaVersionOld(project: Project): String? =
    listOf("compile", "testCompile", "play")
            .flatMap {
                try {
                    listOf(project.configurations.getByName(it))
                } catch (_: UnknownConfigurationException) {
                    emptyList<Configuration>()
                }
            }
            .flatMap { it.allDependencies }
            .find { it.name == "scala-library" }
            ?.version


fun scalaVersion4(project: Project): String? =
        project.configurations
                .filter { it.isCanBeResolved }
                .flatMap { it.allDependencies }
                .find { it.name == "scala-library" }
                ?.version

fun Project.ensimeServerVersion() : String {
    return extensions?.getByType(EnsimePluginExtension::class.java)
            ?.serverVersion
            ?: EnsimePlugin.DEFAULT_SERVER_VERSION
}