package spell.server.akka.remote

import java.io.File
import java.util.UUID
import spell.misc._
import akka.actor._
import com.typesafe.config.ConfigFactory
import scala.io.Source
import scala.concurrent.{Future, Promise, ExecutionContext}
import scala.util.{Success, Failure}


class WordSpawner extends Actor {
  lazy val dict = readFile("src/main/res/words.txt")
  var words: List[GlobalWord] = List()

  def receive: Receive = {
    case RequestWord(delay) =>
    if (canSpawnWord) {
      val word = getRandomWord()
      words = word :: words
      Thread.sleep(delay)
      sender ! WordResponse(word)
    } else {
      sender ! OutOfWords()
    }
  }

  def readFile(fileName:String):List[String] = {
    var list: List[String] = List()
    for(line <- Source.fromFile(fileName).getLines){
      list = line.toUpperCase() :: list
    }
    list
  }

  def isUnique(s:String, list:List[GlobalWord]):Boolean = list match {
    case x::xs if x.text(0) equals s(0) => false
    case x::xs => isUnique(s, xs)
    case _ => true
  }

  def canSpawnWord():Boolean = {
    !dict.filter(w => isUnique(w, words)).isEmpty
  }

  def getRandomWord(): GlobalWord = {
    val list:List[String] = dict.filter(w => isUnique(w, words))
    val random = scala.util.Random
    if(list.isEmpty) null else GlobalWord(UUID.randomUUID(), list(random.nextInt(list.size)), getMultiplier())
  }

  def getMultiplier(): Float = {
    val r = scala.util.Random
    val x = r.nextInt(5000)

    if (x > 4800) 3.0f
    else if (x > 4300) 2.0f
    else 1.0f
  }
}
