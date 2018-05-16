package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import models.daos.DAO
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import models.Segment
import models.Segments

class JSONResponse(val status: String, val descr: String = "")

object JSONResponse {

  val STATUS_OK = "ok"

  val STATUS_ERROR = "error"

  def apply(status: String, descr: String) =
    new JSONResponse(status, descr)

  implicit val writes = new Writes[JSONResponse] {
    def writes(r: JSONResponse) = Json.obj(
      "status" -> r.status,
      "descr" -> r.descr)
  }

}

object JSONResponseOk {

  def apply(): JsValue =
    Json.toJson(new JSONResponse(JSONResponse.STATUS_OK))

  def apply(descr: String): JsValue =
    Json.toJson(new JSONResponse(JSONResponse.STATUS_OK, descr))

}

object JSONResponseError {

  def apply(descr: String): JsValue =
    Json.toJson(new JSONResponse(JSONResponse.STATUS_ERROR, descr))

}

@Singleton
class AppController @Inject() (val dao: DAO) extends Controller {

  def newPath = Action.async { implicit request =>
    Future(Ok(views.html.app.newPath()))
  }

  def index = Action.async { implicit request =>
    dao.getPathesWithSegments map (ps => Ok(views.html.app.index(ps)))
  }

  def getVideosForPath(pathId: Long, pid: Int) = Action.async { implicit request =>
    import models.Video.locationWrites
    dao.getFullVideosForPathId(pathId) map (ps => Ok(Json.toJson(ps)))
  }

  def createNewPath = Action.async(parse.json) { request =>
    import JSONResponse.writes
    println(request.body)
    ((request.body \ "link").asOpt[String],
      (request.body \ "points").asOpt[Array[Array[Double]]]) match {
        case (Some(link), Some(points)) =>
          val prepLink: String =
            if (link.trim.length == 11)
              link.trim
            else
              """(.*)?((youtube\.com\/watch\?v=)|(youtu\.be\/)|(youtube\.com\/embed\/))(.{11})"""
                .r
                .findFirstMatchIn(link.trim)
                .map(_ group 6).getOrElse("")
          if (prepLink.length == 11)
            dao.saveVideoWithPathWithSegments(prepLink, "test1", points) map (_ match {
              case Some(video) => Ok(JSONResponseOk("Your path has been saved!"))
              case _           => BadRequest(JSONResponseError("Can't save your video path!"))
            })
          else
            Future(BadRequest(JSONResponseError("Wrong YouTube link")))
        case _ => Future(BadRequest(JSONResponseError("Some problems!")))
      }
  }

}
