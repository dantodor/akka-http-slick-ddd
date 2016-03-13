package net.petitviolet.domain.health

import spray.json.DefaultJsonProtocol._

case class Status(uptimeSeconds: Long)

object Status {
  implicit val statusFormat = jsonFormat1(Status.apply)
}
