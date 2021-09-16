package akka.persistence.tagless.circe

import akka.persistence.tagless.circe.CirceDecoder.DecodingException
import akka.persistence.tagless.core.typeclass.protocol.Decoder
import io.circe.{DecodingFailure, Json}

class CirceDecoder[+A](implicit decoder: io.circe.Decoder[A]) extends Decoder[Json, A] {
  def decode(payload: Json): A =
    decoder.decodeJson(payload).fold(failure => throw new DecodingException(failure), identity)
}

object CirceDecoder {
  final class DecodingException(failure: DecodingFailure) extends RuntimeException(failure.message)

  implicit def apply[A: io.circe.Decoder]: Decoder[Json, A] = new CirceDecoder[A]
}
