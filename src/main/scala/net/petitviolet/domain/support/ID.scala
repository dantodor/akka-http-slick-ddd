package net.petitviolet.domain.support

import net.petitviolet.domain.support.Identifier.IdType
import spray.json._

case class ID[A <: Entity[_]](value: IdType) extends AnyVal with Identifier[IdType]

object ID {
  //  implicit val idFormat = jsonFormat1(ID.apply)
  implicit val idFormat = new RootJsonFormat[ID[_]] {
    override def read(json: JsValue): ID[_] =
      json.asJsObject.getFields("id") match {
        case Seq(JsString(id)) => ID(id)
        case _ => throw new DeserializationException("ID")
      }

    override def write(obj: ID[_]): JsValue = JsObject("id" -> JsString(obj.value))
  }
}
