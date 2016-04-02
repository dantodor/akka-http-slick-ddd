package net.petitviolet.domain.pong

import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.StandardRoute

case class Pong(msg: String) {
  lazy val out: String = s"Pong!!! $msg"
}

object Pong {
  def apply(): Pong = {
    Pong("")
  }
}

trait PongService {
  def response(pong: Pong = Pong()): StandardRoute = {
    complete(pong.out)
  }
}
trait UsesPongService {
	val pongService: PongService
}

trait MixInPongService {
	val pongService: PongService = new PongServiceImpl
}

class PongServiceImpl extends PongService