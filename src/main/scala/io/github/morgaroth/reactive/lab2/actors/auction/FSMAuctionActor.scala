package io.github.morgaroth.reactive.lab2.actors.auction

import akka.actor._
import io.github.morgaroth.reactive.lab2.actors.auction.AuctionActorMessages._
import io.github.morgaroth.reactive.lab2.actors.auction.FSMAuctionActor._

import scala.concurrent.duration._
import scala.language.postfixOps

object FSMAuctionActor {
  sealed trait State
  case object Created extends State
  case object Active extends State
  case object Ignored extends State
  case object Sold extends State
  case object Deleted extends State

  sealed trait Data
  case object NotYetAuctioned extends Data
  case class AuctionState(actualPrice: Double, winner: ActorRef) extends Data
}


class FSMAuctionActor(itemName: String, bidTime: FiniteDuration) extends Actor
with ActorLogging with FSM[State, Data] {

  import context.dispatcher

  context.system.scheduler.scheduleOnce(bidTime, self, TimeEnd)

  when(Created, stateTimeout = 10 seconds) {
    case Event(offer: Offer, NotYetAuctioned) =>
      log.info(s"${sender().path.name} starts auction with ${offer.price}DC")
      sender() ! OK
      goto(Active) using AuctionState(offer.price, sender())
    case Event(StateTimeout, _) | Event(TimeEnd, _) =>
      log.info(s"auction ends")
      context.system.scheduler.scheduleOnce(30 seconds, self, Delete)
      goto(Ignored) using NotYetAuctioned
  }

  when(Active) {
    case Event(Offer(price), AuctionState(actualPrice, winner)) if price > actualPrice =>
      log.info(s"${sender().path.name} beat with $price DC")
      winner ! Beaten(actualPrice)
      sender ! OK
      stay using AuctionState(price, sender())
    case Event(Offer(_), state: AuctionState) =>
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
    case Event(Offer(_), _) =>
      sender ! ItemSold(itemName)
      stay()
    case Event(Delete, state: AuctionState) =>
      context.parent ! AuctionEnds(sold = true)
      stop()
  }

  startWith(Created, NotYetAuctioned)

  initialize()
}
