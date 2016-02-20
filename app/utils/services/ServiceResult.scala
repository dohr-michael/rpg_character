package utils.services

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

object ServiceResult {
  def unit[A](value: A): ServiceResult[A] = ServiceSuccess(value)

  def unitF[A](value: A): Future[ServiceResult[A]] = Future.successful(unit(value))

  def failed[A](error: ServiceError): ServiceResult[A] = ServiceFailure(error)

  def failedF[A](error: ServiceError): Future[ServiceResult[A]] = Future.successful(failed(error))

  def withAdditional[A](value: A, additional: SuccessAddition): ServiceResult[A] = ServiceSuccess(value, Option(additional))

  def withAdditionalF[A](value: A, additional: SuccessAddition): Future[ServiceResult[A]] = Future.successful(withAdditional(value, additional))

  def ofOpt[A](opt: Option[A]): ServiceResult[A] = opt.fold(failed[A](NotFoundError()))(unit)

  def sequence[A](serviceResults: Seq[ServiceResult[A]]): ServiceResult[Seq[A]] = serviceResults.foldLeft(ServiceResult.unit(Seq.empty[A])) { (acc, cur) =>
    for {
      a <- acc
      c <- cur
    } yield {
      a :+ c
    }
  }

  def sequenceF[A](futureServiceResults: Seq[Future[ServiceResult[A]]])
                  (implicit ec: ExecutionContext): Future[ServiceResult[Seq[A]]] = Future.sequence(futureServiceResults).map { serviceResults =>
    serviceResults.foldLeft(ServiceResult.unit(Seq.empty[A])) { (acc, cur) =>
      for {
        a <- acc
        c <- cur
      } yield {
        a :+ c
      }
    }
  }

  def apply[A](value: => A): ServiceResult[A] = {
    Try(value) match {
      case Success(t) => ServiceSuccess(t)
      case Failure(e) => ServiceFailure(ExceptionError(e))
    }
  }

  def ofFuture[A](value: Future[A])(implicit ec: ExecutionContext): Future[ServiceResult[A]] = value.map(ServiceResult.unit)

  def ofFutureOpt[A](value: Future[Option[A]])(implicit ec: ExecutionContext): Future[ServiceResult[A]] = value.map(ServiceResult.ofOpt)
}

/**
  * Service errors.
  */
trait ServiceError

case object UnknownServiceError extends ServiceError

case class ExceptionError(e: Throwable) extends ServiceError

case class NotFoundError(messages: Seq[String] = Seq.empty) extends ServiceError

case class InvalidRequestError(messages: Seq[String] = Seq.empty) extends ServiceError

case class UnauthorizedError(message: Seq[String] = Seq.empty) extends ServiceError

case class ForbiddenError(message: Seq[String] = Seq.empty) extends ServiceError

/**
  * Service success additional parameters.
  * // TODO add some parameters.
  */
trait SuccessAddition

case class ObjectCreated[T](id: T) extends SuccessAddition

case object ObjectsCreated extends SuccessAddition

case class ObjectDeleted[T](id: T) extends SuccessAddition

case class SuccessWarnings(warns: Seq[String]) extends SuccessAddition


/**
  * @author michaeldohr
  * @since 16/11/15
  */
sealed trait ServiceResult[+A] {
  def serviceError: Option[ServiceError]

  /**
    * Returns the result of the service.
    * @note The service result must be success.
    * @throws NoSuchElementException for failure.
    */
  def get: A

  /**
    * Returns the result as an option.
    * @return
    */
  final lazy val getAsOpt: Option[A] = serviceError.fold[Option[A]](Some(get))(_ => None)

  final def asOpt: Option[A] = getAsOpt

  /**
    * Returns the result of the service if success, otherwise return the 'default'
    * @param default the default expression
    */
  final def getOrElse[B >: A](default: B): B = serviceError.fold[B](get)(_ => default)

  /**
    * Returns a $some containing the result of applying $f of this $ServiceResult's
    * @param f the function to apply
    */
  def map[B](f: A => B): ServiceResult[B]

  /**
    * Applies the function to the underlying object. Doesn't apply if ServiceFailure.
    * @param f the function to apply
    */
  def flatMap[B](f: A => ServiceResult[B]): ServiceResult[B] = serviceError.fold[ServiceResult[B]](f(get))(e => ServiceFailure(e))

  /**
    * Applies fn to the underlying object and if no object, returns Default
    * @param fn the function to apply
    * @param default the default value
    */
  def fold[B](default: ServiceError => B)(fn: A => B): B = {
    serviceError.fold(fn(get))(e => default(e))
  }

  /**
    * Applies fn to the underlying object. fn returns a Future.
    * @param fn the function to apply
    */
  def flatMapF[B](fn: A => Future[ServiceResult[B]]): Future[ServiceResult[B]] = {
    serviceError.fold[Future[ServiceResult[B]]](fn(get))(e => Future.successful(ServiceFailure(e)))
  }

  /**
    * Applies fn to the underlying object. fn returns a Future.
    * @param fn the function to apply
    */
  def mapF[B](fn: A => Future[B])(implicit ec: ExecutionContext): Future[ServiceResult[B]] = {
    serviceError.fold[Future[ServiceResult[B]]](fn(get).map(b => map(_ => b)))(e => Future.successful(ServiceFailure(e)))
  }
}

/**
  * Service success.
  * @param value result of the service.
  * @param addition additional information.
  */
case class ServiceSuccess[T](value: T, addition: Option[SuccessAddition] = None) extends ServiceResult[T] {

  final val get = value

  final override def map[B](f: (T) => B): ServiceResult[B] = ServiceSuccess(f(get), addition)

  override val serviceError: Option[ServiceError] = None
}

/**
  * Service failure.
  * @param error error.
  */
case class ServiceFailure(error: ServiceError) extends ServiceResult[Nothing] {

  final def get: Nothing = throw new NoSuchElementException("ServiceFailure.get")

  override lazy val serviceError: Option[ServiceError] = Some(error)

  final override def map[B](f: (Nothing) => B): ServiceResult[B] = ServiceFailure(error)


}
