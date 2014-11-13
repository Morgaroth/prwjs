package io.github.morgaroth.reactive.lab2.actors

import akka.actor._
import akka.event.LoggingReceive
import io.github.morgaroth.reactive.lab2.actors.AuctionSearch.{SearchResults, Search}
import io.github.morgaroth.reactive.lab2.actors.auction.AuctionActorMessages._
import pl.morgaroth.utils.random.randoms

import scala.concurrent.duration._
import scala.util.Random

class Buyer(target: String) extends Actor with ActorLogging with randoms {

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
      val send = sender()
      context.system.scheduler.scheduleOnce((randomInt(200) + 100) millis) {
        send.!(Offer(price + randGenerator.nextDouble()))(self)
      }
    case NotEnough(price) =>
      val send = sender()
      context.system.scheduler.scheduleOnce((randomInt(200) + 100) millis) {
        send.tell(Offer(price + randGenerator.nextDouble()), self)
      }

    case ItemSold(item) =>
      log.info(s"I not bought $item")
      boughtCTR += 1
      stopIfEnd()

    case YouWin(item, price) =>
      log.info(s"I bought $item for ${price}DC")
      boughtCTR += 1
      stopIfEnd()
  }
}
