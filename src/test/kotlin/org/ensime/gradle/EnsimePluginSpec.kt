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
import io.kotlintest.specs.BehaviorSpec
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import java.nio.charset.StandardCharsets

class EnsimePluginSpec : BehaviorSpec({
    EnsimeTestConfiguration.supportedVersions.forEach { gradleVersion ->
        Given("A Gradle $gradleVersion environment") {
            When("A basic Scala project is created") {
                val rootDir = Files.createTempDir()

                val buildFile = File(rootDir, "build.gradle")
                buildFile.writeText("""
                    |plugins {
                    |  id 'scala'
                    |  id 'org.ensime.gradle'
                    |}
                    |
                    |repositories {
                    |  mavenLocal()
                    |  mavenCentral()
                    |}
                    |
                    |dependencies {
                    |  compile 'org.scala-lang:scala-library:2.10.4'
                    |}
                    |""".trimMargin())
                val result = GradleRunner.create()
                        .forwardOutput()
                        .withGradleVersion(gradleVersion)
                        .withProjectDir(rootDir)
                        .withPluginClasspath()
                        .withArguments("ensime", "--debug", "--stacktrace")
                        .build()
                Then("The Scala version should be read from the configuration") {
                    result.output should include("Using Scala version 2.10.4")
                    result.output should include("org.ensime/server_2.10/2.0.0-M4")
                }
            }
            When("Used with a Java project") {
                val rootDir = Files.createTempDir()

                val buildFile = File(rootDir, "build.gradle")
                buildFile.writeText("""
                    |plugins {
                    |  id 'java'
                    |  id 'org.ensime.gradle'
                    |}
                    |
                    |repositories {
                    |  mavenLocal()
                    |  mavenCentral()
                    |}
                    |""".trimMargin())
                val result = GradleRunner.create()
                        .forwardOutput()
                        .withGradleVersion(gradleVersion)
                        .withProjectDir(rootDir)
                        .withPluginClasspath()
                        .withArguments("ensime", "--debug", "--stacktrace")
                        .build()
                Then("The default Scala version should be used") {
                    result.output should include("Using Scala version ${EnsimePlugin.DEFAULT_SCALA_VERSION}")
                    val output = File(rootDir, ".ensime").readText(StandardCharsets.UTF_8)
                    output.contains(":scala-version 2.12.3")
                }
            }
            When("the ENSIME server version is overridden") {

                val rootDir = Files.createTempDir()

                val buildFile = File(rootDir, "build.gradle")
                buildFile.writeText("""
                    |plugins {
                    |  id 'java'
                    |  id 'org.ensime.gradle'
                    |}
                    |
                    |repositories {
                    |  mavenLocal()
                    |  mavenCentral()
                    |}
                    |
                    |dependencies {
                    |   compile "org.scala-lang:scala-library:2.11.8"
                    |}
                    |
                    |ensime {
                    |  ensimeServerVersion = '1.0.1'
                    |}
                    |""".trimMargin())
                val result = GradleRunner.create()
                        .forwardOutput()
                        .withGradleVersion(gradleVersion)
                        .withProjectDir(rootDir)
                        .withPluginClasspath()
                        .withArguments("ensime", "--debug", "--stacktrace")
                        .build()
                Then("The default Scala version should be used") {
                    result.output should include("org.ensime/server_2.11/1.0.1/")
                }
            }
        }
    }
})