package com.streaming

import akka.actor.ActorRef

object Messages {

  case class SubmitJob(job: Job[_], client: Option[ActorRef] = None)

  case class TriggerTaskManagerRegistration(jobManagerPath: String)
  case class RegisterTaskManager()
  case class AcknowledgeRegistration()
  case class SubmitTask(job: Job[_], client: ActorRef)
}
