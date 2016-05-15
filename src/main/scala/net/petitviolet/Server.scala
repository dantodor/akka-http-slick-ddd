package net.petitviolet

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import net.petitviolet.utils.Logger

import scala.concurrent.{ ExecutionContextExecutor, Future }

class Server(route: Route, serverConfig: ServerConfig)(implicit val context: Context) extends Logger {
  import serverConfig._
  import context._

  var binding: Future[Http.ServerBinding] = _

  def start() = {
    logger.info(s"Starting service on $host:$port")
    binding = Http().bindAndHandle(
      Route.handlerFlow(route),
      host,
      port
    )
    binding
  }

  def stop() = {
    require(binding != null)
    logger.info(s"Stop server $host:$port")
    binding.flatMap(_.unbind()).onComplete { _ => context.shutdown() }
  }

}

trait Context {
  implicit val system: ActorSystem
  implicit val executor: ExecutionContextExecutor
  implicit val materializer: ActorMaterializer

  private[petitviolet] def shutdown() = system.terminate()
}

trait UsesContext {
  val context: Context
}

trait MixInContext {
  val context: Context = ContextImpl
}

object ContextImpl extends Context {
  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()
}

trait ServerConfig {
  val host: String
  val port: Int
}

class ServerConfigImpl extends ServerConfig {
  private val config = ConfigFactory.load()
  val (host, port) = (config.getString("http.interface"), config.getInt("http.port"))
}
