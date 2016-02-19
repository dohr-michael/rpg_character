package utils.security

import play.api.mvc._
import utils.services._

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author michaeldohr
  * @since 16/02/16
  */
trait Auth0SecuredApi extends Auth0SecurityComponent {
  self: ServiceController =>

  implicit val ec: ExecutionContext

  implicit def request2profile[A](req: Auth0AuthenticatedRequest[A]): Auth0UserProfile = {
    req.user
  }

  case class Auth0AuthenticatedRequest[A](user: Auth0UserProfile, request: Request[A]) extends WrappedRequest[A](request)

  case object Auth0SecuredAction extends ActionRefiner[Request, Auth0AuthenticatedRequest] {
    override protected def refine[A](request: Request[A]): Future[Either[Result, Auth0AuthenticatedRequest[A]]] = {
      request.headers.get("Authorization").fold {
        ServiceResult.failedF[Auth0UserProfile](InvalidRequestError(Seq("`Authorization` header not found")))
      } { header =>
        val bearerToken: String = "Bearer "
        if (!header.startsWith(bearerToken)) {
          ServiceResult.failedF[Auth0UserProfile](InvalidRequestError(Seq(s"Bad Authorization header, don't start by `$bearerToken`")))
        } else {
          securityService.validate(header.substring(bearerToken.length))
        }
      }.map {
        case ServiceSuccess(su, _) => Right(Auth0AuthenticatedRequest(su, request))
        case ServiceFailure(se) => Left(se: Result)
      }
    }
  }

}
