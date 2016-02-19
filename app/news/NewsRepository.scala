package news

import context.{Futures, Neo4jConfigurations}
import org.anormcypher.Cypher
import org.anormcypher.CypherParser._
import org.joda.time.DateTime
import play.api.Logger
import utils.repositories.CrudRepository
import utils.security.Auth0UserProfile
import utils.services.{InvalidRequestError, ServiceResult}

import scala.concurrent.Future

/**
  * @author michaeldohr
  * @since 14/02/16
  */
trait NewsRepositoryComponent {

  val newsRepository: NewsRepository

  trait NewsRepository extends CrudRepository[News]

}


trait DefaultNewsRepositoryComponent extends NewsRepositoryComponent {
  self: Futures with Neo4jConfigurations =>

  override val newsRepository: NewsRepository = new NewsRepository {

    private val ReturnDeclaration = "news.id as id, news.title as title, news.content as content, news.creationDate as creationDate"
    private val ReturnMapper = str("id") ~ str("title") ~ str("content") ~ get[BigDecimal]("creationDate")

    def tupleToNews(tuple: (String, String, String, BigDecimal)): News = {
      Logger.info(tuple.toString())
      News(tuple._1, tuple._2, tuple._3, Some(new DateTime(tuple._4.toLong)))
    }

    override def all(implicit user: Auth0UserProfile): Future[ServiceResult[List[News]]] = {
      println(user)
      Cypher(
        s"""
      MATCH (news: News)
      RETURN $ReturnDeclaration
      ORDER BY creationDate
      """
      )
        .asAsync {
          ReturnMapper *
        }.map(items => {
        ServiceResult.unit(items.map(flatten).map(tupleToNews))
      })
    }

    override def one(id: String)(implicit user: Auth0UserProfile): Future[ServiceResult[News]] = ???

    override def create(value: News)(implicit user: Auth0UserProfile): Future[ServiceResult[News]] = createOrUpdate(value)

    override def update(id: String, value: News)(implicit user: Auth0UserProfile): Future[ServiceResult[News]] = {
      if (id == value.id) {
        createOrUpdate(value)
      } else {
        ServiceResult.failedF(InvalidRequestError(Seq(s"$id don't match with ${value.id}")))
      }
    }

    override def delete(id: String)(implicit user: Auth0UserProfile): Future[ServiceResult[Unit]] = ???


    private def createOrUpdate(value: News): Future[ServiceResult[News]] = {
      Cypher(
        s"""
        MERGE (news:News {id: {id}, title: {title}, content: {content}})
        ON CREATE SET news.creationDate = timestamp()
        RETURN $ReturnDeclaration
        """)
        .on("id" -> value.id, "title" -> value.title, "content" -> value.content)
        .asAsync {
          ReturnMapper *
        }.map(items => {
        ServiceResult.ofOpt(items.map(flatten).map(tupleToNews).headOption)
      })
    }
  }
}