import spray.json._


//case class User(id: Long, name: String)
//
//object UserJsonProtocol extends DefaultJsonProtocol {
//  implicit val format = jsonFormat2(User.apply)
//  //  implicit val format: RootJsonFormat[User] = jsonFormat(User.apply, "user_id", "user_name")
//}
//
//import UserJsonProtocol._
//
//// case class -> json
//val user = User(100, "alice")
//user.toJson
//// json -> case class
//val json = "{\"id\":10,\"name\":\"bob\"}"
//json.parseJson.convertTo[User]


trait Identifier[+A] extends Any {
  def value: A
}

object Identifier {
  type IdType = String
}

import Identifier._

case class ID[A <: Entity[_]](value: IdType) extends Identifier[IdType]

object IDJsonProtocol extends DefaultJsonProtocol {
  implicit def idFormat[A <: Entity[_]] = new RootJsonFormat[ID[A]] {
    override def read(json: JsValue): ID[A] =
      json.asJsObject.getFields("id") match {
        case Seq(JsString(id)) => ID(id)
        case _ => throw new DeserializationException("ID")
      }

    override def write(obj: ID[A]): JsValue = JsObject("id" -> JsString(obj.value))
  }
}

trait Entity[Id <: Identifier[_]] {
  val id: Id
}

case class User(id: ID[User], name: Name, email: Email) extends Entity[ID[User]]

case class Name(value: String)
case class Email(value: String)
object UserJsonProtocol extends DefaultJsonProtocol {
  // これらではうまくいかない。。
//    implicit val idFormat = jsonFormat1(ID.apply[User])
//    implicit val userIdFormat = IDJsonProtocol.idFormat[User]
//    implicit val nameFormat = jsonFormat1(Name.apply)
//    implicit val emailFormat = jsonFormat1(Email.apply)
//    implicit val userFormat = jsonFormat3(User.apply)

  implicit val userFormat: RootJsonFormat[User] = new RootJsonFormat[User] {
    override def read(json: JsValue): User =
      json.asJsObject.getFields("id", "name", "email") match {
        case Seq(JsString(id), JsString(name), JsString(email)) =>
          User(ID(id), Name(name), Email(email))
        case _ => throw new DeserializationException("User")
      }

    override def write(user: User): JsValue = JsObject(
      "id" -> JsString(user.id.value),
      "name" -> JsString(user.name.value),
      "email" -> JsString(user.email.value)
    )
  }
}

import UserJsonProtocol.userFormat
val user = User(ID("alice-id"), Name("alice"), Email("alice@example.com"))
user.toJson
