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
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner
import org.gradle.tooling.GradleConnector
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import static org.hamcrest.core.Is.is
import static org.junit.Assert.assertThat
/**
 * Gradle ENSIME Plugin test
 *
 * &copy; Bill Carlson 2012
 */
@Ignore
public class ConfigurationTest extends Specification implements ProjectSpecification {
  @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()

  def setup() {
    setupProject(testProjectDir)
  }

  def "Ensime task is added to the build"() {
    given:
    buildFile << """
      apply plugin: 'scala'
      apply plugin: 'ensime'
    """

    when:
    def project = ProjectBuilder.builder()
            .withProjectDir(testProjectDir.root)
            .build()

    then:
    project.tasks.getByName('ensime') != null
  }

  def "Test Scala project with Ensime"() {
    given:
    def projectRoot = new File("src/test/sample/scala")

    when:
    GradleRunner.create()
            .withProjectDir(projectRoot)
            .withArguments('clean', 'ensime')
            .build()

    then:
    File ensime = new File(projectRoot, "build/ensime_file")
    ensime.exists()
  }

  @Test
  public void testAndroidProjectWithEnsime() throws Exception {
    def build = GradleConnector
      .newConnector()
      .forProjectDirectory(new File("src/test/sample/android/ActionBarCompat-Basic"))
      .connect().newBuild()
    build.forTasks("clean", "ensime").run()

    def ensime = new File("src/test/sample/android/ActionBarCompat-Basic/Application/.ensime")
    assertThat(
      "an ensime file should be created at ${ensime.absolutePath}",
      ensime.exists(),
      is(true)
    )
  }
}
