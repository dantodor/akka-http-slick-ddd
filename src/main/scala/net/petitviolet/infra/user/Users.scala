package net.petitviolet.infra.user

import net.petitviolet.domain.support.ID
import net.petitviolet.domain.user._
import slick.lifted.Tag
import slick.driver.MySQLDriver.api._

import scala.language.postfixOps

class Users(tag: Tag) extends Table[User](tag, "user") {
  def id = column[String]("id", O.PrimaryKey, O.Length(254))
  def name = column[String]("name", O.Length(254))
  def email = column[String]("email", O.Length(254))

  def idx = index("name", name, unique = true)

  private def userToColumn(u: User) =
    Some((
      u.id.value,
      u.name.value,
      u.email.value
    ))

  private def columnToUser(id: String, name: String, email: String) =
    User.apply(
      ID(id),
      Name(name),
      Email(email)
    )

  def * = (id, name, email) <> (columnToUser _ tupled, userToColumn)
}

object Users extends TableQuery(new Users(_)) {
  val findByName = this.findBy(_.name)
}

