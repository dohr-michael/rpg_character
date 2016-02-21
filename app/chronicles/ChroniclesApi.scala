package chronicles

import javax.inject.Inject

import app.RpgApp
import play.api.libs.json.Format
import play.api.mvc.Controller
import utils.auth0.Auth0ValidationService
import utils.crud.{CrudApi, CrudRepository}

import scala.concurrent.ExecutionContext

/**
  * @author michaeldohr
  * @since 20/02/16
  */
class ChroniclesApi @Inject()(val rpgApp: RpgApp) extends Controller with CrudApi[Chronicle] {

  override val formatter: Format[Chronicle] = rpgApp.apiFormats.chronicleFormat

  override protected def copyModel(model: Chronicle)(id: String): Chronicle = {
    model.copy(id = id)
  }

  override val repository: CrudRepository[Chronicle] = rpgApp.chroniclesRepository
  override implicit val ec: ExecutionContext = rpgApp.ec
  override val securityService: Auth0ValidationService = rpgApp.securityService
}
