package net.petitviolet.application.usecase.user

import net.petitviolet.application.usecase.BaseDTO
import net.petitviolet.domain.lifecycle.MixInUserRepository
import net.petitviolet.domain.support.{ ID, Identifier }
import net.petitviolet.domain.user.{ Email, Name, User }
import spray.json._

import scala.concurrent.{ ExecutionContext, Future }

trait UserCreateUseCase {
  def execute(userCreateDTO: UserCreateDTO)(implicit ec: ExecutionContext): Future[ID[User]]
}

trait UsesUserCreateUseCase {
  val userCreateUseCase: UserCreateUseCase
}

trait MixInUserCreateUseCase {
  val userCreateUseCase: UserCreateUseCase = new UserCreateUseCaseImpl
}

class UserCreateUseCaseImpl extends UserCreateUseCase with MixInUserRepository {
  override def execute(userCreateDTO: UserCreateDTO)(implicit ec: ExecutionContext): Future[ID[User]] = {
    val user = User(
      ID(Identifier.generate),
      Name(userCreateDTO.name),
      Email(userCreateDTO.email)
    )
    userRepository.store(user)
  }
}

case class UserCreateDTO(name: String, email: String) extends BaseDTO[User]

object UserCreateDTO {
  implicit val format = new RootJsonFormat[UserCreateDTO] {
    override def read(json: JsValue): UserCreateDTO =
      json.asJsObject.getFields("name", "email") match {
        case Seq(JsString(name), JsString(email)) =>
          UserCreateDTO(name, email)
        case _ => throw new DeserializationException("UserCreateDTO")
      }

    override def write(obj: UserCreateDTO): JsValue = JsObject(
      "name" -> JsString(obj.name),
      "email" -> JsString(obj.email)
    )
  }
}
