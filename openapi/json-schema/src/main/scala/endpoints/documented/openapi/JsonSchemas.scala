package endpoints
package documented
package openapi

import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

/**
  * An interpreter for [[algebra.JsonSchemas]] that produces a documentation of JSON schemas.
  */
trait JsonSchemas extends algebra.JsonSchemas {

  import DocumentedJsonSchema._

  type JsonSchema[+A] = DocumentedJsonSchema
  type Record[+A] = DocumentedRecord
  type CoProd[+A] = DocumentedCoProd

  sealed trait DocumentedJsonSchema

  object DocumentedJsonSchema {

    case class DocumentedRecord(fields: List[Field]) extends DocumentedJsonSchema
    case class Field(name: String, tpe: DocumentedJsonSchema, isOptional: Boolean)

    case class DocumentedCoProd(alternatives: List[(String, DocumentedRecord)]) extends DocumentedJsonSchema

    case class Primitive(name: String) extends DocumentedJsonSchema

    case class Array(elementType: DocumentedJsonSchema) extends DocumentedJsonSchema

  }

  def field[A](name: String)(implicit tpe: DocumentedJsonSchema): DocumentedRecord =
    DocumentedRecord(Field(name, tpe, isOptional = false) :: Nil)

  def optField[A](name: String)(implicit tpe: DocumentedJsonSchema): DocumentedRecord =
    DocumentedRecord(Field(name, tpe, isOptional = true) :: Nil)

  def oneOf[A](alternatives: (String, DocumentedRecord)*)(typeTags: A => String): DocumentedCoProd =
    DocumentedCoProd(alternatives.to[List])

  def zipRecords[A, B](recordA: DocumentedRecord, recordB: DocumentedRecord): DocumentedRecord =
    DocumentedRecord(recordA.fields ++ recordB.fields)

  def invmapRecord[A, B](record: DocumentedRecord, f: A => B, g: B => A): DocumentedRecord = record

  def invmapJsonSchema[A, B](jsonSchema: DocumentedJsonSchema, f: A => B, g: B => A): DocumentedJsonSchema =
    jsonSchema

  lazy val stringJsonSchema: DocumentedJsonSchema = Primitive("string")

  lazy val intJsonSchema: DocumentedJsonSchema = Primitive("integer")

  lazy val longJsonSchema: DocumentedJsonSchema = Primitive("integer")

  lazy val bigdecimalJsonSchema: DocumentedJsonSchema = Primitive("number")

  lazy val booleanJsonSchema: DocumentedJsonSchema = Primitive("boolean")

  implicit def arrayJsonSchema[C[X] <: Seq[X], A](implicit
    jsonSchema: JsonSchema[A],
    cbf: CanBuildFrom[_, A, C[A]]
  ): JsonSchema[C[A]] = Array(jsonSchema)

}
