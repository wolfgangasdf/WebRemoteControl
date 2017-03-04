import java.io.File

object FileBrowser {
  val f = new File(Settings.settings.getProperty("lastfolder", "/"))
  var currentFolder: File = if (f.exists() && f.isDirectory) f else new File("/")
  var currentFiles: List[File] = _

  def updateFiles(folder: File = null): Unit = {
    if (folder != null) {
      currentFolder = folder
      Settings.settings.put("lastfolder", currentFolder.getAbsolutePath)
      Settings.save()
    }
    currentFiles = currentFolder.listFiles.toList
  }
  def getFiles: String = "fblist," + currentFiles.map(f => f.getName + (if (f.isDirectory) "/" else "")).mkString(",")
  def goUp(): Unit = updateFiles(currentFolder.getParentFile)

  updateFiles()

}
