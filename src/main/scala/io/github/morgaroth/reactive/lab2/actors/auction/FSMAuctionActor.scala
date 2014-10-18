package io.github.morgaroth.reactive.lab2.actors.auction

import akka.actor._
import io.github.morgaroth.reactive.lab2.actors.auction.AuctionActorMessages._
import io.github.morgaroth.reactive.lab2.actors.auction.FSMAuctionActor._

import scala.concurrent.duration._

object FSMAuctionActor {
  sealed trait State
  case object Created extends State
  case object Active extends State
  case object Ignored extends State
  case object Sold extends State
  case object Deleted extends State

  sealed trait Data
  case object NotBiddedYet extends Data
  case class AuctionState(actualPrice: Double, winner: ActorRef) extends Data
}


class FSMAuctionActor(itemName: String, bidTime: FiniteDuration) extends Actor
with ActorLogging with FSM[State, Data] {

  import context.dispatcher

  startWith(Created, NotBiddedYet)

  when(Created) {
    case Event(bid: Bid, NotBiddedYet) =>
      log.info(s"${sender.path.name} starts auction with ${bid.price}DC")
      goto(Active) using AuctionState(bid.price, sender)
    case Event(TimeEnd, _) =>
      log.info(s"auction ends")
      context.system.scheduler.scheduleOnce(30 seconds, self, Delete)
      goto(Ignored) using NotBiddedYet
  }

  when(Active) {
    case Event(Bid(price), AuctionState(actualPrice, winner)) if price > actualPrice =>
      log.info(s"${sender.path.name} beat with ${price}DC")
      winner ! Beaten(actualPrice)
      sender ! OK
      stay using AuctionState(price, sender)
    case Event(Bid(_), state: AuctionState) =>
      sender ! NotEnough(state.actualPrice)
      stay()
    case Event(TimeEnd, state: AuctionState) =>
      log.info(s"auction ends")
      state.winner ! YouWin(itemName, state.actualPrice)
      context.system.scheduler.scheduleOnce(30 seconds, self, Delete)
      goto(Sold) using state
  }

  when(Ignored) {
    case Event(Delete, _) =>
      context.parent ! AuctionEnds(sold = false)
      stop()
    // coś co ma wyciągnąć go z powrotem do stanu Created albo Active
  }

  when(Sold) {
    case Event(Bid(_), _) =>
      sender ! ItemSold(itemName)
      stay()
    case Event(Delete, state: AuctionState) =>
      context.parent ! AuctionEnds(sold = true)
      stop()
  }

  context.system.scheduler.scheduleOnce(bidTime, self, TimeEnd)
}
