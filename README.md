# Agnostic session manager based on Akka
## Usage
* Create an UNIQUE instance of class `SessionManager`
```scala
// Your application actor system
// If actor system is not provided, a new one is created automatically
implicit val system: ActorSystem = ActorSystem("my-actor-system")

// Your application configuration (typesafe)
// If configuration is not provided, default configuration is loader automatically using ConfigFactory.load
implicit val config: Config = ConfigFactory.load()

// Session manager UNIQUE instance
val sessionManager = new SessionManager()
```
* Use the instance
```scala

case class MySessionData( userId: String, userPicture: String ) extends SessionData

// Creates a new session
val create: Future[ CreateActionResponse ] = sessionManager.createSession( "session-id", update = false, MySessionData( "1", "http://userPicture.myapp.com" )
create.map {
  case SessionCreated( sessionId ) =>
    println( s"Session was created with id $sessionId" )
  case SessionAlreadyExist( sessionId, updated, data ) =>
    println( s"Session was not created. It already exist with id $sessionId" )
}

// Query a new session
val query: Future[ QueryActionResponse ] = sessionManager.querySession( "session-id", update = false )
query.map {
  case SessionFound( sessionId, updated, data ) =>
    println( s"Session was found with id $sessionId and data $data" )
  case SessionNotFound( sessionId ) =>
    println( s"Session was not found with id $sessionId" )
}

// Close (delete) a session
val close: Future[ CloseActionResponse ] = sessionManager.deleteSession( "session-id" )
close.map {
  case SessionClosed( sessionId ) =>
    println( s"Session was closed with id $sessionId" )
  case SessionNotClosed( sessionId ) =>
    println( s"Session was not closed with id $sessionId. It does not exist" )
}

```