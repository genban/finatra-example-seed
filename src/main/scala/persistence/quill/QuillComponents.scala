package persistence.quill

import helpers.Configuration
import io.getquill._

import scala.concurrent.ExecutionContext

/**
 * @author Corbin
 */
trait QuillComponents {

  implicit def executor: ExecutionContext

  val QDB: QuillContext
}

class QuillContext(
  configuration: Configuration,
  prefix:        String        = "mysql"
) extends ExtQuillContext(SnakeCase, configuration, prefix)
