package io.github.morgaroth.reactive.lab2.actors

import akka.actor._
import akka.event.LoggingReceive
import io.github.morgaroth.reactive.lab2.actors.AuctionSearch.{Search, SearchResults}
import io.github.morgaroth.reactive.lab2.actors.auction.AuctionActorMessages._
import io.github.morgaroth.reactive.lab2.utils.randomsDSL
import io.github.morgaroth.utils.random.randoms

import scala.concurrent.duration._

class Buyer(target: String, maxPrice: Double) extends Actor with ActorLogging with randoms with randomsDSL {

  import context._

  val auctionSearch = context actorSelection "/user/AuctionSearch"

  auctionSearch ! Search(target)

  var boughtCTR = 0
  var items: List[ActorRef] = _

  def stopIfEnd() = if (items.length == boughtCTR) {
    log.info(s"${self.path.name} die.")
    self ! PoisonPill
  }

  override def receive = initializing

  def initializing = LoggingReceive {
    case SearchResults(auctions) =>
      items = auctions
      items.foreach(_ ! Offer(0.1))
      stopIfEnd()
      context become working
  }

  def working = LoggingReceive {
    case OK =>
    case Beaten(price) =>
      offerInAuction(price, sender())

    case NotEnough(price) =>
      offerInAuction(price, sender())

    case ItemSold(item) =>
      log.info(s"I not bought $item")
      boughtCTR += 1
      stopIfEnd()

    case YouWin(item, price) =>
      log.info(s"I bought $item for ${price}DC")
      boughtCTR += 1
      stopIfEnd()
  }

  def offerInAuction(actualPrice: Double, auction: ActorRef) {
    maxPrice - actualPrice match {
      case farFromMax if farFromMax > 1 =>
        sendOfferToAuction(auction, actualPrice + randDouble)
      case notFarFromMax if notFarFromMax > 0 =>
        sendOfferToAuction(auction, maxPrice)
      case moreThanMax =>
        boughtCTR += 1
        log.info(s"I not bought from ${auction.path.name} because price is to high")
        stopIfEnd()
    }
  }
  
  def sendOfferToAuction(auction: ActorRef, price: Double) {
    context.system.scheduler.scheduleOnce((randomInt(200) + 100) millis) {
      auction.!(Offer(price))(self)
    }
  }
}
