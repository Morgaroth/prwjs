package io.github.morgaroth.reactive.lab2.actors.auction

import akka.actor.ActorSystem
import akka.testkit.{TestProbe, ImplicitSender, TestFSMRef, TestKit}
import io.github.morgaroth.reactive.lab2.actors.auction.AuctionActorMessages.{OK, YouWin, Beaten, Offer}
import org.scalatest.{Matchers, OneInstancePerTest, WordSpecLike}

import scala.concurrent.duration._


class FSMAuctionActorTest extends TestKit(ActorSystem("test")) with WordSpecLike with Matchers with ImplicitSender with OneInstancePerTest {
  val underTest = TestFSMRef(new FSMAuctionActor("test_item", 20 seconds))

  "An actor" should {
    "change state to active after first offer" in {
      underTest ! Offer(0.1)
      underTest.stateName should be theSameInstanceAs FSMAuctionActor.Active
    }
    "change state to Ignored after 10 seconds" in {
      awaitAssert(underTest.stateName should be theSameInstanceAs FSMAuctionActor.Ignored, 20 seconds, 2 seconds)
    }
    "change state to Sold after bidTime expired and one offer were" in {
      underTest ! Offer(0.1)
      awaitAssert(underTest.stateName should be theSameInstanceAs FSMAuctionActor.Sold, 30 seconds, 3 seconds)
    }
    "notify previous buyer about beaten" in {
      val (b1, b2) = (TestProbe(), TestProbe())
      underTest.tell(Offer(0.1), b1.ref)
      b1.expectMsg(OK)
      underTest.tell(Offer(0.2), b2.ref)
      b1.expectMsgType[Beaten]
    }
    "notify buyer about auction end" in {
      val b1 = TestProbe()
      underTest.tell(Offer(0.1), b1.ref)
      awaitAssert(b1.expectMsgType[YouWin], 40 seconds, 4 seconds)
    }
    "notify buyer about successful Offer" in {
      val b1 = TestProbe()
      underTest.tell(Offer(0.1), b1.ref)
      b1.expectMsg(20 seconds, AuctionActorMessages.OK)
    }
  }
}
