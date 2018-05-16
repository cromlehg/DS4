package controllers.security

import jp.t2v.lab.play2.auth.AsyncAuth
import jp.t2v.lab.play2.auth.LoginLogout
import play.Logger
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.mvc.Controller
import play.api.mvc.Result
import play.api.mvc.Action
import play.api.mvc.AnyContent
import play.api.mvc.Request
import scala.concurrent.ExecutionContext

import play.api.mvc.Controller

import play.api._
import play.api.mvc._

import javax.inject.Inject
import play.api.i18n.{MessagesApi, I18nSupport}

trait CheckNormalUser extends Controller with AsyncAuth with LoginLogout with AuthConfigImpl with I18nSupport {
/*
  def checkNormalUserAction(authority: Authority, uid: Long)(f: User => (Request[AnyContent] => Result))(implicit context: ExecutionContext): Action[(AnyContent, User)] = {
    authorized(authority)({ implicit user =>
      implicit request =>
        user.permission.name match {
          case Permission.PERMISSION_ADMINISTRATOR => f.apply(user)(request)
          case _ => if (user.id == uid) {
            f.apply(user)(request)
          } else {
            Logger.debug("Access denied for user: " + user.email + " with id = " + user.id)
            Forbidden
          }
        }
    })
  }*/

}