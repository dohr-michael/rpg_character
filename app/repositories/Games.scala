package repositories

import context.{Futures, MongoConfigurations}
import models.Game
import play.api.libs.json.JsObject
import utils.MongoRepository

import scala.concurrent.Future

/**
  * @author michaeldohr
  * @since 11/01/16
  */
trait GamesComponent {

  val gamesRepository: GamesRepository

  trait GamesRepository {

    def findAll: Future[Seq[Game]]

    def findOne(id: String): Future[Option[Game]]

  }

}

trait DefaultGamesComponent extends GamesComponent {
  self: Futures with MongoConfigurations =>

  val gamesRepository = new MongoRepository[Game, String]("games") with GamesRepository {

    override def findOne(id: String): Future[Option[Game]] = searchOne(id)

    override def findAll: Future[Seq[Game]] = search(JsObject(Seq()))
  }

}


