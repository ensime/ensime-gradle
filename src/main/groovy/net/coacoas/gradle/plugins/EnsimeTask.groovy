package net.coacoas.gradle.plugins
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.tasks.TaskAction
/*
 * Implementation of the 'ensime' task.
 */
class EnsimeTask extends DefaultTask {
  private static final String DEF_ENSIME_FILE = "/.ensime"
  private static final String DEF_ENSIME_CACHE = "/.ensime_cache.d"

  @TaskAction
  public void writeFile() {
    // start to put the ensime file togther ...
    Map<String, Object> properties = new LinkedHashMap<String, Object>()
    File outputFile = ensimeFile(project.extensions.ensime.targetFile)

    project.extensions.ensime.with { ensime ->
      project.logger.with { logger ->
        logger.info("Ensime Model: ${ensime}")

        // root-dir ...
        assert !project.rootDir.absolutePath.empty : "root-dir must be not empty"
        properties.put("root-dir", project.rootDir.absolutePath)
        logger.debug("EnsimeTask: Writing root-dir: ${project.rootDir.absolutePath}")

        // cache-dir ...
        String ensimeCacheDir = ensime.cacheDir.empty ?
                project.projectDir.absolutePath + EnsimeTask.DEF_ENSIME_CACHE :
                ensime.cacheDir
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
        getLogger().info("Using extension ${ensime}")
        String javaHome = ensime.javaHome?.absolutePath
        assert javaHome != null && !javaHome.empty, "ensime.javaHome must be set"
        properties.put("java-home", javaHome)
        logger.debug("EnsimeTask: Writing java-home: ${ensime.javaHome}")

        // java-flags ...
        if(!ensime.javaFlags.empty) {
          properties.put("java-flags", ensime.javaFlags)
          logger.debug("EnsimeTask: Writing java-flags: ${ensime.javaFlags}")
        }

        properties.put("formatting-prefs", ensime.formatting?.prefs)

        // reference-source-roots ...
        if(!ensime.referenceSourceRoots.empty) {
          properties.put("reference-source-roots", ensime.referenceSourceRoots)
          logger.debug("EnsimeTask: Writing reference-source-roots: ${ensime.referenceSourceRoots}")
        }

        // scala-version ...
        assert !ensime.scalaVersion.empty, "scala-version must be not empty"
        properties.put("scala-version", ensime.scalaVersion)
        logger.debug("EnsimeTask: Writing scala-version: ${ensime.scalaVersion}")

        // compiler-args ...
        properties.put("compiler-args", ensime.compilerArgs)

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
                  new SubprojectModule(subproject).settings()
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
}
