package utils.neo4j

import org.anormcypher.NeoNode
import org.joda.time.DateTime

/**
  * @author michaeldohr
  * @since 20/02/16
  */
trait NeoUtils {

  implicit class NeoNodeOps(node: NeoNode) {

    def readProp[T](name: String, default: T): T = {
      node.props.getOrElse(name, default).asInstanceOf[T]
    }

    def readPropAsOpt[T](name: String): Option[T] = {
      node.props.get(name).asInstanceOf[Option[T]]
    }

    def readDateTime(name: String, default: DateTime): DateTime = {
      readDateTimeAsOpt(name).getOrElse(default)
    }

    def readDateTimeAsOpt(name: String): Option[DateTime] = {
      readPropAsOpt[BigDecimal](name).map(a => new DateTime(a.toLong))
    }
  }


}
