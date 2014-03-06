GroovyConsoleSpoonPlugin
========================

This is a Pentaho Data Integration plugin that offers a Groovy Console and helper code to Spoon.

It also provides a Gradle (www.gradle.org) task that will bring PDI to the Groovy Console instead of the other way around. To use this just clone this repo and type "gradle console".  This will download the PDI JARs (you can change the version in build.gradle or on the command line) and start a Groovy Console with the PDI classpath and a DSL for interacting with PDI.
