
import mu.KotlinLogging


fun main() {

    System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "TRACE")
    System.setProperty("org.slf4j.simpleLogger.log.org.eclipse.jetty", "INFO")
    System.setProperty(org.slf4j.impl.SimpleLogger.SHOW_DATE_TIME_KEY, "true")
    System.setProperty(org.slf4j.impl.SimpleLogger.DATE_TIME_FORMAT_KEY, "yyyy-MM-dd HH:mm:ss:SSS")
    System.setProperty(org.slf4j.impl.SimpleLogger.LOG_FILE_KEY, "System.out") // and use intellij "grep console" plugin

    val logger = KotlinLogging.logger {} // after set properties!

    logger.error("error")
    logger.warn("warn")
    logger.info("info")
    logger.debug("debug")
    logger.trace("trace")

    WebRemoteControl.showGUI()
}

