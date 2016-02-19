package utils.apis

import org.joda.time.DateTime

/**
  * @author michaeldohr
  * @since 14/02/16
  */
trait CrudModel {
  val id: String
  val creationDate: Option[DateTime]
}
