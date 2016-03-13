package net.petitviolet

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import net.petitviolet.application.service.healthcheck.HealthCheckService

object Main extends App with HealthCheckService {

  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()

  val logger = Logging(system, "demo-service")

  val (interface, port) = (config.getString("http.interface"), config.getInt("http.port"))

  logger.info(s"Starting service on port $port")

  val bindingFuture = Http().bindAndHandle(routes, interface, port)
  scala.io.StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
}
