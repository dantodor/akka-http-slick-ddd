package net.petitviolet.application.service.healthcheck

import java.lang.management.ManagementFactory

import akka.event.Logging
import akka.http.scaladsl.server.Directives._
import net.petitviolet.application.service.ServiceBase
import net.petitviolet.domain.health.Status
import net.petitviolet.domain.pong.Pong

import scala.concurrent.duration._

trait HealthCheckService extends ServiceBase {

  private val pingRoute =
    pathPrefix("ping") {
      pathEnd {
        get {
          logRequest("/ping", Logging.InfoLevel) {
            complete("PONG")
          }
        }
      } ~
      path(".+".r) { msg =>
        complete(Pong(msg).out)
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

  val routes = pingRoute ~ uptimeRoute
}
