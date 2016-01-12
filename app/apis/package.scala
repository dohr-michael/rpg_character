import context.Futures
import models._
import play.api.libs.json._
import play.api.mvc.{Result, Controller}

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author michaeldohr
  * @since 02/01/16
  */
package object apis extends Controller {

  private[apis] implicit val jsonMetaFormat = Json.format[Meta]
  private[apis] implicit val jsonRuleFormat = Json.format[Rule]
  private[apis] implicit val jsonRuleSystemFormat = Json.format[RuleSystem]
  private[apis] implicit val jsonRuleOptionFormat = Json.format[RuleOption]
  private[apis] implicit val jsonGamesFormat = Json.format[Game]

  private[apis] implicit def seqToJsArray[A](f: Seq[A])(implicit wrts: Writes[A]) = JsArray(f.map(wrts.writes))

  private[apis] implicit def futureOptToResult[A](f: Future[Option[A]])(implicit wrts: Writes[A], ec: ExecutionContext) = f.map(opt => opt.fold(NotFound("Not found"))(x => Ok(wrts.writes(x))))

  private[apis] implicit def futureToResult[A](f: Future[A])(implicit wrts: Writes[A], ec: ExecutionContext) = f.map(x => Ok(wrts.writes(x)))

  private[apis] implicit def futureListToResult[A](f: Future[List[A]])(implicit wrts: Writes[A], ec: ExecutionContext) = f.map(x => Ok(JsArray(x.map(wrts.writes))))

}
