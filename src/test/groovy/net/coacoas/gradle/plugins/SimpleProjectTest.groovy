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
            apply plugin: 'org.ensime.gradle'
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

        configuration.contains(':runtime-deps') == false

        configuration.find { it =~ /^:compile\-deps\s+\(".*scala\-library\-2\.10\.5\.jar"\)/ }

        where:
        gradleVersion << supportedVersions
    }

    def "Defaults with Java-only plugin"() {
        given:
        buildFile << """
            apply plugin: 'org.ensime.gradle'
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
        configuration.contains(':runtime-deps') == false

        where:
        gradleVersion << supportedVersions
    }

    def "Transitive dependencies are included in the classpath"() {
        given:
        buildFile << """
            apply plugin: 'org.ensime.gradle'
            apply plugin: 'java'

            repositories {
              mavenCentral()
            }

            dependencies {
              compile 'com.fasterxml.jackson.core:jackson-databind:2.6.4'
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('ensime', '--info', '--stacktrace')
                .build()

        then:
        File ensime = new File(testProjectDir.root, '.ensime')
        ensime.exists()
        String configuration = ensime.readLines()
        configuration.contains('/jackson-databind-2.6.4.jar')
        configuration.contains('/jackson-annotations-2.6.0.jar')
        configuration.contains('/jackson-core-2.6.4.jar')

        where:
        gradleVersion << supportedVersions
    }

    def "Scala default source roots are properly described"() {
        given:
        buildFile << """
            apply plugin: 'org.ensime.gradle'
            apply plugin: 'scala'
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('ensime', '--stacktrace')
                .build()

        then:
        File ensime = new File(testProjectDir.root, '.ensime')
        ensime.exists()
        String configuration = ensime.readLines()
        "${testProjectDir.root}/src/main/resources"
        configuration =~ $/:source-roots \("[^\)]*?/src/main/resources"/$
        configuration =~ $/:source-roots \("[^\)]*?/src/main/java"/$
        configuration =~ $/:source-roots \("[^\)]*?/src/main/scala"/$
        configuration =~ $/:source-roots \("[^\)]*?/src/test/resources"/$
        configuration =~ $/:source-roots \("[^\)]*?/src/test/java"/$
        configuration =~ $/:source-roots \("[^\)]*?/src/test/scala"/$

        where:
        gradleVersion << supportedVersions
    }

    def "Java default source roots are properly described"() {
        given:
        buildFile << """
            apply plugin: 'org.ensime.gradle'
            apply plugin: 'java'
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('ensime', '--stacktrace')
                .build()

        then:
        File ensime = new File(testProjectDir.root, '.ensime')
        ensime.exists()
        String configuration = ensime.readLines()
        configuration =~ $/:source-roots \("[^\)]*?/src/main/resources"/$
        configuration =~ $/:source-roots \("[^\)]*?/src/main/java"/$
        configuration !=~ $/:source-roots \("[^\)]*?/src/main/scala"/$
        configuration =~ $/:source-roots \("[^\)]*?/src/test/resources"/$
        configuration =~ $/:source-roots \("[^\)]*?/src/test/java"/$
        configuration !=~ $/:source-roots \("[^\)]*?/src/test/scala"/$

        where:
        gradleVersion << supportedVersions
    }

    def "Custom source roots are properly described"() {
        given:
        buildFile << """
            apply plugin: 'org.ensime.gradle'
            apply plugin: 'java'

            sourceSets {
              main {
                java {
                  srcDir '/srcJava'
                }
              }
              test {
                java {
                  srcDir '/deeply/nested/directory/for/test/location'
                }
              }
            }
        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('ensime', '--stacktrace')
                .build()

        then:
        File ensime = new File(testProjectDir.root, '.ensime')
        ensime.exists()
        String configuration = ensime.readLines()
        configuration =~ $/:source-roots \("[^\)]*?/src/main/resources"/$
        configuration =~ $/:source-roots \("[^\)]*?/srcJava"/$
        configuration !=~ $/:source-roots \("[^\)]*?/src/main/java"/$
        configuration !=~ $/:source-roots \("[^\)]*?/src/main/scala"/$
        configuration =~ $/:source-roots \("[^\)]*?/src/test/resources"/$
        configuration =~ $/:source-roots \("[^\)]*?/deeply/nested/directory/for/test/location"/$
        configuration !=~ $/:source-roots \("[^\)]*?/src/test/java"/$
        configuration !=~ $/:source-roots \("[^\)]*?/src/test/scala"/$

        where:
        gradleVersion << supportedVersions
    }

    /***
     def "It even works with a Play project"() {
     given:
     buildFile << """
     apply plugin: 'org.ensime.gradle'
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

    def "Test that formatting prefs show up in the .ensime"() {
        given:
        buildFile << """
            apply plugin: 'org.ensime.gradle'
            apply plugin: 'java'

            ensime { 
              formattingPrefs {
                indentSpaces    4
                indentWithTabs  false
                alignParameters true
              }
            }

        """

        when:
        def result = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('ensime', '--debug', '--stacktrace')
                .build()

        then:
        File ensime = new File(testProjectDir.root, '.ensime')
        ensime.exists()
        String configuration = ensime.readLines()
        configuration =~ $/:formatting-prefs \(:indentSpaces 4, :indentWithTabs nil, :alignParameters t\)/$
	
        where:
        gradleVersion << supportedVersions
    }
}
