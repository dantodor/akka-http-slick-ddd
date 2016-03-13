package net.petitviolet.domain.pong

case class Pong(msg: String) {
  lazy val out: String = s"Pong!!! $msg"
}

