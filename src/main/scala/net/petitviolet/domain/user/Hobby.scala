package net.petitviolet.domain.user

import net.petitviolet.domain.support.{Entity, ID}

case class Hobby(id: ID[Hobby], userId: ID[User], content: Content)extends Entity[ID[Hobby]]
case class Content(value: String)
