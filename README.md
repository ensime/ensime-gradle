# Gradle ENSIME Plugin

[![Join the chat at https://gitter.im/ensime/ensime-gradle](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/ensime/ensime-gradle?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/ensime/ensime-gradle.svg)](https://travis-ci.org/ensime/ensime-gradle)

## Purposes

The gradle-ensime plugin creates [.ensime project files](https://github.com/ensime/ensime-server/wiki/Example-Configuration-File) for the ENhanced Scala Integration Mode for Emacs ([ENSIME](https://github.com/ensime)), written by Aemon Cannon.

## Usage

For it to work the build.gradle file either needs to have the [`scala`](http://www.gradle.org/docs/current/userguide/scala_plugin.html) plugin or the [`gradle-android-scala-plugin`](https://github.com/saturday06/gradle-android-scala-plugin) to be configured (the later is WIP).

A working android example can be found [here](https://github.com/rolandtritsch/scala-android-ui-samples).

### Building from source

To build the current dev build, clone this repository and run the build
  ./gradlew clean build install 
  
This copies the jar to your local maven repository.  To use this in your build, add the following to your build.gradle

    buildscript {
      repositories { mavenLocal() }

      dependencies {
        classpath group: 'net.coacoas.gradle', name: 'ensime-gradle', version: '0.2.0-SNAPSHOT'
      }
    }
    apply plugin: 'ensime'
    
### Installing Maven Central

To use the latest release version of the plugin, make sure that the jar file is loaded in the buildscript classpath and the plugin is applied:

    buildscript {
      repositories { mavenCentral() }

      dependencies {
        classpath group: 'net.coacoas.gradle', name: 'ensime-gradle', version: '0.2.0-SNAPSHOT'
      }
    }
    apply plugin: 'ensime'
    
### Running the task

The plugin adds the 'ensime' task to the project to create a .ensime file in the project directory.

    ./gradlew ensime

Each time the task is executed, the .ensime file will be regenerated.

To see the plugin in action you can also clone this repo and then run `gradle build` to build the plugin and then `cd src/test/sample/scala` and run `gradle clean build ensime`.

## Requirements

As of version 0.2.0, the ensime plugin requires Gradle 2.x. 
