import java.io.BufferedInputStream
import java.net.{InetAddress, InetSocketAddress}

import com.sun.net.httpserver.{HttpExchange, HttpHandler, HttpServer}

class SimpleHttpServer(private var port: Int) {

  private class GetHandler extends HttpHandler {

    private def detectContentType(requestURI: String): String = {
      if (requestURI.contains(".html")) return "text/html"
      if (requestURI.contains(".js")) return "text/javascript"
      if (requestURI.contains(".css")) return "text/css"
      if (requestURI.contains(".gif")) return "image/gif"
      if (requestURI.contains(".jpeg")) return "image/jpeg"
      if (requestURI.contains(".png")) return "image/png"
      "text/plain"
    }

    def handle(t: HttpExchange) {
      var requestURI = t.getRequestURI.toString.substring(1)
      if (requestURI.length == 0) requestURI = "index.html"
      val headers = t.getResponseHeaders
      headers.add("Content-Type", detectContentType(requestURI))
      val is = getClass.getResourceAsStream(requestURI)
      if (is != null) {
        val bis = new BufferedInputStream(is)
        val content = try {
          Stream.continually(bis.read).takeWhile(-1 != _).map(_.toByte).toArray
        } finally bis.close()
        t.sendResponseHeaders(200, content.length)
        val os = t.getResponseBody
        os.write(content, 0, content.length)
        os.close()
      } else {
        println("file not found: " + requestURI)
        val response = "File not found :("
        t.sendResponseHeaders(400, -1)
        val os = t.getResponseBody
        os.write(response.getBytes)
        os.close()
      }
    }
  }

  private val server: HttpServer = HttpServer.create(new InetSocketAddress(port), 0)

  server.createContext("/", new GetHandler())

  def start() {
    server.start()
    println(s"Server listening on ${InetAddress.getLocalHost.getHostAddress }:$port")
  }
}
