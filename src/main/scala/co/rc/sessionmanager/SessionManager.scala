package co.rc.sessionmanager

import akka.actor.{ ActorRef, ActorSystem }
import akka.pattern.ask
import akka.util.Timeout

import com.typesafe.config.{ ConfigFactory, Config }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.ExecutionContext.Implicits

/**
 * Trait that provides actorSystem context
 */
sealed trait SystemContext {

  /**
   * Method that returns an ActorSystem
   * @return ActorSystem
   */
  implicit def system: ActorSystem

}

/**
 * Trait that creates SessionRouter Actor
 */
sealed trait ActorFactory { this: SystemContext =>

  /**
   * Session router actor instance
   */
  val sessionRouter: ActorRef = system.actorOf( SessionRouter.props(), "session-router" )

}

/**
 * Class that acts as a facade for an easy session manipulation
 * @param system An existent actor system for session router creation
 * @param config Application configuration resource
 */
class SessionManager()( implicit val system: ActorSystem = ActorSystem( "session-actor-system" ),
  context: ExecutionContext = Implicits.global,
  config: Config = ConfigFactory.load ) extends SystemContext
    with ActorFactory {

  /**
   * Creates a new session
   * @param id Session Id
   * @param data Data to store in session memory
   * @return A future with CreationResponse
   */
  def createSession( id: String,
    update: Boolean,
    data: SessionData = EmptyData,
    expirationTimeValue: Int = config.getInt( "co.rc.sessionmanager.exptime-value" ),
    expirationTimeUnit: String = config.getString( "co.rc.sessionmanager.exptime-unit" ) )( implicit timeout: Timeout = Timeout( config.getInt( "co.rc.sessionmanager.ask-timeout" ).seconds ) ): Future[ SessionRouter.CreateActionResponse ] = {

    ( sessionRouter ? SessionRouter.CreateSession(
      id,
      ( expirationTimeValue, expirationTimeUnit ),
      update,
      data
    )
    ).mapTo[ SessionRouter.CreateActionResponse ]

  }

  /**
   * Method that validates a session
   * @param id Session id
   * @return A future with SessionRouterResponse
   */
  def querySession( id: String,
    update: Boolean )( implicit timeout: Timeout = Timeout( config.getInt( "co.rc.sessionmanager.ask-timeout" ).seconds ) ): Future[ SessionRouter.QueryActionResponse ] = {

    ( sessionRouter ? SessionRouter.QuerySession( id, update ) ).mapTo[ SessionRouter.QueryActionResponse ]

  }

  /**
   * Method that deletes a session
   * @param id Session id
   * @return A future with SessionRouterResponse
   */
  def closeSession( id: String )( implicit timeout: Timeout = Timeout( config.getInt( "co.rc.sessionmanager.ask-timeout" ).seconds ) ): Future[ SessionRouter.CloseActionResponse ] = {

    ( sessionRouter ? SessionRouter.CloseSession( id ) ).mapTo[ SessionRouter.CloseActionResponse ]

  }

}

