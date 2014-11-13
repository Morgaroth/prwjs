package io.github.morgaroth.reactive.lab2.actors

import akka.actor.{ActorLogging, Actor, Terminated, ActorRef}

import scala.collection.mutable.ArrayBuffer

object SoulsReaper {
  // Used by others to register an Actor for watching
  case class WatchMe(ref: ActorRef)
  case class WatchHim(ref: ActorRef)
}

abstract class SoulsReaper extends Actor with ActorLogging {

  import SoulsReaper._

  // Keep track of what we're watching
  val watched = ArrayBuffer.empty[ActorRef]

  // Derivations need to implement this method.  It's the
  // hook that's called when everything's dead
  def allSoulsReaped(): Unit


  private def watchActor(ref: ActorRef) = {
    log.info(s"registering ref=$ref, currently have ${watched.size} registered")
    log.debug(s"registering ref=$ref, currently have registered: $watched")
    context.watch(ref)
    watched += ref
  }

  // Watch and check for termination
  final def receive = {
    case WatchMe(ref) => this watchActor ref
    case WatchHim(ref) => this watchActor ref
    case Terminated(ref) =>
      watched -= ref
      log.info(s"unregistering ref=$ref, currently have ${watched.size} registered")
      log.debug(s"unregistering ref=$ref, currently have registered: $watched")
      if (watched.isEmpty) allSoulsReaped()
  }
}
