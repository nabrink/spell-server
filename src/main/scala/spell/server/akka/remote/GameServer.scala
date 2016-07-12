package spell.server.akka.remote

import java.io.File
import java.util.UUID
import spell.misc._
import akka.actor._
import com.typesafe.config.ConfigFactory
import scala.io.Source

class GameServer extends Actor {
  var players: List[ActorRef] = List()
  var words: List[GlobalWord] = List()
  var spawner:ActorRef = _
  val spawnInterval: Int = 1000
  var gameStarted = false

  override def receive: Receive = {
    case StartGame() =>
      gameStarted = true
      spawner = context.system.actorOf(Props[WordSpawner], name="spawner")
      spawner ! RequestWord(spawnInterval)

    case EndGame() =>
      println("Game end request")
      gameStarted = false
      broadcastEndGame()

    case Disconnect(player) =>
      println(s"#\t$player disconnected")

    case _:Terminated =>
      println(s"#\t$sender disconnected")
      context stop sender

    case Connect(player:ActorRef) =>
      players = player :: players
      val serverName = self.path.name
      if(players.size == 1) {
        context.watch(player)
        val name = player.path.name
        println(s"#\t$name is being watched by $serverName")
      }

      println(s"#\t[$serverName] players: " + players.size)
      players.foreach(p => p ! PlayerConnected(player))


    case WordResponse(word) =>
      println(s"Broadcasting $word")
      broadcastWord(word)
      if (gameStarted) spawner ! RequestWord(spawnInterval)

    case OutOfWords() =>
      println("No more words!")
  }

  def broadcastWord(word: GlobalWord): Unit = {
    players.foreach(p => p ! SpawnWord(word))
  }

  def broadcastEndGame(): Unit = {
    players.foreach(p => p ! GameEnded())
    println("Game ended!")
  }
}
