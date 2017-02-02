import java.awt.{Dimension, MouseInfo, Robot, Toolkit}
import java.awt.event.{InputEvent, KeyEvent}

import com.typesafe.scalalogging.LazyLogging

class RobotHandle extends LazyLogging {

  private val robot: Robot = new Robot()

  private def getScreenSize: Dimension = Toolkit.getDefaultToolkit.getScreenSize
  private val screenWidth: Int = getScreenSize.width
  private val screenHeight: Int = getScreenSize.height
  private var currX = MouseInfo.getPointerInfo.getLocation.x
  private var currY = MouseInfo.getPointerInfo.getLocation.y
  private var oldClientX = -1
  private var oldClientY = -1
  val clientScale = 1.5 // 1.5 means that you have to move 1.5 times the client dimension to move over full server dimension

  robot.setAutoDelay(40)

  robot.setAutoWaitForIdle(true)

  def coerce(x: Int, min: Int, max: Int): Int = {
    if (x < min) min
    else if (x > max) max
    else x
  }
  // now relative coordinates!
  def moveRel(x: Int, y: Int, clientWidth: Int, clientHeight: Int) {
    // make movement proportional in x,y!
    val rel = scala.math.min(screenWidth / (clientWidth * clientScale), screenHeight / (clientHeight * clientScale))
    val relX = ((x - oldClientX) * rel).toInt
    currX = coerce(currX + relX, 0, screenWidth)
    oldClientX = x

    val relY = ((y - oldClientY) * rel).toInt
    currY = coerce(currY + relY, 0, screenHeight)
    oldClientY = y

    robotMoveAbs(currX, currY)
  }

  def tap() {
    robotPressLeftButton()
    robot.delay(25)
    robotReleaseLeftButton()
    robot.delay(25)
  }

  def secondaryTap() {
    robot.mousePress(InputEvent.BUTTON3_MASK)
    robot.delay(25)
    robot.mouseRelease(InputEvent.BUTTON3_MASK)
    robot.delay(25)
  }

  def scroll(amount: Int) {
    robot.delay(40)
    robot.mouseWheel(amount)
  }

  def pressLeftButton() {
    robotPressLeftButton()
  }

  def releaseLeftButton() {
    robotReleaseLeftButton()
  }

  def clickKey(keycode: Int): Unit = {
    logger.debug("robot: click " + keycode)
    robot.keyPress(keycode)
    robot.delay(10)
    robot.keyRelease(keycode)
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

  private def robotMoveAbs(x: Int, y: Int) {
    robot.delay(10)
    robot.mouseMove(x, y)
  }

  private def robotPressLeftButton() {
    robot.mousePress(InputEvent.BUTTON1_MASK)
  }

  private def robotReleaseLeftButton() {
    robot.mouseRelease(InputEvent.BUTTON1_MASK)
  }

}
