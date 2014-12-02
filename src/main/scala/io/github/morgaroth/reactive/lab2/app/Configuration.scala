package io.github.morgaroth.reactive.lab2.app

import com.typesafe.config.ConfigFactory
import io.github.morgaroth.reactive.lab2.utils.MapOfArraysConfigReader
import net.ceedubs.ficus.Ficus._

case class SellerConf(name: String, items: List[String])
case class BuyerConf(name: String, target: String, maxPrice: Double)

trait Configuration {
  def sellers: List[SellerConf]

  def buyers: List[BuyerConf]

  def AuctionPublisherAddress: String
}

trait AppConfiguration extends Configuration with MapOfArraysConfigReader {
  override def sellers = readMapListStringAtPath("reactive.lab2.sellers").map(x => SellerConf(x._1, x._2)).toList

  override def buyers = readList("reactive.lab2.buyers",
    (key: ConfigKey, cfg: ConfigValue) => {
      BuyerConf(key, cfg.getString("target"), cfg.getDouble("max-price"))
    }
  )
  override def AuctionPublisherAddress: String = ConfigFactory.load().as[String]("reactive.lab2.AuctionPublisher.address")
}