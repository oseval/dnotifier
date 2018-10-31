package ru.oseval.datahub

import org.scalatest.{FlatSpecLike, Matchers}
import ru.oseval.datahub.data.{ClockInt, SetDataOps}

class SetDataSpec extends FlatSpecLike with Matchers {
  behavior of "SetData"

  private implicit val cint: ClockInt[Int] = ClockInt(0, 0)
  private val zeroData = SetDataOps.zero[Int, Int]
  private val seedData = 1 to 60

//  it should "add elements" in {
//    val data = seedData.grouped(20).zipWithIndex.map { case (elms, i) =>
//      elms.foldLeft((i*20, zeroData)) { case ((ctime, d), elm) =>
//        val nextTime = ctime + 1
//        (nextTime, d.add(elm)(ClockInt(nextTime, ctime)))
//      }._2
//    }.toList
//
//    val res = (data ++ data).permutations.foldLeft(zeroData)((d, s) =>
//      s.fold(d)(SetDataOps.combine)
//    )
//    res.elements shouldBe seedData.toList
//  }
//
//  it should "add and remove elements" in {
//    val data = seedData.grouped(20).zipWithIndex.map { case (elms, i) =>
//      elms.foldLeft((i*20, zeroData)) { case ((ctime, d), elm) =>
//        val nextTime = ctime + 1
//        if (elm % 3 == 1) (nextTime, d.remove(elm - 1)(ClockInt(nextTime, ctime)))
//        else (nextTime, d.add(elm)(ClockInt(nextTime, ctime)))
//      }._2
//    }.toList
//
//    val res = (data ++ data).permutations.foldLeft(zeroData)((r, s) => s.fold(zeroData)(SetDataOps.combine))
//    res.elements shouldBe seedData.filterNot(_ % 3 == 1).toList
//  }
}
