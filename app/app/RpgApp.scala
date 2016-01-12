package app

import com.google.inject.ImplementedBy
import context.{Futures, MongoConfigurations, PlayApp}
import play.api.{Application, Play}
import play.modules.reactivemongo.ReactiveMongoApi
import repositories.{GamesComponent, DefaultGamesComponent}

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author michaeldohr
  * @since 30/12/15
  */
@ImplementedBy(classOf[DefaultRpgApp])
trait RpgApp extends PlayApp with Futures with GamesComponent {
  /**
    * Ensure store indexes.
    */
  def ensureIndexes(): Future[Unit]
}

trait Injector extends PlayApp with Futures with MongoConfigurations {

  /*val wsclient = NingWSClient()
  lazy implicit val neo4jConnection = Neo4jREST(current.configuration.getString("neo.server.name").get,
    current.configuration.getInt("neo.server.port").get,
    "/db/data/",
    current.configuration.getString("neo.user.name").get,
    current.configuration.getString("neo.user.passwd").get)(wsclient)*/

  override implicit val current: Application = Play.current
  override implicit val ec: ExecutionContext = play.api.libs.concurrent.Execution.defaultContext
  override implicit val db = current.injector.instanceOf[ReactiveMongoApi].db

}

class DefaultRpgApp extends PlayApp
                    with Futures
                    with Injector
                    with RpgApp
                    with DefaultGamesComponent {

  override def ensureIndexes(): Future[Unit] = ???
}