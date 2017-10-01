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
import java.lang.management.ManagementFactory
import java.nio.file.Path

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
        ,val javaCompilerArgs: List<String>
        ,val scalaCompilerArgs: List<String>
        ,val scalaVersion: String
        ,val referenceSourceRoots: List<Path>
//        ,val subProjects: List<SubProject>
//        ,val projects: List<Project>
) {

    fun baseJavaFlags(): List<String> {
        val raw = ManagementFactory.getRuntimeMXBean().inputArguments
        val corrected = raw.filter {
            when {
                it.startsWith("-Xss") -> false
                it.startsWith("-Xms") -> false
                it.startsWith("-Xmx") -> false
                it.startsWith("-XX:MaxPermSize") -> false
                else -> true
            }
        }
        val memory = listOf(
                "-Xss2m",
                "-Xms512m",
                "-Xmx4g")

        val java = System.getProperty("java.version").substring(0, 3)
        val versioned =
                if (java.equals("1.6") || java.equals("1.7"))
                    listOf("-XX:MaxPermSize=256m")
                else
                    listOf("-XX:MaxMetaspaceSize=256m",
                        // these improve ensime-server performance
                        "-XX:StringTableSize=1000003",
                        "-XX:+UnlockExperimentalVMOptions",
                        "-XX:SymbolTableSize=1000003")
        return corrected + memory + versioned
    }

    fun toSExp(): String =
            """|(:root-dir  ${SExpression.from(rootDir)}
               | :cache-dir ${SExpression.from(cacheDir)}
               | :scala-version ${SExpression.from(scalaVersion)}
               | :scala-compiler-jars (${SExpression.from(scalaCompilerJars, 22)})
               | :ensime-server-jars (${SExpression.from(ensimeServerJars, 21)})
               | :name ${SExpression.from(name)}
               | :java-home ${SExpression.from(javaHome)}
               | :java-flags ${SExpression.from(baseJavaFlags() + javaFlags, 12)}
               | :java-sources ${SExpression.from(javaSources)}
               | :java-compiler-args ${SExpression.from(javaCompilerArgs)}
               | :compiler-args ${SExpression.from(scalaCompilerArgs)}
               | :reference-source-roots ${SExpression.from(referenceSourceRoots, 25)}
               | )""".trimMargin()

    override fun toString(): String = toSExp()
}