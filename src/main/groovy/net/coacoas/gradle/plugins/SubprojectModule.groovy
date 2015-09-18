package net.coacoas.gradle.plugins

import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency

/**
 * Creates the subproject configuration for a Scala/Java project. Android projects will use EnsimeAndroidModule
 */
class SubprojectModule {
    final Project project

    SubprojectModule(Project project) {
        this.project = project
    }

    List<String> getProjectDependencies() {
        project.configurations.testRuntime.getAllDependencies().findAll {
            it instanceof ProjectDependency
        }.dependencyProject.collect { it.name }
    }

    List<String> getSourceSets() {
        Set<String> sources = ['java', 'scala', 'resources']
        project.sourceSets.findAll { set ->
            sources.contains(set.name)
        }.collectNested { sourceSet ->
            sourceSet.srcDirs.collect { dir -> dir.absolutePath }
        }
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
        assert !project.sourceSets.main.output.classesDir.absolutePath.empty : "target cannot be empty"
        properties.put("target", project.sourceSets.main.output.classesDir.absolutePath)
        project.logger.debug("EnsimeModule: Writing target: ${project.sourceSets.main.output.classesDir.absolutePath}")

        // test-target ...
        assert !project.sourceSets.test.output.classesDir.absolutePath.empty : "test-target cannot be empty"
        properties.put("test-target", project.sourceSets.test.output.classesDir.absolutePath)
        project.logger.debug("EnsimeModule: Writing test-target: ${project.sourceSets.test.output.classesDir.absolutePath}")

        // depends-on-modules ...
        List<String> dependencies = getProjectDependencies()
        properties.put("depends-on-modules", dependencies)
        project.logger.debug("EnsimeModule: Writing depends-on-modules: ${dependencies}")

        // compile-deps ...
        List<String> classpath = project.sourceSets.main.compileClasspath.collect { it.absolutePath }
        properties.put("compile-deps", classpath)
        project.logger.debug("EnsimeModule: Writing compile-deps: ${classpath}")

        // runtime-deps ...
        classpath = project.sourceSets.main.runtimeClasspath.collect { it.absolutePath }
        properties.put("runtime-deps", classpath)
        project.logger.debug("EnsimeModule: Writing runtime-deps: ${classpath}")

        // test-deps ...
        classpath = project.sourceSets.test.compileClasspath.collect { it.absolutePath }
        properties.put("test-deps", classpath)
        project.logger.debug("EnsimeModule: Writing test-deps: ${classpath}")

        // reference-source-roots ...
        // right now this can only be configure in/through EnsimeTask

        properties
    }

}
