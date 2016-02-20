package utils.crud

import utils.auth0.Auth0SecurityUser
import utils.services.ServiceResult

import scala.concurrent.Future

/**
  * @author michaeldohr
  * @since 14/02/16
  */
trait CrudRepository[T] {


  def all(implicit user: Auth0SecurityUser): Future[ServiceResult[List[T]]]

  def one(id: String)(implicit user: Auth0SecurityUser): Future[ServiceResult[T]]

  def create(value: T)(implicit user: Auth0SecurityUser): Future[ServiceResult[T]]

  def update(id: String, value: T)(implicit user: Auth0SecurityUser): Future[ServiceResult[T]]

  def delete(id: String)(implicit user: Auth0SecurityUser): Future[ServiceResult[Unit]]

}
