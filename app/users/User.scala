package users

import org.anormcypher.NeoNode
import org.joda.time.DateTime
import play.api.libs.json.{Format, Json}
import utils.neo4j.NeoUtils

/**
  * @author michaeldohr
  * @since 15/02/16
  */
case class User(id: String,
                name: Option[String] = None,
                firstName: Option[String] = None,
                lastName: Option[String] = None,
                email: Option[String] = None,
                locale: Option[String] = None,
                genre: Option[String] = None,
                creationDate: Option[DateTime] = None,
                lastConnectionDate: Option[DateTime] = None)

object UserFormat {

  trait ApiFormat {
    self: utils.json.ApiFormat =>

    implicit val userFormat: Format[User] = Json.format[User]
  }

  trait NeoFormat {
    self: NeoUtils =>

    def userNodeMapper(node: NeoNode): User = {
      User(
        node.readProp[String]("id", ""),
        node.readPropAsOpt[String]("name"),
        node.readPropAsOpt[String]("given_name"),
        node.readPropAsOpt[String]("family_name"),
        node.readPropAsOpt[String]("email"),
        node.readPropAsOpt[String]("locale"),
        node.readPropAsOpt[String]("genre"),
        node.readDateTimeAsOpt("creation_date"),
        node.readDateTimeAsOpt("last_connection_date")
      )
    }
  }

}