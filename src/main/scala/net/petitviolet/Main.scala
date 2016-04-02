package net.petitviolet

import akka.actor.ActorSystem
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Sink, Source}
import com.typesafe.config.ConfigFactory
import net.petitviolet.application.service.healthcheck.HealthCheckService
import net.petitviolet.application.service.user.UserService
import net.petitviolet.domain.lifecycle.MixInUserRepository
import net.petitviolet.domain.pong.{MixInPongService, UsesPongService}
import net.petitviolet.domain.support.ID
import net.petitviolet.domain.user._
import net.petitviolet.infra.user._
import net.petitviolet.infra.{MixInDB}
import org.reactivestreams.{Subscription, Subscriber}
import slick.backend.DatabasePublisher
import slick.dbio.DBIOAction

//import slick.jdbc.{PositionedResult, GetResult}
//import slick.lifted.{ProvenShape, Tag}
import slick.driver.MySQLDriver.api._
import akka.http.scaladsl.server.Directives._

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration._
import scala.concurrent.duration._
import scala.language.postfixOps

object Main extends App with MixInDB with MixInUserRepository
  with HealthCheckService with UserService with MixInPongService {

  implicit val system = ActorSystem()
  implicit val executor = system.dispatcher
  implicit val materializer = ActorMaterializer()

  val config = ConfigFactory.load()

  val logger = Logging(system, "demo-service")

  val (interface, port) = (config.getString("http.interface"), config.getInt("http.port"))

  logger.info(s"Starting service on port $port")

  val routes = userRoutes ~ healthRoutes

  val bindingFuture = Http().bindAndHandle(routes, interface, port)
  scala.io.StdIn.readLine()
  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())

  //  implicit val getUserResult = GetResult { (r: PositionedResult) =>
  //    User(r.rs.getInt("id"), r.rs.getString("name"), r.rs.getString("email"))
//  }
//  val f: Future[Seq[User]] = db.run(sql"select * from user".as[User])
//  val f = db.run(
//    Users += User(3, "charles", "charles@example.com")
//  )
//
//  val r = User(1, "a", "b").insert
//  val query = Users.map { _.name }
//  println(query.result.statements)

  //  val stream: DatabasePublisher[User] = db.stream(query)
//  val userStream = db.stream(query.result)
//  stream
//    .mapResult { user => user.name }
//    .foreach { println }
//    .onComplete { case _ => system.terminate() }

//  val f = db.run(
//    Users
//      .filter { _.name.startsWith("alice") }
//        .map(_.name)
//        .update("alice-updated")
//  )

//  val userNamePublisher: DatabasePublisher[String] = userStream.mapResult { user => user.name }

//  val sql = sql"select * from user".as[(Int, String, String)]
//  val f = db.run(Users.result)
//  db.run(Users.result) foreach println
//  db.run(Hobbies.result) foreach println
//
//  val q = for {
//    user <- Users
//    hobby <- Hobbies if hobby.userId === user.id
//  } yield { (user.name, hobby.content) }
//  println(q.result.statements)
//  db.run(q.result) foreach println
//  val y = Users
//    .filter { _.name.startsWith("alice") }
//    .map(_.name)
//    .update("alice-updated")
//  println(y.statements)
//  println(Users.insertStatement)


//  import scala.concurrent.ExecutionContext.Implicits._
//  val users = userRepository.resolveBy(ID("05649372-f685-11e5-ae4c-10ea684a3a44"))
//  println("--------------------")
//  users foreach println
//  userRepository.allUsers foreach println
//  println("--------------------")
//

//  val subscriber = new Subscriber[String] {
//    override def onError(t: Throwable): Unit = {
//      println("onError start")
//      t.printStackTrace()
//      println("onError end")
//    }
//
//    override def onSubscribe(s: Subscription): Unit = {
//      println(s"subscription: $s")
//    }
//
//    override def onComplete(): Unit = {
//      println("onComplete")
//    }
//
//    override def onNext(t: String): Unit = println(s"onNext: $t")
//  }
//
//  val source = Source.fromPublisher(userNamePublisher)
//  val flow = Flow[String].map(s => s"mapped: $s")
//  val sink = Sink.fromSubscriber(subscriber)
//  val graph = source via flow to sink
//  graph run
//  val future = source via flow runForeach println

//  Await.result(future andThen { case _ => system.terminate() }, Duration.Inf)

//  val s: Source[String, Unit] = Source.fromPublisher(g)
//  val sink2: Sink[String, Unit] = Sink.fromSubscriber(subscriber)

//  val mat: Unit = s.map(s => s"mapped: $s")
//    .runForeach(println)
//    .onComplete { case _ =>
//      system.terminate()
//      materializer.shutdown()
//    }
//  .runWith(Sink.ignore)
//  s runWith(sink2)

//  g subscribe subscriber
//  userNamePublisher foreach println andThen { case _ => system.terminate() }

//  Await.result(Future.successful {
//    Thread.sleep(2000L)
//    println("--------------done")
//    system.terminate()
//    materializer.shutdown()
//  }, Duration.Inf)
}

