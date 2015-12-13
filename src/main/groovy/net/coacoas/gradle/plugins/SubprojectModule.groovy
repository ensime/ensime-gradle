package net.coacoas.gradle.plugins
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.specs.NotSpec
import org.gradle.api.specs.Spec
/**
 * Creates the subproject configuration for a Scala/Java project. Android projects will use EnsimeAndroidModule
 */
class SubprojectModule {
    final Project project

    SubprojectModule(Project project) {
        this.project = project
    }

    Spec<Dependency> isProject = new Spec<Dependency>() {
        public boolean isSatisfiedBy(Dependency d) {
            d instanceof ProjectDependency
        }
    }

    List<String> getProjectDependencies() {
        project.configurations.testRuntime
                .allDependencies
                .findAll { it instanceof ProjectDependency }
                .collect { it.dependencyProject }
                .collect { it.path }
    }

    List<String> classPath(String scope) {
        project.configurations[scope]
               .resolvedConfiguration
               .getFirstLevelModuleDependencies(new NotSpec(isProject))
               .collectMany { dependency ->
            dependency.allModuleArtifacts.collect { it.file }
        }
    }

    List<String> getSourceSets() {
        project.sourceSets.collectMany {
            it.allSource.srcDirs
        }
    }

    Map<String, Object> settings() {
        Map<String, Object> properties = new LinkedHashMap<String, Object>()

        // name ...
        assert !project.name.empty : "project name cannot be empty"
        properties.put("name", project.name)
        project.logger.debug("EnsimeModule: Writing name: ${project.name}")

        // source-roots ...
        Iterable<String> sourceRoots = getSourceSets()
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

        //  Classpath modifications
        properties.put("compile-deps", classPath('compile'))
        properties.put("runtime-deps", classPath('runtime'))
        properties.put("test-deps", classPath('testCompile'))

        // reference-source-roots ...
        // right now this can only be configure in/through EnsimeTask

        properties
    }

}
