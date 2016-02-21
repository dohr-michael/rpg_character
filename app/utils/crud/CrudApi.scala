package utils.crud

import org.joda.time.DateTime
import play.api.libs.json.{Format, _}
import play.api.mvc._
import utils.auth0.Auth0SecuredApi
import utils.json.ApiFormat
import utils.services.{ServiceController, ServiceFailure, ServiceSuccess}

/**
  * @author michaeldohr
  * @since 14/02/16
  */
trait CrudApi[T <: CrudModel] extends Auth0SecuredApi with ServiceController with ApiFormat {
  self: Controller =>

  val formatter: Format[T]

  implicit lazy val writer: Writes[T] = formatter

  implicit lazy val reader: Reads[T] = formatter

  private def now = DateTime.now()

  protected def generateId: String = java.util.UUID.randomUUID.toString.replace("-", "")

  val repository: CrudRepository[T]

  protected def copyModel(model: T)(id: String = model.id): T

  def all: Action[AnyContent] = (Action andThen Auth0SecuredAction).async { implicit re =>
    repository.all(re)
  }

  def one(id: String): Action[AnyContent] = (Action andThen Auth0SecuredAction).async { implicit re =>
    repository.one(id)(re)
  }

  def create: Action[T] = (Action andThen Auth0SecuredAction).async(parse.json[T]) { implicit re =>
    repository.create(copyModel(re.body)(id = this.generateId))(re)
  }

  def update(id: String): Action[T] = (Action andThen Auth0SecuredAction).async(parse.json[T]) { implicit re =>
    repository.update(id, re.body)(re)
  }

  def delete(id: String): Action[AnyContent] = (Action andThen Auth0SecuredAction).async { implicit re =>
    repository.delete(id)(re).map {
      case ServiceSuccess(_, _) => Ok(id)
      case ServiceFailure(x) => x: Result
    }
  }
}
