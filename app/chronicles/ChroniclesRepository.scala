package chronicles

import app.AppFormats
import context.{Futures, Neo4jConfigurations}
import org.anormcypher.CypherParser._
import org.anormcypher.{Cypher, NeoNode}
import users.User
import utils.auth0.Auth0SecurityUser
import utils.crud.CrudRepository
import utils.neo4j.NeoUtils
import utils.services.{InvalidRequestError, ServiceResult}

import scala.concurrent.Future

/**
  * @author michaeldohr
  * @since 20/02/16
  */
trait ChroniclesRepositoryComponent {

  val chroniclesRepository: ChroniclesRepository

  trait ChroniclesRepository extends CrudRepository[Chronicle]

}

trait DefaultChroniclesRepositoryComponent extends ChroniclesRepositoryComponent with NeoUtils {
  self: Futures with Neo4jConfigurations with AppFormats =>

  def toChronicleLight(tuple: (NeoNode, Seq[String]))(implicit user: Auth0SecurityUser): Chronicle = {
    Chronicle(
      tuple._1.readProp[String]("id", ""),
      tuple._1.readProp[String]("name", ""),
      tuple._2.find(p => p == "IS_OWNER").fold[Seq[User]](Seq.empty)(_ => Seq(User(user.userId))),
      tuple._2.find(p => p == "IS_PLAYER").fold[Seq[User]](Seq.empty)(_ => Seq(User(user.userId)))
    )
  }

  override val chroniclesRepository: ChroniclesRepository = new ChroniclesRepository {

    override def all(implicit user: Auth0SecurityUser): Future[ServiceResult[List[Chronicle]]] = {
      Cypher(
        """
        MATCH (c :Chronicle)<-[r :IS_OWNER|:IS_PLAYER]-(:User {id: {userId}})
        RETURN c as chronicle, collect(DISTINCT type(r)) as types  order by c.creation_date
        """
      ).on("userId" -> user.userId).asAsync {
        node("chronicle") ~ get[Seq[String]]("types") *
      }.map { items =>
        ServiceResult.unit(items.map(flatten).map(toChronicleLight))
      }
    }

    override def one(id: String)(implicit user: Auth0SecurityUser): Future[ServiceResult[Chronicle]] = {
      Cypher(
        """
        MATCH (c :Chronicle {id: {id}})<-[:IS_OWNER|:IS_PLAYER]-(:User {id: {userId}})
        OPTIONAL MATCH (c)<-[:IS_PLAYER]-(player :User)
        OPTIONAL MATCH (c)<-[:IS_OWNER]-(owner :User)
        RETURN c as chronicle, collect(DISTINCT owner) as owners, collect(DISTINCT player) as players
        """
      ).on("userId" -> user.userId, "id" -> id).asAsync {
        node("chronicle") ~ get[Seq[NeoNode]]("owners") ~ get[Seq[NeoNode]]("players") singleOpt
      }.map { item =>
        ServiceResult.ofOpt(item.map(flatten).map(storeFormats.chronicleNodeMapper))
      }
    }

    override def create(value: Chronicle)(implicit user: Auth0SecurityUser): Future[ServiceResult[Chronicle]] = {
      val params = storeFormats.chronicleCreationFields(value)
      Cypher(
        s"""
        MERGE (owner :User {id: {userId}})
        CREATE (c :Chronicle ${params._1})
        CREATE (c)<-[:IS_OWNER]-(owner)
        WITH c, owner
        OPTIONAL MATCH (c)<-[:IS_PLAYER]-(player :User)
        RETURN c as chronicle, collect(DISTINCT owner) as owners, collect(DISTINCT player) as players
        """
      ).on(params._2 :+ "userId" -> user.userId: _*).asAsync {
        node("chronicle") ~ get[Seq[NeoNode]]("owners") ~ get[Seq[NeoNode]]("players") singleOpt
      }.map { item =>
        item.map(flatten).map(storeFormats.chronicleNodeMapper)
          .fold(ServiceResult.failed[Chronicle](InvalidRequestError(Seq("Error during the save"))))(ServiceResult.unit)
      }
    }

    override def update(id: String, value: Chronicle)(implicit user: Auth0SecurityUser): Future[ServiceResult[Chronicle]] = ???

    override def delete(id: String)(implicit user: Auth0SecurityUser): Future[ServiceResult[Unit]] = ???
  }
}
