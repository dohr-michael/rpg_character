package context

import play.api.libs.ws.WSClient

/**
  * @author michaeldohr
  * @since 20/02/16
  */
trait WebServiceClient {

  implicit val wsclient: WSClient
}
