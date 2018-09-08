import io.javalin.Javalin
import io.javalin.websocket.WsSession
import mu.KotlinLogging
import net.glxn.qrgen.core.image.ImageType
import net.glxn.qrgen.javase.QRCode
import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.ByteArrayInputStream
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO


data class Collaboration(var doc: String = "", val sessions: MutableSet<WsSession> = ConcurrentHashMap.newKeySet())
val WsSession.docId: String get() = this.pathParam("doc-id")
private val logger = KotlinLogging.logger {} // after set properties!

object WebRemoteControl {
    var httpServerPort = 8000
    val urls = LinkedHashMap<String, String>()

    fun showQRCode(parent: Frame, s: String) {
        val qrdim = 250
        val ba = QRCode.from(s).to(ImageType.PNG).withSize(qrdim, qrdim).stream().toByteArray()
        val bi= ImageIO.read(ByteArrayInputStream(ba))
        val can = object : Canvas() {
            override fun paint(g: Graphics) = let { g.drawImage(bi, 0, 0, this) ; Unit}
            init { setSize(qrdim, qrdim) }
        }

        val dia = object : Dialog(parent, "QR code", true) {
            init {
                layout = java.awt.BorderLayout()
                add(can, BorderLayout.CENTER)
                add(object : Button("Close") { init {
                    addActionListener { _ -> getParent().isVisible = false }
                } }, BorderLayout.SOUTH)
            }
        }
        dia.pack()
        dia.isVisible = true
    }

    // https://github.com/tipsy/javalin-realtime-collaboration-example
    fun startServer() {
        val collaborations = ConcurrentHashMap<String, Collaboration>() // can be removed
        val socketInstruct = SocketInstruct()

        val jl = Javalin.create().apply {
            port(httpServerPort)
            enableStaticFiles("/public")
            enableStaticFiles("/META-INF/resources") // for hammer
            ws("/docs/:doc-id") { ws ->
                ws.onConnect { session ->
                    logger.info("${session.remoteAddress.hostName} connected docId=${session.docId} !")
                    session.idleTimeout = 1000
                    if (collaborations[session.docId] == null) {
                        collaborations[session.docId] = Collaboration()
                    }
                    collaborations[session.docId]!!.sessions.add(session)
                    //session.send(collaborations[session.docId]!!.doc)
                    session.send("cmdlist," + WebRemoteControl.urls.keys.joinToString(","))
                }
                ws.onMessage { session, message ->
                    if (message != "keepalive") logger.info("${session.remoteAddress.hostName} docId=${session.docId} msg: $message")
                    socketInstruct.instruct(message, session)
                }
                ws.onClose { session, _, _ ->
                    logger.info("${session.remoteAddress.hostName} closed docId=${session.docId} !")
                    collaborations[session.docId]!!.sessions.remove(session)
                }
            }
        }
        logger.info("Server started!")
        jl.start()
    }

    fun showGUI() {
        val infos = "WebRemoteControl built ${Helpers.getClassBuildTime().toString()}"
        logger.info("Starting $infos")
        val urlho = "http://${InetAddress.getLocalHost().hostName}:$httpServerPort"
        val urlip = "http://${InetAddress.getLocalHost().hostAddress}:$httpServerPort"

        val frame = Frame( "WebRemoteControl" )
        frame.layout = GridLayout(7, 1)
        frame.add(Label(infos))
        frame.add(Label("Listening on http://localhost:$httpServerPort"))
        if (urlho != "") frame.add(object : Button("Show QR code <$urlho>") { init { addActionListener{ _ -> showQRCode(frame, urlho)}}})
        if (urlip != "") frame.add(object : Button("Show QR code <$urlip>") { init { addActionListener{ _ -> showQRCode(frame, urlip)}}})
        frame.add(Label("Optional config file:"))
        frame.add(Label(Settings.getSettingsFile().toString()))
        frame.add(object : Button("Quit") { init { addActionListener{ _ -> System.exit(0)}}})
        frame.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e : WindowEvent) { System.exit(0) }
            override fun windowOpened(e: WindowEvent) { startServer() }
        })
        frame.setSize(400, 300)
        frame.pack()
        frame.isVisible = true
    }
}