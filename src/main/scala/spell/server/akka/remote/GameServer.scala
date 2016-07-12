package spell.server.akka.remote

import java.io.File
import java.util.UUID
import spell.misc._
import akka.actor._
import com.typesafe.config.ConfigFactory
import scala.io.Source

class GameServer extends Actor {
  var players: Map[ActorRef, Player] = Map()
  var words: List[GlobalWord] = List()
  var spawner:ActorRef = _
  val spawnInterval: Int = 1000
  var gameStarted = false
  var engagedWords: Map[ActorRef, GlobalWord] = Map()

  override def receive: Receive = {
    case StartGame() =>
      gameStarted = true
      spawner = context.system.actorOf(Props[WordSpawner], name="spawner")
      spawner ! RequestWord(spawnInterval)

    case EndGame() =>
      println("Game end request")
      gameStarted = false
      broadcastEndGame()

    case Connect(player:ActorRef) =>
      players += (player -> Player(player, 0, false))
      val serverName = self.path.name
      if(players.size == 1) {
        context.watch(player)
        val name = player.path.name
        println(s"#\t$name is being watched by $serverName")
      }

      println(s"#\t[$serverName] players: " + players.size)
      broadcastPlayerConnect(player)

    case Disconnect(player) =>
      println(s"#\t$player disconnected")

    case _:Terminated =>
      println(s"#\t$sender disconnected")
      context stop sender

    case EngagedWord(player, word) =>
      if (!engagedWords.contains(player)) {
        println(s"The word $word is engaged by $player.path.name")
        engagedWords += (player -> word)
        broadcastWordEngaged(player, word)
        println(s"Current engaged words $engagedWords")
      } else {
        sender ! WordDenied(player, word)
        println("Word denied!")
      }

    case FinishedWord(player, word) =>
      engagedWords = removeByWord(word)
      println(s"The following word is finished $word")
      println(s"Current engaged words $engagedWords")
      broadcastWordWinner(player, word)

    case WordResponse(word) =>
      broadcastWord(word)
      if (gameStarted) spawner ! RequestWord(spawnInterval)

    case OutOfWords() =>
      println("No more words!")

    case Ready(player: ActorRef) =>
      val p = players get player
      players += (player -> Player(player, p.get.score, true))
      println(s"Player $player is ready")
      broadcastPlayerRead(player)
      if (checkIfAllReady) self ! StartGame()
  }

  def checkIfAllReady(): Boolean = {
    players.forall(_._2.ready)
  }

  def removeByWord(word: GlobalWord): Map[ActorRef, GlobalWord] = {
    engagedWords.filter(_._2.text != word.text)
  }

  def broadcastPlayerRead(player: ActorRef): Unit = {
    players.foreach(p => p._1 ! PlayerReady(player))
  }

  def broadcastPlayerConnect(player: ActorRef): Unit = {
    players.foreach(p => p._1 ! PlayerConnected(player))
  }

  def broadcastWordEngaged(player: ActorRef, word: GlobalWord): Unit = {
    players.foreach(p => p._1 ! WordEngaged(player, word))
  }

  def broadcastWordWinner(player: ActorRef, word: GlobalWord) = {
    players.foreach(p => p._1 ! WordWinner(player, word))
  }

  def broadcastWord(word: GlobalWord): Unit = {
    players.foreach(p => p._1 ! SpawnWord(word))
  }

  def broadcastEndGame(): Unit = {
    players.foreach(p => p._1 ! GameEnded())
    println("Game ended!")
  }
}
