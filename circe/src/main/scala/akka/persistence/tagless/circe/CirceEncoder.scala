package akka.persistence.tagless.circe

import akka.persistence.tagless.core.typeclass.protocol.Encoder
import io.circe.Json

class CirceEncoder[-A](implicit encoder: io.circe.Encoder[A]) extends Encoder[A, Json] {
  def encode(a: A): Json = encoder.apply(a)
}

object CirceEncoder {
  implicit def apply[A: io.circe.Encoder]: CirceEncoder[A] = new CirceEncoder[A]
}
