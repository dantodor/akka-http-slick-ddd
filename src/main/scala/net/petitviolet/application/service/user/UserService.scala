package net.petitviolet.application.service.user

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import net.petitviolet.application.service.ServiceBase
import net.petitviolet.application.usercase.user.{ MixInUserCreateUseCase, UserCreateDTO, UsesUserCreateUseCase }
import net.petitviolet.domain.lifecycle.{ MixInUserRepository, UsesUserRepository }
import net.petitviolet.domain.support.ID
import net.petitviolet.domain.user.{ Hobby, User }

import scala.concurrent.{ ExecutionContext, Future }

trait UserServiceImpl extends UserService with MixInUserRepository with MixInUserCreateUseCase

trait UserService extends ServiceBase
    with UsesUserRepository
    with UsesUserCreateUseCase {

  import net.petitviolet.domain.user.UserJsonProtocol._

  /**
   * show all user list
   *
   * @return
   */
  private def list = {
    val usersFuture: Future[Seq[User]] = userRepository.allUsers
    onSuccess(usersFuture) {
      case users: Seq[User] => complete(users)
      case _ => complete(StatusCodes.NotFound)
    }
  }

  /**
   * find user by user's id
   *
   * @param id
   * @param ec
   * @return
   */
  private def findUser(id: String)(implicit ec: ExecutionContext) = {
    val userFuture = userRepository.resolveBy(ID(id))
    onSuccess(userFuture) {
      case user: User => complete(user)
      case _ => complete(StatusCodes.NotFound)
    }
  }

  /**
   * show all users with his or her hobbies
   *
   * @param ec
   * @return
   */
  private def listWithHobbies(implicit ec: ExecutionContext) = {
    //    import net.petitviolet.domain.user.HobbyJsonProtocol._
    val resultFuture = userRepository.allUsersWithHobbies
    onSuccess(resultFuture) {
      case userWithHobbies: Map[User, Seq[Hobby]] =>
        val r = userWithHobbies.map {
          case (user, hobbies) =>
            (user.name.value, hobbies map { _.content.value })
        }
        complete(r)
      case _ => complete(StatusCodes.NotFound)
    }
  }

  private def createUser(userCreateDTO: UserCreateDTO)(implicit ec: ExecutionContext) = {
    val result = userCreateUseCase.execute(userCreateDTO)

    onSuccess(result) {
      case ID(id) => complete(s"Success!: $id")
      case _ => complete("Fail!")
    }
  }

  private def deleteUser(id: ID[User])(implicit ec: ExecutionContext) = {
    val result = userRepository.deleteBy(id)

    onSuccess(result) {
      case true => complete(s"Success!: $id")
      case false => complete("Fail!")
    }
  }

  private def usersRoutes(implicit ec: ExecutionContext) =
    pathPrefix("users") {
      pathEndOrSingleSlash {
        get {
          list
        } ~
          post {
            decodeRequest {
              entity(as[UserCreateDTO]) { dto =>
                createUser(dto)
              }
            }
          } ~
          delete {
            decodeRequest {
              import ID._
              // how to resolve smartly
              entity(as[ID[_]]) { (id: ID[_]) =>
                deleteUser(ID(id.value))
              }
            }
          }
      } ~
        path(".+{36}".r) { userId =>
          findUser(userId)
        }
    }

  private def withHobbiesRoutes(implicit ec: ExecutionContext) =
    path("users_with_hobbies") {
      get {
        listWithHobbies
      }
    }

  def userRoutes(implicit ec: ExecutionContext) = usersRoutes ~ withHobbiesRoutes

}
