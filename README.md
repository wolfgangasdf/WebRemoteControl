# WebRemoteControl
Remote control the mouse and keyboard of a Mac/Windows/Linux computer from a html page, e.g. from a smartphone or a tablet.
Adapted from https://github.com/simquat/LocalTrackpad.

The views (use menu or swipe top rows to change):

- Mouse/trackpad control, cursor, generic keys (e.g. for youtube, netflix).
- [VLC](https://www.videolan.org/vlc/index.html) control buttons.
- A filebrowser to open files, stores last visited folder.

# How to run
Note that there is no access control, everybody on the (local) network has access to webremotecontrol!

* On the server:
    * Get the [Java 8 JRE](http://www.oracle.com/technetwork/java/javase/downloads/index.html). Don't forget to untick the [crapware](https://www.google.com/search?q=java+crapware) installer, and/or [disable it permanently](https://www.java.com/en/download/faq/disable_offers.xml)!
    * [Download the jar](https://bitbucket.org/wolfgang/webremotecontrol/downloads).
    * Double-click to run the jar or do `java -jar webremotecontrol.jar` in a terminal.
    * Click on one of the buttons to show a QR code that contains the server URL and scan this with the client.
* Client: Adding an icon to your homescreen should make a web app.


![Screenshot_20170205-121604.png](https://bitbucket.org/repo/AxyGpB/images/1468820713-Screenshot_20170205-121604.png)
(outdated)

# Config file
An optional config file `webremotecontrol.txt` can be placed next to the jar. Example content:

    httpserverport=9000
    urls=npo,https\://www.npo.nl/mijn_npo\#history;netflix,http\://netflix.com;youtube,http\://youtube.com;southpark,http\://southpark.cc.com/full-episodes/random

# How to develop, compile & package

* Get a recent Java JDK 8.
* check out the code (`git clone ...` or download a zip).
* I use the free community version of [IntelliJ IDEA](https://www.jetbrains.com/idea/download/), just open the project to get started.
* Compile and run manually: `gradle run`.
* Package jar: `gradle dist`. The resulting jar is in `build/libs`.

# Used technologies

* [Kotlin](https://kotlinlang.org/) and [Gradle](https://gradle.org/)
* [Shadow](https://github.com/johnrengelman/shadow) to package
* [Javalin](https://javalin.io/) as webserver
* [Hammer.JS](http://hammerjs.github.io/) to capture touch events
* [QRGen](https://github.com/kenglxn/QRGen) to generate the QR code
* [js-mobile-console](https://github.com/B1naryStudio/js-mobile-console) for debugging
