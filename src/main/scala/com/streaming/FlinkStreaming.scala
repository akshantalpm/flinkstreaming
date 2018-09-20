package com.streaming

import java.util.Properties

import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer011, FlinkKafkaProducer011}

object FlinkStreaming extends App {

  def process(kafkaBootstrapServers: String, kafkaTopic: String) {

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", kafkaBootstrapServers)
    properties.setProperty("zookeeper.connect", "localhost:2182")
    properties.setProperty("group.id", "test")
    properties.setProperty("auto.offset.reset", "earliest")

    env
      .addSource(new FlinkKafkaConsumer011[String](kafkaTopic, new SimpleStringSchema(), properties))
      .map(new MapFunction[String, String] {
        override def map(value: String): String = value.toUpperCase
      })
      .addSink(new FlinkKafkaProducer011[String]("output", new SimpleStringSchema(), properties))

    env.execute("Scala SocketTextStreamWordCount Example")
  }
}
