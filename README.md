# Gradle ENSIME Plugin

[![Join the chat at https://gitter.im/ensime/ensime-gradle](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/ensime/ensime-gradle?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Build Status](https://travis-ci.org/ensime/ensime-gradle.svg)](https://travis-ci.org/ensime/ensime-gradle)

## Purposes

The ensime-gradle plugin creates [.ensime project files](https://github.com/ensime/ensime-server/wiki/Example-Configuration-File) for the ENhanced Scala Integration Mode for Emacs ([ENSIME](https://github.com/ensime))

## Usage

This plugin will create .ensime configuration files for projects using the [`java`](https://docs.gradle.org/current/userguide/java_plugin.html), [`scala`](https://docs.gradle.org/current/userguide/scala_plugin.html) or [`gradle-android-scala-plugin`](https://github.com/saturday06/gradle-android-scala-plugin) to be configured (the later is WIP).

A working android example can be found [here](https://github.com/rolandtritsch/scala-android-ui-samples).

Note that this does not yet support the new 'software model' configuration.  Yet. 

### Using the plugin in your build

For gradle 2.1+ 

    plugins {
      id 'org.ensime.gradle' version '0.2.2'
    }

For older versions:

    buildscript { 
      repositories { 
        jcenter()
      }

      dependencies {
        classpath 'net.coacoas.gradle:ensime-gradle:0.2.2'
      }
    }

To use SNAPSHOT builds, 

    buildscript { 
      repositories { 
        maven { 
          url 'https://oss.jfrog.org/oss-snapshot-local'
        }
      }

      dependencies {
        classpath 'net.coacoas.gradle:ensime-gradle:0.3.0-SNAPSHOT'
      }
    }

### Running the task

The plugin adds the 'ensime' task to the project to create a .ensime file in the project directory.

    ./gradlew ensime

Each time the task is executed, the .ensime file will be regenerated.

To see the plugin in action you can also clone this repo and then run `gradle build` to build the plugin and then `cd src/test/sample/scala` and run `gradle clean build ensime`.

## Requirements

As of version 0.2.0, the ensime plugin requires Gradle 2.x. 
