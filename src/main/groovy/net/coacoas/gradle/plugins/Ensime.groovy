package net.coacoas.gradle.plugins
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

import java.util.logging.Logger


/**
 * The Ensime plugin creates an ensime task that allows the build script
 * to create a .ensime project file that can be used for building
 * Scala projects in emacs, Sublime Text 2, or jEdit.
 *
 * &copy; Bill Carlson 2012
 */
class Ensime implements Plugin<Project> {
  public static final String ENSIME_TASK_NAME ="ensime"
  public static Logger log = Logger.getLogger(Ensime.class.name)

  @Override
  public void apply(Project project) {
    project.with {
      if (!plugins.hasPlugin(Ensime)) {
        extensions.create(ENSIME_TASK_NAME, EnsimeModel)
        tasks.create([(Task.TASK_NAME): ENSIME_TASK_NAME,
                      (Task.TASK_GROUP): 'IDE',
                      (Task.TASK_DESCRIPTION): 'Generates an ENSIME configuration (.ensime)',
                      (Task.TASK_TYPE): EnsimeTask])
      }
    }
  }
}

class FormattingPrefsModel {
  def prefs = [:]

  def methodMissing(String name, args) {
    if (args.size() > 1)
      throw new IllegalArgumentException("The configuration for $name takes only one argument")
    if (args.size() < 1)
      throw new IllegalArgumentException("The configuration for $name requires an argument")

    prefs[name] = args[0]
  }

  @Override
  String toString() {
    List<String> entries = prefs.entrySet().collect { entry ->
      "${entry.key} => ${entry.value}"
    }
    "FormattingPrefs: ( ${entries.join(",")} )"
  }

  void apply(Closure c) {
    c.call()
  }
}

/**
 * Define all the extension for the plugin.
 */
class EnsimeModel {
  public String targetFile = ""

  // can be >t< or >nil<
  // TODO - make :use-sbt work (this is not a string))
  // public String useSbt = ""

  String scalaVersion
  File javaHome
  String cacheDir = ""

  //The ensime server jar and dependencies are downloaded to this location
  //then added to the ensime-server-jars property in the .ensime file
  String serverJarsDir = "build/ensime"

  String serverVersion = "1.0.1"
  
  List<String> javaFlags = []
  List<String> referenceSourceRoots = []
  List<String> compilerArgs = []
  FormattingPrefsModel formatting = new FormattingPrefsModel()
  List<String> additionalSources = []
  List<String> additionalJars = []
  List<String> additionalDocs = []

  boolean downloadSources = true
  boolean downloadJavadoc = true

  // TODO - check ensime-server source code for other conv vars like :project-package

  @Override
  public String toString() {
    return "EnsimeModel{" +
            "targetFile='" + targetFile + '\'' +
            ", scalaVersion=" + scalaVersion +
            ", javaHome=" + javaHome +
            ", cacheDir=" + cacheDir +
            ", serverJarsDir=" + serverJarsDir +
            ", serverVersion=" + serverVersion +
            ", javaFlags=" + javaFlags +
            ", referenceSourceRoots=" + referenceSourceRoots +
            ", compilerArgs=" + compilerArgs +
            ", downloadSources = " + downloadSources +
            ", downloadJavadoc = " + downloadJavadoc +
            ", additionalSources = " + additionalSources +
            ", additionalJars = " + additionalJars +
            ", additionalDocs = " + additionalDocs +
	    ", ${formatting}" +
            '}';
  }

  def compilerArgs(String... args) {
    compilerArgs.addAll(args)
  }

  def scalaVersion(String version) {
    this.scalaVersion = version
  }

  def javaHome(String home) {
    javaHome(new File(home))
  }

  def javaHome(File home) {
    if (!home.exists()) {
      throw new IllegalArgumentException("The specified java home directory [${home}] does not exist")
    } else if (!['bin/java', 'bin/java.exe'].exists { new File(home, it).exists()}) {
      throw new IllegalArgumentException("The specified java home directory [${home}] does not point to a valid Java installation")
    } else {
      javaHome = home
    }
  }

  def cacheDir(File cache) {
    if (!cache.exists()) {
      cache.mkdirs()
    }
    cacheDir = cache
  }

  def cacheDir(String cacheDir) {
    cacheDir(new File(cacheDir))
  }

  public void formattingPrefs(Closure c) {
    c.delegate = formatting
    c.resolveStrategy = Closure.DELEGATE_FIRST
    c.call()
  }
}
