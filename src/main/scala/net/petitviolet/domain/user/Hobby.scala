package net.petitviolet.domain.user

import net.petitviolet.domain.support.{ Entity, ID }
import spray.json._

case class Hobby(id: ID[Hobby], userId: ID[User], content: Content) extends Entity[ID[Hobby]]
case class Content(value: String)

object HobbyJsonProtocol extends DefaultJsonProtocol {
  implicit val hobbyFormat: RootJsonFormat[Hobby] = new RootJsonFormat[Hobby] {
    override def read(json: JsValue): Hobby =
      json.asJsObject.getFields("id", "user_id", "content") match {
        case Seq(JsString(id), JsString(userId), JsString(content)) =>
          Hobby(ID(id), ID(userId), Content(content))
        case _ => throw new DeserializationException("Hobby")
      }

    override def write(hobby: Hobby): JsValue = JsObject(
      "id" -> JsString(hobby.id.value),
      "user_id" -> JsString(hobby.userId.value),
      "content" -> JsString(hobby.content.value)
    )
  }
}
