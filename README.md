# WebRemoteControl
Remote control the mouse and keyboard of a Mac/Windows/Linux computer from a html page, e.g. from a smartphone or a tablet.
Adapted from https://github.com/simquat/LocalTrackpad.

The views (dropdown menu or swipe top part left/right):

- Mouse/trackpad control, cursor, generic keys (e.g. for youtube, netflix), send text.
- [VLC](https://www.videolan.org/vlc/index.html) control buttons.
- A filebrowser to open movies/files.
- If a jpg file is opened, all jpg's in the current folder are shown in VLC as image viewer 
- A history of recently opened movies/files.

# How to run
Note that there is no access control, everybody on the (local) network has access to webremotecontrol!

* On the server:
    * Install [VLC](https://www.videolan.org/vlc/index.html) and make sure movies are [automatically](https://wiki.videolan.org/VLC_HowTo/Make_VLC_the_default_player/) opened with it.
    * [Download a zip](https://github.com/wolfgangasdf/WebRemoteControl/releases), extract it somewhere and run (in screen/tmux) 
    `bin/webremotecontrol.bat` (Windows) or `bin/webremotecontrol` (Mac/Linux). It is not signed, google for "open unsigned mac/win".
    * Click on one of the buttons to show a QR code that contains the server URL and scan this with the client.
* Client: Adding an icon to your homescreen should make a web app.

# Config file
The config file `webremotecontrol.txt` is automatically generated in the current folder. Example content:

    httpserverport=9000
    urls=npo,https\://www.npo.nl/mijn_npo\#history;netflix,http\://netflix.com;youtube,http\://youtube.com;southpark,http\://southpark.cc.com/full-episodes/random
    vlc=/Applications/VLC.app or vlc=C\:\\Program Files\\VideoLAN\\VLC\\vlc.exe


The `vlc` setting pointing to the VLC program is only needed to open `VIDEO_TS` DVD folders.

# How to develop, compile & package

* Get Java from https://jdk.java.net
* Clone the repository
* I use the free community version of [IntelliJ IDEA](https://www.jetbrains.com/idea/download/), just open the project to get started.
* Compile and run manually: `./gradlew run`.
* Package: `./gradlew clean dist`. The builds are in `build/libs`.

# Used technologies

* [Kotlin](https://kotlinlang.org/) and [Gradle](https://gradle.org/)
* [Javalin](https://javalin.io/) as webserver
* [Hammer.JS](http://hammerjs.github.io/) to capture touch events
* [QRGen](https://github.com/kenglxn/QRGen) to generate the QR code
* [js-mobile-console](https://github.com/B1naryStudio/js-mobile-console) for debugging
* [Runtime plugin](https://github.com/beryx/badass-runtime-plugin) to make runtimes with JRE
