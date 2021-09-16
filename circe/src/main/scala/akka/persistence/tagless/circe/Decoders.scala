package akka.persistence.tagless.circe

import akka.persistence.tagless.\/
import io.circe.generic.semiauto.deriveDecoder

trait Decoders {
  implicit def eitherDecoder[A, B](implicit
      aDecoder: io.circe.Decoder[A],
      bDecoder: io.circe.Decoder[B]
  ): io.circe.Decoder[A \/ B] = deriveDecoder[A \/ B]

}
