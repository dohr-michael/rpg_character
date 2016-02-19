package app

import com.google.inject.{ImplementedBy, Singleton}
import context.{Futures, Neo4jConfigurations, PlayApp}
import news.{DefaultNewsRepositoryComponent, NewsRepositoryComponent}
import org.anormcypher.Neo4jREST
import play.api.libs.ws.ning.NingWSClient
import play.api.{Application, Play}
import utils.security.{DefaultAuth0SecurityComponent, Auth0SecurityComponent}

import scala.concurrent.ExecutionContext

/**
  * @author michaeldohr
  * @since 30/12/15
  */
@ImplementedBy(classOf[DefaultRpgApp])
trait RpgApp
  extends PlayApp
  with Futures
  with Auth0SecurityComponent
  with NewsRepositoryComponent

trait Injector
  extends PlayApp
  with Futures
  with Neo4jConfigurations {

  override implicit val current: Application = Play.current
  override implicit val ec: ExecutionContext = play.api.libs.concurrent.Execution.defaultContext

  val wsclient = NingWSClient()
  lazy implicit val neo4jConnection = Neo4jREST(current.configuration.getString("neo.server.name").get,
    current.configuration.getInt("neo.server.port").get,
    "/db/data/",
    current.configuration.getString("neo.user.name").get,
    current.configuration.getString("neo.user.passwd").get)(wsclient)

}

@Singleton
class DefaultRpgApp
  extends Injector
  with DefaultAuth0SecurityComponent
  with DefaultNewsRepositoryComponent
  with RpgApp
