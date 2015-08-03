package co.rc.sessionmanager.utils

import akka.testkit.{ ImplicitSender, TestKit }
import akka.actor.ActorSystem

import com.typesafe.config.{ ConfigFactory, Config }
import org.specs2.specification.After

import scala.concurrent.ExecutionContext

/**
 * An abstract class that can be used as a Specs2 'context' in test that involves akka
 */
abstract class AkkaSpecs2Support extends TestKit( ActorSystem( "test-actor-system" ) )
    with ImplicitSender
    with After {

  /**
   * Implicit app configuration
   */
  implicit val config: Config = ConfigFactory.load

  /**
   * Implicit execution context
   */
  implicit val executionContext: ExecutionContext = system.dispatcher

  /**
   * shutdown test actor system
   */
  override def after = system.shutdown()

}
