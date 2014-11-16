package io.github.morgaroth.reactive.lab2.actors

import akka.actor.Actor.Receive
import akka.actor._
import akka.event.LoggingReceive
import akka.routing._
import com.typesafe.config.ConfigFactory
import io.github.morgaroth.reactive.lab2.actors.AuctionSearch.{SearchResults, Search}
import io.github.morgaroth.reactive.lab2.actors.PartitionVariant.Collector
import net.ceedubs.ficus.Ficus._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext


class UnderlyingSearchers(val value: Long)

object UnderlyingSearchers {
  implicit def LongToUnderLyingSearchers(count: Long) = new UnderlyingSearchers(count)
  implicit def IntToUnderLyingSearchers(count: Int): UnderlyingSearchers = count.toLong
  implicit def unwrapUnderlyingSearchers(obj: UnderlyingSearchers) = obj.value
  implicit val defaultUnderlyingSearches = LongToUnderLyingSearchers(
    ConfigFactory.load().as[Option[Long]]("reactive.lab2.master-search.underlying-count").getOrElse(5L)
  )
}

trait MasterSearchRunningLogic {
  def name: String

  def searchingLogic: RoutingLogic

  def registeringLogic: RoutingLogic

  def performSearch(search: Search, requester: ActorRef, router: Router)(implicit ex: ActorRefFactory): Unit
}

object PartitionVariant {
  private[PartitionVariant] class Collector(msgs: Int, requester: ActorRef) extends Actor {
    val collected = ArrayBuffer[ActorRef]()
    var ctr = 0

    override def receive: Receive = {
      case SearchResults(auctions) =>
        collected ++= auctions
        ctr += 1
        if (ctr == msgs) {
          requester ! SearchResults(collected.toList)
          self ! PoisonPill
        }
    }
  }
}
class PartitionVariant extends MasterSearchRunningLogic {
  override def registeringLogic = RoundRobinRoutingLogic()
  override def searchingLogic = BroadcastRoutingLogic()
  override def performSearch(search: Search, requester: ActorRef, router: Router)(implicit ex: ActorRefFactory): Unit = {
    val collector = ex.actorOf(Props(classOf[Collector], router.routees.size, requester))
    router.route(search, collector)
  }
  override def name = "partition"
}
class ReplicatedVariant extends MasterSearchRunningLogic {
  override def registeringLogic = BroadcastRoutingLogic()
  override def searchingLogic = RoundRobinRoutingLogic()
  override def performSearch(search: Search, requester: ActorRef, router: Router)(implicit ex: ActorRefFactory): Unit = {
    router.route(search, requester)
  }
  override def name = "replicate"
}

object MasterSearchRunningLogic {
  implicit val defaultMasterSearchRunningLogic = {
    val value: Option[String] = ConfigFactory.load().as[Option[String]]("reactive.lab2.master-search.logic")
    value.map(_.toLowerCase).getOrElse("replicate") match {
      case "partition" => new PartitionVariant
      case "replicate" => new ReplicatedVariant
      case _ =>
        throw new IllegalArgumentException("incorrect value in reactive.lab2.master-search.logic config, correct partition(default) or replicate")
    }
  }
}

object MasterSearch {
  def props(
             implicit underlyingCount: UnderlyingSearchers,
             runningLogic: MasterSearchRunningLogic
             ) = Props(new MasterSearch)
}

class MasterSearch(
                    implicit underlyingCount: UnderlyingSearchers,
                    runningLogic: MasterSearchRunningLogic
                    )
  extends Actor with ActorLogging {

  log.info(s"MasterSearch actor configured to use ${underlyingCount.value} actors and '${runningLogic.name}' running logic")

  val routees: Vector[ActorRefRoutee] = Vector.fill(5) {
    val r = context.actorOf(Props[AuctionSearch])
    context watch r
    ActorRefRoutee(r)
  }

  var registeringRouter = Router(runningLogic.registeringLogic, routees)
  var searchingRouter = Router(runningLogic.searchingLogic, routees)
  var unregisteringRouter = Router(BroadcastRoutingLogic(), routees)

  override def receive = LoggingReceive {
    case w: AuctionSearch.Search =>
      runningLogic.performSearch(w, sender(), searchingRouter)
    case r: AuctionSearch.Register =>
      registeringRouter.route(r, sender())
    case ur: AuctionSearch.Unregister =>
      unregisteringRouter.route(ur, sender())
    case Terminated(a) =>
      registeringRouter = registeringRouter.removeRoutee(a)
      val r = context.actorOf(Props[AuctionSearch])
      context watch r
      registeringRouter = registeringRouter.addRoutee(r)
  }
}
