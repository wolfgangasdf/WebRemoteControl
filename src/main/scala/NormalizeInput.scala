import java.awt.Dimension
import java.awt.Toolkit
//remove if not needed
//import scala.collection.JavaConversions._

object NormalizeInput {

  private def getScreenSize: Dimension = {
    val screenSize = Toolkit.getDefaultToolkit.getScreenSize
    screenSize
  }

  def getWidth: Int = getScreenSize.width

  def getHeight: Int = getScreenSize.height

  def mapValue(position: Int, clientDimension: Int, screenDimension: Int): Int = {
    (screenDimension * position) / clientDimension
  }
}
