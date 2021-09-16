package akka.persistence.tagless.circe

import io.circe.generic.semiauto.deriveEncoder

trait Encoders {
  implicit def tuple2Encoder[A, B](implicit
      aEncoder: io.circe.Encoder[A],
      bEncoder: io.circe.Encoder[B]
  ): io.circe.Encoder[(A, B)] = deriveEncoder[(A, B)]

  implicit def tuple3Encoder[A, B, C](implicit
      aEncoder: io.circe.Encoder[A],
      bEncoder: io.circe.Encoder[B],
      cEncoder: io.circe.Encoder[C]
  ): io.circe.Encoder[(A, B, C)] = deriveEncoder[(A, B, C)]

  implicit def tuple4Encoder[A, B, C, D](implicit
      aEncoder: io.circe.Encoder[A],
      bEncoder: io.circe.Encoder[B],
      cEncoder: io.circe.Encoder[C],
      dEncoder: io.circe.Encoder[D]
  ): io.circe.Encoder[(A, B, C, D)] = deriveEncoder[(A, B, C, D)]
}
