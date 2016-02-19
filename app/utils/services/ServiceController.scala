package utils.services

import play.api.libs.json._
import play.api.mvc.{Controller, Result}
import scala.concurrent.{ExecutionContext, Future}

/**
  * @author michaeldohr
  * @since 14/02/16
  */
trait ServiceController {
  this: Controller =>

  /**
    * All basics error handlers.
    */
  val basicErrorHandlers: PartialFunction[ServiceError, Result] = {
    case ExceptionError(e: Throwable) => InternalServerError(Option(e.getMessage).fold[JsValue](JsNull)(JsString))
    case NotFoundError(messages: Seq[String]) => NotFound(JsArray(messages.map(JsString)))
    case InvalidRequestError(messages: Seq[String]) => BadRequest(JsArray(messages.map(JsString)))
    case ForbiddenError(messages: Seq[String]) => Forbidden(JsArray(messages.map(JsString)))
    case UnauthorizedError(messages: Seq[String]) => Unauthorized(JsArray(messages.map(JsString)))
    case _ => InternalServerError("Something bad happened")
  }

  /**
    * All basics success handlers.
    */
  val basicSuccessHandlers: JsValue => PartialFunction[SuccessAddition, Result] = { js: JsValue => {
    case ObjectCreated(id) => Created(js).withHeaders("id" -> id.toString)
    case ObjectsCreated => Created(js)
    case SuccessWarnings(warnings) => Ok(js).withHeaders("warnings" -> s"[${warnings.mkString(",")}]")
    case _ => Ok(js)
  }
  }

  /**
    * Must be override to add custom error handlers.
    */
  def errorHandlers: PartialFunction[ServiceError, Result] = PartialFunction.empty

  /**
    * Must be override to add custom error handlers.
    */
  val successHandlers: JsValue => PartialFunction[SuccessAddition, Result] = PartialFunction.empty

  implicit def serviceErrorToResult(err: ServiceError): Result = errorHandlers.orElse(basicErrorHandlers)(err)

  /**
    * Transform a service result to Json based to other implicits.
    */
  implicit def serviceResultToResult[A](sr: ServiceResult[A])(implicit wrts: Writes[A]): Result = sr match {
    case ServiceSuccess(value, additions) => additions.fold(Ok(Json.toJson(value))) { add =>
      val json = Json.toJson(value)
      successHandlers(json).orElse(basicSuccessHandlers(json))(add)
    }
    case ServiceFailure(err) => err: Result
  }



  /**
    * Transform a service result future to a future of result.
    */
  implicit def serviceResultToFutureResult[A](fsr: Future[ServiceResult[A]])(implicit wrts: Writes[A], ec: ExecutionContext): Future[Result] = fsr.map(x => x: Result)

}
