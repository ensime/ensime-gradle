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

import org.ensime.gradle.model.EnsimeConfig
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * Created by bcarlson on 7/2/17.
 */
open class EnsimeTask : DefaultTask() {
    @TaskAction
    fun generateConfig() {
        val taskConfig: EnsimePluginExtension = project.extensions.getByType(EnsimePluginExtension::class.java)
        val config = EnsimeConfig.build(taskConfig)
        val targetFile: File = taskConfig.ensimeFile

        targetFile.writeText(config.toSExp(), StandardCharsets.UTF_8)
    }
}