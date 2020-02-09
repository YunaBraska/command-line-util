# command-line-util

![Build][Build-shield] 
[![Maintainable][Maintainable-image]][Maintainable-Url]
[![Coverage][Coverage-image]][Coverage-Url]
![Central][Central-shield] 
![Tag][Tag-shield]
![Issues][Issues-shield] 
![Commit][Commit-shield] 
![Size][Size-shield] 
![Dependency][Dependency-shield]
![License][License-shield]
![Label][Label-shield]

[License-Url]: https://www.apache.org/licenses/LICENSE-2.0
[Build-Status-Url]: https://travis-ci.org/YunaBraska/command-line-util
[Build-Status-Image]: https://travis-ci.org/YunaBraska/command-line-util.svg?branch=master
[Coverage-Url]: https://codecov.io/gh/YunaBraska/command-line-util?branch=master
[Coverage-image]: https://img.shields.io/codecov/c/github/YunaBraska/command-line-util?style=flat-square
[Maintainable-Url]: https://codeclimate.com/github/YunaBraska/command-line-util/maintainability
[Maintainable-image]: https://img.shields.io/codeclimate/maintainability/YunaBraska/command-line-util?style=flat-square
[Javadoc-url]: http://javadoc.io/doc/berlin.yuna/command-line-util
[Javadoc-image]: http://javadoc.io/badge/berlin.yuna/command-line-util.svg
[Gitter-Url]: https://gitter.im/nats-streaming-server-embedded/Lobby
[Gitter-image]: https://img.shields.io/badge/gitter-join%20chat%20%E2%86%92-brightgreen.svg

[Dependency-shield]: https://img.shields.io/librariesio/github/YunaBraska/command-line-util?style=flat-square
[Tag-shield]: https://img.shields.io/github/v/tag/YunaBraska/command-line-util?style=flat-square
[Central-shield]: https://img.shields.io/maven-central/v/berlin.yuna/command-line-util?style=flat-square
[Size-shield]: https://img.shields.io/github/repo-size/YunaBraska/command-line-util?style=flat-square
[Issues-shield]: https://img.shields.io/github/issues/YunaBraska/command-line-util?style=flat-square
[License-shield]: https://img.shields.io/github/license/YunaBraska/command-line-util?style=flat-square
[Commit-shield]: https://img.shields.io/github/last-commit/YunaBraska/command-line-util?style=flat-square
[Label-shield]: https://img.shields.io/badge/Yuna-QueenInside-blueviolet?style=flat-square
[Build-shield]: https://img.shields.io/travis/YunaBraska/command-line-util/master?style=flat-square

### Description
Command-line-util to get easy access to command line unix/windows - simple and native without any dependencies
For more professional you can use [plexus-utils](https://github.com/sonatype/plexus-utils/tree/master/src/main/java/org/codehaus/plexus/util/cli) [commons-cli](https://commons.apache.org/proper/commons-cli/)

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

//File reading
String content SystemUtil.readFile(path);
List<String> contentLines SystemUtil.readFileLines(path);

//Delete dir recursively
boolean removed = SystemUtil.deleteDirectory
````

### TODO
* [ ] Timeout test
* [ ] waitFor - some commands are too fast to get the exit status - workaround is to run terminal with .timeout().execute

![command-line-util](src/main/resources/banner.png "command-line-util")