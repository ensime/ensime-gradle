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

    // target ...
    File targetClassesDir = new File("${project.buildDir}/classes/debug")
    assert !targetClassesDir.absolutePath.empty : "target cannot be empty"
    properties.put("target", targetClassesDir.absolutePath)
    project.logger.debug("EnsimeModule: Writing target: ${targetClassesDir.absolutePath}")

    // test-target ...
    File testClassesDir = new File("${project.buildDir}/classes/androidTest")
    assert !testClassesDir.absolutePath.empty : "test-target cannot be empty"
    properties.put("test-target", testClassesDir.absolutePath)
    project.logger.debug("EnsimeModule: Writing test-target: ${testClassesDir.absolutePath}")

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
