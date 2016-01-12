package context

import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.DefaultDB

/**
  * @author michaeldohr
  * @since 11/01/16
  */
trait MongoConfigurations {
  implicit val db:DefaultDB
}
