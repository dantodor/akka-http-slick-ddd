package net.petitviolet.application.service

import java.lang.management.ManagementFactory

import akka.event.Logging
import akka.http.scaladsl.marshalling.{ PredefinedToEntityMarshallers, ToResponseMarshallable }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.MethodDirectives
import net.petitviolet.domain.health.Status
import net.petitviolet.domain.pong.UsesPongService
import spray.json._

import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Try

trait RoutingSampleService extends ServiceBase {

  private object Inner {
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

    case class User(id: Long, name: String)

    object UserJsonProtocol extends DefaultJsonProtocol {
      implicit val format: RootJsonWriter[User] = jsonFormat(User.apply, "user_id", "user_name")
    }

    val __route =
      pathPrefix("nice") {
        pathPrefix("user") {
          pathEnd {
            complete("nice user!")
          } ~
            pathSuffix("greet") {
              complete(s"hello!")
            } ~
            pathPrefix(".+".r) { name =>
              pathEndOrSingleSlash {
                complete(s"nice name => $name")
              }
            }
        }
      }

    val pathMatcher: PathMatcher[Tuple2[Int, String]] = "hi" / IntNumber / ".+".r
    val x: Directive[Tuple2[Int, String]] = path(pathMatcher)
    val map: Map[String, Long] = Map(
      "one" -> 1L,
      "two" -> 2L,
      "three" -> 3L
    )
    val route =
      path("nice" / "user") {
        complete("nice user!")
      } ~
        path("nice" / "user" / ("greet" | "hello")) {
          complete("hello!")
        } ~
        path("nice" / "user" / ".+".r ~ Slash.?) { name =>
          complete(s"nice name => $name")
        } ~
        path("hi" / IntNumber / ".+".r) { (n: Int, str: String) =>
          complete(s"n: $n, str: $str")
        } ~
        path(map) { l: Long =>
          complete(s"long: $l")
        } ~
        path("foo" / Segment / "yes") { (x: String) =>
          complete(s"foo segment: $x")
        } ~
        path("bar" / Segment / RestPath) { (x: String, y: Path) =>
          complete(s"bar segment: $x, $y")
        } ~
        path("baz" / Segments) { (x: List[String]) =>
          complete(s"baz segment: $x")
        } ~
        path("hoge" / Segment.repeat(2, Slash)) { x: List[String] =>
          complete(s"repeat segment: $x")
        }

    val aroute =
      pathPrefix("nice" / "user") {
        pathEnd {
          complete("nice user!")
        } ~
          path("greet" | "hello") {
            complete("hello!")
          } ~
          path(".+".r ~ Slash.?) { name =>
            complete(s"nice name => $name")
          }
      }

    val extractUserName = path(Segment ~ (Slash | PathEnd))
    val matcher = get & extractUserName
  }

  val routingRoutes: Route = Inner.route
}

