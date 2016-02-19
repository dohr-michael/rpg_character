package utils.json

import org.joda.time.DateTime
import play.api.libs.json.{Writes, Reads, Format}

/**
  * @author michaeldohr
  * @since 14/02/16
  */
trait ApiFormat {
  val apiDateTimePattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

  implicit val dateTimeFormat:Format[DateTime] = Format[DateTime](Reads.jodaDateReads(apiDateTimePattern), Writes.jodaDateWrites(apiDateTimePattern))
}

object ApiFormat extends ApiFormat
