package vbakaev.app

import java.time.Clock

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.scalalogging.LazyLogging
import vbakaev.app.config.{AppConfig, ServerConfig}
import pureconfig.ConfigSource
import pureconfig.generic.auto._
import vbakaev.app.interfaces.http.ServerRoutes

import scala.concurrent.ExecutionContextExecutor

object Main extends App with LazyLogging {
  implicit val clock: Clock                               = Clock.systemUTC()
  implicit val system: ActorSystem                        = ActorSystem()
  implicit val executionContext: ExecutionContextExecutor = system.dispatcher

  ConfigSource.default
    .load[AppConfig]
    .fold(
      error => logger.error(s"Configuration loading error $error"),
      config => {
        logger.info(s"App starting with configuration: $config")
        val ServerConfig(appRoot, interface, port) = config.http

        val serverRoutes = new ServerRoutes(config).routes
        Http().newServerAt(interface, port).bindFlow(serverRoutes)

        logger.info(s"Server is running on http://$appRoot/status")
        logger.info(s"See documentation http://$appRoot/docs")
      }
    )

  logger.info("Running...")

}
