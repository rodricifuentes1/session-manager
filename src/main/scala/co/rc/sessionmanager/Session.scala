package co.rc.sessionmanager

import akka.actor.{ Scheduler, Cancellable, Props, Actor }
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext

/**
 * Class that represents the Session in memory
 * @param id Session id
 * @param expirationTime Represents the time of session expiration.
 *                       The first argument in the tuple is the time value (Integer)
 *                       The second argument in the tuple is the time unit (seconds, minutes, hours, days)
 */
class Session( id: String, expirationTime: ( Int, String ) ) extends Actor with LazyLogging {

  /**
   * Expiration task
   */
  private[ this ] var expirationTask: Cancellable = createExpirationTask()

  /**
   * Session receive method
   */
  override def receive: Receive = {
    case Session.Update =>
      logger.debug( s"Updating session with id $id ... expiration time $expirationTime" )
      expirationTask.cancel()
      expirationTask = createExpirationTask()
    case Session.Close =>
      logger.debug( s"Closing session with id $id ..." )
      expirationTask.cancel()
      context.stop( self )
    case _ =>
      sender() ! "Message not supported"
  }

  /**
   * Method that initialize expiration task
   * expirationUnit must be one of these: {seconds, minutes, hours, days}
   * expirationTime must be greater than 1 always
   * expirationTime must be greater than 10 if expirationUnit is seconds
   * default expirationTime is 10 and default expirationUnit is minutes
   * @return Expiration task: Cancellable
   */
  def createExpirationTask(): Cancellable = {

    // Execution context
    implicit val executor: ExecutionContext = context.dispatcher

    // System scheduler
    val scheduler: Scheduler = context.system.scheduler

    // Task creation
    expirationTime match {
      case ( value, "seconds" ) if value >= 10 => scheduler.scheduleOnce( value.seconds, self, Session.Close )
      case ( value, "minutes" ) if value >= 1  => scheduler.scheduleOnce( value.minutes, self, Session.Close )
      case ( value, "hours" ) if value >= 1    => scheduler.scheduleOnce( value.hours, self, Session.Close )
      case ( value, "days" ) if value >= 1     => scheduler.scheduleOnce( value.days, self, Session.Close )
      case _                                   => scheduler.scheduleOnce( 10.minutes, self, Session.Close )
    }

  }

}

/**
 * Companion object of session representation
 */
object Session {

  /**
   * Method that returns the session props.
   * @param id Session id
   * @param expirationTime Represents the time of session expiration.
   *                       The first argument in the tuple is the time value (Integer)
   *                       The second argument in the tuple is the time unit (seconds, minutes, hours, days)
   * @return Session actor created props
   */
  def props( id: String,
    expirationTime: ( Int, String ) ): Props = Props( new Session( id, expirationTime ) )

  /**
   * Object that defines "Update" action
   */
  case object Update

  /**
   * Object that defines "Close" action
   */
  case object Close

}
