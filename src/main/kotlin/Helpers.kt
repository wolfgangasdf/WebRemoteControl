import mu.KotlinLogging
import java.io.File
import java.net.URI
import java.awt.Desktop
import java.io.IOException
import java.util.jar.JarFile
import java.net.URISyntaxException
import java.util.*

private val logger = KotlinLogging.logger {}


object Helpers {
    fun isMac() = System.getProperty("os.name").lowercase().contains("mac")
    fun isLinux() = System.getProperty("os.name").lowercase().matches("(.*nix)|(.*nux)".toRegex())
    fun isWin() = System.getProperty("os.name").lowercase().contains("win")
    fun isWinLin() = isWin() || isLinux()
    fun openURL(url: String) {
        if (Desktop.isDesktopSupported() && url != "") {
            val desktop = Desktop.getDesktop()
            if (desktop.isSupported(Desktop.Action.BROWSE)) {
                desktop.browse(URI(url))
            }
        }
    }

    fun openDocument(file: File) {
        if (Desktop.isDesktopSupported()) {
            val desktop = Desktop.getDesktop()
            if (desktop.isSupported(Desktop.Action.OPEN)) {
                desktop.open(file)
            }
        }
    }

    fun runVLC(vararg s: String): Boolean {
        val vlcp = Settings.props.getProperty("vlc")
        return if (vlcp != "") {
            val l = mutableListOf<String>(if (vlcp.endsWith(".app")) "$vlcp/Contents/MacOS/VLC" else vlcp)
            l.addAll(s.toList())
            runProgram(*(l.toTypedArray()))
            true
        } else {
            logger.error("Set vlc path in settings file and restart to open VIDEO_TS folders!")
            false
        }
    }

    fun runProgram(vararg s: String) {
        logger.debug("launching: " + s.asList())
        // don't use this, doesn't work with VIDEO_TS on win10: Runtime.getRuntime().exec(s)
        val builder = ProcessBuilder(s.toList())
        builder.inheritIO()
        builder.start()
    }

    // https://stackoverflow.com/a/22404140
    fun getClassBuildTime(): Date? {
        var d: Date? = null
        val currentClass = object : Any() {

        }.javaClass.enclosingClass
        val resource = currentClass.getResource(currentClass.simpleName + ".class")
        if (resource != null) {
            when(resource.protocol) {
                "file" -> try {
                    d = Date(File(resource.toURI()).lastModified())
                } catch (_: URISyntaxException) {
                }
                "jar" -> {
                    val path = resource.path
                    d = Date(File(path.substring(5, path.indexOf("!"))).lastModified())
                }
                "zip" -> {
                    val path = resource.path
                    val jarFileOnDisk = File(path.substring(0, path.indexOf("!")))
                    //long jfodLastModifiedLong = jarFileOnDisk.lastModified ();
                    //Date jfodLasModifiedDate = new Date(jfodLastModifiedLong);
                    try {
                        JarFile(jarFileOnDisk).use { jf ->
                            val ze = jf.getEntry(path.substring(path.indexOf("!") + 2))//Skip the ! and the /
                            val zeTimeLong = ze.time
                            val zeTimeDate = Date(zeTimeLong)
                            d = zeTimeDate
                        }
                    } catch (_: IOException) {
                    } catch (_: RuntimeException) {
                    }

                }
            }
        }
        return d
    }
}
