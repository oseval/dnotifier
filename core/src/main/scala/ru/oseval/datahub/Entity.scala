package ru.oseval.datahub

import ru.oseval.datahub.data.{Data, DataOps}

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

trait Entity {
  type ID
  // TODO: add type annotation
  val ownId: ID
  val ops: DataOps

  lazy val id: String = makeId(ownId)

  def makeId(ownId: ID): String
}

trait EntityFacade {
  val entity: Entity

  /**
    * Request explicit data difference from entity
    * @param dataClock
    * @return
    */
  def getUpdatesFrom(dataClock: entity.ops.D#C)(implicit timeout: FiniteDuration): Future[entity.ops.D]

  /**
    * Receives updates of related external data
    * @param relatedId
    * @param relatedData
    * @return
    */
  def onUpdate(relatedId: String, relatedData: Data)(implicit timeout: FiniteDuration): Future[Unit]
}