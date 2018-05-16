package controllers

import jp.t2v.lab.play2.auth.AsyncAuth
import jp.t2v.lab.play2.auth.LoginLogout
import jp.t2v.lab.play2.auth.AuthElement

import jp.t2v.lab.play2.auth.AuthActionBuilders

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

import org.mindrot.jbcrypt.BCrypt
import controllers.security.AuthConfigImpl
import play.mvc.Http.RequestHeader
import models.User
import models.Roles

@Singleton
class UsersController @Inject() (override val dao: DAO) extends TraitUserServiceWithRoles {

  import scala.concurrent.Future.{ successful => future }

  def list(pageId: Long) = AuthorizationAction(Roles.ADMIN).async { request =>
    implicit val user = request.user
    dao.getUsersPage(pageId).flatMap {
      users => Future(Ok(views.html.admin.users.list(users)))
    }
  }

  def profile(userId: Long) = ownerAdminActionAsyncUser(userId) { implicit user =>
    dao.findUserById(userId) map {
      case Some(viewedUser) => Ok(views.html.admin.users.profile.profile(viewedUser))
      case None             => BadRequest("Пользователь с таким id = " + userId + " не найден")
    }
  }

}
