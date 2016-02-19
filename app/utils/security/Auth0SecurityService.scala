package utils.security


import java.util

import com.auth0.jwt.JWTVerifier
import context.Futures
import org.apache.commons.codec.binary.Base64
import play.api.libs.json._
import play.api.libs.ws.WSClient
import utils.json.ApiFormat
import utils.services.{ServiceResult, UnauthorizedError}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}


case class Jwt(iss: String, sub: String, aud: String, exp: Int, iat: Int)

trait Auth0SecurityService {

  implicit val dateFormat = ApiFormat.dateTimeFormat
  implicit val userFormat = Json.format[Auth0UserProfile]


  implicit val ec: ExecutionContext
  val wsclient: WSClient

  private lazy val jwtVerifier = new JWTVerifier(
    new Base64(true).decode("m_Xsw8XtHw0URGby6j_5Dmr20YkqwG4LLbV6xkZL9h0hPbvJvzzVA2KI91WxZ3of"),
    "lvMxZADZ47oeRZ0sat0Qw4eXpagLFwWW"
  )

  def validate(token: String): Future[ServiceResult[Auth0UserProfile]] = {
    validateToken(token).flatMapF { jwt =>
      wsclient.url("https://dohrm.eu.auth0.com/tokeninfo")
        .post(Json.obj("id_token" -> token)).map { obj =>
        userFormat.reads(obj.json) match {
          case JsSuccess(r: Auth0UserProfile, _) => ServiceResult.unit(r)
          case _ => ServiceResult.failed(UnauthorizedError())
        }
      }
    }
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

  val securityService: Auth0SecurityService
}

trait DefaultAuth0SecurityComponent extends Auth0SecurityComponent {
  self: Futures =>

  val wsclient: WSClient

  override val securityService: Auth0SecurityService = new DefaultAuth0SecurityService(wsclient)

  class DefaultAuth0SecurityService(override val wsclient: WSClient)(implicit override val ec: ExecutionContext) extends Auth0SecurityService

}

