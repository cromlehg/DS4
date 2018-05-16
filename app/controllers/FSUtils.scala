package controllers

import java.io.File
import java.nio.file.Paths

class FSUtils {

  val CONTENT_FOLDER = "content"

  def removeFileFromPublic(pathToFileRelPublic: String) = new File(getAbsPathForPublicPath(pathToFileRelPublic)) delete

  def getAbsPathForPublicPath(pathToFileRelPublic: String) =
    Paths.get(classOf[AppController].getResource(File.separator).toURI()).getParent.getParent.getParent.toString +
      File.separator + "target" +
      File.separator + "web" +
      File.separator + "public" +
      File.separator + "main" +
      File.separator + CONTENT_FOLDER +
      File.separator + pathToFileRelPublic

}