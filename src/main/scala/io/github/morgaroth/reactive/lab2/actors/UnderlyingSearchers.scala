package io.github.morgaroth.reactive.lab2.actors

import com.typesafe.config.ConfigFactory
import net.ceedubs.ficus.Ficus._

class UnderlyingSearchers(val value: Long)

object UnderlyingSearchers {
  implicit def LongToUnderLyingSearchers(count: Long) = new UnderlyingSearchers(count)
  implicit def IntToUnderLyingSearchers(count: Int): UnderlyingSearchers = count.toLong
  implicit def unwrapUnderlyingSearchers(obj: UnderlyingSearchers) = obj.value
  implicit val defaultUnderlyingSearches = LongToUnderLyingSearchers(
  ConfigFactory.load().as[Option[Long]]("reactive.lab2.master-search.underlying-count").getOrElse(5L)
  )
}
