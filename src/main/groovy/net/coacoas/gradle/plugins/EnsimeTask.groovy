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
    // processing targetFile ...
    String ensimeFileName = (
      project.extensions.ensime.targetFile.empty ?
      project.projectDir.absolutePath + DEF_ENSIME_FILE :
      project.extensions.ensime.targetFile
    )
    File ensimeFile = new File(ensimeFileName)
    if(!ensimeFile.parentFile.exists()) {
      boolean wasAbleToCreateEnsimeFileDir = ensimeFile.parentFile.mkdirs()
      assert wasAbleToCreateEnsimeFileDir : "Failed to mkdirs for ensime file: ${ensimeFileName}"
    }
    project.logger.debug("EnsimeTask: Writing ensime configuration to ${ensimeFileName} ...")

    // start to put the ensime file togther ...
    Map<String, Object> properties = new LinkedHashMap<String, Object>()

    // root-dir ...
    assert !project.rootDir.absolutePath.empty : "root-dir must be not empty"
    properties.put("root-dir", project.rootDir.absolutePath)
    project.logger.debug("EnsimeTask: Writing root-dir: ${project.rootDir.absolutePath}")

    // cache-dir ...
    String ensimeCacheDir = (
      project.extensions.ensime.cacheDir.empty ?
      project.projectDir.absolutePath + DEF_ENSIME_CACHE :
      project.extensions.ensime.cacheDir
    )
    File ensimeCacheDirFile = new File(ensimeCacheDir)
    if(!ensimeCacheDirFile.exists()) {
      boolean wasAbleToCreateEnsimeCacheDir = ensimeCacheDirFile.mkdirs()
      assert wasAbleToCreateEnsimeCacheDir : "Failed to mkdirs cache-dir: ${ensimeCacheDir}"
    }
    properties.put("cache-dir", ensimeCacheDir)
    project.logger.debug("EnsimeTask: Writing cache-dir: ${ensimeCacheDir}")

    // (project) name ...
    assert !project.name.empty, "project.name must be not empty"
    properties.put("name", project.name)
    project.logger.debug("EnsimeTask: Writing name: ${project.name}")

    // java-home ...
    getLogger().info("Using extension ${project.extensions.ensime}")
    String javaHome = project.extensions.ensime.javaHome.absolutePath
    assert javaHome != null && !javaHome.empty, "ensime.javaHome must be set"
    properties.put("java-home", javaHome)
    project.logger.debug("EnsimeTask: Writing java-home: ${project.extensions.ensime.javaHome}")

    // java-flags ...
    if(project.extensions.ensime.javaFlags.size() > 0) {
      properties.put("java-flags", project.extensions.ensime.javaFlags)
      project.logger.debug("EnsimeTask: Writing java-flags: ${project.extensions.ensime.javaFlags}")
    }

    // reference-source-roots ...
    if(project.extensions.ensime.referenceSourceRoots.size() > 0) {
      properties.put("reference-source-roots", project.extensions.ensime.referenceSourceRoots)
      project.logger.debug("EnsimeTask: Writing reference-source-roots: ${project.extensions.ensime.referenceSourceRoots}")
    }

    // scala-version ...
    assert !project.extensions.ensime.scalaVersion.empty, "scala-version must be not empty"
    properties.put("scala-version", project.extensions.ensime.scalaVersion)
    project.logger.debug("EnsimeTask: Writing scala-version: ${project.extensions.ensime.scalaVersion}")

    // compiler-args ...
    if(project.extensions.ensime.compilerArgs.size() > 0) {
      properties.put("compiler-args", project.extensions.ensime.compilerArgs)
      project.logger.debug("EnsimeTask: Writing compiler-args: ${project.extensions.ensime.compilerArgs}")
    }

    Collection<Project> subprojects = project.allprojects.findAll { prj ->
      boolean supported = prj.plugins.hasPlugin('jp.leafytree.android-scala') ||
              prj.plugins.hasPlugin('java')
      boolean notSupported = prj.plugins.hasPlugin('groovy')

      project.logger.debug("Checking project $prj")
      project.logger.debug("Has plugins: ${prj.plugins.collect{it.class.name}}")
      supported && !notSupported
    }
    project.logger.info("Configuring subprojects $subprojects")

    // process subprojects ...
    properties.put("subprojects", subprojects.collect { subproject ->
      subproject.plugins.hasPlugin('jp.leafytree.android-scala') ?
              new EnsimeAndroidModule(subproject).settings() :
              new SubprojectModule(subproject).settings()
    })

    // write and format the file ...
    ensimeFile.write(SExp.format(properties))
  }
}
