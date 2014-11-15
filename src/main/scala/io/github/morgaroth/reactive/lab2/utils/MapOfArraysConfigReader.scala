package io.github.morgaroth.reactive.lab2.utils

import com.typesafe.config.{ConfigObject, ConfigValue, Config, ConfigFactory}

import scala.collection.JavaConverters._

class ConfigObjectWrapper(_key: String, _under: Config) {


  def getString(path: String): String = _under.getString(pathCorrector(path))

  def getDouble(path: String): Double = _under.getDouble(pathCorrector(path))

  private def pathCorrector(path: String) = s"${_key}.$path"
}

trait MapOfArraysConfigReader {

  type ConfigKey = String
  type ConfigValue = ConfigObjectWrapper

  def readMap[T](path: String, valueReader: (Config, ConfigKey) => T): Map[String, T] = {
    val configObj = ConfigFactory.load().getObject(path)
    configObj.keySet.asScala.map { key =>
      key -> valueReader(configObj.toConfig, key)
    }.toMap
  }

  def readList[T](path: String, valueReader: (ConfigKey, ConfigValue) => T): List[T] = {
    val configObj = ConfigFactory.load().getObject(path)
    configObj.keySet().asScala.map { key =>
      valueReader(key, new ConfigObjectWrapper(key, configObj.toConfig))
    }.toList
  }

  def readMapListStringAtPath(path: String) = readMap(path, _.getStringList(_).asScala.toList)

  def readMapStringAtPath(path: String) = readMap(path, _.getString(_))
}
