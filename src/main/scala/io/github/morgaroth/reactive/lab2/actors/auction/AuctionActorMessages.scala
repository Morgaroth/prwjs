package io.github.morgaroth.reactive.lab2.actors.auction

object AuctionActorMessages {
  case class Bid(price: Double)
  case class YouWin(item: String, price: Double)
  case class ItemSold(item:String)
  case class Beaten(price: Double)
  case object OK
  case class NotEnough(price: Double)
  case class AuctionEnds(sold: Boolean)
  private[auction] case object TimeEnd
  private[auction] case object Delete
}
