
package com.streaming

object FlinkStreamingImpl extends App {
  val stream: Stream[Int] = List(1, 3, 6, 7, 10).toStream

  FlinkExecutionEnvironment.execute[Int](stream)(stream =>
    stream.map(_ * 2)
  )

}
