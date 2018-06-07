# WebRemoteControl
Remote control the mouse and keyboard of a Mac/Windows/Linux computer from a html page, e.g. from a smartphone or a tablet.
Adapted from https://github.com/simquat/LocalTrackpad.

The views (use menu or swipe top rows to change):

- Mouse/trackpad control, cursor, generic keys (youtube, netflix)
- VLC control buttons
- A filebrowser to open files

# How to run
Note that there is no access control, everybody on the (local) network has access to webremotecontrol!

* On the server:
    * Get the [Java JRE](http://www.oracle.com/technetwork/java/javase/downloads/index.html) >= 8u101. Don't forget to untick the [crapware](https://www.google.com/search?q=java+crapware) installer, and/or [disable it permanently](https://www.java.com/en/download/faq/disable_offers.xml)!
    * [Download the jar](https://bitbucket.org/wolfgang/webremotecontrol/downloads)
    * Double-click to run the jar or do `java -jar webremotecontrol.jar` in a terminal.
    * Click on one of the buttons to show a QR code that contains the server URL and scan this with the client.
* Client: Adding an icon to your homescreen should make a web app.


![Screenshot_20170205-121604.png](https://bitbucket.org/repo/AxyGpB/images/1468820713-Screenshot_20170205-121604.png)
(outdated)

# Config file
An optional config file can be placed next to the jar. Example content:

    httpserverport=9000
    websocketport=9001
    urls=name1,http://server1;name2,http://server2

# How to develop, compile & package

* Get Java JDK >= 8u101
* check out the code (`git clone ...` or download a zip) 
* I use the free community version of [IntelliJ IDEA](https://www.jetbrains.com/idea/download/) with the scala 
plugin for development, just open the project to get started.
* Compile and run manually: `sbt run`
* Package jar: `sbt dist`. The resulting jar is in `target/`

# Used technologies

* [Scala](http://www.scala-lang.org) and [Scala Build Tool](http://www.scala-sbt.org)
* [Scala Build Tool](http://www.scala-sbt.org/)
* [sbt-buildinfo](https://github.com/sbt/sbt-buildinfo)
* [sbt-assembly](https://github.com/sbt/sbt-assembly)
* [Java WebSockets](https://github.com/TooTallNate/Java-WebSocket)
* [Hammer.JS](http://hammerjs.github.io/) to capture touch events
* [QRGen](https://github.com/kenglxn/QRGen) to generate the QR code
* [js-mobile-console](https://github.com/B1naryStudio/js-mobile-console) for debugging
