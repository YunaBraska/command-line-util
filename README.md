# command-line-util

[![Build][build_shield]][build_link]
[![Maintainable][maintainable_shield]][maintainable_link]
[![Coverage][coverage_shield]][coverage_link]
[![Issues][issues_shield]][issues_link]
[![Commit][commit_shield]][commit_link]
[![Dependencies][dependency_shield]][dependency_link]
[![License][license_shield]][license_link]
[![Central][central_shield]][central_link]
[![Tag][tag_shield]][tag_link]
[![Javadoc][javadoc_shield]][javadoc_link]
[![Size][size_shield]][size_shield]
![Label][label_shield]

### Description
Simple, small, native library for:
* Detects OS, ARCH with [SystemUtil](https://github.com/YunaBraska/command-line-util/blob/main/src/main/java/berlin/yuna/clu/logic/SystemUtil.java). (see [OsTypes](https://github.com/YunaBraska/command-line-util/blob/main/src/main/java/berlin/yuna/clu/model/OsType.java), [Arch](https://github.com/YunaBraska/command-line-util/blob/master/src/main/java/berlin/yuna/clu/model/OsArch.java), [ArchType](https://github.com/YunaBraska/command-line-util/blob/master/src/main/java/berlin/yuna/clu/model/OsType.java))
* Running commands using the [Terminal](https://github.com/YunaBraska/command-line-util/blob/main/src/main/java/berlin/yuna/clu/logic/Terminal.java)
* Reading arguments with [CommandLineReader](https://github.com/YunaBraska/command-line-util/blob/main/src/main/java/berlin/yuna/clu/logic/CommandLineReader.java)
Command-line-util to get easy access to command line unix/windows - simple and native without any dependencies.
For more professional you can use [plexus-utils](https://github.com/sonatype/plexus-utils/tree/main/src/main/java/org/codehaus/plexus/util/cli) [commons-cli](https://commons.apache.org/proper/commons-cli/)

### Usage

```xml

<dependency>
    <groupId>berlin.yuna</groupId>
    <artifactId>command-line-util</artifactId>
    <version>1.0.0</version>
</dependency>
```

### \[Example\] Parsing command line
````java
//it extends a HashMap
final CommandLineReader clr = new CommandLineReader("command_1 command_2 --help -v=\"true\" --verbose=\"true\" -list=\"item 1\" --list=\"item 2\" --list=\"-item 3\"  ");
    clr.getCommand() //returns "command_1"
    clr.getCommand(1) //returns "command_2"
    clr.isPresent("hElp"); //returns true (caseInsensitive)
    clr.getValue("v", "verbose"); //returns "true" (first value of both)
    clr.getValue(1, "list"); //returns "item 2"
````

### \[Example\] Terminal execute command
````java
new Terminal()
    .consumerInfo(System.out::println) //optional listener [Consumer<String>]
    .consumerError(System.err::println) //optional listener [Consumer<String>]
    .consoleInfo() //optional returns all info output in a String
    .consoleError() //optional returns all error output in a String
    .clearConsole() //optional clears previous consoleInfo/consoleError console
    .dir("myWorinkDirectory") //optional sets default working directory
    .timeoutMs(512) //optional to limit command or for workaround when commands are too fast to return exit status
    .breakOnError(false) //optional - only with timeoutMs possible
    .execute("echo Howdy") //executes the command
    .process //optional returns java Process;
````
### \[Example\] Operating system tools
````java
//Enum [ARM, LINUX, MAC, WINDOWS, SOLARIS, UNKNOWN]
OperatingSystem os = SystemUtil.getOsType();

//Kill process
killProcessByName("tomcat");

//Translates and saves [PosixFilePermission]s to generic
SystemUtil.setFilePermissions(file, OWNER_READ, OWNER_WRITE, OWNER_EXECUTE);

//Read file (tries every charset)
String content SystemUtil.readFile(path);
List<String> contentLines SystemUtil.readFileLines(path);

//Delete dir recursively
boolean removed = SystemUtil.deleteDirectory
````

### TODO
* [ ] Timeout test
* [ ] waitFor - some commands are too fast to get the exit status - workaround is to run terminal with .timeout().execute

![command-line-util](src/test/resources/banner.png "command-line-util")

[build_shield]: https://github.com/YunaBraska/command-line-util/workflows/JAVA_CI/badge.svg
[build_link]: https://github.com/YunaBraska/command-line-util/actions?query=workflow%3AJAVA_CI
[maintainable_shield]: https://img.shields.io/codeclimate/maintainability/YunaBraska/command-line-util?style=flat-square
[maintainable_link]: https://codeclimate.com/github/YunaBraska/command-line-util/maintainability
[coverage_shield]: https://img.shields.io/codeclimate/coverage/YunaBraska/command-line-util?style=flat-square
[coverage_link]: https://codeclimate.com/github/YunaBraska/command-line-util/test_coverage
[issues_shield]: https://img.shields.io/github/issues/YunaBraska/command-line-util?style=flat-square
[issues_link]: https://github.com/YunaBraska/command-line-util/commits/main
[commit_shield]: https://img.shields.io/github/last-commit/YunaBraska/command-line-util?style=flat-square
[commit_link]: https://github.com/YunaBraska/command-line-util/issues
[license_shield]: https://img.shields.io/github/license/YunaBraska/command-line-util?style=flat-square
[license_link]: https://github.com/YunaBraska/command-line-util/blob/main/LICENSE
[dependency_shield]: https://img.shields.io/librariesio/github/YunaBraska/command-line-util?style=flat-square
[dependency_link]: https://libraries.io/github/YunaBraska/command-line-util
[central_shield]: https://img.shields.io/maven-central/v/berlin.yuna/command-line-util?style=flat-square
[central_link]:https://search.maven.org/artifact/berlin.yuna/command-line-util
[tag_shield]: https://img.shields.io/github/v/tag/YunaBraska/command-line-util?style=flat-square
[tag_link]: https://github.com/YunaBraska/command-line-util/releases
[javadoc_shield]: https://javadoc.io/badge2/berlin.yuna/command-line-util/javadoc.svg?style=flat-square
[javadoc_link]: https://javadoc.io/doc/berlin.yuna/command-line-util
[size_shield]: https://img.shields.io/github/repo-size/YunaBraska/command-line-util?style=flat-square
[label_shield]: https://img.shields.io/badge/Yuna-QueenInside-blueviolet?style=flat-square
[gitter_shield]: https://img.shields.io/gitter/room/YunaBraska/nats-streaming-server-embedded?style=flat-square
[gitter_link]: https://gitter.im/nats-streaming-server-embedded/Lobby
