package net.petitviolet.domain.lifecycle

import net.petitviolet.domain.support.ID
import net.petitviolet.domain.user.User
import net.petitviolet.infra.MixInDB
import net.petitviolet.infra.user.Users

import scala.concurrent.{Future, ExecutionContext}

trait UserRepository extends Repository[ID[User], User] {
  def allUsers: Future[Seq[User]]
}


trait UsesUserRepository {
  val userRepository: UserRepository
}

trait MixInUserRepository {
  val userRepository: UserRepository = new UserRepositoryImpl
}

class UserRepositoryImpl extends UserRepository with MixInDB {
  import slick.driver.MySQLDriver.api._
  private def findById(id: ID[User]) = Users.filter { _.id === id.value }

  override def store(entity: User)(implicit ec: ExecutionContext): Future[Boolean] = {
    val query = Users += entity
    db.run(query) map { _ > 0 }
  }

  override def existsBy(id: ID[User])(implicit ec: ExecutionContext): Future[Boolean] =
    db.run(findById(id).result) map { _.nonEmpty }

  override def deleteBy(id: ID[User])(implicit ec: ExecutionContext): Future[Boolean] =
    db.run(findById(id).delete) map { _ > 0 }

  override def resolveBy(id: ID[User])(implicit ec: ExecutionContext): Future[User] =
    db.run(findById(id).result.head)

  def allUsers: Future[Seq[User]] =
    db.run(Users.result)
}