package io.github.morgaroth.reactive.lab2.actors

import akka.actor.{ActorLogging, Actor, ActorRef}
import akka.event.LoggingReceive
import akka.pattern.ask
import akka.util.Timeout
import io.github.morgaroth.reactive.lab2.actors.Store.Auctions
import io.github.morgaroth.reactive.lab2.actors.auction.AuctionActorMessages._
import pl.morgaroth.utils.random.randoms

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Random

class Buyer(store: ActorRef) extends Actor with ActorLogging with randoms {
  implicit val t: Timeout = 5 seconds span

  import context._

  val items: Future[List[ActorRef]] = (store ? Auctions).mapTo[List[ActorRef]].map {
    auctions => new Random(Random.nextLong()).shuffle(auctions).take(2).map {
      auction =>
        auction ! Bid(0.1)
        auction
    }
  }

  var boughtCTR = 0

  def stopIfEnd = items.map {
    ims => if (ims.length == boughtCTR) {
      log.info(s"${self.path.name} die.")
      context stop self
    }
  }

  stopIfEnd

  override def receive = LoggingReceive {
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
      stopIfEnd

    case YouWin(item, price) =>
      log.info(s"I bought $item for ${price}DC")
      boughtCTR += 1
      stopIfEnd
  }
}
