package persistence.quill

import java.sql.Timestamp

import io.getquill.context.async._
import org.joda.time.{ LocalDate => JodaLocalDate, LocalDateTime => JodaLocalDateTime, _ }

/**
 * @author Corbin
 */
trait DateTimeEncoding {
  this: AsyncContext[_, _, _] with Decoders with Encoders =>

  override implicit val jodaDateTimeEncoder: Encoder[DateTime] = encoder[DateTime]({
    dt: DateTime => new Timestamp(dt.getMillis)
  }, SqlTypes.TIMESTAMP)

  override implicit val jodaDateTimeDecoder: Decoder[DateTime] = decoder[DateTime]({
    case value: Timestamp         => new DateTime(value.getTime)
    case value: JodaLocalDate     => new DateTime(value.toDate.getTime)
    case value: JodaLocalDateTime => new DateTime(value.toDate.getTime)
  }, SqlTypes.TIMESTAMP)
}
