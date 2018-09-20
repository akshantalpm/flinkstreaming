package com.streaming

import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

import scala.concurrent.Future


class SocketTextStreamWordCountTest extends WordSpec with BeforeAndAfterAll with Matchers with Eventually {


  override def afterAll(): Unit = {
    EmbeddedKafka.stop()
  }

  "should transform message received from kafka using flink" in {
    val userDefinedConfig = EmbeddedKafkaConfig(kafkaPort = 9092, zooKeeperPort = 2182)
    import scala.concurrent.ExecutionContext.Implicits.global

    EmbeddedKafka.withRunningKafkaOnFoundPort(userDefinedConfig) { implicit actualConfig =>
      EmbeddedKafka.createCustomTopic("input")
      EmbeddedKafka.createCustomTopic("output")
      Future {
        FlinkStreaming.process("localhost:9092", "input")
      }
      EmbeddedKafka.publishStringMessageToKafka("input", "Test Data")
      EmbeddedKafka.consumeFirstStringMessageFrom("output") shouldEqual "TEST DATA"
    }
  }

}
