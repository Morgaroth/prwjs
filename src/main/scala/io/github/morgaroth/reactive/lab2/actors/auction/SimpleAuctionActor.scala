package io.github.morgaroth.reactive.lab2.actors.auction

import akka.actor.{Actor, ActorLogging, ActorRef, PoisonPill}
import akka.event.LoggingReceive
import io.github.morgaroth.reactive.lab2.actors.auction.AuctionActorMessages._

import scala.concurrent.duration._


class SimpleAuctionActor(itemName: String, bidTime: FiniteDuration)
  extends Actor with ActorLogging {

  import context._

  var actualPrice: Double = _
  context.system.scheduler.scheduleOnce(bidTime, self, TimeEnd)

  override def receive = created

  var winner: Option[ActorRef] = None

  def active = LoggingReceive {
    case Offer(price) if price > actualPrice =>
      actualPrice = price
      winner.map(_ ! Beaten(actualPrice))
      winner = Some(sender)
      sender ! OK
    case Offer(_) =>
      sender ! NotEnough(actualPrice)
    case TimeEnd =>
      winner.map(_ ! YouWin(itemName, actualPrice))
      parent ! AuctionEnds(sold = true)
      context.system.scheduler.scheduleOnce(30 seconds, self, Delete)
      context become sold
  }

  def created = LoggingReceive {
    case TimeEnd =>
      context.system.scheduler.scheduleOnce(30 seconds, self, Delete)
      context become ignored
    case bid: Offer =>
      context become active
      self forward bid
  }

  def ignored: Receive = LoggingReceive {
    case Delete =>
      parent ! AuctionEnds(sold = false)
      self ! PoisonPill
  }

  def sold = LoggingReceive {
    case Delete =>
      self ! PoisonPill
  }
}
