package net.coacoas.gradle.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.artifacts.UnknownConfigurationException
import org.gradle.api.tasks.TaskAction

/*
 * Implementation of the 'ensime' task.
 */
class EnsimeTask extends DefaultTask {
  private static final String DEF_ENSIME_FILE = "/.ensime"
  private static final String DEF_ENSIME_CACHE = "/.ensime_cache.d"
  private static final String DEFAULT_SCALA_VERSION = "2.11.7"

  @TaskAction
  public void writeFile() {
    // start to put the ensime file together ...
    Map<String, Object> properties = new LinkedHashMap<String, Object>()
    File outputFile = ensimeFile(project.extensions.ensime.targetFile)

    project.extensions.ensime.with { EnsimeModel model ->
      project.logger.with { logger ->
        logger.info("Ensime Model: ${model}")

        // root-dir ...
        assert !project.rootDir.absolutePath.empty : "root-dir must be not empty"
        properties.put("root-dir", project.rootDir.absolutePath)
        logger.debug("EnsimeTask: Writing root-dir: ${project.rootDir.absolutePath}")

        // cache-dir ...
        String ensimeCacheDir = model.cacheDir.empty ?
                project.projectDir.absolutePath + EnsimeTask.DEF_ENSIME_CACHE :
                model.cacheDir
        File ensimeCacheDirFile = new File(ensimeCacheDir)
        if(!ensimeCacheDirFile.exists()) {
          boolean wasAbleToCreateEnsimeCacheDir = ensimeCacheDirFile.mkdirs()
          assert wasAbleToCreateEnsimeCacheDir : "Failed to mkdirs cache-dir: ${ensimeCacheDir}"
        }
        properties.put("cache-dir", ensimeCacheDir)
        logger.debug("EnsimeTask: Writing cache-dir: ${ensimeCacheDir}")

        // (project) name ...
        assert !project.name.empty, "project.name must be not empty"
        properties.put("name", project.name)
        logger.debug("EnsimeTask: Writing name: ${project.name}")

        // java-home ...
        properties.put("java-home", this.findJavaHome(model))
        logger.debug("EnsimeTask: Writing java-home: ${properties['java-home']}")

        // java-flags ...
        if (!model.javaFlags.empty) {
          properties.put("java-flags", model.javaFlags)
          logger.debug("EnsimeTask: Writing java-flags: ${model.javaFlags}")
        }

        properties.put("formatting-prefs", model.formatting?.prefs)

        // reference-source-roots ...
        if (!model.referenceSourceRoots.empty) {
          properties.put("reference-source-roots", model.referenceSourceRoots)
          logger.debug("EnsimeTask: Writing reference-source-roots: ${model.referenceSourceRoots}")
        }

        // scala-version ...
        properties.put("scala-version", this.findScalaVersion(project))
        logger.debug("EnsimeTask: Writing scala-version: ${model.scalaVersion}")

        // compiler-args ...
        properties.put("compiler-args", model.compilerArgs)

        Collection<Project> subprojects = project.allprojects.findAll { prj ->
          boolean supported = prj.plugins.hasPlugin('jp.leafytree.android-scala') ||
                  prj.plugins.hasPlugin('java')
          boolean notSupported = prj.plugins.hasPlugin('groovy')

          logger.debug("Checking project $prj")
          logger.debug("Has plugins: ${prj.plugins.collect{it.class.name}}")
          supported && !notSupported
        }
        logger.info("Configuring subprojects $subprojects")

        // process subprojects ...
        properties.put("subprojects", subprojects.collect { subproject ->
          subproject.plugins.hasPlugin('jp.leafytree.android-scala') ?
                  new EnsimeAndroidModule(subproject).settings() :
                  new SubprojectModule(subproject, model).settings()
        })

      }
    }


    // write and format the file ...
    outputFile.write(SExp.format(properties))
  }

/**
 * Returns the lcoation for the .ensime file and ensures that the parent directory is created.
 * @param targetFile
 * @return
 */
  File ensimeFile(String targetFile) {
    String fileName = targetFile.empty ?
            project.projectDir.absolutePath + DEF_ENSIME_FILE :
            project.extensions.ensime.targetFile
    File file = new File(fileName)
    if(!file.parentFile.exists()) {
      assert file.parentFile.mkdirs() : "Failed to mkdirs for ensime file: ${fileName}"
    }
    project.logger.debug("EnsimeTask: Writing ensime configuration to ${fileName} ...")
    file
  }

  List<String> lookupScalaVersions(DependencySet dependencies) {
    List<String> scalaVersions = dependencies.findAll {
      it.group.equals('org.scala-lang') &&
        it.name.equals('scala-library')
    }.collect { it.version }.sort()

    project.logger.debug("Found scala versions: ${scalaVersions}")
    scalaVersions
  }

  File findJavaHome(EnsimeModel model) {
    model.javaHome ?: {
      File home = new File(System.getProperty('java.home'))
      home.name.equals('jre') ? home.getParentFile() : home
    }.call();
  }

  String findScalaVersion(Project project) {
    return project.extensions.ensime.scalaVersion ?: {
      Collection<Configuration> ensimeConfigurations = ['compile', 'testCompile', 'play'].
              collectMany {
                try {
                  [project.configurations.getByName(it)]
                } catch (UnknownConfigurationException ignored) {
                  []
                }
              }
      project.logger.debug("Configurations found: ${ensimeConfigurations}")

      List<String> versions = ensimeConfigurations
              .collect { it.allDependencies }
              .collectMany { lookupScalaVersions(it) }
              .sort()
      project.logger.debug("Found Scala versions ${versions}")

      String version = versions.empty ? DEFAULT_SCALA_VERSION : versions.head()
      project.logger.debug("Using Scala version ${version}")
      version
    }.call();
  }
}
