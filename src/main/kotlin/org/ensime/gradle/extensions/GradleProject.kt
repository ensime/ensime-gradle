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
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.ResolvedArtifactResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.component.Artifact
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.scala.ScalaCompile
import org.gradle.jvm.JvmLibrary
import org.gradle.language.base.artifact.SourcesArtifact
import org.gradle.language.java.artifact.JavadocArtifact
import java.nio.file.Path

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

fun Project.javaCompilerArgs(): List<String> =
        this.tasks.filterIsInstance(JavaCompile::class.java)
                .filter { it.enabled }
                .flatMap { it.options.compilerArgs }
                .distinct()

fun Project.scalaCompilerArgs(): List<String> =
        this.tasks.filterIsInstance(ScalaCompile::class.java)
                .filter { it.enabled }
                .flatMap { it.options.compilerArgs }
                .distinct()

fun <T : Artifact> Project.getArtifacts(configuration: Configuration, clazz: Class<T>): List<ResolvedArtifactResult> {
    val componentIds = configuration.incoming
            .resolutionResult
            .allDependencies
            .filterIsInstance(ResolvedDependencyResult::class.java)
            .map { it?.selected?.id }
            .filterIsInstance(ModuleComponentIdentifier::class.java)

    return this.dependencies
            .createArtifactResolutionQuery()
            .forComponents(componentIds)
            .withArtifacts(JvmLibrary::class.java, clazz)
            .execute()
            .resolvedComponents
            .flatMap { it.getArtifacts(clazz) }
            .filterIsInstance(ResolvedArtifactResult::class.java)
}

fun Project.getReferenceSourceRoots(): List<Path> =
        getArtifacts(this.configurations.getByName("testCompile"), SourcesArtifact::class.java)
                .map { it.file.absoluteFile.toPath() }

fun Project.getDocJars():List<Path> =
        getArtifacts(project.configurations.getByName("testCompile"), JavadocArtifact::class.java)
                .map { it.file.absoluteFile.toPath() }

fun Set<Dependency>.findScalaVersion(): String? =
        this.find { it.name == "scala-library" }?.version

fun Set<Dependency>.findScalaOrg(): String? =
        this.find { it.name == "scala-library" }?.group

object SExpression {
    fun spaces(indent: Int): String = if (indent == 0) "" else (0..indent).map { " " }.joinToString("")

    fun <A> from(list: List<A>, indent: Int = 0) =
            if (list.isEmpty()) "nil"
            else {
                "(" + list.map { from(it) }.joinToString("\n${spaces(indent)}") + ")"
            }

    fun <A> from(a: A, indent: Int = 0): String =
            """${spaces(indent)}"$a""""
}
