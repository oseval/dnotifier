package ru.oseval.datahub

import akka.actor.{Actor, ActorRef, Props}
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import org.slf4j.LoggerFactory
import ru.oseval.datahub.User.{ChangeName, UserEntity}
import ru.oseval.datahub.data.{AIData, AIDataOps, ClockInt}

import scala.concurrent.duration._

case class User(id: Long, name: String)

object User {
  def props(id: Long, name: String, notifier: ActorRef): Props =
    Props(classOf[UserActor], id, name, notifier)

  case class ChangeName(newName: String)

  object UserOps extends AIDataOps[User]()

  case class UserEntity(userId: Long) extends Entity {
    lazy val id: String = "user_" + userId
    override val ops = UserOps
  }
}

private class UserActor(id: Long, name: String, notifier: ActorRef)
  extends Actor with ActorDataMethods {
  import context.dispatcher

  private val log = LoggerFactory.getLogger(getClass)

  private implicit val timeout: Timeout = 3.seconds
  private val user = UserEntity(id)
  protected val storage = new LocalDataStorage(log, ActorFacade(_, self), notifier.ask(_).mapTo[Unit])

  storage.addEntity(user)(AIData(User(id, name), System.currentTimeMillis))

  override def receive: Receive = handleDataMessage(user) orElse {
    case ChangeName(n) =>
      storage.combineEntity(user)(AIData(User(id, n), _)) pipeTo sender()
  }
}