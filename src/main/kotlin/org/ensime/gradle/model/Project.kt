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