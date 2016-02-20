package users

import context.{Futures, Neo4jConfigurations}
import org.anormcypher.CypherParser._
import org.anormcypher.{Cypher, NeoNode}
import utils.auth0.{Auth0SecurityUser, Auth0UserProfile}
import utils.crud.CrudRepository
import utils.services.ServiceResult

import scala.concurrent.Future

/**
  * @author michaeldohr
  * @since 15/02/16
  */
trait UsersRepositoryComponent {

  val usersServices: UsersServices

  trait UsersServices extends CrudRepository[User]

}

trait DefaultUsersRepositoryComponent extends UsersRepositoryComponent {
  self: Futures with Neo4jConfigurations =>


  def tupleToUser(value: (NeoNode, Seq[String])): User = {
    ???
  }

  override val usersServices = new UsersServices {

    override def all(implicit user: Auth0SecurityUser): Future[ServiceResult[List[User]]] = {
      Cypher(
        """
        MATCH (u: User)
        OPTIONAL MATCH (u)-[:HAS_ROLE]->(r: SecurityRole)
        RETURN u, collect(DISTINCT r.id) as roles
        """).asAsync {
        node("u") ~ get[Seq[String]]("roles") *
      }.map(re =>
        ServiceResult(re.map(flatten).map(tupleToUser))
      )
    }

    override def one(id: String)(implicit user: Auth0SecurityUser): Future[ServiceResult[User]] = ???

    override def update(id: String, value: User)(implicit user: Auth0SecurityUser): Future[ServiceResult[User]] = ???

    override def delete(id: String)(implicit user: Auth0SecurityUser): Future[ServiceResult[Unit]] = ???

    override def create(value: User)(implicit user: Auth0SecurityUser): Future[ServiceResult[User]] = ???
  }
}
