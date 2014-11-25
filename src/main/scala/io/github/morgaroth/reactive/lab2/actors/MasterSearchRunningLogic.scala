package io.github.morgaroth.reactive.lab2.actors

import akka.actor._
import akka.routing.{BroadcastRoutingLogic, RoundRobinRoutingLogic, Router, RoutingLogic}
import com.typesafe.config.ConfigFactory
import io.github.morgaroth.reactive.lab2.actors.AuctionSearch.{SearchResults, Search}
import io.github.morgaroth.reactive.lab2.actors.PartitionVariant.Collector
import net.ceedubs.ficus.Ficus._

import scala.collection.mutable.ArrayBuffer

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
  def apply() = new PartitionVariant
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

object ReplicatedVariant{
  def apply() = new ReplicatedVariant
}

class ReplicatedVariant extends MasterSearchRunningLogic {
  override def registeringLogic = BroadcastRoutingLogic()
  override def searchingLogic = RoundRobinRoutingLogic()
  override def performSearch(search: Search, requester: ActorRef, router: Router)(implicit ex: ActorRefFactory): Unit = {
    router.route(search, requester)
  }
  override def name = "replicate"
}
