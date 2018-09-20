package com.streaming

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props, Stash}
import akka.pattern.ask
import com.streaming.Messages.{AcknowledgeRegistration, RegisterTaskManager, SubmitTask, TriggerTaskManagerRegistration}
import com.streaming.{Job, Messages}
import org.apache.flink.runtime.akka.AkkaUtils

import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration.DurationLong

object JobSubmissionActor extends Actor {
  private val jobManager: ActorRef = Await.result(context.system.actorSelection("/user/jobManager").resolveOne(5.seconds), 15.seconds)

  override def receive: Receive = {
    case Messages.SubmitJob(job, None) => jobManager ! Messages.SubmitJob(job, Some(sender()))
  }
}




object JobManager extends Actor with Stash {
  var mayBeTaskManger: Option[ActorRef] = None

  implicit val timeout = akka.util.Timeout(5.seconds)

  override def receive: Receive = {
    case RegisterTaskManager =>
      println(s"Registered TaskManager")

      val taskManager = sender()
      context.watch(taskManager)
      mayBeTaskManger = Some(taskManager)
      unstashAll()
      taskManager ! AcknowledgeRegistration
    case Messages.SubmitJob(job, Some(client)) =>
      if(mayBeTaskManger.isEmpty)
        stash()
      else {
        mayBeTaskManger.get ! SubmitTask(job, client)
      }
  }
}


object FlinkTaskManger extends Actor {
  override def receive: Receive = {
    case TriggerTaskManagerRegistration(path) =>
      val jobManager: ActorRef = Await.result(context.system.actorSelection(path).resolveOne(15.seconds), 5.seconds)
      jobManager ! RegisterTaskManager

    case  AcknowledgeRegistration =>
      val jobManager = sender()
      println("Received 'AcknowledgeRegistration' message from JobManager")

    case SubmitTask(job: Job[_], client) =>
      val result = FlinkResult(job.function(job.data))
      client ! result
  }

  override def preStart(): Unit = {
    self ! TriggerTaskManagerRegistration("/user/jobManager")
  }
}


