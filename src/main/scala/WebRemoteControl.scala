
import java.awt._
import java.awt.event.{ActionEvent, WindowAdapter, WindowEvent}
import java.io.ByteArrayInputStream
import java.net.{InetAddress, InetSocketAddress}
import javax.imageio.ImageIO

import com.typesafe.scalalogging.LazyLogging
import net.glxn.qrgen.core.image.ImageType
import net.glxn.qrgen.javase.QRCode
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import org.java_websocket.{WebSocket, WebSocketImpl}

import scala.collection.mutable

object WebRemoteControl extends LazyLogging {

  var httpServerPort = 8000
  var webSocketPort = 8001
  val urls = new mutable.LinkedHashMap[String, String]()

  def showQRCode(parent: Frame, s: String): Unit = {
    val QRDIM = 250
    val ba = QRCode.from(s).to(ImageType.PNG).withSize(QRDIM, QRDIM).stream.toByteArray
    val bi= ImageIO.read(new ByteArrayInputStream(ba))
    val can = new Canvas {
      setSize(QRDIM, QRDIM)
      override def paint(g: Graphics): Unit = g.drawImage(bi, 0, 0, this)
    }
    val dia = new Dialog(parent, "QR code", true) {
      setLayout(new java.awt.BorderLayout())
      add(can, BorderLayout.CENTER)
      add(new Button("Close") { addActionListener((_: ActionEvent) => getParent.setVisible(false)) }, BorderLayout.SOUTH)
    }
    dia.pack()
    dia.setVisible(true)
  }

  def main(args: Array[String]) {
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG")
    val infos = s"${buildinfo.BuildInfo.name} ${buildinfo.BuildInfo.version} built ${buildinfo.BuildInfo.buildTime}"
    logger.info(s"Starting $infos")
    Settings.ini()
    val (urlho, urlip) = try {
      (s"http://${InetAddress.getLocalHost.getHostName}:$httpServerPort", s"http://${InetAddress.getLocalHost.getHostAddress}:$httpServerPort")
    } catch { case _: Exception => logger.error("Can't get hostname or IP"); ("", "") }

    val frame = new Frame( "WebRemoteControl" )
    frame.setLayout(new GridLayout(8, 1))
    frame.add(new Label(infos))
    frame.add(new Label(s"Listening on http://localhost:$httpServerPort"))
    frame.add(new Label(s"Websocket on port $webSocketPort"))
    if (urlho != "") frame.add(new Button(s"Show QR code <$urlho>") { addActionListener((_: ActionEvent) => showQRCode(frame, urlho) ) })
    if (urlip != "") frame.add(new Button(s"Show QR code <$urlip>") { addActionListener((_: ActionEvent) => showQRCode(frame, urlip) ) })
    frame.add(new Label(s"Optional config file:"))
    frame.add(new Label(Settings.configfile.toString))
    frame.add(new Button("Quit") { addActionListener((_: ActionEvent) => System.exit(0)) })
    frame.addWindowListener( new WindowAdapter() {
      override def windowClosing(e : WindowEvent){ System.exit(0) }
      override def windowOpened(e: WindowEvent): Unit = {
        new SimpleHttpServer(httpServerPort).start()
        WebSocketImpl.DEBUG = false
        new WebRemoteControlServer(webSocketPort).start()
      }
    })
    frame.setSize(400, 300)
    frame.pack()
    frame.setVisible(true)
  }
}

class WebRemoteControlServer(port: Int) extends WebSocketServer(new InetSocketAddress(port)) with LazyLogging {

  private val socketInstruct: SocketInstruct = new SocketInstruct()

  override def onOpen(conn: WebSocket, handshake: ClientHandshake) {
    logger.info(s"${conn.getRemoteSocketAddress.getAddress.getHostAddress} connected! :)")
    Settings.ini()
    conn.send("cmdlist," + WebRemoteControl.urls.keySet.mkString(","))
  }

  override def onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
    logger.info(s"${conn.getRemoteSocketAddress.getAddress.getHostAddress } disconnected! :(")
  }

  override def onMessage(conn: WebSocket, message: String) {
    socketInstruct.instruct(message, conn)
  }

  override def onError(conn: WebSocket, ex: Exception) {
    ex.printStackTrace()
    if (conn != null) {
    }
  }

  override def onStart(): Unit = {
    logger.info(s"webserver started!")
  }
}
