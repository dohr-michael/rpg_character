package context

import scala.concurrent.ExecutionContext

/**
  * @author michaeldohr
  * @since 30/12/15
  */
trait Futures {
  implicit val ec: ExecutionContext
}
