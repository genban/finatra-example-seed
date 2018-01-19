package persistence

import com.github.mauricio.async.db.RowData

/**
 * @author Corbin
 */
package object quill {

  implicit class RichRowData(val row: RowData) extends AnyVal {

    def get[T](key: Int): T = row.apply(key).asInstanceOf[T]

    def get[T](key: String): T = row.apply(key).asInstanceOf[T]
  }
}
