package models

import play.db.anorm.defaults._

case class Survey(
  uid: String, sid: String
)

object Survey {
}

case class Files(
  text: String
)

object Files extends Magic[Files]