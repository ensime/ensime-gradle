package net.coacoas.gradle.plugins
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

public class SimpleProjectTest extends Specification implements ProjectSpecification {
    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()

    final static def supportedVersions = ['2.1', '2.6', '2.9']

    def setup() {
        setupProject(testProjectDir)
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
                .withGradleVersion(gradleVersion)
                .withProjectDir(testProjectDir.root)
                .withArguments('ensime', '--debug')
                .build()

        then:
        result.output.contains("Using Scala version 2.10.5")
        File ensime = new File(testProjectDir.root, '.ensime')
        ensime.exists()
        List<String> configuration = ensime.readLines()

        configuration.contains(":scala-version \"2.10.5\"")
        String javaVersion = ":java-home \"${javaHome()}\""
        configuration.contains(javaVersion)

        configuration.find { it =~ /^:compile\-deps\s+\(".*scala\-library\-2\.10\.5\.jar"\)/ }

        where:
        gradleVersion << supportedVersions
    }

    def "Defaults with Java-only plugin"() {
        given:
        buildFile << """
            apply plugin: 'ensime'
            apply plugin: 'java'
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('ensime', '--debug', '--stacktrace')
                .build()

        then:
        result.output.contains("Using Scala version 2.11.7")
        File ensime = new File(testProjectDir.root, '.ensime')
        ensime.exists()
        String configuration = ensime.readLines()
        configuration.contains(":scala-version \"2.11.7\"")
        configuration.contains(":java-home \"${javaHome()}\"")

        where:
        gradleVersion << supportedVersions
    }

    /***
    def "It even works with a Play project"() {
        given:
        buildFile << """
            apply plugin: 'ensime'
            plugins {
               id 'play'
            }
            model {
              play {

              }
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('ensime', '--debug', '--stacktrace')
                .build()

        then:
        result.standardOutput.contains("Using Scala version 2.11.7")
        File ensime = new File(testProjectDir.root, '.ensime')
        ensime.exists()
        String configuration = ensime.readLines()
        configuration.contains(":scala-version \"2.11.7\"")
        configuration.contains(":java-home \"${javaHome()}\"")
    }
    ***/

    def javaHome() {
        String javaProperty = System.getProperty("java.home")
        javaProperty.endsWith('jre') ?
                new File(javaProperty).parentFile.absolutePath :
                new File(javaProperty).absolutePath
    }
}
