package endpoints.documented.algebra

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

/**
  * An algebra interface for defining JSON schemas.
  */
trait JsonSchemas {

  /** The JSON schema of a type `A` */
  type JsonSchema[A]

  /** The JSON schema of a record type (case class) `A` */
  type Record[A] <: JsonSchema[A]

  /** The JSON schema of a coproduct type (sealed trait) `A` */
  type CoProd[A] <: JsonSchema[A]

  /** The JSON schema of a record with a single field `name` of type `A` */
  def field[A](name: String)(implicit tpe: JsonSchema[A]): Record[A]

  /** The JSON schema of a record with a single optional field `name` of type `A` */
  def optField[A](name: String)(implicit tpe: JsonSchema[A]): Record[Option[A]]

  /** The JSON schema of a coproduct made of the given alternative records */
  def oneOf[A](alternatives: (String, Record[A])*)(typeTags: A => String): CoProd[A]

  /** The JSON schema of a record merging the fields of the two given records */
  def zipRecords[A, B](recordA: Record[A], recordB: Record[B]): Record[(A, B)]

  /** Transforms the type of the JSON schema */
  def invmapRecord[A, B](record: Record[A], f: A => B, g: B => A): Record[B]

  /** Transforms the type of the JSON schema */
  def invmapJsonSchema[A, B](jsonSchema: JsonSchema[A], f: A => B, g: B => A): JsonSchema[B]

  /** Convenient infix operations */
  final implicit class RecordOps[A](recordA: Record[A]) {
    def zip[B](recordB: Record[B]): Record[(A, B)] = zipRecords(recordA, recordB)
    def invmap[B](f: A => B)(g: B => A): Record[B] = invmapRecord(recordA, f, g)
  }

  /** Convenient infix operations */
  final implicit class JsonSchemaOps[In, A](jsonSchema: JsonSchema[A]) {
    def invmap[B](f: A => B)(g: B => A): JsonSchema[B] = invmapJsonSchema(jsonSchema, f, g)
  }

  /** A JSON schema for type `String` */
  implicit def stringJsonSchema: JsonSchema[String]

  /** A JSON schema for type `Int` */
  implicit def intJsonSchema: JsonSchema[Int]

  /** A JSON schema for type `Long` */
  implicit def longJsonSchema: JsonSchema[Long]

  /** A JSON schema for type `BigDecimal` */
  implicit def bigdecimalJsonSchema: JsonSchema[BigDecimal]

  /** A JSON schema for type `Boolean` */
  implicit def booleanJsonSchema: JsonSchema[Boolean]

  /** A JSON schema for sequences */
  implicit def arrayJsonSchema[C[X] <: Seq[X], A](implicit
    jsonSchema: JsonSchema[A],
    cbf: CanBuildFrom[_, A, C[A]]
  ): JsonSchema[C[A]]

}
