package chronicles

import org.anormcypher.NeoNode
import org.joda.time.DateTime
import play.api.libs.json.{Format, Json}
import users.{User, UserFormat}
import utils.crud.CrudModel
import utils.neo4j.NeoUtils


/**
  * @author michaeldohr
  * @since 20/02/16
  */
case class Chronicle(id: String,
                     name: String,
                     owners: Seq[User] = Seq.empty,
                     players: Seq[User] = Seq.empty,
                     creationDate: Option[DateTime] = None) extends CrudModel

object ChronicleFormat {

  trait ApiFormat {
    this: UserFormat.ApiFormat =>

    implicit val chronicleFormat: Format[Chronicle] = Json.format[Chronicle]

  }

  trait NeoFormat {
    this: NeoUtils with UserFormat.NeoFormat =>

    /**
      *
      * @param nodes (chronicle, owners, players)
      * @return
      */
    def chronicleNodeMapper(nodes: (NeoNode, Seq[NeoNode], Seq[NeoNode])): Chronicle = {
      Chronicle(
        nodes._1.readProp[String]("id", ""),
        nodes._1.readProp[String]("name", ""),
        nodes._2.map(userNodeMapper),
        nodes._3.map(userNodeMapper),
        nodes._1.readDateTimeAsOpt("creation_date")
      )
    }

    def chronicleCreationFields(chronicle: Chronicle): (String, Seq[(String, Any)]) = {
      val req = "{ id: {id}, name: {name}, creation_date: timestamp()}"
      val params = Seq("id" -> chronicle.id, "name" -> chronicle.name)
      (req, params)
    }

  }

}