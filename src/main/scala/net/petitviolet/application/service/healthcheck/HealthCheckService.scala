package net.petitviolet.application.service.healthcheck

import java.lang.management.ManagementFactory

import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import net.petitviolet.application.service.ServiceBase
import net.petitviolet.domain.health.Status
import net.petitviolet.domain.pong.{ Pong, UsesPongService }

import scala.concurrent.duration._
import scala.language.postfixOps

trait HealthCheckService extends ServiceBase with UsesPongService {

  private val route =
    pathPrefix("ping") {
      post {
        logRequestResult("ping") {
          path(".+".r) { msg =>
            pongService.response(Pong(msg))
          }
        }
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

  val healthRoutes: Route = route ~ uptimeRoute
}

