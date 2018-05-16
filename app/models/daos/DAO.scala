package models.daos

import scala.concurrent.Future

import javax.inject.Inject
import models.AccountStatus
import models.Param
import models.Timezone
import models.User
import models.UserStatus
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import controllers.MediaUtils
import models.UserStatus

/**
 *
 * Queries with SlickBUG should be replace leftJoin with for comprehesive. Bug:
 * "Unreachable reference to after resolving monadic joins"
 *
 */

// inject this 
// conf: play.api.Configuration, 
// and then get conf value
// conf.underlying.getString(Utils.meidaPath)
class DAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider, mediaUtils: MediaUtils) extends DAOSlick {

  val pageSize = 10

  import driver.api._

  val timezones = Map(1 -> "Europe/Moscow")

  val accountStatuses = Map(1 -> models.AccountStatus.ACTIVE, 2 -> models.AccountStatus.LOCKED)

  val userStatuses = Map(1 -> models.UserStatus.ONLINE, 2 -> models.UserStatus.OFFLINE)

  def param(name: String) =
    db.run(params.filter(_.name === name).result.headOption).map(_.map(p => new Param(p.id, p.name, p.value)))

  def getUsersPage(pageId: Long): Future[Seq[models.User]] = {
    db.run(users.sortBy(_.id.desc).drop(if (pageId > 0) pageSize * (pageId - 1) else 0).take(pageSize).result).map(_.map {
      case (dbUser) => userFrom(dbUser)
    })
  }

  def findMediaByIdWthoutOwner(id: Long) =
    db.run(media.filter(_.id === id).result.headOption).map(_.map { m =>
      new models.Media(mediaUtils.getFilesFolderAbs, m.id, None, m.path, m.mimeType, m.created)
    })

  def findUserById(id: Long) =
    getUserFromQuery(users.filter(_.id === id))

  def findUserByEmail(email: String): Future[Option[User]] =
    getUserFromQuery(users.filter(_.email === email))

  def findUserBySUIDAndSessionId(sessionId: Long, suid: String): Future[Option[User]] = {
    val query = for {
      dbSession <- sessions.filter(t => t.id === sessionId && t.suid === suid)
      dbUser <- users.filter(_.id === dbSession.userId)
    } yield (dbUser, dbSession)
    db.run(query.result.headOption).map(_.map {
      case (dbUser, dbSession) =>
        new User(
          dbUser.id,
          dbUser.email,
          dbUser.hash,
          Some(new models.Session(dbSession.id, dbSession.userId, dbSession.suid)),
          List(),
          None,
          new UserStatus(dbUser.userStatusId, userStatuses(dbUser.userStatusId)),
          new AccountStatus(dbUser.accountStatusId, accountStatuses(dbUser.accountStatusId)),
          dbUser.name,
          dbUser.surname,
          new Timezone(dbUser.timezoeId, timezones(dbUser.timezoeId)),
          dbUser.registered)
    })
  }

  def getUserFromQuery(query: Query[(Users), (DBUser), Seq]) =
    db.run(query.result.headOption).map(_.map(userFrom))

  def userFrom(dbUser: DBUser) =
    new User(
      dbUser.id,
      dbUser.email,
      dbUser.hash,
      None,
      List(),
      None,
      new UserStatus(dbUser.userStatusId, userStatuses(dbUser.userStatusId)),
      new AccountStatus(dbUser.accountStatusId, accountStatuses(dbUser.accountStatusId)),
      dbUser.name,
      dbUser.surname,
      new Timezone(dbUser.timezoeId, timezones(dbUser.timezoeId)),
      dbUser.registered)

  def findUserWithRolesById(userId: Long): Future[Option[User]] =
    updateUserWithRoles(findUserById(userId))

  def updateUserWithRoles(futureOptUser: Future[Option[User]]): Future[Option[User]] =
    futureOptUser flatMap {
      case Some(u) =>
        findRolesByUserId(u.id).map { r =>
          Some(
            new User(
              u.id,
              u.email,
              u.hash,
              u.session,
              r.toList,
              None,
              u.userStatus,
              u.accountStatus,
              u.name,
              u.surname,
              u.timezone,
              u.registered))

        }
      case None => Future(None)
    }

  def findUserWithRolesByMIXSUID(sessionId: Long, suid: String): Future[Option[User]] =
    updateUserWithRoles(findUserBySUIDAndSessionId(sessionId, suid))

  def findRolesByUserId(userId: Long) =
    db.run(userRoles.filter(_.userId === userId).result).map(_.map(_.role))

  def deleteAllSessionsForUser(userId: Long) =
    db.run(sessions.filter(_.userId === userId).delete)

  def deleteAllSessionsForUserExcludingOne(userId: Long, sessionId: Long) =
    db.run(sessions.filter(t => t.userId === userId && t.id =!= sessionId).delete)

  def createSessionAndDisposeAllPervious(userId: Long, suid: String): Future[Option[models.Session]] =
    createSession(userId, suid).flatMap { t =>
      t match {
        case Some(s) => deleteAllSessionsForUserExcludingOne(userId, s.id).map { case y => t }
        case None    => Future(None)
      }
    }

  def createSession(userId: Long, suid: String): Future[Option[models.Session]] = {
    val query = for {
      dbSession <- (sessions returning sessions.map(_.id) into ((v, id) => v.copy(id = id))) += DBSession(0, userId, suid)
    } yield dbSession
    db.run(query.transactionally) map (s => Some(new models.Session(s.id, s.userId, s.suid)))
  }

  def insertMedia(m: models.Media): Future[Option[models.Media]] = {
    val query = for {
      dbMedia <- (media returning media.map(_.id) into ((v, id) => v.copy(id = id))) += new DBMedia(
        m.id, m.owner.flatMap { t => Some(t.id) }, m.path, m.mimeType, m.created)
    } yield dbMedia
    db.run(query.transactionally) map (s =>
      Some(new models.Media(mediaUtils.getFilesFolderAbs, s.id, None, s.path, s.mimeType, s.created)))
  }

  def getAllPathes: Future[Seq[models.Path]] = {
    val query = for {
      dbPath <- pathes
    } yield (dbPath)
    db.run(query.result).map(_.map {
      case (dbPath) =>
        new models.Path(dbPath.id, dbPath.descr)
    })
  }

  def getFullVideosForPathId(pathId: Long): Future[Seq[models.Video]] =
    db.run(video.filter(_.pathId === pathId).result).map(_.map { v =>
      new models.Video(v.id, v.pathId, v.link)
    })

  def updatePathesWithSegments(pathesFut: Future[Seq[models.Path]]): Future[Seq[models.Path]] =
    pathesFut flatMap { pathes =>
      Future.sequence {
        pathes.map { path =>
          findSegmentsByPathId(path.id).map { segments =>
            new models.Path(path.id,
              path.descr,
              segments)
          }
        }
      }
    }

  def findSegmentsByPathId(pathId: Long): Future[Seq[models.Segment]] = {
    val query = for {
      dbPathSegment <- pathSegments.filter(_.pathId === pathId)
      dbSegment <- segments.filter(_.id === dbPathSegment.segmentId)
    } yield (dbSegment)
    db.run(query.result).map(_.map {
      case (dbSegment) =>
        new models.Segment(dbSegment.id,
          dbSegment.startLongitude,
          dbSegment.startLatitude,
          dbSegment.endLongitude,
          dbSegment.endLatitude)
    })
  }

  def getPathesWithSegments = updatePathesWithSegments(getAllPathes)

  def savePath(descr: String): Future[Option[models.Path]] = {
    val query = for {
      dbPath <- (pathes returning pathes.map(_.id) into ((v, id) => v.copy(id = id))) += DBPath(0, descr)
    } yield dbPath
    db.run(query.transactionally) map { p =>
      Some(new models.Path(p.id, p.descr))
    }
  }

  def savePathSegments(path: models.Path, segments: Seq[models.Segment]): Future[Seq[models.PathSegment]] = {
    val action = pathSegments ++= (for (i <- 0 to segments.length - 1) yield new DBPathSegment(path.id, segments(i).id, i))
    val query = for {
      dbPathSegments <- pathSegments.filter(_.pathId === path.id)
    } yield dbPathSegments
    db.run(action.transactionally) flatMap (r =>
      db.run(query.result) map (_ map { ps =>
        new models.PathSegment(ps.pathId, ps.segmentId, ps.order)
      }))
  }

  def saveVideoWithPathWithSegments(link: String, descr: String, points: Array[Array[Double]]): Future[Option[models.Video]] =
    savePathWithSegments(descr, points) flatMap { segments =>
      if (segments.isEmpty) Future(None) else saveVideo(segments.last.pathId, link)
    }

  def savePathWithSegments(descr: String, points: Array[Array[Double]]): Future[Seq[models.PathSegment]] =
    saveSegments(points) flatMap { segments =>
      savePath(descr) flatMap (_ match {
        case Some(path) => savePathSegments(path, segments)
        case _          => Future(Seq.empty[models.PathSegment])
      })
    }

  def saveVideo(pathId: Long, link: String): Future[Option[models.Video]] = {
    val query = for {
      dbVideo <- (video returning video.map(_.id) into ((v, id) => v.copy(id = id))) += DBVideo(0, pathId, link)
    } yield dbVideo
    db.run(query.transactionally) map { v =>
      Some(new models.Video(v.id, v.pathId, v.link))
    }
  }

  def saveSegments(points: Array[Array[Double]]): Future[Seq[models.Segment]] = {
    val query = for {
      dbSegments <- (segments returning segments.map(_.id) into ((v, id) => v.copy(id = id))) ++= models.Segments(points) map { t =>
        DBSegment(0,
          t.startLongitude,
          t.startLatitude,
          t.endLongitude,
          t.endLatitude)
      }
    } yield dbSegments
    db.run(query.transactionally) map (_ map { s =>
      new models.Segment(s.id,
        s.startLongitude,
        s.startLatitude,
        s.endLongitude,
        s.endLatitude)
    })
  }

}
