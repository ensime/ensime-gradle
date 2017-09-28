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

import org.gradle.api.Plugin
import org.gradle.api.Project

class EnsimePlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.create(ENSIME_PLUGIN_NAME, EnsimePluginExtension::class.java, project)

        project.tasks.create(ENSIME_PLUGIN_NAME, EnsimeTask::class.java)
    }

    companion object {
        val ENSIME_PLUGIN_NAME = "ensime"
        val DEFAULT_SCALA_VERSION = "2.12.3"
        val DEFAULT_SERVER_VERSION = "2.0.0-M4"
        val DEFAULT_ENSIME_FILE = ".ensime"
        val DEFAULT_ENSIME_CACHE = ".ensime_cache"
        val DEFAULT_SCALA_ORG = "org.scala-lang"

        val DEFAULT_SCALA_LIBRARIES = setOf<String>(
                "scalap",
                "scala-compiler",
                "scala-reflect",
                "scala-library")

    }
}
