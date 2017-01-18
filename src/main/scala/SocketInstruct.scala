import com.typesafe.scalalogging.LazyLogging

class SocketInstruct extends LazyLogging {

  private val robotHandle: RobotHandle = new RobotHandle()

  private var instructions: Array[String] = _

  private var inputKind: String = _

  private var clientScreenWidth: Int = 0

  private var clientScreenHeight: Int = 0

  def instruct(message: String) {
    logger.debug("instruct: " + message)
    instructions = message.split(",")
    inputKind = instructions(0)
    inputKind match {
      case "move" => move(java.lang.Integer.parseInt(instructions(1)), java.lang.Integer.parseInt(instructions(2)))
      case "tap" => tap()
      case "tap2" => secondaryTap()
      case "screen" => setClientScreenSize(java.lang.Integer.parseInt(instructions(1)), java.lang.Integer.parseInt(instructions(2)))
      case "dragStart" => pressLeftButton()
      case "dragEnd" => releaseLeftButton()
      case "scroll" => scroll(java.lang.Integer.parseInt(instructions(1)))
      case "text" => typeText(instructions.drop(1).mkString(","))
      case "key" => clickKey(java.lang.Integer.parseInt(instructions(1)))
      case "exit" =>
        logger.info("exit")
        System.exit(0)

      case _ => logger.error("invalid instruction")
    }
  }

  private def move(x: Int, y: Int) {
    robotHandle.moveRel(x, y, clientScreenWidth, clientScreenHeight)
  }

  private def tap() {
    robotHandle.tap()
  }

  private def secondaryTap() {
    robotHandle.secondaryTap()
  }

  private def pressLeftButton() {
    robotHandle.pressLeftButton()
  }

  private def releaseLeftButton() {
    robotHandle.releaseLeftButton()
  }

  private def setClientScreenSize(width: Int, height: Int) {
    clientScreenWidth = width
    clientScreenHeight = height
  }

  private def scroll(amount: Int) {
    robotHandle.scroll(amount)
  }

  private def typeText(text: String): Unit = {
    robotHandle.typeText(text)
  }

  private def clickKey(keycode: Int): Unit = {
    logger.debug("click key " + keycode)
    robotHandle.clickKey(keycode)
  }
}
