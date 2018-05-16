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
import javax.security.auth.Subject
import scala.concurrent.ExecutionContext
import models.Roles

trait TraitUserService extends Controller with AuthActionBuilders with AuthConfigImpl {

  val dao: DAO

  def ownerAction(authority: Authority, resOwnerId: Long)(f: Request[AnyContent] => Result)(implicit context: ExecutionContext) =
    AuthorizationAction(authority) { request =>
      if (request.user.id == resOwnerId) f(request) else Forbidden("You have not access to this resource")
    }

  def ownerActionAsync(authority: Authority, resOwnerId: Long)(f: Future[Result])(implicit context: ExecutionContext) =
    AuthorizationAction(authority).async { request =>
      if (request.user.id == resOwnerId) f else Future(Forbidden("You have not access to this resource"))
    }

  def ownerActionAsyncRequest(authority: Authority, resOwnerId: Long)(f: AuthRequest[AnyContent] => Future[Result])(implicit context: ExecutionContext) =
    AuthorizationAction(authority).async { request =>
      if (request.user.id == resOwnerId) f(request) else Future(Forbidden("You have not access to this resource"))
    }

  def ownerActionAsyncUser(authority: Authority, resOwnerId: Long)(f: User => Future[Result])(implicit context: ExecutionContext) =
    AuthorizationAction(authority).async { request =>
      if (request.user.id == resOwnerId) f(request.user) else Future(Forbidden("You have not access to this resource"))
    }

  def ownerActionAsyncRequestUser(authority: Authority, resOwnerId: Long)(f: User => (AuthRequest[AnyContent] => Future[Result]))(implicit context: ExecutionContext) =
    AuthorizationAction(authority).async { request =>
      if (request.user.id == resOwnerId) f.apply(request.user)(request) else Future(Forbidden("You have not access to this resource"))
    }

  def actionAsyncUser(authority: Authority)(f: User => Future[Result])(implicit context: ExecutionContext): Action[AnyContent] =
    actionAsyncUser(BodyParsers.parse.anyContent, authority)(f)

  def actionAsyncUser[A](p: BodyParser[A], authority: Authority)(f: User => Future[Result])(implicit context: ExecutionContext) =
    AuthorizationAction(authority).async(p) { r => f(r.user) }

  def actionAsyncRequestUser(authority: Authority)(f: User => (AuthRequest[AnyContent] => Future[Result]))(implicit context: ExecutionContext): Action[AnyContent] =
    actionAsyncRequestUser(BodyParsers.parse.anyContent, authority)(f)

  def actionAsyncRequestUser[A](p: BodyParser[A], authority: Authority)(f: User => (AuthRequest[A] => Future[Result]))(implicit context: ExecutionContext) =
    AuthorizationAction(authority).async(p) { r => f.apply(r.user)(r) }

  def actionAsyncRequest[A](p: BodyParser[A], authority: Authority)(f: AuthRequest[A] => Future[Result])(implicit context: ExecutionContext): Action[A] =
    AuthorizationAction(authority).async(p)(f)

  def actionAsyncRequest(authority: Authority)(f: AuthRequest[AnyContent] => Future[Result])(implicit context: ExecutionContext): Action[AnyContent] =
    actionAsyncRequest(BodyParsers.parse.anyContent, authority)(f)(context)

}

trait TraitUserServiceWithRoles extends TraitUserService {

  def ownerAdminAction(resOwnerId: Long)(f: Request[AnyContent] => Result)(implicit context: ExecutionContext) =
    ownerAction(Roles.ADMIN, resOwnerId)(f)(context)

  def ownerClientAction(resOwnerId: Long)(f: Request[AnyContent] => Result)(implicit context: ExecutionContext) =
    ownerAction(Roles.CLIENT, resOwnerId)(f)(context)

  def ownerAdminActionAsync(resOwnerId: Long)(f: Future[Result])(implicit context: ExecutionContext) =
    ownerActionAsync(Roles.ADMIN, resOwnerId)(f)(context)

  def ownerClientActionAsync(resOwnerId: Long)(f: Future[Result])(implicit context: ExecutionContext) =
    ownerActionAsync(Roles.CLIENT, resOwnerId)(f)(context)

  def ownerAdminActionAsyncRequest(resOwnerId: Long)(f: AuthRequest[AnyContent] => Future[Result])(implicit context: ExecutionContext) =
    ownerActionAsyncRequest(Roles.ADMIN, resOwnerId)(f)(context)

  def ownerClientActionAsyncRequest(resOwnerId: Long)(f: AuthRequest[AnyContent] => Future[Result])(implicit context: ExecutionContext) =
    ownerActionAsyncRequest(Roles.CLIENT, resOwnerId)(f)(context)

  def ownerAdminActionAsyncUser(resOwnerId: Long)(f: User => Future[Result])(implicit context: ExecutionContext) =
    ownerActionAsyncUser(Roles.ADMIN, resOwnerId)(f)(context)

  def ownerClientActionAsyncUser(resOwnerId: Long)(f: User => Future[Result])(implicit context: ExecutionContext) =
    ownerActionAsyncUser(Roles.CLIENT, resOwnerId)(f)(context)

  def ownerAdminActionAsyncRequestUser(resOwnerId: Long)(f: User => (AuthRequest[AnyContent] => Future[Result]))(implicit context: ExecutionContext) =
    ownerActionAsyncRequestUser(Roles.ADMIN, resOwnerId)(f)(context)

  def ownerClientActionAsyncRequestUser(resOwnerId: Long)(f: User => (AuthRequest[AnyContent] => Future[Result]))(implicit context: ExecutionContext) =
    ownerActionAsyncRequestUser(Roles.CLIENT, resOwnerId)(f)(context)

  def actionAdminAsyncUser(f: User => Future[Result])(implicit context: ExecutionContext): Action[AnyContent] =
    actionAsyncUser(Roles.ADMIN)(f)(context)

  def actionAdminAsyncUser[A](p: BodyParser[A])(f: User => Future[Result])(implicit context: ExecutionContext) =
    actionAsyncUser(p, Roles.ADMIN)(f)(context)

  def actionAdminAsyncRequestUser(f: User => (AuthRequest[AnyContent] => Future[Result]))(implicit context: ExecutionContext): Action[AnyContent] =
    actionAsyncRequestUser(Roles.ADMIN)(f)(context)

  def actionAdminAsyncRequestUser[A](p: BodyParser[A])(f: User => (AuthRequest[A] => Future[Result]))(implicit context: ExecutionContext) =
    actionAsyncRequestUser(p, Roles.ADMIN)(f)(context)

  def actionClientAsyncUser(f: User => Future[Result])(implicit context: ExecutionContext): Action[AnyContent] =
    actionAsyncUser(Roles.CLIENT)(f)(context)

  def actionClientAsyncUser[A](p: BodyParser[A])(f: User => Future[Result])(implicit context: ExecutionContext) =
    actionAsyncUser(p, Roles.CLIENT)(f)(context)

  def actionClientAsyncRequestUser(f: User => (AuthRequest[AnyContent] => Future[Result]))(implicit context: ExecutionContext): Action[AnyContent] =
    actionAsyncRequestUser(Roles.CLIENT)(f)(context)

  def actionClientAsyncRequestUser[A](p: BodyParser[A])(f: User => (AuthRequest[A] => Future[Result]))(implicit context: ExecutionContext) =
    actionAsyncRequestUser(p, Roles.CLIENT)(f)(context)

  def actionAdminAsyncRequest[A](p: BodyParser[A])(f: AuthRequest[A] => Future[Result])(implicit context: ExecutionContext): Action[A] =
    actionAsyncRequest(p, Roles.ADMIN)(f)(context)

  def actionAdminAsyncRequest(f: AuthRequest[AnyContent] => Future[Result])(implicit context: ExecutionContext): Action[AnyContent] =
    actionAsyncRequest(BodyParsers.parse.anyContent, Roles.ADMIN)(f)(context)

  def actionClientAsyncRequest[A](p: BodyParser[A])(f: AuthRequest[A] => Future[Result])(implicit context: ExecutionContext): Action[A] =
    actionAsyncRequest(p, Roles.ADMIN)(f)(context)

  def actionClientAsyncRequest(f: AuthRequest[AnyContent] => Future[Result])(implicit context: ExecutionContext): Action[AnyContent] =
    actionAsyncRequest(BodyParsers.parse.anyContent, Roles.ADMIN)(f)(context)

}
