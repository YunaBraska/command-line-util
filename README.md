# System-Util

[![License][License-Image]][License-Url]
[![Build][Build-Status-Image]][Build-Status-Url] 
[![Coverage][Coverage-image]][Coverage-Url] 
[![Maintainable][Maintainable-image]][Maintainable-Url] 
[![Gitter][Gitter-image]][Gitter-Url] 

### Description
System util to get easy access to command line unix/windows

### [Example] Execute command
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
### [Example] Operating system tools
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
* [] waitFor - some commands are too fast to get the exit status - workaround is to run terminal with .timeout().execute

![system-util](src/main/resources/banner.png "system-util")

[License-Url]: https://www.apache.org/licenses/LICENSE-2.0
[License-Image]: https://img.shields.io/badge/License-Apache2-blue.svg
[github-release]: https://github.com/YunaBraska/system-util
[Build-Status-Url]: https://travis-ci.org/YunaBraska/system-util
[Build-Status-Image]: https://travis-ci.org/YunaBraska/system-util.svg?branch=master
[Coverage-Url]: https://codecov.io/gh/YunaBraska/system-util?branch=master
[Coverage-image]: https://codecov.io/gh/YunaBraska/system-util/branch/master/graphs/badge.svg
[Version-url]: https://github.com/YunaBraska/system-util
[Version-image]: https://badge.fury.io/gh/YunaBraska%2Fsystem-util.svg
[Central-url]: https://search.maven.org/#search%7Cga%7C1%7Ca%3A%22system-util%22
[Central-image]: https://maven-badges.herokuapp.com/maven-central/berlin.yuna/system-util/badge.svg
[Maintainable-Url]: https://codeclimate.com/github/YunaBraska/system-util
[Maintainable-image]: https://codeclimate.com/github/YunaBraska/system-util.svg
[Gitter-Url]: https://gitter.im/nats-streaming-server-embedded/Lobby
[Gitter-image]: https://img.shields.io/badge/gitter-join%20chat%20%E2%86%92-brightgreen.svg
[Javadoc-url]: http://javadoc.io/doc/berlin.yuna/system-util
[Javadoc-image]: http://javadoc.io/badge/berlin.yuna/system-util.svg