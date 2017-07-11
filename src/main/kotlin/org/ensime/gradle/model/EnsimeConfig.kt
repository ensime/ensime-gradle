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
package org.ensime.gradle.model

import org.ensime.gradle.extensions.ensimeServerVersion
import org.ensime.gradle.extensions.findScalaVersion
import java.io.File
import java.nio.file.Path

data class EnsimeConfig(
        val rootDir: Path
        ,val cacheDir: Path
        ,val scalaVersion: String
        ,val ensimeServerVersion: String
//        ,val scalaCompilerJars: List<Path>
//        ,val ensimeServerJars: List<Path>
//        ,val name: String
//        ,val javaHome: Path
//        ,val javaSources: List<Path>
//        ,val javaCompilerArgs: List<String>
//        ,val referenceSourceRoots: List<Path>
//        ,val compilerArgs: List<String>
//        ,val subProjects: List<SubProject>
//        ,val projects: List<Project>
) {
    companion object {
        val CACHE_DIR = ".ensime_cache"

        fun fromGradleProject(project: org.gradle.api.Project) = EnsimeConfig(
                rootDir = project.rootDir.toPath()
                ,cacheDir = project.rootDir.toPath().resolve(CACHE_DIR)
                ,scalaVersion = project.findScalaVersion()
                ,ensimeServerVersion = project.ensimeServerVersion()
//                ,scalaCompilerJars = TODO("Retrieve scala jars")
//                ,ensimeServerJars = TODO("Retrieve ENSIME server jars")
//                ,name = project.name
//                ,javaHome = File(System.getProperty("java.home")).toPath()
//                ,javaSources = TODO("Retrieve Java sources")
//                ,javaCompilerArgs = TODO("Java compiler args")
//                ,referenceSourceRoots = TODO("Reference source roots")
//                ,compilerArgs = listOf()
//                ,subProjects = listOf()
//                ,projects = listOf()
        )
    }
}

