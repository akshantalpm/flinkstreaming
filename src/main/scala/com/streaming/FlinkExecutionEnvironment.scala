package com.streaming

import akka.actor.{ActorSystem, Props}
import akka.pattern.ask
import com.streaming.Messages.SubmitJob

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

object FlinkExecutionEnvironment {
  StandaloneClient.run

  def execute[T](data: Stream[T])(function: Stream[T] => Stream[_]) = {
    StandaloneClient.submitJob(Job(data, function))
  }
}

case class Job[T](data: Stream[T], function: Stream[T] => Stream[_])

object StandaloneClient {
  def run = {
    actorSystem.actorOf(Props(JobManager), "jobManager")
    actorSystem.actorOf(Props(FlinkTaskManger), "taskManager")
  }

  private val actorSystem = ActorSystem("flink")

  def submitJob[T](job: Job[T]) = {
    FlinkJobClient.submitJobAndWait(actorSystem, job)
  }
}


object FlinkJobClient {
  def submitJobAndWait[T](actorSystem: ActorSystem, job: Job[T]) = {
    implicit val timeout = akka.util.Timeout(15.seconds)
    val actorRef = actorSystem.actorOf(Props(JobSubmissionActor))

    val result = Await.result(ask(actorRef, SubmitJob(job)), 15.seconds)
    println("Result:" +  result.asInstanceOf[FlinkResult[_]].result.mkString(","))
  }
}
