package net.petitviolet.application.service.user

import akka.http.scaladsl.coding.{ Deflate, Gzip }
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import net.petitviolet.application.service.ServiceBase
import net.petitviolet.application.usecase.user._
import net.petitviolet.domain.lifecycle.{ MixInUserRepository, UsesUserRepository }
import net.petitviolet.domain.support.{ Entity, ID }
import net.petitviolet.domain.user.{ Content, Hobby, Name, User }

import scala.concurrent.{ ExecutionContext, Future }

trait UserServiceImpl
  extends UserService
  with MixInUserRepository
  with MixInUserCreateUseCase
  with MixInAddHobbyUseCase

trait UserService extends ServiceBase
    with UsesUserRepository
    with UsesUserCreateUseCase
    with UsesAddHobbyUseCase {

  import ID._

  /**
   * show all user list
   *
   * @return
   */
  private def list = {
    import net.petitviolet.domain.user.UserJsonProtocol._
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
  private def findUserById(id: ID[User])(implicit ec: ExecutionContext) = {
    val userFuture = userRepository.resolveBy(id)
    handleFutureResult(userFuture)
  }

  private def findUserByName(name: Name)(implicit ec: ExecutionContext) = {
    val userFuture = userRepository.findByName(name)
    handleFutureResult(userFuture)
  }

  private def handleFutureResult(userFuture: Future[User])(implicit ec: ExecutionContext): Route = {
    import net.petitviolet.domain.user.UserJsonProtocol._
    onSuccess(userFuture) {
      case result: User => complete(result)
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
    import net.petitviolet.domain.user.HobbyJsonProtocol._
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
          // /users?name=foo
          parameter('name) { name =>
            findUserByName(Name(name))
          } ~
            // /users
            encodeResponseWith(Gzip, Deflate) {
              list
            }
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
              // how to resolve smartly, and should I prepare IdDTO?
              entity(as[ID[_]]) { (id: ID[_]) =>
                deleteUser(ID(id.value))
              }
            }
          }
      } ~
        // /users/<ID>
        pathPrefix(".+{36}".r) { userId =>
          pathEnd {
            get {
              // should I use ID? or raw String?
              findUserById(ID(userId))
            }
          } ~
            // /users/<ID>/hobbies
            path("hobbies") {
              get {
                hobbies(ID(userId))
              } ~ {
                post {
                  import net.petitviolet.domain.user.HobbyJsonProtocol._
                  decodeRequest {
                    entity(as[Content]) { (content: Content) =>
                      addHobby(AddHobbyDTO(ID(userId), content))
                    }
                  }
                }
              }
            }
        }
    }

  def hobbies(userId: ID[User])(implicit ec: ExecutionContext) = {
    import net.petitviolet.domain.user.HobbyJsonProtocol._
    val result = userRepository.hobbies(userId)
    onSuccess(result) {
      case hobbies: Seq[Hobby] => complete(hobbies)
    }
  }

  def addHobby(addHobbyUseCaseDTO: AddHobbyDTO)(implicit ec: ExecutionContext) = {
    val result = addHobbyUseCase.execute(addHobbyUseCaseDTO)
    onSuccess(result) {
      case ID(id) => complete(s"Success!: $id")
      case _ => complete("Fail!")
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
