package net.coacoas.gradle.plugins

/**
 * This is used in testing to build a classpath in the TestKit projects that includes the code being tested..
 * It's a hack, but it's what's recommended by the Gradle team at this point. I'm assuming they will
 * eventually fix the TestKit to make this not needed anymore.
 */
trait ProjectSpecification {
    File buildFile

    def setupProject(tempDir) {
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
}
