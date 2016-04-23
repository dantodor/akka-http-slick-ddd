package net.petitviolet.application.service.healthcheck

import akka.event.Logging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import net.petitviolet.application.service.ServiceBase
import net.petitviolet.domain.pong.UsesPongService

trait HealthCheckService extends ServiceBase with UsesPongService {
  val rejectionHandler =
    RejectionHandler
      .newBuilder()
      .handleNotFound {
        complete((StatusCodes.NotFound, "requested path was invalid."))
      }
      .handle {
        case r: Rejection =>
          complete((StatusCodes.BadRequest, s"something wrong: $r"))
      }
      .result()

  val exceptionHandler = ExceptionHandler {
    case t: Throwable => complete((StatusCodes.InternalServerError, s"Error is $t"))
  }

  private val _pingRoute =
    pathPrefix("ping") {
      pathEnd {
        get {
          logRequest("/ping", Logging.InfoLevel) {
            pongService.response()
          }
        }
      } ~
        path(".+".r) { msg =>
          if (msg == "hoge") {
            throw new IllegalArgumentException("cannot understand `hoge`")
          } else if (msg.length < 10) {
            reject {
              new ValidationRejection(
                "Your message is too short!",
                Some(new IllegalArgumentException("length must be longer than 10"))
              )
            }
          } else {
            complete(s"$msg")
          }
        }
    }

  val pingRoute =
    handleExceptions(exceptionHandler) {
      handleRejections(rejectionHandler) {
        _pingRoute
      }
    }

  private val uptimeRoute = path("uptime") {
    get {
      logRequest("/uptime", Logging.InfoLevel) {
        val uptime = Duration(ManagementFactory.getRuntimeMXBean.getUptime, MILLISECONDS).toSeconds
        complete(Status(uptime))
      }
    }
  }

  val healthRoutes: Route = pingRoute ~ uptimeRoute
}

