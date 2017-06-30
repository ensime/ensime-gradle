/*
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
package net.coacoas.gradle.plugins

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

/**
 * Converts a Android project into a collection of settings representing a subproject in Ensime.
 */
class EnsimeAndroidModule {
  private Project project

  EnsimeAndroidModule(Project project) {
    this.project = project
  }

  List<String> getProjectDependencies() {
    project.android.configurations.testRuntime.getAllDependencies().findAll {
      it instanceof ProjectDependency
    }.dependencyProject.collect { it.name }
  }

  List<String> getSourceSets() {
    project.android.sourceSets.collect {
      it.java.srcDirs.collect { it.absolutePath } +
      it.scala.srcDirs.collect { it.absolutePath } +
      it.res.srcDirs.collect { it.absolutePath }
    }.flatten()
  }

  Map<String, Object> settings() {
    Map<String, Object> properties = new LinkedHashMap<String, Object>()

    // name ...
    assert !project.name.empty : "project name cannot be empty"
    properties.put("name", project.name)
    project.logger.debug("EnsimeModule: Writing name: ${project.name}")

    // source-roots ...
    List<String> sourceRoots = getSourceSets()
    properties.put("source-roots", sourceRoots)
    project.logger.debug("EnsimeModule: Writing source-roots: ${sourceRoots}")

    // targets ...
    File targetClassesDir = new File("${project.buildDir}/classes/debug")
    assert !targetClassesDir.absolutePath.empty : "targets cannot be empty"
    List<String> targets = [targetClassesDir.absolutePath]
    properties.put("targets", targets)
    project.logger.debug("EnsimeModule: Writing targets: ${targets}")

    // test-targets ...
    File testClassesDir = new File("${project.buildDir}/classes/androidTest")
    assert !testClassesDir.absolutePath.empty : "test-targets cannot be empty"
    List<String> testTargets = [testClassesDir.absolutePath]
    properties.put("test-targets", testTargets)
    project.logger.debug("EnsimeModule: Writing test-targets: ${testTargets}")

    // depends-on-modules ...
    // TODO - fix dependencies
    // List<String> dependencies = getProjectDependencies()
    List<String> dependencies = new LinkedList()
    properties.put("depends-on-modules", dependencies)
    project.logger.debug("EnsimeModule: Writing depends-on-modules: ${dependencies}")

    // compile-deps ...
    List<String> classpath = project.getTasksByName("compileDebugScala", false).toList().first().classpath.collect { it.absolutePath }
    properties.put("compile-deps", classpath)
    project.logger.debug("EnsimeModule: Writing compile-deps: ${classpath}")

    // runtime-deps ...
   classpath = project.getTasksByName("compileReleaseScala", false).toList().first().classpath.collect { it.absolutePath }
    // properties.put("runtime-deps", classpath)
    project.logger.debug("EnsimeModule: Writing runtime-deps: ${classpath}")

    // test-deps ...
   classpath = project.getTasksByName("compileDebugTestScala", false).toList().first().classpath.collect { it.absolutePath }
    properties.put("test-deps", classpath)
    project.logger.debug("EnsimeModule: Writing test-deps: ${classpath}")

    // reference-source-roots ...
    // right now this can only be configure in/through EnsimeTask

    properties
  }
}
