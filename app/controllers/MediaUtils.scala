package controllers

import javax.inject.Inject
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime
import java.io.File
import models.daos.DAO

class MediaUtils @Inject() (fsUtils: FSUtils) {

  val ldt = new LocalDateTime(DateTimeZone.UTC)

  val filesFolder = "files"

  def getYearDay = ldt.toString("yyyy" + File.separator + "MM")

  def getFilesFolderAbs = fsUtils.getAbsPathForPublicPath(filesFolder)

  def getFileRelPathForNow(file: String) = getYearDay + File.separator + file

  def getFileAbsPath(file: String) = getFilesFolderAbs + File.separator + file

//  def getAttachmentLink(id: Long): String = dao.findMediaByIdWthoutOwner(id).map(
//    fsUtils.CONTENT_FOLDER + File.separator + filesFolder + File.separator + _.data.getOrElse("")).getOrElse("")
//
//  def getAttachmentLink(id: Option[Long]): String = id.map { lid => getAttachmentLink(lid) }.getOrElse("")

}