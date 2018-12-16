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
        if (!ff.exists()) {
            props["urls"] = "npo,https://www.npo.nl/mijn_npo#history;netflix,http://netflix.com;youtube,http://youtube.com;southpark,http://southpark.cc.com/full-episodes/random"
            props["httpserverport"] = "8000"
            save()
        }
        props.load(FileInputStream(ff))
        WebRemoteControl.httpServerPort = props.getProperty("httpserverport").toInt()
        WebRemoteControl.urls.clear()
        props.getProperty("urls").split(";").map{
            it -> WebRemoteControl.urls.put(it.split("\t")[0], it.split("\t")[1])
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


    init {
        load()
    }
}
