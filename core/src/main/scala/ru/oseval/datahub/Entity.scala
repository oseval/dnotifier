package ru.oseval.datahub

import ru.oseval.datahub.data.{Data, DataOps}

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration

trait Entity {
  val id: String
  val ops: DataOps
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