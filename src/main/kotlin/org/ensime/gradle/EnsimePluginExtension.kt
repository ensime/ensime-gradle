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

import org.gradle.api.Project
import java.io.File
import java.nio.file.Paths

open class EnsimePluginExtension(val project: Project) {
    private fun defaultJavaHome(): File {
        val home = File(System.getProperty("java.home"))
        return if (home.name.equals("jre")) home.parentFile else home
    }

    private fun defaultJavaSources(): List<File> {
        val home = File(System.getProperty("java.home"))
        return listOf(home).filter { it.exists() }
    }

    var scalaVersion: String? = null
    var scalaOrg: String? = null
    var ensimeServerVersion: String = EnsimePlugin.DEFAULT_SERVER_VERSION
    var ensimeFile: File = project.file(EnsimePlugin.DEFAULT_ENSIME_FILE)
    var cacheDir: File = project.file(EnsimePlugin.DEFAULT_ENSIME_CACHE)
    var javaHome: File = defaultJavaHome()
        set(home) {
            if (!home.exists()) {
                throw IllegalArgumentException("The specified java home directory [$home] does not exist")
            } else if (listOf("bin/java", "bin/java.exe").filter { File(home, it).exists()}.isEmpty()) {
                throw IllegalArgumentException("The specified java home directory [$home] does not point to a valid Java installation")
            } else {
                javaHome = home
            }
        }
    var javaFlags: List<String> = listOf()
    var javaSources: List<File> = defaultJavaSources()

    var serverJarsDir: String = Paths.get("build/ensime").toString()
    var downloadSources: Boolean = true
    var downloadJavadoc: Boolean = true

    override fun toString() =
            """|${this.javaClass.canonicalName}
               |Scala version: $scalaVersion
               |Scala organization: $scalaOrg
               |ENSIME Server Version: $ensimeServerVersion
               |ENSIME Configuration file: $ensimeFile
               |ENSIME Cache Directory: $cacheDir
               |Java Home Directory: $javaHome
               |""".trimMargin()
}
