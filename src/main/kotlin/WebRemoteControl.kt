@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE") // needed to use com.apple.eawt, very strange

import io.javalin.Javalin
import io.javalin.http.staticfiles.Location
import io.javalin.websocket.WsContext
import mu.KotlinLogging
import net.glxn.qrgen.core.image.ImageType
import net.glxn.qrgen.javase.QRCode
import org.eclipse.jetty.websocket.api.Session
import org.imgscalr.Scalr
import java.awt.*
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.io.ByteArrayInputStream
import java.io.File
import java.net.InetAddress
import java.util.concurrent.ConcurrentHashMap
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JFrame
import javax.swing.JLabel
import kotlin.math.max
import kotlin.system.exitProcess


data class Collaboration(var doc: String = "", val sessions: MutableSet<Session> = ConcurrentHashMap.newKeySet())
val WsContext.docId: String get() = this.pathParam("doc-id")
private val logger = KotlinLogging.logger {} // after set properties!

lateinit var picLabel: JLabel

object ImageViewer {
    var isShown = false
    var shownImgeFilename = ""

    private lateinit var iframe: JFrame
    private var width: Int = 0
    private var height: Int = 0
    private lateinit var imageIcon: ImageIcon
    private lateinit var lab: JLabel
    private const val fullscreen = true // for debugging

    fun show() {
        if (isShown) return
        iframe = JFrame("Image viewer")
        picLabel = JLabel() //Create the Label to display the picture
        iframe.add(picLabel)
        iframe.isVisible = true
        iframe.contentPane.background = Color.BLACK
        if (fullscreen) {
            val sd = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice
            if (Helpers.isMac()) {
                // com.apple.eawt.FullScreenUtilities.setWindowCanFullScreen(iframe.rootPane,true)
                com.apple.eawt.Application.getApplication().requestToggleFullScreen(iframe)
            } else {
                if (sd.isFullScreenSupported) {
                    sd.fullScreenWindow = iframe
                } else {
                    logger.error("Full screen not supported")
                    iframe.setSize(800, 600) // just something to let you see the window
                    iframe.isVisible = true
                }
            }
        } else {
            iframe.minimumSize = Dimension(800,600)
        }
        val screenSize = Toolkit.getDefaultToolkit().screenSize
        width = screenSize.getWidth().toInt()
        height = screenSize.getHeight().toInt()
        imageIcon = ImageIcon()
        lab = JLabel(imageIcon) // variable needed to update images
        iframe.add(lab)
        isShown = true
    }
    fun hide() {
        if (!isShown) return
        iframe.dispose()
        isShown = false
    }
    fun showImage(filename: String) {
        if (!isShown) show()
        logger.debug("showing image $filename")
        val start = System.currentTimeMillis()
        val bimg = ImageIO.read(File(filename))
        logger.debug("XX read took ${System.currentTimeMillis()-start}ms")
        val iwidth = bimg.width
        val iheight = bimg.height
        val fact = max(iwidth.toDouble() / width, iheight.toDouble() / height)
        imageIcon.image = Scalr.resize(bimg, Scalr.Method.AUTOMATIC, (iwidth/fact).toInt(), (iheight/fact).toInt())
        logger.debug("XX read+scale took ${System.currentTimeMillis()-start}ms")
        lab.repaint() // update image!
        logger.debug("XX read+scale+repaint took ${System.currentTimeMillis()-start}ms")
        shownImgeFilename = filename
    }
}

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
                layout = BorderLayout()
                add(can, BorderLayout.CENTER)
                add(object : Button("Close") { init {
                    addActionListener { getParent().isVisible = false }
                } }, BorderLayout.SOUTH)
            }
        }
        dia.pack()
        dia.isVisible = true
    }

    private fun getHostName(sa: java.net.SocketAddress?): String {
        return if (sa != null) when (sa) {
            is java.net.InetSocketAddress -> sa.hostName
            else -> sa.toString()
        } else "null"
    }

    // https://github.com/tipsy/javalin-realtime-collaboration-example
    fun startServer() {
        val collaborations = ConcurrentHashMap<String, Collaboration>() // can be removed
        val socketInstruct = SocketInstruct()

        val jl = Javalin.create { config ->
            config.staticFiles.add("/public", Location.CLASSPATH)
            config.staticFiles.add("/META-INF/resources", Location.CLASSPATH) // for hammer
        }.apply {
            ws("/docs/{doc-id}") { ws ->
                ws.onConnect { ctx ->
                    logger.info("${getHostName(ctx.session.remoteAddress)} connected docId=${ctx.docId} !")
                    if (collaborations[ctx.docId] == null) {
                        collaborations[ctx.docId] = Collaboration()
                    }
                    collaborations[ctx.docId]!!.sessions.add(ctx.session)
                    ctx.send("cmdlist\t" + urls.keys.joinToString("\t"))
                }
                ws.onMessage { ctx ->
                    logger.info("${getHostName(ctx.session.remoteAddress)} docId=${ctx.docId} msg: ${ctx.message()}")
                    socketInstruct.instruct(ctx.message(), ctx)
                }
                ws.onClose { ctx ->
                    logger.info("${getHostName(ctx.session.remoteAddress)} closed docId=${ctx.docId} !")
                    collaborations[ctx.docId]!!.sessions.remove(ctx.session)
                }
            }
        }
        logger.info("Starting server...")
        jl.start(httpServerPort)
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
        frame.add(object : Button("Show QR code <$urlho>") { init { addActionListener{ showQRCode(frame, urlho)}}})
        frame.add(object : Button("Show QR code <$urlip>") { init { addActionListener{ showQRCode(frame, urlip)}}})
        frame.add(Label("Optional config file:"))
        frame.add(Label(Settings.getSettingsFile().toString()))
        frame.add(object : Button("Quit") { init { addActionListener{ exitProcess(0) }}})
        frame.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e : WindowEvent) { exitProcess(0) }
            override fun windowOpened(e: WindowEvent) { startServer() }
        })
        frame.setSize(400, 300)
        frame.pack()
        frame.isVisible = true

//        ImageViewer.show()
//        ImageViewer.showImage("/Users/wolle/Pictures/IMG_0606.JPG")
//        ImageViewer.showImage("/Users/wolle/Pictures/IMG_0607.JPG")
//        ImageViewer.showImage("/Users/wolle/Pictures/IMG_0608.JPG")

    }
}