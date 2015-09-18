package net.coacoas.gradle.plugins
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.artifacts.UnknownConfigurationException

import java.util.logging.Logger
/**
 * The Ensime plugin creates an ensime task that allows the build script
 * to create a .ensime project file that can be used for building
 * Scala projects in emacs, Sublime Text 2, or jEdit.
 *
 * &copy; Bill Carlson 2012
 */
class Ensime implements Plugin<Project> {
  public static final String TASK_NAME="ensime"
  public static Logger log = Logger.getLogger(Ensime.class.name)
  private static final DEFAULT_SCALA_VERSION = "2.11.7"

  static List<String> lookupScalaVersions(DependencySet dependencies) {
    List<String> scalaVersions = dependencies.findAll {
      it.group.equals('org.scala-lang') && it.name.equals('scala-library')
    }.collect { it.version }.sort()

    log.fine("Found scala versions: ${scalaVersions}")
    scalaVersions
  }

  @Override
  public void apply(Project project) {
    if (!project.plugins.hasPlugin(Ensime)) {
      project.extensions.create(TASK_NAME, EnsimeModel)
      project.tasks.create(TASK_NAME, EnsimeTask)

      project.afterEvaluate {
        // Once the evaluation has occurred, we can inspect the
        // configurations to get the configured Scala version for the project
        project.extensions.ensime.scalaVersion = getScalaVersion(project)
        project.extensions.ensime.javaHome = getJavaHome(project)
        log.fine("Ensime model populated as ${project.extensions.ensime}")
      }

    }
  }

  private static File getJavaHome(Project project) {
    return project.extensions.ensime.javaHome ?: {
      File home = new File(System.getProperty('java.home'))
      def javaHome = home.name.equals('jre') ? home.getParentFile() : home
      log.fine("Using java home $javaHome")
      javaHome
    }.call();
  }

  private static String getScalaVersion(Project project) {
    return project.extensions.ensime.scalaVersion ?: {
      Collection<Configuration> ensimeConfigurations = ['compile', 'testCompile', 'play'].
              collectMany {
                try {
                  [project.configurations.getByName(it)]
                } catch (UnknownConfigurationException ignored) {
                  []
                }
              }
      log.fine("Configurations found: ${ensimeConfigurations}")

      List<String> versions = ensimeConfigurations
              .collect { it.allDependencies }
              .collectMany { lookupScalaVersions(it) }
              .sort()
      log.fine("Found Scala versions ${versions}")

      String version = versions.empty ? DEFAULT_SCALA_VERSION : versions.head()
      log.fine("Using Scala version ${version}")
      version
    }.call();
  }
}

/**
 * Define all the extension for the plugin.
 */
class EnsimeModel {
  // e.g. "<absolutePath>/.ensime"
  public String targetFile = ""

  // can be >t< or >nil<
  // TODO - make :use-sbt work (this is not a string))
  // public String useSbt = ""

  // allow to set the vars in the .ensime file
  // (https://github.com/ensime/ensime-server/wiki/Example-Configuration-File)
  // that cannot be set/configured through the project conf
  public String scalaVersion
  public File javaHome
  public String cacheDir = ""

  public List<String> javaFlags = []
  public List<String> referenceSourceRoots = []
  public List<String> compilerArgs = []
  // public formatingPrefs = [:]
  // TODO - implement :formating-prefs

  // TODO - check ensime-server source code for other conv vars like :project-package


  @Override
  public String toString() {
    return "EnsimeModel{" +
            "targetFile='" + targetFile + '\'' +
            ", scalaVersion='" + scalaVersion + '\'' +
            ", javaHome=" + javaHome +
            ", cacheDir='" + cacheDir + '\'' +
            ", javaFlags=" + javaFlags +
            ", referenceSourceRoots=" + referenceSourceRoots +
            ", compilerArgs=" + compilerArgs +
            '}';
  }
}
