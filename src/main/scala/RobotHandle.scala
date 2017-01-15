import java.awt.Robot
import java.awt.event.{InputEvent, KeyEvent}

class RobotHandle {

  private val robot: Robot = new Robot()

  private val screenWidth: Int = NormalizeInput.getWidth

  private val screenHeight: Int = NormalizeInput.getHeight

  val instructions: Array[String] = null

  val coordinates: Array[Int] = Array.ofDim[Int](2)

  robot.setAutoDelay(40)

  robot.setAutoWaitForIdle(true)

  def move(x: Int, y: Int, clientWidth: Int, clientHeight: Int) {
    robotMove(NormalizeInput.mapValue(x, clientWidth, screenWidth), NormalizeInput.mapValue(y, clientHeight, screenHeight))
  }

  def tap() {
    robotLeftClick()
  }

  def secondaryTap() {
    robotRightClick()
  }

  def scroll(amount: Int) {
    robotScroll(amount)
  }

  def pressLeftButton() {
    robotPressLeftButton()
  }

  def releaseLeftButton() {
    robotReleaseLeftButton()
  }

  def clickKey(keycode: Int): Unit = {
    println("robot: click " + keycode)
    robot.keyPress(keycode)
    robot.delay(10)
    robot.keyRelease(keycode)
  }

  def typeText(text: String): Unit = {
    println("robot: type [" + text + "]")
    // http://stackoverflow.com/a/29665705
//    val stringSelection = new StringSelection(text)
//    val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
//    clipboard.setContents(stringSelection, stringSelection)
//
//    robot.keyPress(KeyEvent.VK_CONTROL)
//    robot.keyPress(KeyEvent.VK_V)
//    robot.keyRelease(KeyEvent.VK_V)
//    robot.keyRelease(KeyEvent.VK_CONTROL)

    // http://stackoverflow.com/questions/15260282/converting-a-char-into-java-keyevent-keycode
    for (ch <- text) {
      val keyCode = KeyEvent.getExtendedKeyCodeForChar(ch)
      println(" keycode: " + keyCode + " xxx " + KeyEvent.VK_A)
      robot.keyPress(keyCode)
      robot.delay(10)
      robot.keyRelease(keyCode)
      robot.delay(50)
    }
  }

  private def robotMove(x: Int, y: Int) {
    robot.delay(10)
    robot.mouseMove(x, y)
  }

  private def robotPressLeftButton() {
    robot.mousePress(InputEvent.BUTTON1_MASK)
  }

  private def robotReleaseLeftButton() {
    robot.mouseRelease(InputEvent.BUTTON1_MASK)
  }

  private def robotLeftClick() {
    robotPressLeftButton()
    robot.delay(25)
    robotReleaseLeftButton()
    robot.delay(25)
  }

  private def robotRightClick() {
    robot.mousePress(InputEvent.BUTTON3_MASK)
    robot.delay(25)
    robot.mouseRelease(InputEvent.BUTTON3_MASK)
    robot.delay(25)
  }

  private def robotScroll(amount: Int) {
    robot.delay(40)
    robot.mouseWheel(amount)
  }
}
