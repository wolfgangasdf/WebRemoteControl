import java.net.InetSocketAddress
import org.java_websocket.WebSocket
import org.java_websocket.WebSocketImpl
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer

object WebRemoteControl {

  def main(args: Array[String]) {
    println(s"Starting ${buildinfo.BuildInfo.name} ${buildinfo.BuildInfo.version} built ${buildinfo.BuildInfo.buildTime}")
    val simpleHttpServerPort = 8000
    val webSocketPort = 8001
    new SimpleHttpServer(simpleHttpServerPort).start()
    WebSocketImpl.DEBUG = false
    val webRemoteControl = new WebRemoteControl(webSocketPort)
    webRemoteControl.start()
  }
}

class WebRemoteControl(port: Int) extends WebSocketServer(new InetSocketAddress(port)) {

  private val socketInstruct: SocketInstruct = new SocketInstruct()

  override def onOpen(conn: WebSocket, handshake: ClientHandshake) {
    println(s"\n ${conn.getRemoteSocketAddress.getAddress.getHostAddress} connected! :)")
  }

  override def onClose(conn: WebSocket, code: Int, reason: String, remote: Boolean) {
    println(s"${conn.getRemoteSocketAddress.getAddress.getHostAddress } disconnected! ':(")
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
