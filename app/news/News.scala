package news

import org.joda.time.DateTime
import utils.apis.CrudModel

/**
  * @author michaeldohr
  * @since 14/02/16
  */
case class News(id: String, title: String, content: String, creationDate: Option[DateTime] = None) extends CrudModel
