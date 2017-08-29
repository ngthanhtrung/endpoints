package endpoints
package documented
package circe

import endpoints.algebra.CirceEntities.CirceCodec
import io.circe.{DecodingFailure, HCursor, Json, JsonObject}

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

/**
  * An interpreter for [[algebra.JsonSchemas]] that produces a circe codec.
  */
trait JsonSchemas
  extends algebra.JsonSchemas {

  type JsonSchema[A] = CirceCodec[A]
  type Record[A] = JsonSchema[A]
  type CoProd[A] = JsonSchema[A]

  def field[A](name: String)(implicit tpe: JsonSchema[A]): Record[A] =
    CirceCodec.fromEncoderAndDecoder(
      io.circe.Encoder.instance[A](a => Json.obj(name -> tpe.encoder.apply(a))),
      io.circe.Decoder.instance[A](cursor => tpe.decoder.tryDecode(cursor.downField(name)))
    )

  // FIXME Check that this is the correct way to model optional fields with circe
  def optField[A](name: String)(implicit tpe: JsonSchema[A]): Record[Option[A]] =
    CirceCodec.fromEncoderAndDecoder(
      io.circe.Encoder.instance[Option[A]](a => Json.obj(name -> io.circe.Encoder.encodeOption(tpe.encoder).apply(a))),
      io.circe.Decoder.instance[Option[A]](cursor => io.circe.Decoder.decodeOption(tpe.decoder).tryDecode(cursor.downField(name)))
    )

  def oneOf[A](alternatives: (String, Record[A])*)(typeTags: A => String): CoProd[A] = {
    val encoder =
      io.circe.Encoder.instance[A] { a =>
        val typeTag = typeTags(a)
        alternatives.collectFirst { case (`typeTag`, codec) =>
          Json.obj(typeTag -> codec.encoder.apply(a))
        }.get // TODO Error handling
      }
    val decoder =
      io.circe.Decoder.instance[A] { cursor =>
        cursor.as[JsonObject].right.flatMap { jsonObject =>
          jsonObject.toList.headOption match {
            case Some((typeTag, aJson)) =>
              alternatives.collectFirst { case (`typeTag`, codec) =>
                codec.decoder.decodeJson(aJson)
              }.getOrElse(Left(DecodingFailure(s"No decoder for type tag $typeTag", Nil)))
            case None => Left(DecodingFailure("Missing type tag field", Nil))
          }
        }
      }
    CirceCodec.fromEncoderAndDecoder[A](encoder, decoder)
  }

  def zipRecords[A, B](recordA: Record[A], recordB: Record[B]): Record[(A, B)] = {
    val encoder =
      io.circe.Encoder.instance[(A, B)] { case (a, b) =>
        recordA.encoder.apply(a).deepMerge(recordB.encoder.apply(b))
      }
    val decoder = new io.circe.Decoder[(A, B)] {
      def apply(c: HCursor) = recordA.decoder.product(recordB.decoder).apply(c, c)
    }
    CirceCodec.fromEncoderAndDecoder(encoder, decoder)
  }

  def invmapRecord[A, B](record: Record[A], f: A => B, g: B => A): Record[B] = invmapJsonSchema(record, f, g)

  def invmapJsonSchema[A, B](jsonSchema: JsonSchema[A], f: A => B, g: B => A): JsonSchema[B] =
    CirceCodec.fromEncoderAndDecoder(jsonSchema.encoder.contramap(g), jsonSchema.decoder.map(f))

  implicit def stringJsonSchema: JsonSchema[String] = CirceCodec.fromEncoderAndDecoder(implicitly, implicitly)

  implicit def intJsonSchema: JsonSchema[Int] = CirceCodec.fromEncoderAndDecoder(implicitly, implicitly)

  implicit def longJsonSchema: JsonSchema[Long] = CirceCodec.fromEncoderAndDecoder(implicitly, implicitly)

  implicit def bigdecimalJsonSchema: JsonSchema[BigDecimal] = CirceCodec.fromEncoderAndDecoder(implicitly, implicitly)

  implicit def booleanJsonSchema: JsonSchema[Boolean] = CirceCodec.fromEncoderAndDecoder(implicitly, implicitly)

  implicit def arrayJsonSchema[C[X] <: Seq[X], A](implicit jsonSchema: JsonSchema[A], cbf: CanBuildFrom[_, A, C[A]]): JsonSchema[C[A]] =
    CirceCodec.fromEncoderAndDecoder(
      io.circe.Encoder.encodeList[A](jsonSchema.encoder).contramap[C[A]](_.toList),
      io.circe.Decoder.decodeList[A](jsonSchema.decoder).map(_.to[C])
    )

}
