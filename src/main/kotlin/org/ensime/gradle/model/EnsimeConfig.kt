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

import org.ensime.gradle.EnsimePluginExtension
import org.ensime.gradle.extensions.toSExp
import java.nio.file.Path

data class EnsimeConfig(
        val rootDir: Path
//        ,val cacheDir: Path
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
    fun toSExp(): String =
            """|(:root-dir ${rootDir.toSExp}
               | :scala-version ${scalaVersion.toSExp}
               | :ensime-server-version ${ensimeServerVersion.toSExp}
               |)""".trimMargin("|")


    companion object {
        val CACHE_DIR = ".ensime_cache"

        fun build(extension: EnsimePluginExtension): EnsimeConfig = EnsimeConfig(
                rootDir = extension.project.rootDir.toPath()
                ,scalaVersion = extension.scalaVersion
                ,ensimeServerVersion = extension.serverVersion
        )
    }
}

