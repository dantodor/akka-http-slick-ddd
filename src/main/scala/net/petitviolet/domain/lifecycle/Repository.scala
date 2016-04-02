package net.petitviolet.domain.lifecycle

import net.petitviolet.domain.support.{ Entity, Identifier }

import scala.concurrent.{ Future, ExecutionContext }

trait Repository[Id <: Identifier[_], E <: Entity[Id]] {

  def store(entity: E)(implicit ec: ExecutionContext): Future[Id]

  def resolveBy(id: Id)(implicit ec: ExecutionContext): Future[E]

  def existsBy(id: Id)(implicit ec: ExecutionContext): Future[Boolean]

  def deleteBy(id: Id)(implicit ec: ExecutionContext): Future[Boolean]
}