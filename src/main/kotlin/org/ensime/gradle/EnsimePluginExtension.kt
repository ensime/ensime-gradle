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
import org.ensime.gradle.extensions.findScalaVersion
import java.io.File

open class EnsimePluginExtension(val project: Project) {
    private var _scalaVersion: String? = null
    var scalaVersion: String
        get() = _scalaVersion ?: project.findScalaVersion() ?: EnsimePlugin.DEFAULT_SCALA_VERSION
        set(value) { _scalaVersion = value }

    var serverVersion: String = EnsimePlugin.DEFAULT_SERVER_VERSION
    var ensimeFile: File = File(EnsimePlugin.DEFAULT_ENSIME_FILE)

//        ,var javaHome: Path? = Paths.get(System.getProperty("java.home"))
//        ,var cacheDir: Path? = Paths.get("./ensime_cache")
//        ,var serverJarsDir: Path? = Paths.get("build/ensime")
//        ,var downloadSources: Boolean? = true
//        ,var downloadJavadoc: Boolean? = true

    override fun toString() =
            """|${this.javaClass.canonicalName}
               |Scala version: $scalaVersion
               |Server version: $serverVersion
               """.trimMargin()
}