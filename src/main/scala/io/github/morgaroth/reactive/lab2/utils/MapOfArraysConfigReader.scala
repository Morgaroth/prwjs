package io.github.morgaroth.reactive.lab2.utils

import com.typesafe.config.{Config, ConfigFactory}

import scala.collection.JavaConverters._

trait MapOfArraysConfigReader {

  def readMap[T](path: String, valueReader: (Config, String) => T): Map[String, T] = {
    val configObj = ConfigFactory.load().getObject(path)
    configObj.keySet.asScala.map { key =>
      key -> valueReader(configObj.toConfig, key)
    }.toMap
  }

  def readMapListStringAtPath(path: String) = readMap(path, _.getStringList(_).asScala.toList)

  def readMapStringAtPath(path: String) = readMap(path, _.getString(_))

}
