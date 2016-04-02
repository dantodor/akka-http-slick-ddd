package net.petitviolet.infra.user

import net.petitviolet.domain.support.ID
import net.petitviolet.domain.user._
import slick.lifted.Tag
import slick.driver.MySQLDriver.api._

import scala.language.postfixOps

class Hobbies(tag: Tag) extends Table[Hobby](tag, "hobby") {
  def id = column[String]("id", O.PrimaryKey)
  def userId = column[String]("user_id")
  def content = column[String]("content")

  def user = foreignKey("user_FK", userId, Users) (
    _.id,
    onUpdate = ForeignKeyAction.Cascade,
    onDelete = ForeignKeyAction.Cascade
  )

  private def hobbyToColumn(h: Hobby) =
    Some((
      h.id.value,
      h.userId.value,
      h.content.value
      ))

  private def columnToHobby(id: String, userId: String, content: String) =
    Hobby(ID[Hobby](id), ID[User](userId), Content(content))

  def * = (id, userId, content) <> (columnToHobby _ tupled, hobbyToColumn)
}

object Hobbies extends TableQuery(new Hobbies(_))

