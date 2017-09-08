package ru.oseval.datahub

import org.scalatest
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike}
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import ru.oseval.datahub.Data.{GetDifferenceFrom, RelatedDataUpdated}

class DataHolderSpec extends FlatSpecLike
  with BeforeAndAfterAll
  with ScalaFutures
  with scalatest.Matchers
  with Eventually {

  import ProductTestData._
  import WarehouseTestData._

//  behavior of "Data holder"

//  it should "response on get difference from id" in {
//    val product = ProductEntity("1")
//    val productHolder = system.actorOf(productProps(product.ownId, notifier.ref))
//
//    productHolder ! Ping
//    expectMsg(Pong)
//    notifier.expectMsgType[Register]
//
//    productHolder ! GetDifferenceFrom(product.id, product.ops.zero.clock)
//    expectMsgType[ProductData] shouldEqual product.ops.zero
//
//    val newProductData = ProductData("Product name", 1, System.currentTimeMillis)
//    productHolder ! UpdateData(newProductData)
//    notifier.expectMsgType[NotifyDataUpdated].data shouldEqual newProductData
//    notifier.lastSender ! ()
//    expectMsgType[Unit]
//
//    productHolder ! GetDifferenceFrom(product.id, product.ops.zero.clock)
//    expectMsgType[ProductData] shouldEqual newProductData
//  }
//
//  it should "send data update to notifier" in {
//    val product = ProductEntity("2")
//    val productHolder = system.actorOf(productProps(product.ownId, notifier.ref))
//
//    productHolder ! Ping
//    expectMsg(Pong)
//    notifier.expectMsgType[Register]
//
//    val productData = ProductData("Product name", 1, System.currentTimeMillis)
//    productHolder ! UpdateData(productData)
//    val msg = notifier.expectMsgType[NotifyDataUpdated]
//    msg.entityId shouldBe product.id
//    msg.data.clock shouldBe productData.clock
//  }
//
//  it should "update related data" in {
//    val product = ProductEntity("3")
//    val warehouse = WarehouseEntity("1")
//    val warehouseHolder = system.actorOf(warehouseProps(warehouse.ownId, notifier.ref))
//
//    notifier.expectMsgType[Register]
//
//    warehouseHolder ! GetDifferenceFrom(warehouse.id, WarehouseOps.zero.clock)
//    expectMsgType[WarehouseData] shouldEqual WarehouseOps.zero
//
//    warehouseHolder ! AddProduct(product.ownId)
//    notifier.expectMsgType[NotifyDataUpdated]
//    warehouseHolder ! ()
//
//    warehouseHolder ! GetDifferenceFrom(warehouse.id, WarehouseOps.zero.clock)
//    expectMsgType[WarehouseData].products.values should contain(product.id)
//
//    val productData = ProductData("Product name", 1, System.currentTimeMillis)
//    warehouseHolder ! RelatedDataUpdated(warehouse.id, product.id, productData)
//    expectMsgType[Unit]
//  }
}