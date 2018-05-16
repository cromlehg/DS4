package controllers.security

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.reflect.ClassTag
import scala.reflect.classTag
import jp.t2v.lab.play2.auth.AuthConfig
import play.api.mvc.RequestHeader
import play.api.mvc.Results.Forbidden
import play.api.mvc.Results.Redirect
import jp.t2v.lab.play2.auth.CookieTokenAccessor
import jp.t2v.lab.play2.auth.TokenAccessor
import play.api.mvc.Controller
import controllers.routes
import play.Logger

object AuthConfigObject {

  val RETURN_URL = "returnURL"

}

trait AuthConfigImpl extends AuthConfig { self: Controller =>

  val dao: models.daos.DAO

  type Id = String

  type User = models.User

  type Authority = String

  val idTag: ClassTag[Id] = classTag[Id]

  val sessionTimeoutInSeconds = 36000

  def resolveUser(id: Id)(implicit ctx: ExecutionContext) = {
    val splitted = id.split("_")
    if (splitted.length == 2)
      (splitted(0), splitted(1).toInt) match {
        case (suid, sid) => dao.findUserWithRolesByMIXSUID(sid, suid)
        case _           => Future(None)
      }
    else
      Future(None)
  }

  def loginSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext) = {
    val uri = request.session.get(AuthConfigObject.RETURN_URL).getOrElse(routes.PanelController.panel.toString())
    Future.successful(Redirect(uri).withSession(request.session - AuthConfigObject.RETURN_URL))
  }

  def logoutSucceeded(request: RequestHeader)(implicit ctx: ExecutionContext) =
    Future.successful(Redirect(routes.PanelController.login))

  def authenticationFailed(request: RequestHeader)(implicit ctx: ExecutionContext) =
    Future.successful(Redirect(routes.PanelController.login).withSession(AuthConfigObject.RETURN_URL -> request.uri))

  def authorizationFailed(request: RequestHeader)(implicit ctx: ExecutionContext) =
    Future.successful(Forbidden("no permission"))

  def authorizationFailed(request: RequestHeader, user: User, authority: Option[Authority])(implicit ctx: ExecutionContext) =
    Future.successful(Forbidden("no permission"))

  def authorize(user: User, authority: Authority)(implicit ctx: ExecutionContext) = Future.successful(user.roles.contains(authority))

  override lazy val tokenAccessor: TokenAccessor = new CookieTokenAccessor(
    cookieName = "PLAY2AUTH_SESS_ID",
    //cookieSecureOption = play.api.Play.isProd(play.api.Play.current),
    cookieSecureOption = false,
    cookieHttpOnlyOption = true,
    cookieDomainOption = None,
    cookiePathOption = "/",
    cookieMaxAge = Some(sessionTimeoutInSeconds))

}
