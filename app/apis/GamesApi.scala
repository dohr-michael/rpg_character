package apis

import javax.inject.Inject

import apis._
import app.RpgApp
import context.Futures
import play.api.libs.json.{JsObject, JsArray}
import play.api.mvc.{Result, Action, Controller}

import scala.concurrent.{Future, ExecutionContext}

/**
  * @author michaeldohr
  * @since 11/01/16
  */
trait GamesApi {
  self: Controller with Futures =>

  val rpgApp: RpgApp

  val repository = rpgApp.gamesRepository

  def games = Action.async {
    repository.findAll
  }

  //
  private implicit val jsArrayToResult: Future[JsArray] => Future[Result] = x => x.map(l => Ok(l))
  private implicit val jsObjectToResult: Future[JsObject] => Future[Result] = x => x.map(l => Ok(l))

}

class DefaultGamesApi @Inject()(override val rpgApp: RpgApp) extends Controller with Futures with GamesApi {
  override implicit val ec: ExecutionContext = rpgApp.ec
}