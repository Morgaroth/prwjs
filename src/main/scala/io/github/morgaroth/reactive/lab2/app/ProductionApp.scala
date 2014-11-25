package io.github.morgaroth.reactive.lab2.app

object ProductionApp extends AppConfiguration with AppCoreSystem with Application {
  def main(args: Array[String]) {
    run()
  }
}
