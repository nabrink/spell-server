package spell.server.akka.remote

import java.io.File
import java.util.UUID
import spell.misc._
import akka.actor._
import com.typesafe.config.ConfigFactory
import scala.io.Source

class GameServer extends Actor with FSM[GameState, GameData] {

  startWith(Lobby, LobbyData(Map()))

  onTransition {
    case Lobby -> GameRunning => self ! StartGameLoop(1000)
  }

  when(Lobby) {
    case Event(Connect(player), data: LobbyData) =>
      handlePlayerConnect(player, data)
    case Event(Disconnect(player), data: LobbyData) =>
      handlePlayerDisconnect(player, data)
    case Event(Ready(player), LobbyData(players)) =>
      handlePlayerReady(player, players)
  }

  when(GameRunning) {
    case Event(StartGameLoop(delay), GameSessionData(_, _, _, spawner)) =>
      handleStartGameLoop(spawner, delay)

    case Event(EndGame(), GameSessionData(players, _, _, _)) =>
      handleEndGame(players)

    case Event(EngagedWord(player, word), data: GameSessionData) =>
      handleEngagedWord(player, word, data)

    case Event(FinishedWord(player, word), data: GameSessionData) =>
      handleFinishedWord(player, word, data)

    case Event(WordResponse(word), data: GameSessionData) =>
      handleWordResponse(word, data)

    case Event(OutOfWords(), _) =>
      handleOutOfWords()
  }

  when(GameOver) {
    case Event(GetStats, _) =>
      println("Stats!")
      stay
  }

  initialize()

  def handlePlayerReady(player: ActorRef, players: Map[ActorRef, Player]): GameServer.this.State = {
    println(s"Player ${player.path.name} is ready")

    if (everyoneIsReady(player, players)) {
      broadcastPlayerReady(player, players)
      goto(GameRunning) using GameSessionData(players, Map(), List(),
          context.system.actorOf(Props[WordSpawner], name="spawner"))
    } else {
      players get player match {
        case Some(p: Player) => {
          broadcastPlayerReady(player, players)
          stay using LobbyData(players = players + (player -> Player(player, p.score, true)))
        }
        case _ => stay
      }
    }
  }

  def handlePlayerConnect(player: ActorRef, data: LobbyData): GameServer.this.State = {
    println(s"#\t${player.path.name} connected")
    val newPlayersList = data.players + (player -> Player(player, 0, false))
    broadcastPlayersConnected(newPlayersList)
    stay using data.copy(players = newPlayersList)
  }

  def handlePlayerDisconnect(player: ActorRef, data: LobbyData): GameServer.this.State = {
    println(s"#\t${player.path.name} disconnected")
    broadcastPlayerDisconnect(player, data.players)
    stay using data.copy(players = data.players - player)
  }

  def handleStartGameLoop(spawner: ActorRef, delay: Int): GameServer.this.State = {
    spawner ! RequestWord(delay)
    stay
  }

  def handleEngagedWord(player: ActorRef, word: GlobalWord, data: GameSessionData): GameServer.this.State = {
    if (!data.engagedWords.contains(player)) {
      println(s"The word ${word.text} is engaged by ${player.path.name}")
      broadcastWordEngaged(player, word, data.players)
      println(s"Current engaged words ${data.engagedWords}")
      stay using data.copy(engagedWords = data.engagedWords + (player -> word))
    } else {
      sender ! WordDenied(player, word)
      println("Word denied!")
      stay
    }
  }

  def handleWordResponse(word: GlobalWord, data: GameSessionData): GameServer.this.State = {
    broadcastWord(word, data.players)
    data.spawner ! RequestWord(1000)
    stay using data.copy(words = word :: data.words)
  }

  def handleOutOfWords(): GameServer.this.State = {
    println("No more words!")
    stay
  }

  def handleFinishedWord(player: ActorRef, word: GlobalWord, data: GameSessionData): GameServer.this.State = {
    println(s"The following word is finished ${word.text}")
    broadcastWordWinner(player, word, data.players)
    stay using data.copy(engagedWords = removeByWord(word, data.engagedWords),
                               players = updatePlayerScore(player, word, data.players))
  }

  def handleEndGame(players: Map[ActorRef, Player]): GameServer.this.State = {
    broadcastEndGame(players)
    println("Game ended!")
    goto(GameOver) using Stats(players)
  }

  def updatePlayerScore(player: ActorRef, word: GlobalWord, players: Map[ActorRef, Player]): Map[ActorRef, Player] = {
    val newScore = getScoreForWord(word)
    broadcastScoreUpdated(player, newScore, players)
    val p: Player = players get player match {
      case Some(x: Player) => x
      case _ => null
    }

    players + (player -> Player(player, p.score + newScore, p.ready))
  }

  def getScoreForWord(word: GlobalWord): Int = {
    word.text.length
  }

  def everyoneIsReady(player: ActorRef, players: Map[ActorRef, Player]): Boolean = {
    val everyoneExceptYou = players.filterKeys(_ != player)
    everyoneExceptYou.isEmpty || everyoneExceptYou.forall(_._2.ready)
  }

  def removeByWord(word: GlobalWord, engagedWords: Map[ActorRef, GlobalWord]): Map[ActorRef, GlobalWord] = {
    engagedWords.filter(_._2.text != word.text)
  }

  def broadcastScoreUpdated(player: ActorRef, score: Int, players: Map[ActorRef, Player]): Unit = {
    players.foreach(p => p._1 ! ScoreUpdated(player, score))
  }

  def broadcastPlayerReady(player: ActorRef, players: Map[ActorRef, Player]): Unit = {
    players.foreach(p => p._1 ! PlayerReady(player))
  }

  def broadcastPlayersConnected(players: Map[ActorRef, Player]): Unit = {
    players.foreach(p => p._1 ! PlayersConnected(players.keys.toList))
  }

  def broadcastPlayerDisconnect(player: ActorRef, players: Map[ActorRef, Player]): Unit = {
    players.foreach(p => p._1 ! PlayerDisconnected(player))
  }

  def broadcastWordEngaged(player: ActorRef, word: GlobalWord, players: Map[ActorRef, Player]): Unit = {
    players.foreach(p => p._1 ! WordEngaged(player, word))
  }

  def broadcastWordWinner(player: ActorRef, word: GlobalWord, players: Map[ActorRef, Player]) = {
    players.foreach(p => p._1 ! WordWinner(player, word))
  }

  def broadcastWord(word: GlobalWord, players: Map[ActorRef, Player]): Unit = {
    players.foreach(p => p._1 ! SpawnWord(word))
  }

  def broadcastEndGame(players: Map[ActorRef, Player]): Unit = {
    players.foreach(p => p._1 ! GameEnded())
  }
}
