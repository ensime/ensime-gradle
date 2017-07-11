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