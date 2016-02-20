package app

import activities.{ActivitiesRepositoryComponent, DefaultActivitiesRepositoryComponent}
import com.google.inject.{ImplementedBy, Singleton}
import context.{Futures, Neo4jConfigurations, PlayApp, WebServiceClient}
import org.anormcypher.Neo4jREST
import play.api.libs.ws.ning.NingWSClient
import play.api.{Application, Play}
import utils.auth0._

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
  with Auth0DetailsApiComponent
  with ActivitiesRepositoryComponent

trait Injector
  extends PlayApp
  with Futures
  with WebServiceClient
  with Neo4jConfigurations
  with Auth0Context {

  override implicit val current: Application = Play.current
  override implicit val ec: ExecutionContext = play.api.libs.concurrent.Execution.defaultContext

  override implicit val wsclient = NingWSClient()

  override lazy implicit val neo4jConnection = Neo4jREST(current.configuration.getString("neo.server.name").get,
    current.configuration.getInt("neo.server.port").get,
    "/db/data/",
    current.configuration.getString("neo.user.name").get,
    current.configuration.getString("neo.user.passwd").get)(wsclient)


  override val auth0Endpoint: String = current.configuration.getString("auth0.endpoint").get
  override val auth0ClientId: String = current.configuration.getString("auth0.clientId").get
  override val auth0ClientSecret: String = current.configuration.getString("auth0.clientSecret").get
}

@Singleton
class DefaultRpgApp
  extends Injector
  with DefaultAuth0SecurityComponent
  with DefaultAuth0DetailsApiComponent
  with DefaultActivitiesRepositoryComponent
  with RpgApp
