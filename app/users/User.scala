package users

import org.joda.time.DateTime

/**
  * @author michaeldohr
  * @since 15/02/16
  */
case class User(id: String, creationDate: Option[DateTime], lastConnectionDate: Option[DateTime], roles: List[String])
