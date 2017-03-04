import java.io.File
import java.net.URI

object Helpers {
  def isMac: Boolean = System.getProperty("os.name").toLowerCase.contains("mac")
  def isLinux: Boolean = System.getProperty("os.name").toLowerCase.matches("(.*nix)|(.*nux)")
  def isWin: Boolean = System.getProperty("os.name").toLowerCase.contains("win")
  def openURL(url: String): Unit = {
    import java.awt.Desktop
    if (Desktop.isDesktopSupported && url != "") {
      val desktop = Desktop.getDesktop
      if (desktop.isSupported(Desktop.Action.BROWSE)) {
        desktop.browse(new URI(url))
      }
    }
  }
  def openDocument(file: File): Unit = {
    import java.awt.Desktop
    if (Desktop.isDesktopSupported) {
      val desktop = Desktop.getDesktop
      if (desktop.isSupported(Desktop.Action.OPEN)) {
        desktop.open(file)
      }
    }
  }
}
