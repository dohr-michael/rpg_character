package activities

import javax.inject.Inject

import app.RpgApp
import play.api.libs.json.{Format, Json}
import play.api.mvc.Controller
import utils.auth0.Auth0ValidationService
import utils.crud.{CrudApi, CrudRepository}

import scala.concurrent.ExecutionContext

/**
  * @author michaeldohr
  * @since 14/02/16
  */
class ActivitiesApi @Inject()(val rpgApp: RpgApp) extends Controller with CrudApi[Activities] {


  override protected def copyModel(model: Activities)(id: String = model.id): Activities = {
    model.copy(id = id)
  }

  override implicit val ec: ExecutionContext = rpgApp.ec
  override val securityService: Auth0ValidationService = rpgApp.securityService

  override val formatter: Format[Activities] = Json.format[Activities]
  override val repository: CrudRepository[Activities] = rpgApp.activitiesRepository
}
