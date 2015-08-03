package co.rc.sessionmanager

import akka.actor.ActorRef
import co.rc.sessionmanager.utils.AkkaSpecs2Support
import scala.concurrent.duration._
import org.specs2.mutable.Specification

/**
 * Test specification for Session Actor
 * Max duration for each test is: 10 seconds
 */
class SessionTest extends Specification {
  sequential

  "Session specification" should {

    "Update its expiration time when receive Session.Update" in new AkkaSpecs2Support {
      within( 10 seconds ) {
        val session: ActorRef = system.actorOf( Session.props( "1", ( 10, "seconds" ) ), "test-session" )
        session ! Session.Update
        expectNoMsg()
      }
    }

    "Delete itself when receive Session.Delete" in new AkkaSpecs2Support {
      within( 10 seconds ) {
        val session: ActorRef = system.actorOf( Session.props( "1", ( 10, "seconds" ) ), "test-session" )
        session ! Session.Update
        expectNoMsg()
      }
    }

    "Other messages: NOT support other messages" in new AkkaSpecs2Support {
      within( 10 seconds ) {
        val session: ActorRef = system.actorOf( Session.props( "1", ( 10, "seconds" ) ), "test-session" )
        session ! "Other message"
        expectMsg( "Message not supported" )
      }
    }

  }

}
