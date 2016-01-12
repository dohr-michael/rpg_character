import models.Game
import play.api.libs.json.Json

/**
  * @author michaeldohr
  * @since 03/01/16
  */
package object repositories {

  private[repositories] implicit val gameFormat = Json.format[Game]

}
