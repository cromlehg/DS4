package models.daos

import slick.driver.JdbcProfile
import slick.lifted.ProvenShape.proveShapeOf

trait DBTableDefinitions {

  protected val driver: JdbcProfile
  import driver.api._

  //  case class DBPath(
  //      id: Long,
  //      startLongitude: Double,
  //      startLatitude: Double,
  //      endLongitude: Double,
  //      endLatitude: Double)

  case class DBVideo(
    id: Long,
    pathId: Long,
    link: String)

  case class DBSegment(
    id: Long,
    startLongitude: Double,
    startLatitude: Double,
    endLongitude: Double,
    endLatitude: Double)

  case class DBSegmentVideoContext(
    segmentId: Long,
    videoId: Long,
    startTime: Double,
    endTime: Double)

  case class DBPath(
    id: Long,
    descr: String)

  case class DBPathSegment(
    pathId: Long,
    segmentId: Long,
    order: Int)

  case class DBMedia(
    id: Long,
    ownerId: Option[Long],
    path: String,
    mimeType: Option[String],
    created: Long)

  case class DBUser(
    id: Long,
    email: String,
    hash: Option[String],
    avatarId: Option[Long],
    userStatusId: Int,
    accountStatusId: Int,
    name: Option[String],
    surname: Option[String],
    timezoeId: Int,
    registered: Long)

  case class DBSession(
    id: Long,
    userId: Long,
    suid: String)

  case class DBUserRole(
    userId: Long,
    role: String)

  case class DBParam(
    id: Long,
    name: String,
    value: String)

  case class DBTimezone(
    id: Long,
    timezone: String)

  //  class Pathes(tag: Tag) extends Table[DBPath](tag, "pathes") {
  //    def id = column[Long]("id", O.PrimaryKey)
  //    def startLongitude = column[Double]("start_longitude")
  //    def startLatitude = column[Double]("start_latitude")
  //    def endLongitude = column[Double]("end_longitude")
  //    def endLatitude = column[Double]("end_latitude")
  //    def * = (id, startLongitude, startLatitude, endLongitude, endLatitude) <> (DBPath.tupled, DBPath.unapply)
  //  }

  class Segments(tag: Tag) extends Table[DBSegment](tag, "segments") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def startLongitude = column[Double]("start_longitude")
    def startLatitude = column[Double]("start_latitude")
    def endLongitude = column[Double]("end_longitude")
    def endLatitude = column[Double]("end_latitude")
    def * = (id, startLongitude, startLatitude, endLongitude, endLatitude) <> (DBSegment.tupled, DBSegment.unapply)
  }

  class Video(tag: Tag) extends Table[DBVideo](tag, "video") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def pathId = column[Long]("path_id")
    def link = column[String]("link")
    def * = (id, pathId, link) <> (DBVideo.tupled, DBVideo.unapply)
  }

  class SegmentsVideoContexts(tag: Tag) extends Table[DBSegmentVideoContext](tag, "segments_video_contexts") {
    def segmentId = column[Long]("segment_id")
    def videoId = column[Long]("video_id")
    def startTime = column[Double]("start_time")
    def endTime = column[Double]("end_time")
    def pk_a = primaryKey("pk_a", (segmentId, videoId))
    def * = (segmentId, videoId, startTime, endTime) <> (DBSegmentVideoContext.tupled, DBSegmentVideoContext.unapply)
  }

  class Pathes(tag: Tag) extends Table[DBPath](tag, "pathes") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def descr = column[String]("descr")
    def * = (id, descr) <> (DBPath.tupled, DBPath.unapply)
  }

  class PathSegments(tag: Tag) extends Table[DBPathSegment](tag, "path_segments") {
    def pathId = column[Long]("path_id")
    def segmentId = column[Long]("segment_id")
    def order = column[Int]("order")
    def pk_b = primaryKey("pk_b", (pathId, segmentId))
    def * = (pathId, segmentId, order) <> (DBPathSegment.tupled, DBPathSegment.unapply)
  }

  class Media(tag: Tag) extends Table[DBMedia](tag, "media") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def ownerId = column[Option[Long]]("owner_id")
    def path = column[String]("path")
    def mimeType = column[Option[String]]("mime_type")
    def created = column[Long]("created")
    def * = (id,
      ownerId,
      path,
      mimeType,
      created) <> (DBMedia.tupled, DBMedia.unapply)
  }

  class Users(tag: Tag) extends Table[DBUser](tag, "users") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def email = column[String]("email")
    def hash = column[Option[String]]("hash")
    def avatarId = column[Option[Long]]("avatar_id")
    def userStatusId = column[Int]("user_status_id")
    def accountStatusId = column[Int]("account_status_id")
    def name = column[Option[String]]("name")
    def surname = column[Option[String]]("surname")
    def timezoneId = column[Int]("timezone_id")
    def registered = column[Long]("registered")
    def * = (id,
      email,
      hash,
      avatarId,
      userStatusId,
      accountStatusId,
      name,
      surname,
      timezoneId,
      registered) <> (DBUser.tupled, DBUser.unapply)
  }

  class Sessions(tag: Tag) extends Table[DBSession](tag, "sessions") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def userId = column[Long]("user_id")
    def suid = column[String]("suid")
    def * = (id, userId, suid) <> (DBSession.tupled, DBSession.unapply)
  }

  class UserRoles(tag: Tag) extends Table[DBUserRole](tag, "user_roles") {
    def userId = column[Long]("user_id")
    def role = column[String]("role")
    def * = (userId, role) <> (DBUserRole.tupled, DBUserRole.unapply)
  }

  class Params(tag: Tag) extends Table[DBParam](tag, "params") {
    def id = column[Long]("id")
    def name = column[String]("name")
    def value = column[String]("value")
    def * = (id, name, value) <> (DBParam.tupled, DBParam.unapply)
  }

  val pathes = TableQuery[Pathes]

  val pathSegments = TableQuery[PathSegments]

  val segments = TableQuery[Segments]

  val video = TableQuery[Video]

  val segmentsVideoContexts = TableQuery[SegmentsVideoContexts]

  val media = TableQuery[Media]

  val users = TableQuery[Users]

  val sessions = TableQuery[Sessions]

  val userRoles = TableQuery[UserRoles]

  val params = TableQuery[Params]

}

