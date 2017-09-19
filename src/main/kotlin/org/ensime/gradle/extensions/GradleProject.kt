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
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.UnknownConfigurationException

fun Project.scalaDependencies(): Set<Dependency> {
    fun scalaDependencies4(): Set<Dependency> = project.allprojects
            .flatMap { it.configurations }
            .filter { it.isCanBeResolved }
            .flatMap { it.allDependencies }
            .filter { EnsimePlugin.DEFAULT_SCALA_LIBRARIES.contains(it.name) }
            .toSet()

    fun scalaDependenciesOld(): Set<Dependency> = listOf("compile", "testCompile", "play")
            .flatMap {
                try {
                    listOf(project.configurations.getByName(it))
                } catch (_: UnknownConfigurationException) {
                    emptyList<Configuration>()
                }
            }
            .flatMap { it.allDependencies }
            .filter { EnsimePlugin.DEFAULT_SCALA_LIBRARIES.contains(it.name) }
            .toSet()


    return if (project.gradle.gradleVersion > "4") scalaDependencies4()
    else scalaDependenciesOld()
}


fun Set<Dependency>.findScalaVersion(): String? =
        this.find { it.name == "scala-library" }?.version

fun Set<Dependency>.findScalaOrg(): String? =
        this.find { it.name == "scala-library" }?.group
