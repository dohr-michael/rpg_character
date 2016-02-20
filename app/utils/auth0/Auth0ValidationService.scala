package utils.auth0


import java.util

import com.auth0.jwt.JWTVerifier
import context.Futures
import org.apache.commons.codec.binary.Base64
import utils.services.{ServiceResult, UnauthorizedError}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}


case class Jwt(iss: String, sub: String, aud: String, exp: Int, iat: Int)

trait Auth0ValidationService {

  def validate(token: String): Future[ServiceResult[Auth0SecurityUser]]

}

class DefaultAuth0ValidationService(val clientId: String, val clientSecret: String)(implicit ec: ExecutionContext) extends Auth0ValidationService {

  private val jwtVerifier = new JWTVerifier(new Base64(true).decode(clientSecret), clientId)

  override def validate(token: String): Future[ServiceResult[Auth0SecurityUser]] = {
    Future.successful(
      validateToken(token).map { jwt =>
        Auth0SecurityUser(jwt.sub)
      }
    )
  }


  private def validateToken(jwt: String): ServiceResult[Jwt] = {
    Try(jwtVerifier.verify(jwt)) match {
      case Success(props: util.Map[String, AnyRef]) => ServiceResult.unit(
        Jwt(
          props.get("iss").asInstanceOf[String],
          props.get("sub").asInstanceOf[String],
          props.get("aud").asInstanceOf[String],
          props.get("exp").asInstanceOf[Int],
          props.get("iat").asInstanceOf[Int]
        )
      )
      case err => ServiceResult.failed(UnauthorizedError())
    }
  }
}

trait Auth0SecurityComponent {

  val securityService: Auth0ValidationService
}

trait DefaultAuth0SecurityComponent extends Auth0SecurityComponent {
  self: Auth0Context with Futures =>

  override val securityService: Auth0ValidationService = new DefaultAuth0ValidationService(auth0ClientId, auth0ClientSecret)

}

