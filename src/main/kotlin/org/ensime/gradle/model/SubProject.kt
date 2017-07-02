package org.ensime.gradle.model

import java.nio.file.Path

data class SubProject(
        val name: String,
        val sourceRoots: List<Path>,
        val targets: List<Path>,
        val testTargets: List<Path>,
        val dependsOnModules: List<SubProject>,
        val compileDeps: List<Path>,
        val runtimeDeps: List<Path>,
        val testDeps: List<Path>,
        val docJars: List<Path>,
        val referenceSourceRoots: List<Path>
)