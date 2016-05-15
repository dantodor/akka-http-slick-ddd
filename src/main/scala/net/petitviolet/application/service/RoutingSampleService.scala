package net.petitviolet.application.service

import akka.http.scaladsl.marshalling.{ PredefinedToEntityMarshallers, ToResponseMarshallable }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.model.headers._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import akka.http.scaladsl.server.directives.MethodDirectives
import akka.http.scaladsl.server.util.ClassMagnet
import akka.http.scaladsl.unmarshalling.Unmarshaller
import net.petitviolet.UsesContext
import spray.json._

import scala.concurrent.{ ExecutionContext, Future }
import scala.language.postfixOps
import scala.reflect.ClassTag
import scala.util.{ Random, Try }

trait RoutingSampleService extends ServiceBase with UsesContext {

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

    //    final class MyHeader(token: String) extends ModeledCustomHeader[$$OriginalHeader] {
    //      override def companion: ModeledCustomHeaderCompanion[$$OriginalHeader] = $$OriginalHeader
    //
    //      override def value(): String = s"value: $token"
    //    }
    //
    //    object MyHeader extends ModeledCustomHeaderCompanion[$$OriginalHeader] {
    //      override def name: String = "My-Header"
    //
    //      // class名と違う
    //      override def parse(value: String): Try[$$OriginalHeader] = Try(new $$OriginalHeader(value))
    //    }
    //
    //    val myHeaderExtract = optionalHeaderValuePF {
    //      case h @ $$OriginalHeader(token) => h
    //    }

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
    //    case class Message(all: String)

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
        path("foo" / Segment / "yes") { x =>
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

    val extractProductName: Directive1[String] = path("product" / Segment ~ (Slash | PathEnd))

    val matcher: Directive[(String, Option[Long], Option[Int])] =
      get &
        extractProductName &
        parameters('price.as[Long]?, 'count.as[Int]?)

    case class Product(name: String, price: Option[Long], count: Option[Int])

    val productRoute = matcher.as(Product) { product =>
      complete(s"$product")
    }

    object Content {
      def apply(id: Long, content: Option[AwesomeBody]): Content =
        content.map(Message(id, _)) getOrElse EmptyMessage(id)
    }

    sealed trait Content {
      val id: Long
      val response = complete(s"Requested: $this")
    }
    case class EmptyMessage(id: Long) extends Content
    case class Message(id: Long, body: AwesomeBody) extends Content
    case class AwesomeBody(value: String)

    val bodyUnmarshaller: Unmarshaller[String, AwesomeBody] =
      Unmarshaller.apply { (ec: ExecutionContext) => (s: String) => Future.successful(AwesomeBody(s)) }

    import spray.json.DefaultJsonProtocol
    object ContentJsonProtocol extends DefaultJsonProtocol {
      implicit val contentFormat = new RootJsonReader[Content] {
        override def read(json: JsValue): Content =
          json.asJsObject.getFields("id", "body") match {
            case Seq(JsNumber(id)) => EmptyMessage(id.toLong)
            case Seq(JsNumber(id), JsString(body)) => Message(id.toLong, AwesomeBody(body))
            case _ => throw new DeserializationException("Content")
          }

      }
    }
    case class Reply(replyId: Long, body: AwesomeBody)
    object Reply {
      def apply(content: Content): Reply = Reply(
        replyId = new Random().nextInt(100).toLong,
        body = AwesomeBody(s"thank you! : $content")
      )
    }
    object ReplyJsonProtocol extends DefaultJsonProtocol {
      implicit val bodyFormat = jsonFormat1(AwesomeBody)
      implicit val replyFormat: RootJsonFormat[Reply] = jsonFormat2(Reply.apply)
    }
    import ContentJsonProtocol._
    import ReplyJsonProtocol._

    val requestBindRoute =
      path("message") {
        get {
          parameters('id.as[Long], 'body.as(bodyUnmarshaller)?).as(Content.apply _) { c: Content =>
            c.response
          }
        } ~
          post {
            entity(as[Content]) { c: Content =>
              //              complete(s"pong: $c")
              complete(Reply(c))
            }
          }
      }

    val originalHeaderMagnet = new ClassMagnet[OriginalHeader] {
      override def classTag: ClassTag[OriginalHeader] = implicitly[ClassTag[OriginalHeader]]
      override def runtimeClass: Class[OriginalHeader] = classOf[OriginalHeader]
      override def extractPF: PartialFunction[Any, OriginalHeader] = {
        case h: CustomHeader if h.name == OriginalHeader.name => OriginalHeader(h.value())
        case h: RawHeader if h.name == OriginalHeader.name => OriginalHeader(h.value)
      }
    }

    val originalHeaderExtract = headerValuePF {
      case h @ OriginalHeader(token) => h
    }

    val headerRoute =

      pathPrefix("header") {
        path("ping") {
          get {
            headerValueByName('Message) { msg: String =>
              complete(s"pong: $msg")
            }
          }
        } ~
          path("ua") {
            headerValueByType[`User-Agent`]() { userAgent =>
              get {
                complete(s"User-Agent => $userAgent")
              }
            }
          } ~
          path("original") {
            headerValueByType[OriginalHeader](OriginalHeader) { originalHeader =>
              //              originalHeaderExtract { originalHeader =>
              get {
                complete(s"original => $originalHeader")
              }
            }
          }
      }

  }

  val routingRoutes: Route =
    Inner.route ~
      Inner.productRoute ~
      Inner.requestBindRoute ~
      Inner.headerRoute
}

class OriginalHeader(token: String) extends ModeledCustomHeader[OriginalHeader] {
  override def companion: ModeledCustomHeaderCompanion[OriginalHeader] = OriginalHeader
  override def value(): String = s"token($token)"
}

object OriginalHeader extends ModeledCustomHeaderCompanion[OriginalHeader] with ClassMagnet[OriginalHeader] {
  override def name: String = "My-Header" // class名と違う
  override def parse(value: String): Try[OriginalHeader] = Try(new OriginalHeader(value))

  override def classTag: ClassTag[OriginalHeader] = implicitly[ClassTag[OriginalHeader]]
  override def runtimeClass: Class[OriginalHeader] = classOf[OriginalHeader]
  override def extractPF: PartialFunction[Any, OriginalHeader] = {
    case h: CustomHeader if h.name == name => apply(h.value())
    case h: RawHeader if h.name == name => apply(h.value)
  }
}

