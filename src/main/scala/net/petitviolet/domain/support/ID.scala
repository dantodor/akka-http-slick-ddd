package net.petitviolet.domain.support

import net.petitviolet.domain.support.Identifier.IdType

case class ID[A](value: IdType) extends AnyVal with Identifier[IdType]
