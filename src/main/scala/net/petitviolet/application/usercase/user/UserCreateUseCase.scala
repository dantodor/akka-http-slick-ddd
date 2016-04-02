package net.petitviolet.application.usercase.user

import net.petitviolet.application.usercase.BaseDTO
import net.petitviolet.domain.lifecycle.MixInUserRepository
import net.petitviolet.domain.support.{ ID, Identifier }
import net.petitviolet.domain.user.{ Email, Name, User }
import net.petitviolet.infra.user.Users
import spray.json._

import scala.concurrent.{ Future, ExecutionContext }

trait UserCreateUseCase {
  def execute(userCreateDTO: UserCreateDTO)(implicit ec: ExecutionContext): Future[Boolean]
}

trait UsesUserCreateUseCase {
  val userCreateUseCase: UserCreateUseCase
}

trait MixInUserCreateUseCase {
  val userCreateUseCase: UserCreateUseCase = new UserCreateUseCaseImpl
}

class UserCreateUseCaseImpl extends UserCreateUseCase with MixInUserRepository {
  import slick.driver.MySQLDriver.api._
  override def execute(userCreateDTO: UserCreateDTO)(implicit ec: ExecutionContext): Future[Boolean] = {
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
