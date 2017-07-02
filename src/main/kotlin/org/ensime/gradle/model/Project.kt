package org.ensime.gradle.model

import java.nio.file.Path

data class Project(
        val id: ProjectId,
        val depends: List<ProjectId>,
        val sources: List<Path>,
        val targets: List<Path>,
        val scalacOptions: List<String>,
        val javacOptions: List<String>,
        val libraryJars: List<Path>,
        val librarySources: List<Path>,
        val libraryDocs: List<Path>
)