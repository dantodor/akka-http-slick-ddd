package net.petitviolet.application

import net.petitviolet.infra.MixInDB
import net.petitviolet.infra.user.{ Hobbies, Users }

import scala.concurrent.Await
import scala.concurrent.duration.Duration

//object DDL extends App with MixInDB {
object DDL extends MixInDB {
  import slick.driver.MySQLDriver.api._
  val schema =
    Users.schema ++
      Hobbies.schema

  import scala.concurrent.ExecutionContext.Implicits.global
  //  val drop = db.run(schema.drop)
  val dropDB = sqlu"DROP DATABASE `akka-db`"
  val createDB = sqlu"CREATE DATABASE `akka-db`"
  val useDB = sqlu"USE `akka-db`"

  schema.createStatements.foreach(println)

  val f = db.run(DBIO.seq(
    dropDB,
    createDB,
    useDB,
    schema.create
  ))
  println(Await.result(f, Duration.Inf))
}
