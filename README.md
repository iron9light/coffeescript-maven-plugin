# Coffeescript Maven Plugin

Yet another coffeescript maven plugin.

## Purpose

Love coffeescript. Love Maven. Wanna replace javascript at all.

Just play with your .coffee like what you did to .js before, and let me take care of the rest:

* Compile (all or modified) coffeescript to javascript.
* Watch your changing (modify, create, delete, rename) and sync the javascript files at the same time.

## Why another coffeescript maven plugin?

There are two maven plugins can compile coffeescript to javascript, as I know:

* [brew](https://github.com/jakewins/brew)
* [coffee-maven-plugin](https://github.com/talios/coffee-maven-plugin)

brew can do more than just compile coffeescript, but it's not up to date, see [here](https://github.com/jakewins/brew/issues/4).

coffee-maven-plugin works great, but it is short of some fetures.

## Requires

* Maven 2
* JDK 7

## Usage

Just add the plugin to your pom:

    <build>
      ...
      <plugins>
        ...
        <plugin>
          <groupId>com.github.iron9light</groupId>
          <artifactId>coffeescript-maven-plugin</artifactId>
          <version>1.0</version>
          <executions>
            <execution>
              <goals>
                <goal>compile</goal>
              </goals>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </build>

The full pom configuration is here.
Configuration options shown are default values and can be ignored for normal use.

    <build>
      <plugins>
        <plugin>
          <groupId>com.github.iron9light</groupId>
          <artifactId>coffeescript-maven-plugin</artifactId>
          <version>1.0-SNAPSHOT</version>
          <configuration>
            <srcDir>${basedir}/src/main/webapp</srcDir>
            <outputDir>${basedir}/src/main/webapp</outputDir>
            <bare>false</bare>
            <modifiedOnly>false</modifiedOnly>
            <allowedDelete>true</allowedDelete>
          </configuration>
          <executions>
            <execution>
              <id>coffeescript</id>
              <phase>generate-resources</phase>
              <goals>
                <goal>compile</goal>
              </goals>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </build>

## Goal:compile

    mvn coffeescript:compile

### Description:

Compile coffeescript files to javascript files.

### Attributes:

* Requires a Maven project to be executed.
* Binds by default to the lifecycle phase: generate-resources.

### Optional Parameters:

* bare    Boolean	-	Bare mode. Default value is: false.
* modifiedOnly	Boolean	-	Only compile modified files. Default value is: false.
* outputDir	File	-	Output Directory. Expression: ${basedir}/src/main/webapp
* srcDir	File	-	Source Directory. Expression: ${basedir}/src/main/webapp

## Goal:watch

    mvn coffeescript:watch

### Description:

Compile coffeescript files to javascript files, and recompile as soon as a change occurs.

### Attributes:

* Requires a Maven project to be executed.

### Optional Parameters:

* allowedDelete    Boolean	-	Delete javascript file when the coffeescript file deleted. Default value is: true.
* bare    Boolean    -	Bare mode. Default value is: false.
* modifiedOnly	Boolean	-	Only compile modified files. Default value is: false.
* outputDir	File	-	Output Directory. Expression: ${basedir}/src/main/webapp
* srcDir	File	-	Source Directory. Expression: ${basedir}/src/main/webapp

# License

[The Apache Software License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt).

# Acknowledgement

* Thank you, [Jeremy Ashkenas](https://github.com/jashkenas). [Coffeescript](https://github.com/jashkenas/coffee-script) Rock!
* Thank you, [Mark Derricutt](https://github.com/talios). Some of the code are based on your great [coffee-maven-plugin](https://github.com/talios/coffee-maven-plugin).