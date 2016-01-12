package utils

import play.api.libs.json._
import play.modules.reactivemongo.json._
import play.modules.reactivemongo.json.collection._
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.DefaultDB

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author michaeldohr
  * @since 11/01/16
  */
abstract class MongoRepository[MODEL, ID](val collectionName: String)(implicit db: DefaultDB,
                                                                      ec: ExecutionContext,
                                                                      idFormat: Format[ID],
                                                                      modelFormat: Format[MODEL]) {

  protected val collection: JSONCollection = db.collection[JSONCollection](collectionName)

  private def idToJson = (id: ID) => idFormat.writes(id)
  private def modelToJson = (model: MODEL) => modelFormat.writes(model)


  def searchOne(id: ID): Future[Option[MODEL]] = {
    collection
      .find(Json.obj("_id" -> id))
      .one[MODEL]
  }

  def search(filter: JsObject): Future[Seq[MODEL]] = {
    collection
      .find(filter)
      .cursor[MODEL]()
      .collect[List]()
  }
}
