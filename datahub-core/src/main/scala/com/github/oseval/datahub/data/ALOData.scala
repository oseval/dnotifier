package com.github.oseval.datahub.data

import com.github.oseval.datahub.data.InferredOps.InferredOps

/**
  * This wrapper intended to add the at-least-one delivery control ability to data.
  * Useful in case of compound data where each pieces are ACID data and have a lot of such pieces
  * replicated separately. For example sequence of updates of some big data.
  * An A must be associative and idempotent.
  *
  * @tparam AO
  */
object ALODataOps {
  def apply[Dt <: Data](z: Dt)(implicit behavior: InferredOps.ImplicitClockBehavior[Dt#C]): ALODataOps[InferredOps[Dt]] =
    new ALODataOps[InferredOps[Dt]] {
      override protected val ops = InferredOps(z)
    }
  def apply[Ops <: DataOps](o: Ops): ALODataOps[Ops] =
    new ALODataOps[Ops] {
      override protected val ops: Ops = o
    }
}

trait ALODataOps[AO <: DataOps] extends DataOps {
  protected val ops: AO
  type A = ops.D
  type D = ALOData[ops.D]

  override lazy val ordering = ops.ordering
  override lazy val zero: ALOData[ops.D] = ALOData(ops.zero, ops.zero.clock, ops.zero.clock, None)

  override def diffFromClock(a: ALOData[A], from: A#C): ALOData[A] = {
    val res = ops.diffFromClock(a.data, from)
    ALOData(res, res.clock, from, a.further.map(diffFromClock(_, from)))
  }

  override def merge(a: D, b: D): D = {
    val (first, second) =  if (ordering.gt(a.clock, b.clock)) (b, a) else (a, b)

//    | --- | |---|
//
//    | --- |
//       | --- |
//
//      | --- |
//    | -------- |

    if (ordering.gteq(first.clock, second.previousClock)) {
      if (ordering.gteq(first.previousClock, second.previousClock)) second
      else {
        val visible = ALOData[A](
          data = ops.merge(first.data, second.data),
          second.clock,
          first.previousClock
        )

        val further = (first.further, second.further) match {
          case (Some(ff), Some(sf)) => Some(merge(ff, sf))
          case (Some(ff), None) => Some(ff)
          case (None, Some(sf)) => Some(sf)
          case (None, None) => None
        }

        further.map(merge(visible, _)).getOrElse(visible)
      }
    } else // further
      ALOData(
        ops.merge(first.data, second.data),
        first.clock,
        first.previousClock,
        first.further.map(merge(_, second)).orElse(Some(second))
      )
  }

  override def nextClock(current: ops.D#C): ops.D#C = ops.nextClock(current)
}

object ALOData {
  def apply[A <: Data](data: A)(implicit prevClock: A#C): ALOData[A] =
    ALOData(data, data.clock, prevClock)
}

case class ALOData[A <: Data](data: A,
                              clock: A#C,
                              previousClock: A#C,
                              private[data] val further: Option[ALOData[A]] = None
                             ) extends AtLeastOnceData {
  override type C = A#C
  val isSolid: Boolean = further.isEmpty
  def updated(updated: A): ALOData[A] =
    copy(data = updated, clock = updated.clock)
}
