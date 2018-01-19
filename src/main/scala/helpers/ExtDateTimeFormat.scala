package helpers

import org.joda.time._
import org.joda.time.format._

object ExtDateTimeFormat {

  val DATE = "yyyy-MM-dd"
  val DATETIME = "yyyy-MM-dd HH:mm:ss"
  val DATETIMEZONE = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

  /**
   * DefaultDateTimeFormatter.parseDateTime("2008-04-21 11:06:34")
   *
   * @return yyyy-MM-dd HH:mm:ss格式的DateTimeFormatter
   */
  def DefaultDateTimeFormatter: DateTimeFormatter =
    DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss").withZone(DateTimeZone.forID("Asia/Shanghai"))

  /**
   * ShortDateTimeFormatter.parseDateTime("2008-04-21")
   *
   * @return yyyy-MM-dd格式的DateTimeFormatter
   */
  def ShortDateTimeFormatter: DateTimeFormatter =
    DateTimeFormat.forPattern("yyyy-MM-dd").withZone(DateTimeZone.forID("Asia/Shanghai"))
}
