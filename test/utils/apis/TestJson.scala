package utils.apis

import activities.Activities
import org.joda.time.DateTime
import org.scalatestplus.play.PlaySpec
import play.api.libs.json.{JsString, Json}
import utils.json.{ApiFormat, JsonUtils}

/**
  * @author michaeldohr
  * @since 14/02/16
  */
class TestJson extends PlaySpec with TestJsonConf {

  val json = Json.obj(
    "id" -> "toto",
    "title" -> "My Awesome title",
    "content" -> "Content",
    "creationDate" -> dateTimeFormat.writes(DateTime.now()))


  "Toto" must {
    "Titi" in {
      println(json)
      println(formatter.reads(json))
    }
  }

}


trait TestJsonConf extends ApiFormat with JsonUtils {
  self: TestJson =>

  val formatter2 = {
    val euu = Json.obj("id" -> "heuu")

  }


  val formatter = extend("id" -> "heuuu") andThen Json.format[Activities]


}