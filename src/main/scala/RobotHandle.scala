import java.awt.{Dimension, MouseInfo, Robot, Toolkit}
import java.awt.event.{InputEvent, KeyEvent}

import com.typesafe.scalalogging.LazyLogging

class RobotHandle extends LazyLogging {

  private val robot: Robot = new Robot()

  private def getScreenSize: Dimension = Toolkit.getDefaultToolkit.getScreenSize
  private val screenWidth: Int = getScreenSize.width
  private val screenHeight: Int = getScreenSize.height
  val clientScale = 1.5 // 1.5 means that you have to move 1.5 times the client dimension to move over full server dimension

  robot.setAutoDelay(40)

  robot.setAutoWaitForIdle(true)

  def coerce(x: Int, min: Int, max: Int): Int = if (x < min) min else if (x > max) max else x

  def moveRel(x: Int, y: Int): Unit = {
    val mpl = MouseInfo.getPointerInfo.getLocation
    robotMoveAbs(coerce(mpl.x + x, 0, screenWidth - 1), coerce(mpl.y + y, 0, screenHeight - 1))
  }

  def tap(): Unit = {
    robotPressLeftButton()
    robot.delay(25)
    robotReleaseLeftButton()
    robot.delay(25)
  }

  def secondaryTap(): Unit = {
    robot.mousePress(InputEvent.BUTTON3_MASK)
    robot.delay(25)
    robot.mouseRelease(InputEvent.BUTTON3_MASK)
    robot.delay(25)
  }

  def scroll(amount: Int): Unit = {
    robot.delay(40)
    robot.mouseWheel(amount)
  }

  def pressLeftButton(): Unit = {
    robotPressLeftButton()
  }

  def releaseLeftButton(): Unit = {
    robotReleaseLeftButton()
  }

  def typeText(text: String): Unit = {
    logger.debug("robot: type [" + text + "]")
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
      logger.debug(" keycode: " + keyCode + " xxx " + KeyEvent.VK_A)
      robot.keyPress(keyCode)
      robot.delay(10)
      robot.keyRelease(keyCode)
      robot.delay(50)
    }
  }

  def clickKey(keycode: Int): Unit = {
    clickCombo(Seq(keycode))
  }

  def clickCombo(c: Seq[Int]) {
    logger.debug("robot: click combo " + c.mkString(","))
    for (k <- c) robot.keyPress(k)
    robot.delay(10)
    for (k <- c.reverse) robot.keyRelease(k)
  }

  private def robotMoveAbs(x: Int, y: Int): Unit = {
    robot.delay(10)
    robot.mouseMove(x, y)
  }

  private def robotPressLeftButton(): Unit = {
    robot.mousePress(InputEvent.BUTTON1_MASK)
  }

  private def robotReleaseLeftButton(): Unit = {
    robot.mouseRelease(InputEvent.BUTTON1_MASK)
  }

}
