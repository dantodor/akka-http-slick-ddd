package net.petitviolet.application.service

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import net.petitviolet.UsesContext

trait ServiceBase extends SprayJsonSupport with UsesContext
