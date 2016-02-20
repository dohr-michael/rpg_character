package utils.auth0

import context.{Futures, WebServiceClient}
import play.api.libs.json.{JsSuccess, Json}
import play.api.libs.ws.WSClient
import utils.json.ApiFormat
import utils.services.{ServiceResult, UnauthorizedError}

import scala.concurrent.Future

/**
  * @author michaeldohr
  * @since 20/02/16
  */
trait Auth0DetailsApiComponent {

  val auth0Api: Auth0Api

  trait Auth0Api {
    def tokenInfo(token: String)(implicit ws: WSClient): Future[ServiceResult[Auth0UserProfile]]
  }

}

trait DefaultAuth0DetailsApiComponent extends Auth0DetailsApiComponent {
  self: Futures with Auth0Context with WebServiceClient =>

  val auth0TokeninfoEndpoint = s"$auth0Endpoint/tokeninfo"

  override val auth0Api = new Auth0Api {

    private implicit val dateFormat = ApiFormat.dateTimeFormat
    private implicit val userFormat = Json.format[Auth0UserProfile]

    override def tokenInfo(token: String)(implicit ws: WSClient): Future[ServiceResult[Auth0UserProfile]] = {
      ws.url(auth0TokeninfoEndpoint)
        .post(Json.obj("id_token" -> token)).map { obj =>
        userFormat.reads(obj.json) match {
          case JsSuccess(r: Auth0UserProfile, _) => ServiceResult.unit(r)
          case _ => ServiceResult.failed(UnauthorizedError())
        }
      }
    }
  }


}
