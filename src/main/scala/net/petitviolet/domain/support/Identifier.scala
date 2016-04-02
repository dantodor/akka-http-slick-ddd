package net.petitviolet.domain.support

import java.util.UUID

trait Identifier[+A] extends Any {
  def value: A

  override def equals(obj: scala.Any): Boolean = obj match {
    case that: Identifier[_] => that.value == this.value
    case _ => false
  }
}

object Identifier {
  type IdType = String
  def generate: IdType = UUID.randomUUID().toString
}
