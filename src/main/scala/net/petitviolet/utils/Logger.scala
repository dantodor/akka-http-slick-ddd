package net.petitviolet.utils

import akka.actor.ActorSystem
import akka.event.Logging
import net.petitviolet.UsesContext

trait Logger extends UsesContext {
  val logger = Logging(context.system, "demo-service")
}
