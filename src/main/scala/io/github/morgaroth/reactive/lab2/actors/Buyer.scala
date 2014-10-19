package io.github.morgaroth.reactive.lab2.actors

import akka.actor.{Actor, ActorLogging, ActorRef}
import akka.event.LoggingReceive
import io.github.morgaroth.reactive.lab2.actors.Store.Auctions
import io.github.morgaroth.reactive.lab2.actors.auction.AuctionActorMessages._
import pl.morgaroth.utils.random.randoms

import scala.concurrent.duration._
import scala.util.Random

class Buyer(store: ActorRef) extends Actor with ActorLogging with randoms {
  import context._

  var boughtCTR = 0
  var items: List[ActorRef] = _

  def stopIfEnd() = if (items.length == boughtCTR) {
    log.info(s"${self.path.name} die.")
    context stop self
  }

  override def receive = initializing

  store ! Auctions

  def initializing = LoggingReceive {
    case auctions: List[ActorRef] =>
      items = new Random(Random.nextLong()).shuffle(auctions).take(2).map {
        auction =>
          auction ! Bid(0.1)
          auction
      }
      stopIfEnd()
      context become working
  }


  def working = LoggingReceive {
    case OK =>
    case Beaten(price) =>
      val send = sender
      context.system.scheduler.scheduleOnce((randomInt(400) + 300) millis) {
        send.!(Bid(price + randGenerator.nextDouble()))(self)
      }
    case NotEnough(price) =>
      val send = sender
      context.system.scheduler.scheduleOnce((randomInt(400) + 300) millis) {
        send.!(Bid(price + randGenerator.nextDouble()))(self)
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
