/*
 *    Copyright 2017 Bill Carlson
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package net.coacoas.gradle.plugins
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.artifacts.component.ModuleComponentIdentifier
import org.gradle.api.artifacts.result.ArtifactResult
import org.gradle.api.artifacts.result.DependencyResult
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

    def canResolve(scope) {
        try { 
            scope.isCanBeResolved()
        } catch (MissingMethodException e) { 
            true
        } 
    }

    List<String> classPath(Collection<Configuration> scopes) {
        scopes.findAll { canResolve(it) }
              .collect { it.resolvedConfiguration }  
              .collectMany { it.getFirstLevelModuleDependencies(new NotSpec(isProject)) }
              .collectMany { dependency ->
                  dependency.allModuleArtifacts.collect { it.file }
              }
    }

    List<Configuration> getConfiguration(String scope) {
        try {
            [project.configurations[scope]] ?: []
        } catch (Exception e) {
            []
        }
    }

    List<ArtifactResult> getArtifacts(Configuration configuration, Class<? extends Artifact> clazz) {
        def componentIds = configuration.incoming.resolutionResult.allDependencies.collectMany { DependencyResult dependency ->
            def id = dependency.selected.id
            if (id instanceof ModuleComponentIdentifier) {
                [id]
            } else {
                []
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

        // targets ...
        assert !project.sourceSets.main.output.classesDir.absolutePath.empty : "targets cannot be empty"
        List<String> targets = [project.sourceSets.main.output.classesDir.absolutePath]
        properties.put("targets", targets)
        project.logger.debug("EnsimeModule: Writing targets: ${targets}")

        // test-target ...
        assert !project.sourceSets.test.output.classesDir.absolutePath.empty : "test-targets cannot be empty"
        List<String> testTargets = [project.sourceSets.test.output.classesDir.absolutePath]
        properties.put("test-targets", testTargets)
        project.logger.debug("EnsimeModule: Writing test-targets: ${testTargets}")

        // depends-on-modules ...
        List<String> dependencies = getProjectDependencies()
        properties.put("depends-on-modules", dependencies)
        project.logger.debug("EnsimeModule: Writing depends-on-modules: ${dependencies}")

        //  Classpath modifications
        properties.put("compile-deps", classPath(compileConfigs()))
        properties.put("test-deps", classPath(testCompileConfigs()))

        // reference-source-roots ...
        // right now this can only be configure in/through EnsimeTask

        properties
    }

    Collection<Configuration> testCompileConfigs() {
        project.configurations.findAll { config ->
            config.hierarchy.contains(project.configurations['testCompile'])
        }
    }

    Collection<Configuration> compileConfigs() {
        project.configurations.findAll { config ->
            config.hierarchy.contains(project.configurations['compile'])
        } - testCompileConfigs()
    }
}
