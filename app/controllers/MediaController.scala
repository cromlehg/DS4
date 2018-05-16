package controllers

import javax.inject._
import play.api._
import play.api.mvc._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.api.data._
import play.api.data.Form
import play.api.data.Forms.email
import play.api.data.Forms.text
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText

import models.daos.DAO
import scala.util.Random
import play.api.libs.Files.TemporaryFile
import play.api.mvc.MultipartFormData
import org.joda.time.LocalDateTime
import org.joda.time.DateTimeZone
import java.io.File
import org.joda.time.DateTime
import models.Roles

@Singleton
class MediaController @Inject() (override val dao: DAO, mediaUtils: MediaUtils) extends TraitUserServiceWithRoles {

  def list(pageId: Long) = actionClientAsyncUser { implicit user =>
    Future(Ok(views.html.admin.media.list()))
  }

  //  def page(pageNumber: Int, filter: String) = authorizedAction(Permission.administartor) { implicit user =>
  //    implicit request =>
  //      Ok(html.modules.media.filteredPage(Content.page(pageNumber, ApplicationObj.assemblyFilter(filter, "kind" -> Content.KIND_ATTACHMENT): _*)))
  //  }
  //
  //  def remove(id: Long) = authorizedAction(Permission.administartor) { implicit user =>
  //    implicit request =>
  //      Content.delete(id) match {
  //        case 1L => {
  //          FSUtils.removeFileFromPublic(controllers.MediaObj.getAttachmentLink(id))
  //          Redirect(controllers.modules.routes.Media.list).
  //            flashing("success" -> messagesApi("module.media.remove.success", "\"" + id + "\""))
  //        }
  //        case _ => BadRequest
  //      }
  //  }
  //
  //  def create = authorizedAction(Permission.administartor) { implicit user =>
  //    implicit request =>
  //      Ok(html.modules.media.create())
  //  }

  def upload = actionClientAsyncRequestUser(parse.multipartFormData) {
    implicit user =>
      implicit request =>
        request.body.file("file").map { file =>
          val prefRelFilePath = mediaUtils getFileRelPathForNow file.filename
          var relFilePath = prefRelFilePath
          var targetFile = new File(mediaUtils getFileAbsPath relFilePath)
          if (targetFile.exists()) {
            var counter = 1
            while (targetFile.exists()) {
              val startExtIndex = prefRelFilePath.indexOf(".")
              if (startExtIndex == 0) {
                relFilePath = "_" + counter + prefRelFilePath
              } else if (startExtIndex > 0) {
                relFilePath = prefRelFilePath.substring(0, startExtIndex) + "_" + counter + prefRelFilePath.substring(startExtIndex)
              } else {
                relFilePath = prefRelFilePath + "_" + counter
              }
              targetFile = new File(mediaUtils getFileAbsPath relFilePath)
              counter = counter + 1
            }
          }
          file.ref.moveTo(targetFile, true)
          dao.insertMedia(new models.Media(
            mediaUtils.getFilesFolderAbs,
            0,
            Some(user),
            relFilePath,
            file.contentType,
            new DateTime().getMillis() / 1000)) map {
            case Some(media) => Ok(media.id.toString)
            case None        => BadRequest
          }
        }.getOrElse(Future.successful(BadRequest))
  }

}
