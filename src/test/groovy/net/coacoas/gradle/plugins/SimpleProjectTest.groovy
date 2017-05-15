package net.coacoas.gradle.plugins
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

public class SimpleProjectTest extends Specification implements ProjectSpecification {
    @Rule final TemporaryFolder testProjectDir = new TemporaryFolder()

    final static def supportedVersions = ['2.1', '2.6', '2.13']

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
              compile 'org.scala-lang:scala-library:2.10.4'
            }
        """

        when:
        def result = GradleRunner.create()
                .withGradleVersion(gradleVersion)
                .withProjectDir(testProjectDir.root)
                .withArguments('ensime', '--debug', '--stacktrace')
//                .withDebug(true)
                .build()

        then:
        result.output.contains("Using Scala version 2.10.4")
        File ensime = new File(testProjectDir.root, '.ensime')
        ensime.exists()
        List<String> configuration = ensime.readLines()

        configuration.contains(":scala-version \"2.10.4\"")
        String javaVersion = ":java-home \"${javaHome()}\""
        configuration.contains(javaVersion)
        configuration.contains(":java-flags ()")

	configuration.find { it.matches(":ensime-server-jars.*") }

	!configuration.contains(':runtime-deps')

        configuration.find { it =~ /^:compile\-deps\s+\(".*scala\-library\-2\.10\.4\.jar"\)/ }

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

        List<String> configuration = ensime.readLines()
        configuration.contains(":scala-version \"2.11.7\"")

        String javaVersion = ":java-home \"${javaHome()}\""
        configuration.contains(javaVersion)
        configuration.contains(":java-flags ()")

	configuration.find { it.contains(':ensime-server-jars') }

	!configuration.contains(':runtime-deps')

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
        GradleRunner.create()
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
        GradleRunner.create()
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
        GradleRunner.create()
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
        GradleRunner.create()
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

    def "Javadoc jars are not added when downloadJavadoc is false"() {
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

            ensime {
              downloadJavadoc = false
            }
        """

        when:
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('ensime', '--info', '--stacktrace')
                .build()

        then:
        File ensime = new File(testProjectDir.root, '.ensime')
        ensime.exists()
        String configuration = ensime.readLines()
        configuration != ~$/:doc-jars/$
    }

    def "Doc jars gets set properly"() {
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
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('ensime', '--info', '--stacktrace')
                .build()

        then:
        File ensime = new File(testProjectDir.root, '.ensime')
        ensime.exists()
        String configuration = ensime.readLines()
        configuration =~ $/:doc-jars \("[^\)]*?jackson-databind-2.6.4-javadoc.jar"/$
        configuration =~ $/:doc-jars \("[^\)]*?jackson-annotations-2.6.0-javadoc.jar"/$
        configuration =~ $/:doc-jars \("[^\)]*?jackson-core-2.6.4-javadoc.jar"/$

        where:
        gradleVersion << supportedVersions

    }

    def "Reference source roots are not added when downloadSources is false"() {
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

            ensime {
              downloadSources = false
            }
        """

        when:
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('ensime', '--info', '--stacktrace')
                .build()

        then:
        File ensime = new File(testProjectDir.root, '.ensime')
        ensime.exists()
        String configuration = ensime.readLines()
        configuration != ~$/:reference-source-roots/$
    }

    def "Reference source roots gets set properly"() {
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
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('ensime', '--info', '--stacktrace')
                .build()

        then:
        File ensime = new File(testProjectDir.root, '.ensime')
        ensime.exists()
        String configuration = ensime.readLines()
        configuration =~ $/:reference-source-roots \("[^\)]*?jackson-databind-2.6.4-sources.jar"/$
        configuration =~ $/:reference-source-roots \("[^\)]*?jackson-annotations-2.6.0-sources.jar"/$
        configuration =~ $/:reference-source-roots \("[^\)]*?jackson-core-2.6.4-sources.jar"/$

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
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('ensime', '--debug', '--stacktrace')
                .build()

        then:
        File ensime = new File(testProjectDir.root, '.ensime')
        ensime.exists()
        String configuration = ensime.readLines()
        configuration =~ $/:formatting-prefs \(:indentSpaces 4, :indentWithTabs nil, :alignParameters t\)/$
	configuration !=~ /::/
	
        where:
        gradleVersion << supportedVersions
    }

    def "Test compilerArgs configuration method"() {
        given:
        buildFile << """
            apply plugin: 'org.ensime.gradle'
            apply plugin: 'java'

            ensime {
              scalaVersion '2.11.7'
	      compilerArgs '-a', '--compiler-arg', '-Xlint'
 	      compilerArgs '-b'
              cacheDir     file('.ensime.cache.d')
            }

        """

        when:
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('ensime', '--debug', '--stacktrace')
                .build()

        then:
        File ensime = new File(testProjectDir.root, '.ensime')
        ensime.exists()
        String configuration = ensime.readLines()
        configuration =~ $/:compiler-args \("-a" "--compiler-arg" "-Xlint" "-b"\)/$
        configuration =~ $/:cache-dir "[\w\d:/\\]+\.ensime\.cache\.d"/$
	configuration !=~ /::/
	
        where:
        gradleVersion << supportedVersions
    }

    def "Test compileOnly libs are added to compile-deps"() {
        given:
        buildFile << """
            apply plugin: 'org.ensime.gradle'
            apply plugin: 'scala'

            repositories {
                jcenter()
            }

            dependencies {
                compile "org.scala-lang:scala-library:2.11.7"
                compileOnly 'com.fasterxml.jackson.core:jackson-databind:2.6.4'
            }
        """

        when:
        GradleRunner.create()
                .withGradleVersion(gradleVersion)
                .withProjectDir(testProjectDir.root)
                .withArguments('ensime', '--debug', '--stacktrace')
                .build()

        then:
        File ensime = new File(testProjectDir.root, '.ensime')
        ensime.exists()
        String configuration = ensime.readLines()
        configuration =~ $/:compile-deps \("[^\)]*?jackson-databind-2.6.4.jar"/$

        where:
        gradleVersion << ['2.12']
    }

    def "Custom configuraitons that extend from compile should show in compile-deps"() {
        given:
        buildFile << """
            apply plugin: 'org.ensime.gradle'
            apply plugin: 'scala'

            repositories {
                jcenter()
            }

            configurations {
                provided
                provided.extendsFrom(compile)
            }

            dependencies {
                compile "org.scala-lang:scala-library:2.11.7"
                provided 'com.fasterxml.jackson.core:jackson-databind:2.6.4'
            }
        """

        when:
        GradleRunner.create()
                .withGradleVersion(gradleVersion)
                .withProjectDir(testProjectDir.root)
                .withArguments('ensime', '--debug', '--stacktrace')
                .build()

        then:
        File ensime = new File(testProjectDir.root, '.ensime')
        ensime.exists()
        String configuration = ensime.readLines()
        configuration =~ $/:compile-deps \("[^\)]*?jackson-databind-2.6.4.jar"/$

        where:
        gradleVersion << ['2.12']
    }

    def "Test that additional- Sources, Jars and Docs show up in .ensime"() {
        given:
        buildFile << """
            apply plugin: 'org.ensime.gradle'
            apply plugin: 'java'

            ensime {
              additionalSources = ["libs/sources.jar"]
              additionalJars = ["libs/compile.jar"]
              additionalDocs = ["libs/docs.jar"]
            }

        """

        when:
        GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withArguments('ensime', '--debug', '--stacktrace')
                .build()

        then:
        File ensime = new File(testProjectDir.root, '.ensime')
        ensime.exists()
        String configuration = ensime.readLines()
        configuration =~ $/:reference-source-roots \("[^\)]*?libs/sources\.jar"/$
        configuration =~ $/:compile-deps \("[^\)]*?libs/compile\.jar"/$
        configuration =~ $/:doc-jars \("[^\)]*?libs/docs\.jar"/$

        where:
        gradleVersion << supportedVersions
    }

}
