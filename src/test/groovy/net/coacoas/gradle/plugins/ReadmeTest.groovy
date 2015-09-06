package net.coacoas.gradle.plugins

import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

public class ReadmeTest extends Specification {
    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()
    File buildFile

    def setup() {
        buildFile = testProjectDir.newFile('build.gradle')
        def pluginClasspathResource = getClass().classLoader.findResource("plugin-classpath.txt")
        if (pluginClasspathResource == null) {
            throw new IllegalStateException("Did not find plugin classpath resource, run `testClasses` build task.")
        }

        def pluginClasspath = pluginClasspathResource.readLines()
                .collect { it.replace('\\', '\\\\') } // escape backslashes in Windows paths
                .collect { "'$it'" }
                .join(", ")

        // Add the logic under test to the test build
        buildFile << """
            buildscript {
                dependencies {
                    classpath files($pluginClasspath)
                }
            }
        """
    }

    def "Defaults with Scala plugin"() {
        given:
        buildFile << """
            apply plugin: 'ensime'
            apply plugin: 'scala'

            repositories {
              mavenCentral()
            }

            dependencies {
              compile 'org.scala-lang:scala-library:2.10.5'
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('ensime', '--debug')
                .build()

        then:
        result.standardOutput.contains("Using Scala version 2.10.5")
        File ensime = new File(testProjectDir.root, '.ensime')
        ensime.exists()
        String configuration = ensime.readLines()
        configuration.contains(":scala-version \"2.10.5\"")
        configuration.contains(":java-home \"${System.getProperty("java.home")}\"")
    }
}
