package zio.kafka.serde

import org.apache.kafka.common.header.internals.RecordHeaders
import zio._
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._

object DeserializerSpec extends DefaultRunnableSpec {
  override def spec = suite("Deserializer")(
    suite("asOption")(
      testM("deserialize to None when value is null") {
        assertM(stringDeserializer.asOption.deserialize("topic1", new RecordHeaders, null))(isNone)
      },
      testM("deserialize to None when value is null also when underlying deserializer fails on null values") {
        val deserializer = Deserializer((_, _, _) => ZIO.fail(new RuntimeException("cannot handle null")))
        assertM(deserializer.asOption.deserialize("topic1", new RecordHeaders, null))(isNone)
      },
      testM("deserialize to Some when value is not null") {
        checkM(Gen.anyString) { string =>
          assertM(stringDeserializer.asOption.deserialize("topic1", new RecordHeaders, string.getBytes))(
            isSome(equalTo(string))
          )
        }
      }
    ) @@ sequential //For some reason the tests have to run in sequence else they would hang and timeout eventually
  )

  private lazy val stringDeserializer: Deserializer[Any, String] = Serde.string
}
