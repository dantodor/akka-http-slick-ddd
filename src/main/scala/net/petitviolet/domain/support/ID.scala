package net.petitviolet.domain.support

import net.petitviolet.domain.support.Identifier.IdType
import spray.json._

case class ID[A <: Entity[_]](value: IdType) extends AnyVal with Identifier[IdType]

object ID extends DefaultJsonProtocol {
  //  implicit def idFormat[A <: Entity[_]] = jsonFormat(ID.apply[A] _, "value")
  implicit def idFormat[A <: Entity[_]] = new RootJsonFormat[ID[A]] {
    override def read(json: JsValue): ID[A] =
      json.asJsObject.getFields("id") match {
        case Seq(JsString(id)) => ID(id)
        case _ => throw new DeserializationException("ID")
      }

    override def write(obj: ID[A]): JsValue = JsObject("id" -> JsString(obj.value))
  }
}
