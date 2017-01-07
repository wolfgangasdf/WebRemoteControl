import java.awt.Dimension
import java.awt.Toolkit

object NormalizeInput {

  private def getScreenSize: Dimension = Toolkit.getDefaultToolkit.getScreenSize

  def getWidth: Int = getScreenSize.width

  def getHeight: Int = getScreenSize.height

  def mapValue(position: Int, clientDimension: Int, screenDimension: Int): Int = {
    (screenDimension * position) / clientDimension
  }
}
