package net.coacoas.gradle.plugins

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.result.ArtifactResult
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.artifacts.result.UnresolvedDependencyResult
import org.gradle.api.component.Artifact
import org.gradle.api.specs.NotSpec
import org.gradle.api.specs.Spec
import org.gradle.jvm.JvmLibrary
import org.gradle.language.base.artifact.SourcesArtifact
import org.gradle.language.java.artifact.JavadocArtifact
/**
 * Creates the subproject configuration for a Scala/Java project. Android projects will use EnsimeAndroidModule
 */
class SubprojectModule {
    final Project project
    final EnsimeModel model

    SubprojectModule(Project project, EnsimeModel model) {
        this.project = project
        this.model = model
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

    List<ArtifactResult> getArtifacts(Configuration configuration, Class<? extends Artifact> clazz) {
        def componentIds = configuration.incoming.resolutionResult.allDependencies.collectMany { DependencyResult dependency ->
            if (dependency instanceof UnresolvedDependencyResult) {
                []
            } else {
                [dependency.selected.id]
            }
        }

        project.dependencies.createArtifactResolutionQuery()
                .forComponents(componentIds)
                .withArtifacts(JvmLibrary, clazz)
                .execute()
                .resolvedComponents
                .collectMany { it.getArtifacts(clazz) }
    }

    List<String> getReferenceSourceRoots() {
        return getArtifacts(project.configurations.testCompile, SourcesArtifact)
                .collect { it.file.absolutePath }
    }

    List<String> getDocJars() {
        return getArtifacts(project.configurations.testCompile, JavadocArtifact)
                .collect { it.file.absolutePath }
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

        if (model.downloadSources) {
            List<String> referenceSourceRoots = getReferenceSourceRoots()
            properties.put("reference-source-roots", referenceSourceRoots)
            project.logger.debug("EnsimeModule: Writing reference-source-roots: ${referenceSourceRoots}")
        }

        if (model.downloadJavadoc) {
            List<String> docJars = getDocJars()
            properties.put("doc-jars", docJars)
            project.logger.debug("EnsimeModule: Writing doc-jars: ${docJars}")
        }

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
        properties.put("test-deps", classPath('testCompile'))

        // reference-source-roots ...
        // right now this can only be configure in/through EnsimeTask

        properties
    }
}
