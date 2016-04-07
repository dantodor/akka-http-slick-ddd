package net.petitviolet.application.usecase.user

import net.petitviolet.application.usecase.BaseDTO
import net.petitviolet.domain.lifecycle.MixInUserRepository
import net.petitviolet.domain.support.{ ID, Identifier }
import net.petitviolet.domain.user.{ Content, Hobby, User }
import spray.json._

import scala.concurrent.{ ExecutionContext, Future }

case class AddHobbyDTO(userId: ID[User], content: Content) extends BaseDTO[Hobby]

trait AddHobbyUseCase {
  def execute(addHobbyDTO: AddHobbyDTO)(implicit ec: ExecutionContext): Future[ID[Hobby]]
}

trait UsesAddHobbyUseCase {
  val addHobbyUseCase: AddHobbyUseCase
}

trait MixInAddHobbyUseCase {
  val addHobbyUseCase: AddHobbyUseCase = new AddHobbyUseCaseImpl
}

class AddHobbyUseCaseImpl extends AddHobbyUseCase with MixInUserRepository {
  override def execute(addHobbyDTO: AddHobbyDTO)(implicit ec: ExecutionContext): Future[ID[Hobby]] = {
    val hobby = Hobby(
      ID(Identifier.generate),
      addHobbyDTO.userId,
      addHobbyDTO.content
    )
    userRepository.addHobby(hobby)
  }
}

object AddHobbyDTO {
  implicit val format = new RootJsonFormat[AddHobbyDTO] {
    override def read(json: JsValue): AddHobbyDTO =
      json.asJsObject.getFields("user_id", "content") match {
        case Seq(JsString(userId), JsString(content)) =>
          AddHobbyDTO(ID(userId), Content(content))
        case _ => throw new DeserializationException("AddHobbyDTO")
      }

    override def write(obj: AddHobbyDTO): JsValue = JsObject(
      "user_id" -> JsString(obj.userId.value),
      "content" -> JsString(obj.content.value)
    )
  }
}

