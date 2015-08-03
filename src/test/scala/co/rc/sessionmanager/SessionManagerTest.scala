package co.rc.sessionmanager

import co.rc.sessionmanager.utils.{ TestData, AkkaSpecs2Support }

import org.specs2.mutable.Specification

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

/**
 * Test specification for SessionManager
 * Max duration for each test is: 20 seconds
 * Max await block for each future is: 5 seconds
 */
class SessionManagerTest extends Specification {
  sequential

  "Session manager specification" should {

    // ------------------------------------------------
    // Create session tests
    // ------------------------------------------------

    "Create Session: Create a new session" in new AkkaSpecs2Support {
      within( 20 seconds ) {
        val sessionManager: SessionManager = new SessionManager()
        val creationFuture: Future[ SessionRouter.CreateActionResponse ] = sessionManager.createSession( "SM:CS:new-session", update = false, TestData( 1 ) )
        val creationResult: SessionRouter.CreateActionResponse = Await.result( creationFuture, 5.seconds )
        creationResult must beAnInstanceOf[ SessionRouter.SessionCreated ]
        creationResult.asInstanceOf[ SessionRouter.SessionCreated ].sessionId must_== "SM:CS:new-session"
      }
    }

    "Create Session: Existing session. NOT update session expiration time." in new AkkaSpecs2Support {
      within( 20 seconds ) {
        val sessionManager: SessionManager = new SessionManager()

        // FIRST CREATION
        val creationFuture1: Future[ SessionRouter.CreateActionResponse ] = sessionManager.createSession( "SM:CS:existing-session", update = false, TestData( 1 ) )
        val creationResult1: SessionRouter.CreateActionResponse = Await.result( creationFuture1, 5.seconds )
        creationResult1 must beAnInstanceOf[ SessionRouter.SessionCreated ]
        creationResult1.asInstanceOf[ SessionRouter.SessionCreated ].sessionId must_== "SM:CS:existing-session"

        // SECOND CREATION
        val creationFuture2: Future[ SessionRouter.CreateActionResponse ] = sessionManager.createSession( "SM:CS:existing-session", update = false, TestData( 1 ) )
        val creationResult2: SessionRouter.CreateActionResponse = Await.result( creationFuture2, 5.seconds )

        creationResult2 must beAnInstanceOf[ SessionRouter.SessionAlreadyExist ]
        creationResult2.asInstanceOf[ SessionRouter.SessionAlreadyExist ].sessionId must_== "SM:CS:existing-session"
        creationResult2.asInstanceOf[ SessionRouter.SessionAlreadyExist ].updated must_== false

        creationResult2.asInstanceOf[ SessionRouter.SessionAlreadyExist ].data must beAnInstanceOf[ TestData ]
        creationResult2.asInstanceOf[ SessionRouter.SessionAlreadyExist ].data.asInstanceOf[ TestData ].data must_== 1
      }
    }

    "Create Session: Existing session. Update session expiration time." in new AkkaSpecs2Support {
      within( 20 seconds ) {
        val sessionManager: SessionManager = new SessionManager()

        // FIRST CREATION
        val creationFuture1: Future[ SessionRouter.CreateActionResponse ] = sessionManager.createSession( "SM:CS:existing-session2", update = false, TestData( 1 ) )
        val creationResult1: SessionRouter.CreateActionResponse = Await.result( creationFuture1, 5.seconds )
        creationResult1 must beAnInstanceOf[ SessionRouter.SessionCreated ]
        creationResult1.asInstanceOf[ SessionRouter.SessionCreated ].sessionId must_== "SM:CS:existing-session2"

        // SECOND CREATION
        val creationFuture2: Future[ SessionRouter.CreateActionResponse ] = sessionManager.createSession( "SM:CS:existing-session2", update = true, TestData( 1 ) )
        val creationResult2: SessionRouter.CreateActionResponse = Await.result( creationFuture2, 5.seconds )

        creationResult2 must beAnInstanceOf[ SessionRouter.SessionAlreadyExist ]
        creationResult2.asInstanceOf[ SessionRouter.SessionAlreadyExist ].sessionId must_== "SM:CS:existing-session2"
        creationResult2.asInstanceOf[ SessionRouter.SessionAlreadyExist ].updated must_== true

        creationResult2.asInstanceOf[ SessionRouter.SessionAlreadyExist ].data must beAnInstanceOf[ TestData ]
        creationResult2.asInstanceOf[ SessionRouter.SessionAlreadyExist ].data.asInstanceOf[ TestData ].data must_== 1
      }
    }

    // ------------------------------------------------
    // Query session tests
    // ------------------------------------------------

    "Query Session: NOT find a non existing session" in new AkkaSpecs2Support {
      within( 20 seconds ) {
        val sessionManager: SessionManager = new SessionManager()
        val findFuture: Future[ SessionRouter.QueryActionResponse ] = sessionManager.querySession( "SM:QS:nonexisting", update = false )
        val findFutureResult: SessionRouter.QueryActionResponse = Await.result( findFuture, 5.seconds )
        findFutureResult must beAnInstanceOf[ SessionRouter.SessionNotFound ]
        findFutureResult.asInstanceOf[ SessionRouter.SessionNotFound ].sessionId must_== "SM:QS:nonexisting"
      }
    }

    "Query Session: Find a session. NOT update expiration time" in new AkkaSpecs2Support {
      within( 20 seconds ) {
        val sessionManager: SessionManager = new SessionManager()

        // FIRST CREATION
        val creationFuture1: Future[ SessionRouter.CreateActionResponse ] = sessionManager.createSession( "SM:QS:existing-session", update = false, TestData( 1 ) )
        val creationResult1: SessionRouter.CreateActionResponse = Await.result( creationFuture1, 5.seconds )
        creationResult1 must beAnInstanceOf[ SessionRouter.SessionCreated ]
        creationResult1.asInstanceOf[ SessionRouter.SessionCreated ].sessionId must_== "SM:QS:existing-session"

        // FIRST QUERY
        val findFuture: Future[ SessionRouter.QueryActionResponse ] = sessionManager.querySession( "SM:QS:existing-session", update = false )
        val findFutureResult: SessionRouter.QueryActionResponse = Await.result( findFuture, 5.seconds )

        findFutureResult must beAnInstanceOf[ SessionRouter.SessionFound ]
        findFutureResult.asInstanceOf[ SessionRouter.SessionFound ].sessionId must_== "SM:QS:existing-session"
        findFutureResult.asInstanceOf[ SessionRouter.SessionFound ].updated must_== false

        findFutureResult.asInstanceOf[ SessionRouter.SessionFound ].data must beAnInstanceOf[ TestData ]
        findFutureResult.asInstanceOf[ SessionRouter.SessionFound ].data.asInstanceOf[ TestData ].data must_== 1
      }
    }

    "Query Session: Find a session. Update expiration time" in new AkkaSpecs2Support {
      within( 20 seconds ) {
        val sessionManager: SessionManager = new SessionManager()

        // FIRST CREATION
        val creationFuture1: Future[ SessionRouter.CreateActionResponse ] = sessionManager.createSession( "SM:QS:existing-session", update = false, TestData( 1 ) )
        val creationResult1: SessionRouter.CreateActionResponse = Await.result( creationFuture1, 5.seconds )
        creationResult1 must beAnInstanceOf[ SessionRouter.SessionCreated ]
        creationResult1.asInstanceOf[ SessionRouter.SessionCreated ].sessionId must_== "SM:QS:existing-session"

        // FIRST QUERY
        val findFuture: Future[ SessionRouter.QueryActionResponse ] = sessionManager.querySession( "SM:QS:existing-session", update = true )
        val findFutureResult: SessionRouter.QueryActionResponse = Await.result( findFuture, 5.seconds )

        findFutureResult must beAnInstanceOf[ SessionRouter.SessionFound ]
        findFutureResult.asInstanceOf[ SessionRouter.SessionFound ].sessionId must_== "SM:QS:existing-session"
        findFutureResult.asInstanceOf[ SessionRouter.SessionFound ].updated must_== true

        findFutureResult.asInstanceOf[ SessionRouter.SessionFound ].data must beAnInstanceOf[ TestData ]
        findFutureResult.asInstanceOf[ SessionRouter.SessionFound ].data.asInstanceOf[ TestData ].data must_== 1
      }
    }

    // ------------------------------------------------
    // Close session tests
    // ------------------------------------------------

    "Close Session: NOT close a non existing session" in new AkkaSpecs2Support {
      within( 20 seconds ) {
        val sessionManager: SessionManager = new SessionManager()
        val deleteFuture: Future[ SessionRouter.CloseActionResponse ] = sessionManager.closeSession( "SM:CS:nonexisting" )
        val deleteFutureResult: SessionRouter.CloseActionResponse = Await.result( deleteFuture, 5.seconds )
        deleteFutureResult must beAnInstanceOf[ SessionRouter.SessionNotClosed ]
        deleteFutureResult.asInstanceOf[ SessionRouter.SessionNotClosed ].sessionId must_== "SM:CS:nonexisting"
      }
    }

    "Close Session: Close an existing session" in new AkkaSpecs2Support {
      within( 20 seconds ) {
        val sessionManager: SessionManager = new SessionManager()

        // FIRST CREATION
        val creationFuture1: Future[ SessionRouter.CreateActionResponse ] = sessionManager.createSession( "SM:CS:existing-session", update = false, TestData( 1 ) )
        val creationResult1: SessionRouter.CreateActionResponse = Await.result( creationFuture1, 5.seconds )
        creationResult1 must beAnInstanceOf[ SessionRouter.SessionCreated ]
        creationResult1.asInstanceOf[ SessionRouter.SessionCreated ].sessionId must_== "SM:CS:existing-session"

        // FIRS DELETE
        val deleteFuture: Future[ SessionRouter.CloseActionResponse ] = sessionManager.closeSession( "SM:CS:existing-session" )
        val deleteFutureResult: SessionRouter.CloseActionResponse = Await.result( deleteFuture, 5.seconds )

        deleteFutureResult must beAnInstanceOf[ SessionRouter.SessionClosed ]
        deleteFutureResult.asInstanceOf[ SessionRouter.SessionClosed ].sessionId must_== "SM:CS:existing-session"
      }
    }

  }
}