
import io.javalin.websocket.WsContext
import java.awt.event.KeyEvent
import mu.KotlinLogging
import java.io.File
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

class SocketInstruct {

    private val robotHandle = RobotHandle()

    private var instructions = listOf<String>()

    private var inputKind = ""

    // mac: cmd=VK_META
    private val combos = hashMapOf(
        "bspace" to listOf(KeyEvent.VK_SPACE),
        "bf" to listOf(KeyEvent.VK_F),
        "bup" to listOf(KeyEvent.VK_UP),
        "bdown" to listOf(KeyEvent.VK_DOWN),
        "bleft" to listOf(KeyEvent.VK_LEFT),
        "bright" to listOf(KeyEvent.VK_RIGHT),
        "bescape" to listOf(KeyEvent.VK_ESCAPE),
        "bclosetab" to listOf(if (Helpers.isMac()) KeyEvent.VK_META else KeyEvent.VK_CONTROL, KeyEvent.VK_W),
        "quit" to  if (Helpers.isMac()) listOf(KeyEvent.VK_META, KeyEvent.VK_Q) else listOf(KeyEvent.VK_ALT, KeyEvent.VK_F4),
        "vlcfullscreen" to (if (Helpers.isWinLin()) listOf(KeyEvent.VK_F) else listOf(KeyEvent.VK_META, KeyEvent.VK_F)),
        "vlcvoldown" to (if (Helpers.isWinLin()) listOf(KeyEvent.VK_CONTROL, KeyEvent.VK_DOWN) else listOf(KeyEvent.VK_DOWN)),
        "vlcvolup" to (if (Helpers.isWinLin()) listOf(KeyEvent.VK_CONTROL, KeyEvent.VK_UP) else listOf(KeyEvent.VK_UP)),
        "vlcspace" to listOf(KeyEvent.VK_SPACE),
        "vlcjbbb" to (if (Helpers.isWinLin()) listOf(KeyEvent.VK_ALT, KeyEvent.VK_CONTROL, KeyEvent.VK_LEFT) else
        listOf(KeyEvent.VK_META, KeyEvent.VK_SHIFT, KeyEvent.VK_ALT, KeyEvent.VK_LEFT)),
        "vlcjfff" to (if (Helpers.isWinLin()) listOf(KeyEvent.VK_ALT, KeyEvent.VK_CONTROL, KeyEvent.VK_RIGHT) else
        listOf(KeyEvent.VK_META, KeyEvent.VK_SHIFT, KeyEvent.VK_ALT, KeyEvent.VK_RIGHT)),
        "vlcjbb" to (if (Helpers.isWinLin()) listOf(KeyEvent.VK_CONTROL, KeyEvent.VK_LEFT) else
        listOf(KeyEvent.VK_META, KeyEvent.VK_SHIFT, KeyEvent.VK_LEFT)),
        "vlcjff" to (if (Helpers.isWinLin()) listOf(KeyEvent.VK_CONTROL, KeyEvent.VK_RIGHT) else
        listOf(KeyEvent.VK_META, KeyEvent.VK_SHIFT, KeyEvent.VK_RIGHT)),
        "vlcjb" to (if (Helpers.isWinLin()) listOf(KeyEvent.VK_ALT, KeyEvent.VK_LEFT) else
        listOf(KeyEvent.VK_META, KeyEvent.VK_ALT, KeyEvent.VK_LEFT)),
        "vlcjf" to (if (Helpers.isWinLin()) listOf(KeyEvent.VK_ALT, KeyEvent.VK_RIGHT) else
        listOf(KeyEvent.VK_META, KeyEvent.VK_ALT, KeyEvent.VK_RIGHT)),
        "vlccaudio" to listOf(if (Helpers.isWinLin()) KeyEvent.VK_B else KeyEvent.VK_L),
        "vlccsubti" to listOf(if (Helpers.isWinLin()) KeyEvent.VK_V else KeyEvent.VK_S),
        "vlccaspect" to listOf(KeyEvent.VK_A),
        "vlcccrop" to listOf(KeyEvent.VK_C),
        "vlccaudev" to listOf(KeyEvent.VK_SHIFT, KeyEvent.VK_A),
        "ivprev" to listOf(KeyEvent.VK_META, KeyEvent.VK_LEFT),
        "ivnext" to listOf(KeyEvent.VK_META, KeyEvent.VK_RIGHT)
    )

    fun instruct(message: String, ctx: WsContext) {
        instructions = message.split("\t")
        inputKind = instructions[0]
        // logger.debug("instruct: inputKind=$inputKind")
        when(inputKind) {
            "move" -> move(instructions[1].toInt(), instructions[2].toInt())
            "tap" -> robotHandle.tap()
            "tap2" -> robotHandle.secondaryTap()
            "dragStart" -> robotHandle.pressLeftButton()
            "dragEnd" -> robotHandle.releaseLeftButton()
            "scroll" -> robotHandle.scroll(instructions[1].toInt())
            "debug" -> logger.debug("JS: " + instructions.drop(1).joinToString("\t"))
            "char" -> robotHandle.clickChar(instructions[1][0])
            "pastetext" -> robotHandle.pasteText(instructions[1])
            "cmd" -> doCommand(instructions[1])
            "bauto" ->  {
                combos[instructions[1]]?.let { robotHandle.clickCombo(it) }
            }
            "hgethistory" -> ctx.send("hlist\t" + Settings.historyGet())
            "fbgetfiles" -> {
                ctx.send(FileBrowser.getFiles())
                val fn = Settings.historyGet(0)
                if (fn != "") {
                    val f = File(fn)
                    if (f.exists() && FileBrowser.currentFiles.contains(f)) ctx.send("fbreveal\t${FileBrowser.currentFiles.indexOf(f)}")
                }
            }
            "fbup" -> {
                val oldf = FileBrowser.currentFolder
                FileBrowser.goUp()
                ctx.send(FileBrowser.getFiles())
                ctx.send("fbreveal\t${FileBrowser.currentFiles.indexOf(oldf)}")
            }
            "fbopen" -> {
                fun openedPath(f: File) {
                    Settings.historyAdd(f)
                    ctx.send("fbreveal\t${FileBrowser.currentFiles.indexOf(f)}")
                    ctx.send("showvlc")
                }
                val f = FileBrowser.currentFiles[instructions[1].toInt()]
                if (!f.isDirectory) {
                    if (f.extension == "jpg") { // load all jpgs in folder
                        val args = mutableListOf<String>("--fullscreen", "--image-duration=-1")
                        args.addAll(FileBrowser.currentFiles.map { it.canonicalPath })
                        Helpers.runVLC(*(args.toTypedArray()))
                        ctx.send("showiv")
                    } else {
                        Helpers.openDocument(f)
                        openedPath(f)
                    }
                } else if (f.name.uppercase() == "VIDEO_TS") {
                    val vlcp = Settings.props.getProperty("vlc")
                    if (vlcp != "") {
                        Helpers.runVLC(f.canonicalPath)
                        openedPath(f)
                    } else {
                        logger.error("Set vlc path in settings file and restart to open VIDEO_TS folders!")
                    }
                } else {
                    FileBrowser.updateFiles(f)
                    ctx.send(FileBrowser.getFiles())
                }
            }
            "hdelete" -> {
                Settings.historyDelete(instructions[1].toInt())
            }
            "hopen" -> {
                val i = instructions[1].toInt()
                val f = File(Settings.historyGet(i))
                if (f.exists()) {
                    FileBrowser.updateFiles(f.parentFile)
                    ctx.send(FileBrowser.getFiles())
                    ctx.send("fbreveal\t${FileBrowser.currentFiles.indexOf(f)}")
                }
            }
            "exit" -> {
                logger.info("exit")
                exitProcess(0)
            }
            else -> logger.error("invalid instruction: $message")
        }
    }

    private fun move(x: Int, y: Int) {
        robotHandle.moveRel(x, y)
    }

    private fun doCommand(cmd: String) {
        WebRemoteControl.urls[cmd]?.let { Helpers.openURL(it) }
    }

}
