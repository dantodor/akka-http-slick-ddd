package net.petitviolet.domain.lifecycle

import net.petitviolet.domain.support.ID
import net.petitviolet.domain.user.{ Name, Hobby, User }
import net.petitviolet.infra.MixInDB
import net.petitviolet.infra.user.{ Hobbies, Users }

import scala.concurrent.{ Future, ExecutionContext }
import scala.language.postfixOps

trait UserRepository extends Repository[ID[User], User] {
  def allUsers: Future[Seq[User]]

  def allUsersWithHobbies(implicit ec: ExecutionContext): Future[Map[User, Seq[Hobby]]]

  def findByName(name: Name)(implicit ec: ExecutionContext): Future[User]

  def hobbies(userId: ID[User])(implicit ec: ExecutionContext): Future[Seq[Hobby]]

  /**
   * this API is owned by UserRepository, since Hobbies are attributes of Users',
   */
  def addHobby(hobby: Hobby)(implicit ec: ExecutionContext): Future[ID[Hobby]]
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

  override def store(entity: User)(implicit ec: ExecutionContext): Future[ID[User]] = {
    val query = Users += entity
    db.run(query) map { _ => entity.id }
  }

  override def existsBy(id: ID[User])(implicit ec: ExecutionContext): Future[Boolean] =
    db.run(findById(id).result) map { _.nonEmpty }

  override def deleteBy(id: ID[User])(implicit ec: ExecutionContext): Future[Boolean] =
    db.run(findById(id).delete) map { _ > 0 }

  override def resolveBy(id: ID[User])(implicit ec: ExecutionContext): Future[User] =
    db.run(findById(id).result) map { _.head }

  override def allUsers: Future[Seq[User]] =
    db.run(Users.result)

  override def allUsersWithHobbies(implicit ec: ExecutionContext): Future[Map[User, Seq[Hobby]]] = {
    val query = for {
      user <- Users
      hobby <- Hobbies if hobby.userId === user.id
    } yield user -> hobby

    db.run(query.result) map { results: Seq[(User, Hobby)] =>
      results.groupBy { _._1 }
        .mapValues { _.map { _._2 } }
    }
  }

  override def hobbies(userId: ID[User])(implicit ec: ExecutionContext): Future[Seq[Hobby]] = {
    val q = Hobbies filter { hobby => hobby.userId === userId.value }
    db.run(q.result)
  }

  override def findByName(name: Name)(implicit ec: ExecutionContext): Future[User] = {
    val q = Users.findByName(name.value)
    db.run(q.result) map { _.head }
  }

  override def addHobby(hobby: Hobby)(implicit ec: ExecutionContext): Future[ID[Hobby]] = {
    val q = Hobbies += hobby
    db.run(q) map { _ => hobby.id }
  }
}