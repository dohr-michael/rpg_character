package context

import org.anormcypher.Neo4jREST

/**
  * @author michaeldohr
  * @since 01/01/16
  */
trait Neo4jConfigurations {

  implicit val neo4jConnection: Neo4jREST

}
