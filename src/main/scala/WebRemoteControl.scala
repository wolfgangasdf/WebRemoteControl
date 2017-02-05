import java.awt._
import java.awt.event.{ActionEvent, WindowAdapter, WindowEvent}
import java.io.{File, FileInputStream}
import java.net.InetSocketAddress
import java.util.Properties

import com.typesafe.scalalogging.LazyLogging
import org.java_websocket.{WebSocket, WebSocketImpl}
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer

object WebRemoteControl extends LazyLogging {

  val settings = new Properties()
  var httpServerPort = 8000
  var webSocketPort = 8001

  def main(args: Array[String]) {
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG")
    val infos = s"${buildinfo.BuildInfo.name} ${buildinfo.BuildInfo.version} built ${buildinfo.BuildInfo.buildTime}"
    logger.info(s"Starting $infos")
    val jarfile = new File(WebRemoteControl.getClass.getProtectionDomain.getCodeSource.getLocation.toURI.getPath)
    val configfolder = if (jarfile.toString.endsWith(".jar"))
      jarfile.getParent
    else
      new File(".").getAbsoluteFile.getParent
    val configfile = new File(configfolder + File.separator + "webremotecontrol.txt")
    logger.info(s"Config file: $configfile")

    if (configfile.exists) {
      settings.load(new FileInputStream(configfile))
      httpServerPort = settings.getProperty("httpserverport", "8000").toInt
      webSocketPort = settings.getProperty("websocketport", "8001").toInt
    }

    val frame = new Frame( "WebRemoteControl" )
    frame.setLayout(new GridLayout(6, 1))
    val btQuit = new Button("Quit")
    btQuit.addActionListener((_: ActionEvent) => System.exit(0))
    frame.add(new Label(infos))
    frame.add(new Label(s"Listening on http://localhost:$httpServerPort"))
    frame.add(new Label(s"Websocket on port $webSocketPort"))
    frame.add(new Label(s"Optional config file:"))
    frame.add(new Label(configfile.toString))
    frame.add(btQuit)
    frame.addWindowListener( new WindowAdapter() {
      override def windowClosing(e : WindowEvent){ System.exit(0) }
      override def windowOpened(e: WindowEvent): Unit = {
        new SimpleHttpServer(httpServerPort).start()
        WebSocketImpl.DEBUG = false
        val webRemoteControl = new WebRemoteControl(webSocketPort)
        webRemoteControl.start()
      }
    })
    frame.setSize( 400, 300 )
    frame.pack()
    frame.setVisible(true)
  }
}

class WebRemoteControl(port: Int) extends WebSocketServer(new InetSocketAddress(port)) with LazyLogging {

  private val socketInstruct: SocketInstruct = new SocketInstruct()

  override def onOpen(conn: WebSocket, handshake: ClientHandshake) {
    logger.info(s"${conn.getRemoteSocketAddress.getAddress.getHostAddress} connected! :)")
  }

  override def onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
    logger.info(s"${conn.getRemoteSocketAddress.getAddress.getHostAddress } disconnected! ':(")
  }

  override def onMessage(conn: WebSocket, message: String) {
    socketInstruct.instruct(message)
  }

  override def onError(conn: WebSocket, ex: Exception) {
    ex.printStackTrace()
    if (conn != null) {
    }
  }
}
