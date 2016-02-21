package activities

import context.{Futures, Neo4jConfigurations}
import org.anormcypher.Cypher
import org.anormcypher.CypherParser._
import org.joda.time.DateTime
import play.api.Logger
import utils.auth0.Auth0SecurityUser
import utils.crud.CrudRepository
import utils.services.{InvalidRequestError, ServiceResult}

import scala.concurrent.Future

/**
  * @author michaeldohr
  * @since 14/02/16
  */
trait ActivitiesRepositoryComponent {

  val activitiesRepository: ActivitiesRepository

  trait ActivitiesRepository extends CrudRepository[Activities]

}


trait DefaultActivitiesRepositoryComponent extends ActivitiesRepositoryComponent {
  self: Futures with Neo4jConfigurations =>

  override val activitiesRepository: ActivitiesRepository = new ActivitiesRepository {

    private val ReturnDeclaration = "news.id as id, news.title as title, news.content as content, news.creationDate as creationDate"
    private val ReturnMapper = str("id") ~ str("title") ~ str("content") ~ get[BigDecimal]("creationDate")

    def tupleToActivity(tuple: (String, String, String, BigDecimal)): Activities = {
      Logger.info(tuple.toString())
      Activities(tuple._1, tuple._2, tuple._3, Some(new DateTime(tuple._4.toLong)))
    }

    override def all(implicit user: Auth0SecurityUser): Future[ServiceResult[List[Activities]]] = {
      Cypher(
        s"""
        MATCH (news: News)
        RETURN $ReturnDeclaration
        ORDER BY creationDate
        """
      ).asAsync {
        ReturnMapper *
      }.map(items => {
        ServiceResult.unit(items.map(flatten).map(tupleToActivity))
      })
    }

    override def one(id: String)(implicit user: Auth0SecurityUser): Future[ServiceResult[Activities]] = {
      Cypher(
        s"""
            MATCH (
         """
      )
      ???
    }

    override def create(value: Activities)(implicit user: Auth0SecurityUser): Future[ServiceResult[Activities]] = createOrUpdate(value)

    override def update(id: String, value: Activities)(implicit user: Auth0SecurityUser): Future[ServiceResult[Activities]] = {
      if (id == value.id) {
        createOrUpdate(value)
      } else {
        ServiceResult.failedF(InvalidRequestError(Seq(s"$id don't match with ${value.id}")))
      }
    }

    override def delete(id: String)(implicit user: Auth0SecurityUser): Future[ServiceResult[Unit]] = ???


    private def createOrUpdate(value: Activities): Future[ServiceResult[Activities]] = {
      Cypher(
        s"""
        MERGE (news:News {id: {id}, title: {title}, content: {content}})
        ON CREATE SET news.creationDate = timestamp()
        RETURN $ReturnDeclaration
        """).on("id" -> value.id, "title" -> value.title, "content" -> value.content).asAsync {
        ReturnMapper *
      }.map(items => {
        ServiceResult.ofOpt(items.map(flatten).map(tupleToActivity).headOption)
      })
    }
  }
}