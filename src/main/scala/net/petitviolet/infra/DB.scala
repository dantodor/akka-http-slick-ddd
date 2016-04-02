package net.petitviolet.infra

import slick.jdbc.JdbcBackend
import slick.jdbc.JdbcBackend.Database

trait UsesDB {
  val db: JdbcBackend.DatabaseDef
}

trait MixInDB {
  val db: JdbcBackend.DatabaseDef = MixInDB.db
}

// 同じオブジェクトを参照するようにするため
private object MixInDB {
  val db: JdbcBackend.DatabaseDef = Database.forConfig("mysql-local")
}

