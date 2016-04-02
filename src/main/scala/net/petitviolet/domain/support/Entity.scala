package net.petitviolet.domain.support

trait Entity[Id <: Identifier[_]] {
  val id: Id

  override def equals(obj: scala.Any): Boolean = obj match {
    case that: Entity[_] => that.id == this.id
    case _ => false
  }
}
