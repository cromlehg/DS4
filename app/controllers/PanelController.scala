package controllers

import javax.inject._
import play.api._
import play.api.mvc._

import jp.t2v.lab.play2.auth.AsyncAuth
import jp.t2v.lab.play2.auth.LoginLogout
import jp.t2v.lab.play2.auth.AuthElement

import jp.t2v.lab.play2.auth.AuthActionBuilders

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import play.api.data._
import play.api.data.Form
import play.api.data.Forms.email
import play.api.data.Forms.text
import play.api.data.Forms.mapping
import play.api.data.Forms.nonEmptyText

import org.mindrot.jbcrypt.BCrypt

import models.daos.DAO
import scala.util.Random

import org.mindrot.jbcrypt.BCrypt
import controllers.security.AuthConfigImpl
import models.Roles

@Singleton
class PanelController @Inject() (override val dao: DAO) extends TraitUserServiceWithRoles with LoginLogout {

  case class AuthData(val email: String, val pass: String)

  val random = Random

  val authForm = Form(
    mapping(
      "email" -> email,
      "pass" -> text)(AuthData.apply)(AuthData.unapply))

  def login = Action.async {
    Future {
      Ok(views.html.admin.security.login(authForm))
    }
  }

  def panel = AuthorizationAction(Roles.ADMIN).async { request =>
    Future {
      Logger.debug("panel called")
      implicit val user = request.user
      Ok(views.html.admin.index())
    }
  }

  // needs in logout => goToLogoutSucceeded
  
  def auth = Action.async { implicit request =>
    Logger.debug("auth()")
    authForm.bindFromRequest.fold(
      formWithErrors => Future.successful(BadRequest(views.html.admin.security.login(formWithErrors))), { authData =>
        dao.findUserByEmail(authData.email) flatMap {
          case Some(user) => {
            user.hash match {
              case Some(hash) =>
                if (BCrypt.checkpw(authData.pass, hash)) {
                  dao.createSessionAndDisposeAllPervious(user.id, BCrypt.hashpw(random.nextString(19), BCrypt.gensalt())) flatMap {
                    case Some(session) => {
                      gotoLoginSucceeded(session.suid + "_" + session.id)
                      /*Redirect(controllers.routes.PanelController.panel).withSession {
                        AppConstants.COOKIE_SESSION_CONST -> (session.suid + "_" + session.id)
                      }*/
                    }
                    case None => Future(BadRequest(views.html.admin.security.login(authForm)))
                  }
                } else
                  Future(BadRequest(views.html.admin.security.login(authForm)))
              case None => Future(BadRequest(views.html.admin.security.login(authForm)))
            }
          }
          case None => Future(BadRequest(views.html.admin.security.login(authForm)))
        }
      })
  }

}

