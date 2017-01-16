import java.net.InetSocketAddress

import com.typesafe.scalalogging.LazyLogging
import org.java_websocket.WebSocket
import org.java_websocket.WebSocketImpl
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer

object WebRemoteControl extends LazyLogging {

  def main(args: Array[String]) {
    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG")
    logger.info(s"Starting ${buildinfo.BuildInfo.name} ${buildinfo.BuildInfo.version} built ${buildinfo.BuildInfo.buildTime}")
    val simpleHttpServerPort = 8000
    val webSocketPort = 8001
    new SimpleHttpServer(simpleHttpServerPort).start()
    WebSocketImpl.DEBUG = false
    val webRemoteControl = new WebRemoteControl(webSocketPort)
    webRemoteControl.start()
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
