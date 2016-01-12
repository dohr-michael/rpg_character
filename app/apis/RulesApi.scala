package apis

import javax.inject.Inject

import app.RpgApp
import context.Futures
import play.api.libs.json.JsArray
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author michaeldohr
  * @since 30/12/15
  */
trait RulesApi {
  self: Controller with Futures =>

  val rpgApp: RpgApp
  /*val repository = rpgApp.rulesRepository


  def systems = Action.async {
    repository.rules()
  }

  def system(name: String) = Action.async {
    repository.rule(name)
  }

  def translations(system: String) = Action.async {
    Future.successful(Ok(JsArray()))
  }

  def options(system: String, name: String) = Action.async {
    repository.options(system, name)
  }*/

}

class DefaultRulesApi @Inject()(override val rpgApp: RpgApp) extends Controller with Futures with RulesApi {
  override implicit val ec: ExecutionContext = play.api.libs.concurrent.Execution.defaultContext
}