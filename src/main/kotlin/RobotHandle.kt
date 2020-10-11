import java.awt.MouseInfo
import java.awt.Robot
import java.awt.Toolkit
import java.awt.event.InputEvent
import java.awt.event.KeyEvent
import mu.KotlinLogging
import kotlin.math.sqrt

private val logger = KotlinLogging.logger {}

class RobotHandle {

    private val robot = Robot()

    private fun getScreenSize() = Toolkit.getDefaultToolkit().screenSize
    private val screenWidth: Int = getScreenSize().width
    private val screenHeight: Int = getScreenSize().height
    private var lasttime = System.currentTimeMillis()
//    val clientScale = 1.5 // 1.5 means that you have to move 1.5 times the client dimension to move over full server dimension

    init {
        robot.autoDelay = 40
        robot.isAutoWaitForIdle = true
    }

    @Suppress("SameParameterValue")
    private fun coerce(x: Int, min: Int, max: Int): Int = if (x < min) min else if (x > max) max else x

    // accellerated moving https://stackoverflow.com/a/8773322
    fun moveRel(x: Int, y: Int) {
        val mpl = MouseInfo.getPointerInfo().location
        val dr = sqrt((x*x+y*y).toDouble())
        val t = System.currentTimeMillis()
        val dt = t - lasttime
        lasttime = t
        val v = dr/dt
        val accela = 1.0
        val accelb = 2.0
        val vnew = accela * v + accelb * v*v // probably average speed (and direction?) last 3 moves?
        val drnew = vnew * dt
        val dxnew = (x * drnew / dr).toInt()
        val dynew = (y * drnew / dr).toInt()
        robotMoveAbs(coerce(mpl.x + dxnew, 0, screenWidth - 1), coerce(mpl.y + dynew, 0, screenHeight - 1))
    }

    fun tap() {
        robotPressLeftButton()
        robot.delay(25)
        robotReleaseLeftButton()
        robot.delay(25)
    }

    fun secondaryTap() {
        robot.mousePress(InputEvent.BUTTON3_DOWN_MASK)
        robot.delay(25)
        robot.mouseRelease(InputEvent.BUTTON3_DOWN_MASK)
        robot.delay(25)
    }

    fun scroll(amount: Int) {
        robot.delay(40)
        robot.mouseWheel(amount)
    }

    fun pressLeftButton() {
        robotPressLeftButton()
    }

    fun releaseLeftButton() {
        robotReleaseLeftButton()
    }

    fun typeText(text: String) {
        logger.debug("robot: type [$text]")
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
        for (ch in text) {
        val keyCode = KeyEvent.getExtendedKeyCodeForChar(ch.toInt())
        logger.debug(" keycode: " + keyCode + " xxx " + KeyEvent.VK_A)
        robot.keyPress(keyCode)
        robot.delay(10)
        robot.keyRelease(keyCode)
        robot.delay(50)
    }
    }

    fun clickKey(keycode: Int) {
        clickCombo(listOf(keycode))
    }

    fun clickCombo(c: List<Int>) {
        logger.debug("robot: click combo " + c.joinToString(","))
        for (k in c) robot.keyPress(k)
        robot.delay(10)
        for (k in c.reversed()) robot.keyRelease(k)
    }

    private fun robotMoveAbs(x: Int, y: Int) {
        robot.delay(10)
        robot.mouseMove(x, y)
    }

    private fun robotPressLeftButton() {
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK)
    }

    private fun robotReleaseLeftButton() {
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK)
    }

}
