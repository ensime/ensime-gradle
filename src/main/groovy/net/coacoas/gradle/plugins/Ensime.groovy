package net.coacoas.gradle.plugins
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.DependencySet

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

  static String lookupScalaVersion(DependencySet dependencies) {
    List<String> scalaVersions = dependencies.findAll {
      it.group.equals('org.scala-lang') && it.name.equals('scala-library')
    }.collect { it.version }.sort()

    log.fine("Found scala versions: ${scalaVersions}")
    return scalaVersions.empty ? DEFAULT_SCALA_VERSION : scalaVersions.head()
  }

  @Override
  public void apply(Project project) {
    project.with {
      if (!plugins.hasPlugin(Ensime)) {
        extensions.create(TASK_NAME, EnsimeModel)
        tasks.create(TASK_NAME, EnsimeTask)

        afterEvaluate {
          // Once the evaluation has occurred, we can inspect the
          // configurations to get the configured Scala version for the project
          if (extensions.ensime.scalaVersion == null) {
            DependencySet dependencies = configurations.getByName('compile').allDependencies
            extensions.ensime.scalaVersion = lookupScalaVersion(dependencies)
          }

          log.info("Using Scala version ${extensions.ensime.scalaVersion}")
        }
      }
    }
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
  public String cacheDir = ""
  public String javaHome = System.getProperty("java.home")
  public List<String> javaFlags = []
  public List<String> referenceSourceRoots = []
  public String scalaVersion
  public List<String> compilerArgs = []
  // public formatingPrefs = [:]
  // TODO - implement :formating-prefs

  // TODO - check ensime-server source code for other conv vars like :project-package
}
