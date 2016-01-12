package context

import play.api.Application

/**
  * @author michaeldohr
  * @since 30/12/15
  */
trait PlayApp {
  implicit val current: Application
}
