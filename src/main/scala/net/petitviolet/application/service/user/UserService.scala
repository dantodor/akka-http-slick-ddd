package net.petitviolet.application.service.user

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.server.Directives._
import net.petitviolet.application.service.ServiceBase
import net.petitviolet.domain.lifecycle.UsesUserRepository
import net.petitviolet.domain.user.User

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

trait UserService extends ServiceBase with UsesUserRepository {
  private val PREFIX = "user"
  import ExecutionContext.Implicits.global

  private def list(implicit executionContext: ExecutionContext) = {
    val usersFuture: Future[Seq[User]] = userRepository.allUsers
    val result = usersFuture.map { _.mkString("{", ",", "}") }
    val str = Await.result(result, Duration.Inf)
    HttpResponse(entity = str)
//    result.onComplete { s =>
//      complete(s)
//    }
  }

  private val listRoute =
    pathPrefix("users") {
      pathEndOrSingleSlash {
        get {
          complete(list)
        }
      }
    }
  
  val userRoutes = listRoute
}
