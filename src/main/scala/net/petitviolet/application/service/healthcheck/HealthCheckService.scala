package net.petitviolet.application.service.healthcheck

import java.lang.management.ManagementFactory

import akka.event.Logging
import akka.http.scaladsl.coding._
import akka.http.scaladsl.marshalling.{ PredefinedToEntityMarshallers, ToResponseMarshallable }
import akka.http.scaladsl.model.{ HttpMethods, StatusCodes }
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.ExceptionHandler._
import akka.http.scaladsl.server.directives.{ MethodDirectives, PathDirectives }
import akka.http.scaladsl.server.util.ApplyConverter
import net.petitviolet.application.service.ServiceBase
import net.petitviolet.domain.health.Status
import net.petitviolet.domain.pong.{ MixInPongService, UsesPongService, Pong }

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{ Try, Failure, Success }

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

  final class MyHeader(token: String) extends ModeledCustomHeader[MyHeader] {
    override def companion: ModeledCustomHeaderCompanion[MyHeader] = MyHeader

    override def value(): String = s"value: $token"
  }

  object MyHeader extends ModeledCustomHeaderCompanion[MyHeader] {
    override def name: String = "My-Header"

    // class名と違う
    override def parse(value: String): Try[MyHeader] = Try(new MyHeader(value))
  }

  val myHeaderExtract = optionalHeaderValuePF {
    case h @ MyHeader(token) => h
  }

  import spray.json.DefaultJsonProtocol._

  case class Message(value: String)
  implicit val format = jsonFormat1(Message.apply)

  case class Num(value: Long)

  // path
  val pingSegment: String = "ping"
  val pingMatcher: PathMatcher[Unit] = segmentStringToPathMatcher(pingSegment)
  val pingDirective: Directive[Unit] = path(pingMatcher)

  // response
  val pongStr: String = "pong"
  val pongMarshal: ToResponseMarshallable =
    ToResponseMarshallable.apply(pongStr)(PredefinedToEntityMarshallers.StringMarshaller)
  val pongStandardRoute: StandardRoute = complete(pongMarshal)

  // construct route
  val getPingPath: (StandardRoute) => Route = s =>
    Directive.addByNameNullaryApply(MethodDirectives.get).apply(s)
  val pingRoute: Route = getPingPath(pongStandardRoute)
  val pingRouter: (=> Route) => Route = Directive.addByNameNullaryApply(pingDirective)
  val _route: Route = pingRouter(pingRoute)

  Directive.addByNameNullaryApply(path(segmentStringToPathMatcher("ping"))) {
    Directive.addByNameNullaryApply(get) {
      complete(ToResponseMarshallable("pong"))
    }
  }

  val route =
    path("a|b|c(hoge|foo|bar)".r) { rgx: String =>
      get {
        complete(s"matched: $rgx")
      }
    }

  //  val route =
  //    pathPrefix("ping") {
  //      path("hi") {
  //        get {
  //          complete("hi")
  //        }
  //      } ~
  //        path("a|b|c(hoge|foo|bar)+".r) { rgx: String =>
  //          get {
  //            complete(s"matched: $rgx")
  //          }
  //        } ~
  //        path(HexIntNumber) { i =>
  //          get {
  //            complete(s"int: $i")
  //          }
  //        } ~
  //        path(LongNumber) { l =>
  //          get {
  //            complete(s"long: $l")
  //          }
  //        } ~
  //        path(DoubleNumber) { d =>
  //          get {
  //            complete(s"double: $d")
  //          }
  //        } ~
  //        path(IntNumber) { hi =>
  //          get {
  //            complete(s"hexInt: $hi")
  //          }
  //        } ~
  //        path(HexLongNumber) { hl =>
  //          get {
  //            complete(s"hexLong: $hl")
  //          }
  //        } ~
  //        path("pong") {
  //          pathEndOrSingleSlash {
  //            complete(s"pong")
  //          } ~
  //            pathSuffix("hello") {
  //              complete(s"hello")
  //            }
  //        }
  //
  //    } ~
  //      post {
  //        logRequestResult("") {
  //          path("pong") {
  //            complete("nice")
  //          }
  //        }
  //      }

  //  val pingRoute =
  //    handleExceptions(exceptionHandler) {
  //      handleRejections(rejectionHandler) {
  //        _pingRoute
  //      }
  //    }

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

