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

import io.kotlintest.matchers.beInstanceOf
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldEqual
import io.kotlintest.matchers.shouldNotBe
import io.kotlintest.specs.FeatureSpec
import org.gradle.testfixtures.ProjectBuilder

class EnsimePluginTest : FeatureSpec() {
    init {
        EnsimeTestConfiguration.supportedVersions.forEach { gradleVersion ->
            feature("The ensime plugin in $gradleVersion") {
                scenario("enables the ensime task") {
                    val project = ProjectBuilder.builder().build()
                    project.pluginManager.apply("org.ensime.gradle")
                    project.extensions.findByName(EnsimePlugin.ENSIME_PLUGIN_NAME) shouldNotBe null
                    project.tasks.findByName("ensime") should beInstanceOf(EnsimeTask::class)
                }
                scenario("can be updated with non-default values") {
                    val project = ProjectBuilder.builder().build()
                    project.pluginManager.apply("org.ensime.gradle")
                    val ext = project.extensions.findByName(EnsimePlugin.ENSIME_PLUGIN_NAME) as? EnsimePluginExtension
                    ext?.scalaVersion = "2.10.4"

                    ext?.scalaVersion shouldEqual "2.10.4"
                    ext?.ensimeFile?.name shouldEqual EnsimePlugin.DEFAULT_ENSIME_FILE
                }
            }
        }
    }
}
