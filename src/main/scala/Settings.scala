import java.io.{File, FileInputStream, FileOutputStream}
import java.util.Properties

import com.typesafe.scalalogging.LazyLogging

object Settings extends LazyLogging {

  val jarfile = new File(WebRemoteControl.getClass.getProtectionDomain.getCodeSource.getLocation.toURI.getPath)
  val configfolder: String = if (jarfile.toString.endsWith(".jar")) jarfile.getParent else new File(".").getAbsoluteFile.getParent
  val configfile = new File(configfolder + File.separator + "webremotecontrol.txt")

  val settings = new Properties()

  def save(): Unit = {
    settings.store(new FileOutputStream(configfile.getAbsolutePath), "")
  }
  def ini(): Unit = {
    logger.info(s"Config file: $configfile")
    if (configfile.exists) {
      settings.load(new FileInputStream(configfile))
      WebRemoteControl.httpServerPort = settings.getProperty("httpserverport", "8000").toInt
      WebRemoteControl.webSocketPort = settings.getProperty("websocketport", "8001").toInt
    }
  }
}
