package org.ensime.gradle.model

import java.nio.file.Path

data class EnsimeConfig(
        val rootDir: Path,
        val cacheDir: Path,
        val scalaCompilerJars: List<Path>,
        val ensimeServerJars: List<Path>,
        val ensimeServerVersion: String,
        val name: String,
        val javaHome: Path,
        val javaFlags: Path,
        val javaSources: List<Path>,
        val javaCompilerArgs: List<String>,
        val referenceSourceRoots: List<Path>,
        val scalaVersion: String,
        val compilerArgs: List<String>,
        val subProjects: List<SubProject>,
        val projects: List<Project>
)

