package spell.misc

import akka.actor._
import java.util.UUID

sealed trait ServerMessage
case class RequestWord(delay:Int) extends ServerMessage
case class WordResponse(word:GlobalWord) extends ServerMessage
case class OutOfWords() extends ServerMessage
case class StartGameLoop(delay:Int) extends ServerMessage
case class ShutdownServer(server: ActorRef) extends ServerMessage
