package net.petitviolet.domain.support

case class ID[A](value: String) extends AnyVal with Identifier[String]
