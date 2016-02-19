package news

import javax.inject.Inject

import app.RpgApp
import play.api.libs.json.{Format, Json}
import play.api.mvc.Controller
import utils.apis.CrudApi
import utils.repositories.CrudRepository
import utils.security.Auth0SecurityService

import scala.concurrent.ExecutionContext

/**
  * @author michaeldohr
  * @since 14/02/16
  */
class NewsApi @Inject()(val rpgApp: RpgApp) extends Controller with CrudApi[News] {


  override protected def copyModel(model: News)(id: String = model.id): News = {
    model.copy(id = id)
  }

  override implicit val ec: ExecutionContext = rpgApp.ec
  override val securityService: Auth0SecurityService = rpgApp.securityService

  override val formatter: Format[News] = Json.format[News]
  override val repository: CrudRepository[News] = rpgApp.newsRepository
}
