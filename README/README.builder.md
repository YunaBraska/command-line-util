[var target]: # (/)

# !{project.name}

[include]: # (/README/shields.include.md)

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