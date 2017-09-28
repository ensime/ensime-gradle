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

import org.ensime.gradle.extensions.SExpression
import java.io.File

data class
EnsimeConfig(
        val rootDir: File
        ,val cacheDir: File
        ,val scalaCompilerJars: List<File>
        ,val ensimeServerJars: List<File>
        ,val ensimeServerVersion: String
        ,val name: String
        ,val javaHome: File
        ,val javaFlags: List<String>
        ,val javaSources: List<File>
//        ,val javaCompilerArgs: List<String>
//        ,val referenceSourceRoots: List<Path>
//        ,val compilerArgs: List<String>
//        ,val subProjects: List<SubProject>
//        ,val projects: List<Project>
) {
    fun toSExp(): String =
            """|(:root-dir  ${SExpression.from(rootDir)}
               | :cache-dir ${SExpression.from(cacheDir)}
               | :scala-compiler-jars (${SExpression.from(scalaCompilerJars, 22)})
               | :ensime-server-jars (${SExpression.from(ensimeServerJars, 21)})
               | :name ${SExpression.from(name)}
               | :java-home ${SExpression.from(javaHome)}
               | :java-flags ${SExpression.from(javaFlags)}
               | :java-sources ${SExpression.from(javaSources)}
               | )""".trimMargin()

    override fun toString(): String = toSExp()
}