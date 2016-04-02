package net.petitviolet.application.service.user

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import net.petitviolet.application.service.ServiceBase
import net.petitviolet.domain.lifecycle.UsesUserRepository
import net.petitviolet.domain.support.ID
import net.petitviolet.domain.user.User
import net.petitviolet.domain.user.UserJsonProtocol._

import scala.concurrent.{ExecutionContext, Future}

trait UserService extends ServiceBase with UsesUserRepository {

  private def list = {
    val usersFuture: Future[Seq[User]] = userRepository.allUsers
    onSuccess(usersFuture) {
      case users: Seq[User] => complete(users)
      case _ => complete(StatusCodes.NotFound)
    }
  }

  private def findUser(id: String)(implicit ec: ExecutionContext) = {
    val userFuture = userRepository.resolveBy(ID(id))
    onSuccess(userFuture) {
      case user: User => complete(user)
      case _ => complete(StatusCodes.NotFound)
    }
  }

  def userRoutes(implicit ec: ExecutionContext) =
    pathPrefix("users") {
      pathEndOrSingleSlash {
        get {
          list
        }
      } ~
      path(".+{36}".r) { userId =>
        findUser(userId)
      }
    }

}
