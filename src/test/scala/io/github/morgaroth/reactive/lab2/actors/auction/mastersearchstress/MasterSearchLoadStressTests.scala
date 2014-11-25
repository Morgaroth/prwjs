package io.github.morgaroth.reactive.lab2.actors.auction.mastersearchstress

import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{TestKit, TestProbe}
import akka.util.Timeout
import io.github.morgaroth.reactive.lab2.actors.AuctionSearch.{Register, Search, SearchResults}
import io.github.morgaroth.reactive.lab2.actors._
import io.github.morgaroth.reactive.lab2.actors.auction.utils.LoremIpsum
import io.github.morgaroth.utils.files.PrintToFile
import io.github.morgaroth.utils.math.k._
import org.scalatest.WordSpecLike

import scala.collection.mutable
import scala.concurrent.duration._
import scala.util.Random

class TestData(val underTest: ActorRef, val allWords: List[String])
object TestData extends LoremIpsum {
  def apply(underlyingCount: UnderlyingSearchers, masterSearchLogic: MasterSearchRunningLogic, auctionsCount: Int, searchsCount: Int)(implicit system: ActorSystem) = {
    val underTest= system.actorOf(MasterSearch.props(underlyingCount, masterSearchLogic))
    val words = mutable.HashSet.empty[String]
    loremIpsumSentences(4, auctionsCount).map { name =>
      underTest.tell(Register(name), TestProbe().ref)
      words ++= name.toLowerCase.split(" ")
    }
    new TestData(underTest, words.toList)
  }
}

abstract class MasterSearchLoadStressTestsBase extends TestKit(ActorSystem("loadTests")) with WordSpecLike with PrintToFile {
  def underlyingCount: UnderlyingSearchers
  def masterSearchLogic: MasterSearchRunningLogic
  def auctionsCount: Int
  def searchsCount: Int

  val log = akka.event.Logging.apply(system, "test")

  val times = new mutable.ArrayBuffer[Long] with scala.collection.mutable.SynchronizedBuffer[Long]

  implicit val t: Timeout = 20 minutes

  import akka.pattern.ask
  import system.dispatcher

  s"executing $searchsCount searching requests" in {
    val dataStart = System.currentTimeMillis()
    val data = TestData(underlyingCount, masterSearchLogic, auctionsCount, searchsCount)
    val dataEnd = System.currentTimeMillis()
    log.error(s"preparation of data for ${underlyingCount.value} underlying actors in ${masterSearchLogic.name} logic takes ${(dataEnd - dataStart) / 1000}s.\n")
    1 to searchsCount foreach { i =>
      val start = System.nanoTime()
      (data.underTest ? Search(data.allWords(Random.nextInt(data.allWords.size)))).collect {
        case SearchResults(list) =>
          times += (System.nanoTime() - start)
          log.info("received " + times.size)
        case another =>
          log.error("unrecognized response " + another)
      }
    }
    awaitCond(times.size >= searchsCount, 60.minutes, 1.millis)
    log.error(s"test for ${underlyingCount.value} underlying actors in ${masterSearchLogic.name} logic takes ${System.currentTimeMillis() - dataEnd}ms.")
    val mean: Long = (0L /: times)(_ + _) / times.size
    log.error(s"mean ${mean}ns, ${mean * 1.0 / 1000000000}s")
    val path = s"${searchsCount}_searchs__${underlyingCount.value}_underlying_actors__${masterSearchLogic.name}_logic.data"
    times.mkString("\n").printToFile(path)
  }
}

abstract class AuctionsSearchs100k extends MasterSearchLoadStressTestsBase {
  override val searchsCount: Int = 50.k
  override val auctionsCount: Int = 50.k
}

abstract class Replicated extends AuctionsSearchs100k {
  override val masterSearchLogic = new ReplicatedVariant
}

abstract class Partitioned extends AuctionsSearchs100k {
  override val masterSearchLogic = new PartitionVariant
}

class Replicated5Actors extends Replicated {
  override val underlyingCount = UnderlyingSearchers(5)
}

class Replicated1Actor extends Replicated {
  override val underlyingCount = UnderlyingSearchers(1)
}

class Replicated10Actors extends Replicated {
  override val underlyingCount = UnderlyingSearchers(10)
}

class Replicated50Actors extends Replicated {
  override val underlyingCount = UnderlyingSearchers(50)
}

class Replicated100Actors extends Replicated {
  override val underlyingCount = UnderlyingSearchers(100)
}

class Partitioned10Actors extends Partitioned {
  override val underlyingCount = UnderlyingSearchers(10)
}

class Partitioned50Actors extends Partitioned {
  override val underlyingCount = UnderlyingSearchers(50)
}

class Partitioned100Actors extends Partitioned {
  override val underlyingCount = UnderlyingSearchers(100)
}

class Partitioned5Actors extends Partitioned {
  override val underlyingCount = UnderlyingSearchers(5)
}

class Partitioned1Actor extends Partitioned {
  override val underlyingCount = UnderlyingSearchers(1)
}
