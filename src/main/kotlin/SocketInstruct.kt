
import io.javalin.embeddedserver.jetty.websocket.WsSession
import java.awt.event.KeyEvent
import mu.KotlinLogging

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
    "vlcfullscreen" to (if (Helpers.isWin()) listOf(KeyEvent.VK_F) else listOf(KeyEvent.VK_META, KeyEvent.VK_F)),
    "vlcvoldown" to (if (Helpers.isWin()) listOf(KeyEvent.VK_CONTROL, KeyEvent.VK_DOWN) else listOf(KeyEvent.VK_DOWN)),
    "vlcvolup" to (if (Helpers.isWin()) listOf(KeyEvent.VK_CONTROL, KeyEvent.VK_UP) else listOf(KeyEvent.VK_UP)),
    "vlcspace" to listOf(KeyEvent.VK_SPACE),
    "vlcjbbb" to (if (Helpers.isWin()) listOf(KeyEvent.VK_ALT, KeyEvent.VK_CONTROL, KeyEvent.VK_LEFT) else
    listOf(KeyEvent.VK_META, KeyEvent.VK_SHIFT, KeyEvent.VK_ALT, KeyEvent.VK_LEFT)),
    "vlcjfff" to (if (Helpers.isWin()) listOf(KeyEvent.VK_ALT, KeyEvent.VK_CONTROL, KeyEvent.VK_RIGHT) else
    listOf(KeyEvent.VK_META, KeyEvent.VK_SHIFT, KeyEvent.VK_ALT, KeyEvent.VK_RIGHT)),
    "vlcjbb" to (if (Helpers.isWin()) listOf(KeyEvent.VK_CONTROL, KeyEvent.VK_LEFT) else
    listOf(KeyEvent.VK_META, KeyEvent.VK_SHIFT, KeyEvent.VK_LEFT)),
    "vlcjff" to (if (Helpers.isWin()) listOf(KeyEvent.VK_CONTROL, KeyEvent.VK_RIGHT) else
    listOf(KeyEvent.VK_META, KeyEvent.VK_SHIFT, KeyEvent.VK_RIGHT)),
    "vlcjb" to (if (Helpers.isWin()) listOf(KeyEvent.VK_ALT, KeyEvent.VK_LEFT) else
    listOf(KeyEvent.VK_META, KeyEvent.VK_ALT, KeyEvent.VK_LEFT)),
    "vlcjf" to (if (Helpers.isWin()) listOf(KeyEvent.VK_ALT, KeyEvent.VK_RIGHT) else
    listOf(KeyEvent.VK_META, KeyEvent.VK_ALT, KeyEvent.VK_RIGHT)),
    "vlccaudio" to listOf(if (Helpers.isWin()) KeyEvent.VK_B else KeyEvent.VK_L),
    "vlccsubti" to listOf(if (Helpers.isWin()) KeyEvent.VK_V else KeyEvent.VK_S),
    "vlccaspect" to listOf(KeyEvent.VK_A),
    "vlcccrop" to listOf(KeyEvent.VK_C),
    "vlccaudev" to listOf(KeyEvent.VK_SHIFT, KeyEvent.VK_A)
    )

    fun instruct(message: String, conn: WsSession) {
        instructions = message.split(",")
        inputKind = instructions[0]
        when(inputKind) {
            "move" -> move(instructions[1].toInt(), instructions[2].toInt())
            "tap" -> robotHandle.tap()
            "tap2" -> robotHandle.secondaryTap()
            "dragStart" -> robotHandle.pressLeftButton()
             "dragEnd" -> robotHandle.releaseLeftButton()
             "scroll" -> robotHandle.scroll(instructions[1].toInt())
             "text" -> robotHandle.typeText(instructions.drop(1).joinToString(","))
             "debug" -> logger.debug("JS: " + instructions.drop(1).joinToString(","))
             "key" -> robotHandle.clickKey(instructions[1].toInt())
             "combo" -> robotHandle.clickCombo(instructions.drop(1).map { s -> s.toInt() })
             "cmd" -> doCommand(instructions[1])
             "bauto" ->  combos[instructions[1]]?.let { robotHandle.clickCombo(it) }

             "fbgetfiles" -> conn.send(FileBrowser.getFiles())
             "fbup" -> { FileBrowser.goUp() ; conn.send(FileBrowser.getFiles()) }
             "fbopen" -> {
                 val f = FileBrowser.currentFiles[instructions[1].toInt()]
                 if (f.isDirectory) {
                     FileBrowser.updateFiles(f)
                     conn.send(FileBrowser.getFiles())
                 } else Helpers.openDocument(f)
                 conn.send(FileBrowser.getFiles())
             }
             "exit" -> {
                 logger.info("exit")
                 System.exit(0)
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
