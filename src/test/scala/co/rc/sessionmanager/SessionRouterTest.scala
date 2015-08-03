package co.rc.sessionmanager

import akka.actor.ActorRef
import co.rc.sessionmanager.utils.{ TestData, AkkaSpecs2Support }
import org.specs2.mutable.Specification
import scala.concurrent.duration._

/**
 * Test specification for SessionRouter Actor
 * Max duration for each test is: 10 seconds
 */
class SessionRouterTest extends Specification {
  sequential

  "Session router specification" should {

    // ------------------------------------------------
    // Expiration time tests
    // ------------------------------------------------

    "Create Session: Create a new session with 10 seconds of expirationTime" in new AkkaSpecs2Support {
      within( 10 seconds ) {
        val sessionRouter: ActorRef = system.actorOf( SessionRouter.props(), "test-session-router" )
        sessionRouter ! SessionRouter.CreateSession( "CS:10-seconds", ( 10, "seconds" ), update = false, TestData( 1 ) )
        val msg: SessionRouter.SessionCreated = expectMsgClass( classOf[ SessionRouter.SessionCreated ] )
        msg.sessionId must_== "CS:10-seconds"
      }
    }

    "Create Session: Create a new session with 1 minute of expirationTime" in new AkkaSpecs2Support {
      within( 10 seconds ) {
        val sessionRouter: ActorRef = system.actorOf( SessionRouter.props(), "test-session-router" )
        sessionRouter ! SessionRouter.CreateSession( "CS:1-minute", ( 1, "minutes" ), update = false, TestData( 1 ) )
        val msg: SessionRouter.SessionCreated = expectMsgClass( classOf[ SessionRouter.SessionCreated ] )
        msg.sessionId must_== "CS:1-minute"
      }
    }

    "Create Session: Create a new session with 1 hour of expirationTime" in new AkkaSpecs2Support {
      within( 10 seconds ) {
        val sessionRouter: ActorRef = system.actorOf( SessionRouter.props(), "test-session-router" )
        sessionRouter ! SessionRouter.CreateSession( "CS:1-hour", ( 1, "hours" ), update = false, TestData( 1 ) )
        val msg: SessionRouter.SessionCreated = expectMsgClass( classOf[ SessionRouter.SessionCreated ] )
        msg.sessionId must_== "CS:1-hour"
      }
    }

    "Create Session: Create a new session with 1 day of expirationTime" in new AkkaSpecs2Support {
      within( 10 seconds ) {
        val sessionRouter: ActorRef = system.actorOf( SessionRouter.props(), "test-session-router" )
        sessionRouter ! SessionRouter.CreateSession( "CS:1-day", ( 1, "days" ), update = false, TestData( 1 ) )
        val msg: SessionRouter.SessionCreated = expectMsgClass( classOf[ SessionRouter.SessionCreated ] )
        msg.sessionId must_== "CS:1-day"
      }
    }

    "Create Session: Create a new session with unmatched expirationTime (0 centuries)" in new AkkaSpecs2Support {
      within( 10 seconds ) {
        val sessionRouter: ActorRef = system.actorOf( SessionRouter.props(), "test-session-router" )
        sessionRouter ! SessionRouter.CreateSession( "CS:0-centuries", ( 0, "centuries" ), update = false, TestData( 1 ) )
        val msg: SessionRouter.SessionCreated = expectMsgClass( classOf[ SessionRouter.SessionCreated ] )
        msg.sessionId must_== "CS:0-centuries"
      }
    }

    // ------------------------------------------------
    // Create session tests
    // ------------------------------------------------

    "Create Session: Create a new session" in new AkkaSpecs2Support {
      within( 10 seconds ) {
        val sessionRouter: ActorRef = system.actorOf( SessionRouter.props(), "test-session-router" )
        sessionRouter ! SessionRouter.CreateSession( "CS:new-session", ( 1, "minutes" ), update = false, TestData( 1 ) )
        val msg: SessionRouter.SessionCreated = expectMsgClass( classOf[ SessionRouter.SessionCreated ] )
        msg.sessionId must_== "CS:new-session"
      }
    }

    "Create Session: Existing session. NOT update session expiration time." in new AkkaSpecs2Support {
      within( 10 seconds ) {

        // FIRST CREATION
        val sessionRouter: ActorRef = system.actorOf( SessionRouter.props(), "test-session-router" )
        sessionRouter ! SessionRouter.CreateSession( "CS:existing-session1", ( 1, "minutes" ), update = false, TestData( 1 ) )
        val msg: SessionRouter.SessionCreated = expectMsgClass( classOf[ SessionRouter.SessionCreated ] )
        msg.sessionId must_== "CS:existing-session1"

        // SECOND CREATION
        sessionRouter ! SessionRouter.CreateSession( "CS:existing-session1", ( 1, "minutes" ), update = false, TestData( 1 ) )
        val msg2: SessionRouter.SessionAlreadyExist = expectMsgClass( classOf[ SessionRouter.SessionAlreadyExist ] )
        msg2.sessionId must_== "CS:existing-session1"
        msg2.updated must_== false
        msg2.data must beAnInstanceOf[ TestData ]
        msg2.data.asInstanceOf[ TestData ].data must_== 1
      }
    }

    "Create Session: Existing session. Update session expiration time." in new AkkaSpecs2Support {
      within( 10 seconds ) {

        // FIRST CREATION
        val sessionRouter: ActorRef = system.actorOf( SessionRouter.props(), "test-session-router" )
        sessionRouter ! SessionRouter.CreateSession( "CS:existing-session2", ( 1, "minutes" ), update = false, TestData( 1 ) )
        val msg: SessionRouter.SessionCreated = expectMsgClass( classOf[ SessionRouter.SessionCreated ] )
        msg.sessionId must_== "CS:existing-session2"

        // SECOND CREATION
        sessionRouter ! SessionRouter.CreateSession( "CS:existing-session2", ( 1, "minutes" ), update = true, TestData( 1 ) )
        val msg2: SessionRouter.SessionAlreadyExist = expectMsgClass( classOf[ SessionRouter.SessionAlreadyExist ] )
        msg2.sessionId must_== "CS:existing-session2"
        msg2.updated must_== true
        msg2.data must beAnInstanceOf[ TestData ]
        msg2.data.asInstanceOf[ TestData ].data must_== 1
      }
    }

    // ------------------------------------------------
    // Query session tests
    // ------------------------------------------------

    "Query Session: NOT find a non existing session" in new AkkaSpecs2Support {
      within( 10 seconds ) {
        val sessionRouter: ActorRef = system.actorOf( SessionRouter.props(), "session-router" )
        sessionRouter ! SessionRouter.QuerySession( "QS:nonexisting", update = false )
        val msg: SessionRouter.SessionNotFound = expectMsgClass( classOf[ SessionRouter.SessionNotFound ] )
        msg.sessionId must_== "QS:nonexisting"
      }
    }

    "Query Session: Find a session. NOT update expiration time" in new AkkaSpecs2Support {
      within( 10 seconds ) {

        // FIRST CREATION
        val sessionRouter: ActorRef = system.actorOf( SessionRouter.props(), "test-session-router" )
        sessionRouter ! SessionRouter.CreateSession( "QS:existing-session1", ( 1, "minutes" ), update = false, TestData( 1 ) )
        val msg: SessionRouter.SessionCreated = expectMsgClass( classOf[ SessionRouter.SessionCreated ] )
        msg.sessionId must_== "QS:existing-session1"

        // FIRST QUERY
        sessionRouter ! SessionRouter.QuerySession( "QS:existing-session1", update = false )
        val msg2: SessionRouter.SessionFound = expectMsgClass( classOf[ SessionRouter.SessionFound ] )
        msg2.sessionId must_== "QS:existing-session1"
        msg2.updated must_== false
        msg2.data must beAnInstanceOf[ TestData ]
        msg2.data.asInstanceOf[ TestData ].data must_== 1
      }
    }

    "Query Session: Find a session. Update expiration time" in new AkkaSpecs2Support {
      within( 10 seconds ) {

        // FIRST CREATION
        val sessionRouter: ActorRef = system.actorOf( SessionRouter.props(), "test-session-router" )
        sessionRouter ! SessionRouter.CreateSession( "QS:existing-session2", ( 1, "minutes" ), update = false, TestData( 1 ) )
        val msg: SessionRouter.SessionCreated = expectMsgClass( classOf[ SessionRouter.SessionCreated ] )
        msg.sessionId must_== "QS:existing-session2"

        // FIRST QUERY
        sessionRouter ! SessionRouter.QuerySession( "QS:existing-session2", update = true )
        val msg2: SessionRouter.SessionFound = expectMsgClass( classOf[ SessionRouter.SessionFound ] )
        msg2.sessionId must_== "QS:existing-session2"
        msg2.updated must_== true
        msg2.data must beAnInstanceOf[ TestData ]
        msg2.data.asInstanceOf[ TestData ].data must_== 1
      }
    }

    // ------------------------------------------------
    // Close session tests
    // ------------------------------------------------

    "Close Session: NOT close a non existing session" in new AkkaSpecs2Support {
      within( 10 seconds ) {
        val sessionRouter: ActorRef = system.actorOf( SessionRouter.props(), "session-router" )
        sessionRouter ! SessionRouter.CloseSession( "1" )
        val msg: SessionRouter.SessionNotClosed = expectMsgClass( classOf[ SessionRouter.SessionNotClosed ] )
        msg.sessionId must_== "1"
      }
    }

    "Close Session: Close an existing session" in new AkkaSpecs2Support {
      within( 10 seconds ) {

        // FIRST CREATION
        val sessionRouter: ActorRef = system.actorOf( SessionRouter.props(), "test-session-router" )
        sessionRouter ! SessionRouter.CreateSession( "DS:existing-session1", ( 1, "minutes" ), update = false, TestData( 1 ) )
        val msg: SessionRouter.SessionCreated = expectMsgClass( classOf[ SessionRouter.SessionCreated ] )
        msg.sessionId must_== "DS:existing-session1"

        // FIRST DELETE
        sessionRouter ! SessionRouter.CloseSession( "DS:existing-session1" )
        val msg2: SessionRouter.SessionClosed = expectMsgClass( classOf[ SessionRouter.SessionClosed ] )
        msg2.sessionId must_== "DS:existing-session1"
      }
    }

    // ------------------------------------------------
    // Other messages test
    // ------------------------------------------------

    "Other messages: DO NOT support other messages that are not specified" in new AkkaSpecs2Support {
      within( 10 seconds ) {
        val sessionRouter: ActorRef = system.actorOf( SessionRouter.props(), "session-router" )
        sessionRouter ! "Other message"
        expectMsg( "Message not supported" )
      }
    }

  }

}
