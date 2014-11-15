package io.github.morgaroth.reactive.lab2.app

import io.github.morgaroth.reactive.lab2.utils.MapOfArraysConfigReader

case class SellerConf(name: String, items: List[String])
case class BuyerConf(name: String, target: String, maxPrice: Double)

trait Configuration {
  def sellers: List[SellerConf]

  def buyers: List[BuyerConf]
}

trait AppConfiguration extends Configuration with MapOfArraysConfigReader {
  override def sellers = readMapListStringAtPath("reactive.lab2.sellers").map(x => SellerConf(x._1, x._2)).toList

  override def buyers = readList("reactive.lab2.buyers",
    (key: ConfigKey, cfg: ConfigValue) => {
      print(cfg)
      BuyerConf(key, cfg.getString("target"), cfg.getDouble("max-price"))
    }
  )
}