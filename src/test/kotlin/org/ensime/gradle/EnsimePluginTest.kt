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

import com.google.common.io.Files
import io.kotlintest.matchers.include
import io.kotlintest.matchers.should
import io.kotlintest.matchers.shouldBe
import io.kotlintest.specs.BehaviorSpec
import org.gradle.testkit.runner.GradleRunner
import java.io.File

class EnsimePluginTest : BehaviorSpec() {

    init {
        val supportedVersions = listOf("2.8", "2.11", "3.1", "3.5", "4.0.1")

        supportedVersions.forEach { gradleVersion ->
            Given("A basic project for version $gradleVersion") {
                val rootDir = Files.createTempDir()
                val buildFile = File(rootDir, "build.gradle")
                buildFile.writeText("""
                    |plugins {
                    |  id 'scala'
                    |  id 'org.ensime.gradle'
                    |}
                    |
                    |repositories {
                    |  mavenCentral()
                    |}
                    |
                    |dependencies {
                    |  compile 'org.scala-lang:scala-library:2.10.4'
                    |}
                    |""".trimMargin())
                When("Executed") {
                    val result = GradleRunner.create()
                            .withGradleVersion(gradleVersion)
                            .withProjectDir(rootDir)
                            .withPluginClasspath()
                            .withArguments("ensime", "--debug", "--stacktrace")
                            .build()
                    Then("The plugin should be enabled") {
                        result.output should include("Using Scala version 2.10.4")
//                        File(rootDir, ".ensime").exists() shouldBe true
//                        File(rootDir, ".ensime").readText() should include("(:scala-version \"2.10.4\")")
                    }
                }
            }
        }
    }
}