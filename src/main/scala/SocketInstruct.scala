import com.typesafe.scalalogging.LazyLogging
import org.java_websocket.WebSocket
import java.awt.event.KeyEvent

class SocketInstruct extends LazyLogging {

  private val robotHandle: RobotHandle = new RobotHandle()

  private var instructions: Array[String] = _

  private var inputKind: String = _

  private var clientScreenWidth: Int = 0

  private var clientScreenHeight: Int = 0

  // mac: cmd=VK_META
  private val combos = collection.immutable.HashMap(
    "bspace" -> Seq(KeyEvent.VK_SPACE),
    "bf" -> Seq(KeyEvent.VK_F),
    "bup" -> Seq(KeyEvent.VK_UP),
    "bdown" -> Seq(KeyEvent.VK_DOWN),
    "bleft" -> Seq(KeyEvent.VK_LEFT),
    "bright" -> Seq(KeyEvent.VK_RIGHT),
    "bescape" -> Seq(KeyEvent.VK_ESCAPE),
    "bclosetab" -> Seq(if (Helpers.isMac) KeyEvent.VK_META else KeyEvent.VK_CONTROL, KeyEvent.VK_W),
    "vlcfullscreen" -> (if (Helpers.isWin) Seq(KeyEvent.VK_F) else Seq(KeyEvent.VK_META, KeyEvent.VK_F)),
    "vlcvoldown" -> (if (Helpers.isWin) Seq(KeyEvent.VK_CONTROL, KeyEvent.VK_DOWN) else Seq(KeyEvent.VK_DOWN)),
    "vlcvolup" -> (if (Helpers.isWin) Seq(KeyEvent.VK_CONTROL, KeyEvent.VK_UP) else Seq(KeyEvent.VK_UP)),
    "vlcspace" -> Seq(KeyEvent.VK_SPACE),
    "vlcjbbb" -> (if (Helpers.isWin) Seq(KeyEvent.VK_ALT, KeyEvent.VK_CONTROL, KeyEvent.VK_LEFT) else
      Seq(KeyEvent.VK_META, KeyEvent.VK_SHIFT, KeyEvent.VK_ALT, KeyEvent.VK_LEFT)),
    "vlcjfff" -> (if (Helpers.isWin) Seq(KeyEvent.VK_ALT, KeyEvent.VK_CONTROL, KeyEvent.VK_RIGHT) else
      Seq(KeyEvent.VK_META, KeyEvent.VK_SHIFT, KeyEvent.VK_ALT, KeyEvent.VK_RIGHT)),
    "vlcjbb" -> (if (Helpers.isWin) Seq(KeyEvent.VK_CONTROL, KeyEvent.VK_LEFT) else
      Seq(KeyEvent.VK_META, KeyEvent.VK_SHIFT, KeyEvent.VK_LEFT)),
    "vlcjff" -> (if (Helpers.isWin) Seq(KeyEvent.VK_CONTROL, KeyEvent.VK_RIGHT) else
      Seq(KeyEvent.VK_META, KeyEvent.VK_SHIFT, KeyEvent.VK_RIGHT)),
    "vlcjb" -> (if (Helpers.isWin) Seq(KeyEvent.VK_ALT, KeyEvent.VK_LEFT) else
      Seq(KeyEvent.VK_META, KeyEvent.VK_ALT, KeyEvent.VK_LEFT)),
    "vlcjf" -> (if (Helpers.isWin) Seq(KeyEvent.VK_ALT, KeyEvent.VK_RIGHT) else
      Seq(KeyEvent.VK_META, KeyEvent.VK_ALT, KeyEvent.VK_RIGHT)),
    "vlccaudio" -> Seq(if (Helpers.isWin) KeyEvent.VK_B else KeyEvent.VK_L),
    "vlccsubti" -> Seq(if (Helpers.isWin) KeyEvent.VK_V else KeyEvent.VK_S),
    "vlccaspect" -> Seq(KeyEvent.VK_A),
    "vlcccrop" -> Seq(KeyEvent.VK_C),
    "vlccaudev" -> Seq(KeyEvent.VK_SHIFT, KeyEvent.VK_A)
  )

  def instruct(message: String, conn: WebSocket) {
    logger.debug("instruct: " + message)
    instructions = message.split(",")
    inputKind = instructions(0)
    inputKind match {
      case "move" => move(instructions(1).toInt, instructions(2).toInt)
      case "tap" => robotHandle.tap()
      case "tap2" => robotHandle.secondaryTap()
      case "screen" => setClientScreenSize(instructions(1).toInt, instructions(2).toInt)
      case "dragStart" => robotHandle.pressLeftButton()
      case "dragEnd" => robotHandle.releaseLeftButton()
      case "scroll" => robotHandle.scroll(instructions(1).toInt)
      case "text" => robotHandle.typeText(instructions.drop(1).mkString(","))
      case "debug" => logger.debug("JS: " + instructions.drop(1).mkString(","))
      case "key" => robotHandle.clickKey(instructions(1).toInt)
      case "combo" => robotHandle.clickCombo(instructions.tail.map(s => s.toInt))
      case "cmd" => doCommand(instructions(1))
      case "bauto" =>  robotHandle.clickCombo(combos(instructions(1)))

      case "fbgetfiles" =>
        conn.send(FileBrowser.getFiles)
      case "fbup" =>
        FileBrowser.goUp()
        conn.send(FileBrowser.getFiles)
      case "fbopen" =>
        val f = FileBrowser.currentFiles(instructions(1).toInt)
        if (f.isDirectory) {
          FileBrowser.updateFiles(f)
          conn.send(FileBrowser.getFiles)
        } else Helpers.openDocument(f)
        conn.send(FileBrowser.getFiles)
      case "exit" =>
        logger.info("exit")
        System.exit(0)

      case _ => logger.error("invalid instruction: " + message)
    }
  }

  private def move(x: Int, y: Int) {
    robotHandle.moveRel(x, y, clientScreenWidth, clientScreenHeight)
  }

  private def setClientScreenSize(width: Int, height: Int) {
    clientScreenWidth = width
    clientScreenHeight = height
  }

  private def doCommand(cmd: String): Unit = {
    cmd match {
      case "netflix" => Helpers.openURL("https://netflix.com")
      case "youtube" => Helpers.openURL("https://youtube.com")
    }
  }

}
