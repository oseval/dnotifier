package com.github.oseval.datahub.data

/**
  * Data which are associative and idempotent
  */
object AIDataOps {
  def nextClock(current: Long): Long =
    System.currentTimeMillis max (current + 1L)
}

/**
  * An A must be associative (could be evaluated in any order).
  * If it is not, then you have to use EffOnceData.
  * This data is not at-least-once, therefore each update must contain full data.
  * @param relations
  * @tparam A
  */
abstract class AIDataOps[A] extends DataOps {

  override type D = AIData[A]
  override val ordering: Ordering[Long] = Ordering.Long
  override val zero: D = AIData()

  override def merge(a: D, b: D): D =
    if (a.clock > b.clock) a else b

  override def diffFromClock(a: D, from: Long): D = a

  override def nextClock(current: Long): Long = AIDataOps.nextClock(current)
}

object AIData {
  def apply[A](data: A, clock: Long): AIData[A] =
    AIData(Some(data), clock)
}

case class AIData[A] protected (data: Option[A] = None, clock: Long = 0L) extends Data {
  override type C = Long
  def updated(update: A): AIData[A] = AIData(Some(update), AIDataOps.nextClock(clock))
}
