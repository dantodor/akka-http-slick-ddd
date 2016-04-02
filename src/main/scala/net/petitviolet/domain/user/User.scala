package net.petitviolet.domain.user

import net.petitviolet.domain.support.{Entity, ID}

case class User(id: ID[User], name: Name, email: Email) extends Entity[ID[User]]

case class Name(value: String)
case class Email(value: String)


