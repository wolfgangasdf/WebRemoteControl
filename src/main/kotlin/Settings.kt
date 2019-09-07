import mu.KotlinLogging
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

private val logger = KotlinLogging.logger {}

object Settings {

    val props = java.util.Properties()

    fun getSettingsFile(): File {
        val fp = File(".").absoluteFile.parentFile // gets CWD!
        return File(fp.path + File.separator + "webremotecontrol.txt")
    }

    private fun load() {
        val ff = getSettingsFile()
        logger.debug("load config: settings file = " + ff.path)
        if (!ff.exists()) save()
        props.load(FileInputStream(ff))
        props.putIfAbsent("urls", "npo\thttps://www.npo.nl/mijn_npo#history;netflix\thttp://netflix.com;youtube\thttp://youtube.com;southpark\thttp://southpark.cc.com/full-episodes/random")
        props.putIfAbsent("httpserverport", "8000")
        props.putIfAbsent("vlc", "")
        save()

        WebRemoteControl.httpServerPort = props.getProperty("httpserverport").toInt()
        WebRemoteControl.urls.clear()
        props.getProperty("urls").split(";").map{
            WebRemoteControl.urls.put(it.split("\t")[0], it.split("\t")[1])
        }
    }

    fun save() {
        try {
            val ff = getSettingsFile()
            logger.debug("save config: settings file = " + ff.path)
            val fos = FileOutputStream(ff)
            props.store(fos,null)
            fos.close()
        } catch(e: Throwable) {
            logger.debug("got ex",e)
        }
    }

    fun historyAdd(f: File) {
        if (historyGet(0) != f.absolutePath) {
            props["history"] = "${f.absolutePath}\t${historyGet()}".removeSuffix("\t")
            save()
        }
    }

    fun historyDelete(idx: Int) {
        props["history"] = historyGet().split("\t").toMutableList().apply { removeAt(idx) }.joinToString("\t")
        save()
    }

    fun historyGet(): String = props.getProperty("history", "")

    fun historyGet(idx: Int): String = historyGet().split("\t")[idx]

    init {
        load()
    }
}
