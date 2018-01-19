package helpers

import org.slf4j.{ Logger => SLFLogger, LoggerFactory => SLFLoggerFactory }

trait Logging {
  self =>

  implicit lazy val Logger: SLFLogger = self match {
    case s: CanonicalNamed => SLFLoggerFactory getLogger s.canonicalName
    case _                 => SLFLoggerFactory getLogger this.getClass
  }
}