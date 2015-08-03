package co.rc.sessionmanager

import com.typesafe.scalalogging.LazyLogging
import akka.actor.{ Terminated, Props, ActorRef, Actor }

/**
 * Class that represents a router that contains all in-memory sessions
 */
class SessionRouter() extends Actor with LazyLogging {

  /**
   * A map where sessions are stored
   */
  private[ this ] var sessions: Map[ String, ( ActorRef, SessionData ) ] = Map.empty[ String, ( ActorRef, SessionData ) ]

  /**
   * Session router receive method
   */
  override def receive: Receive = {
    case SessionRouter.CreateSession( sessionId, expirationTime, update, data ) => sender() ! createSession( sessionId, expirationTime, update, data )
    case SessionRouter.QuerySession( sessionId, update )                        => sender() ! querySession( sessionId, update )
    case SessionRouter.CloseSession( sessionId )                                => sender() ! closeSession( sessionId )
    case Terminated( actor ) =>
      sessions.find( _._2._1 == actor ) match {
        case Some( session ) =>
          logger.debug( s"A session has been terminated. Session id: ${session._1}. Removing session from sessions list ..." )
          sessions = sessions - session._1
        case None =>
          logger.debug( s"A session has been terminated but it looks it has already been removed from sessions list. " +
            s"Usually this is caused when a session is closed by command and not when expiration time is completed." )
      }
    case _ =>
      sender() ! "Message not supported"
  }

  /**
   * Method that creates a session
   * @param sessionId Session id
   * @param expirationTime Session expiration time
   * @param update True if session is supposed to be updated, false otherwise
   * @param data Data to store in session
   * @return SessionRouter.CreateActionResponse
   */
  private def createSession( sessionId: String,
    expirationTime: ( Int, String ),
    update: Boolean,
    data: SessionData ): SessionRouter.CreateActionResponse = {
    logger.debug( s"Create session request arrived. Session id: $sessionId. Verifying its existence ..." )
    sessions.keySet.contains( sessionId ) match {
      case false =>
        logger.debug( s"Session does not exist. Creating a new one with id $sessionId and expiration time $expirationTime" )
        val session: ActorRef = context.actorOf( Session.props( sessionId, expirationTime ), s"session-$sessionId" )
        sessions = sessions + ( ( sessionId, ( session, data ) ) )
        context watch session
        SessionRouter.SessionCreated( sessionId )
      case true =>
        logger.debug( s"Session already exist with id $sessionId." )
        if ( update ) sessions( sessionId )._1 ! Session.Update
        SessionRouter.SessionAlreadyExist( sessionId, update, sessions( sessionId )._2 )
    }
  }

  /**
   * Method that queries a session
   * @param sessionId Session id to query
   * @param update True if session is supposed to be updated, false otherwise
   * @return SessionRouter.QueryActionResponse
   */
  private def querySession( sessionId: String,
    update: Boolean ): SessionRouter.QueryActionResponse = {
    logger.debug( s"Query session request arrived. Session id: $sessionId. Verifying its existence ..." )
    sessions.keySet.contains( sessionId ) match {
      case false =>
        logger.debug( s"Requested session for query was not found. Session id: $sessionId" )
        SessionRouter.SessionNotFound( sessionId )
      case true =>
        logger.debug( s"Requested session for query was found with id $sessionId" )
        if ( update ) {
          logger.debug( s"Updating session expiration time ..." )
          sessions( sessionId )._1 ! Session.Update
        }
        SessionRouter.SessionFound( sessionId, update, sessions( sessionId )._2 )
    }
  }

  /**
   * Method that closes a session
   * @param sessionId Session id to close
   * @return SessionRouter.CloseActionResponse
   */
  private def closeSession( sessionId: String ): SessionRouter.CloseActionResponse = {
    logger.debug( s"Delete session request arrived. Session id: $sessionId. Verifying its existence ..." )
    sessions.keySet.contains( sessionId ) match {
      case false =>
        logger.debug( s"Requested session for deletion was not found. Session id: $sessionId" )
        SessionRouter.SessionNotClosed( sessionId )
      case true =>
        logger.debug( s"Requested session for deletion was found with id $sessionId. Deleting session ..." )
        sessions( sessionId )._1 ! Session.Close
        sessions = sessions - sessionId
        SessionRouter.SessionClosed( sessionId )
    }
  }

}

/**
 * Companion object of session router representation
 */
object SessionRouter {

  /**
   * Method that returns the session router props.
   * @return Session router created props
   */
  def props(): Props = Props( new SessionRouter() )

  // ------------------------------------------------
  // MESSAGES FOR CREATION ACTION
  // ------------------------------------------------

  /**
   * Class that defines "CreateSession" action
   * @param sessionId Session id
   * @param expirationTime Session expiration time
   * @param update True if session is supposed to be updated when it already exist
   *               False otherwise
   *               Default is false
   * @param data Data to store in the session
   */
  case class CreateSession( sessionId: String,
    expirationTime: ( Int, String ),
    update: Boolean = false,
    data: SessionData )

  /**
   * Represents a valid response for "CreateSession" action
   */
  sealed trait CreateActionResponse

  /**
   * "CreateSession" action was executed successfully
   * @param sessionId Created session id
   */
  case class SessionCreated( sessionId: String ) extends CreateActionResponse

  /**
   * "CreateSession" action was not executed successfully
   * The session with that already exist in the sessions map
   * @param sessionId Session id
   * @param updated If session was updated
   * @param data Stored data in the existent session
   */
  case class SessionAlreadyExist( sessionId: String,
    updated: Boolean,
    data: SessionData ) extends CreateActionResponse

  // ------------------------------------------------
  // MESSAGES FOR QUERY ACTION
  // ------------------------------------------------

  /**
   * Class that defines "QuerySession" action
   * @param sessionId Session id to query
   * @param update True if session is supposed to be updated when it is found
   *               False otherwise
   *               Default is true
   */
  case class QuerySession( sessionId: String,
    update: Boolean = true )

  /**
   * Represents a valid response for "QuerySession" action
   */
  sealed trait QueryActionResponse

  /**
   * "QuerySession" action was executed successfully
   * Session exist in the sessions map
   * @param sessionId Found session id
   * @param updated If session was updated when it was found
   * @param data Stored data in found session
   */
  case class SessionFound( sessionId: String,
    updated: Boolean,
    data: SessionData ) extends QueryActionResponse

  /**
   * "QuerySession" action was not executed successfully
   * Session with specified id was not found in the sessions map
   * @param sessionId Specified session id to query
   */
  case class SessionNotFound( sessionId: String ) extends QueryActionResponse

  // ------------------------------------------------
  // MESSAGES FOR CLOSE ACTION
  // ------------------------------------------------

  /**
   * Class that defines "CloseSession" action
   * @param sessionId Session id to close
   */
  case class CloseSession( sessionId: String )

  /**
   * Represents a valid response for "CloseSession" action
   */
  sealed trait CloseActionResponse

  /**
   * "CloseSession" action was executed successfully
   * @param sessionId Session id that was closed
   */
  case class SessionClosed( sessionId: String ) extends CloseActionResponse

  /**
   * "CloseSession" action was not executed successfully
   * @param sessionId Session id that was requested to close
   */
  case class SessionNotClosed( sessionId: String ) extends CloseActionResponse

}
