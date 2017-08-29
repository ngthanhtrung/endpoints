package endpoints
package documented
package generic

import shapeless.labelled.{FieldType, field => shapelessField}
import shapeless.ops.hlist.Tupler
import shapeless.{::, Generic, HList, HNil, LabelledGeneric, Witness}

import scala.language.implicitConversions

/**
  * Enriches [[algebra.JsonSchemas]] with two kinds of operations:
  *
  * - `genericJsonSchema[A]` derives the `JsonSchema` of an algebraic
  *   data type `A`;
  * - `(field1 :×: field2 :×: …).as[A]` builds a tuple of `Record`s and maps
  *   it to a case class `A`
  */
trait JsonSchemas extends JsonSchemas1 {

  implicit def jsonSchemaSingleton[L <: Symbol, A](implicit
    labelSingleton: Witness.Aux[L],
    jsonSchemaSingleton: JsonSchema[A]
  ): GenericRecord[FieldType[L, A] :: HNil] =
    new GenericRecord[FieldType[L, A] :: HNil] {
      def jsonSchema: Record[FieldType[L, A] :: HNil] =
        field(labelSingleton.value.name)(jsonSchemaSingleton)
          .invmap[FieldType[L, A] :: HNil](a => shapelessField[L](a) :: HNil)(_.head)
    }

}

trait JsonSchemas1 extends JsonSchemas2 {

  implicit def jsonSchemaHList[L <: Symbol, H, T <: HList](implicit
    labelHead: Witness.Aux[L],
    jsonSchemaHead: JsonSchema[H],
    jsonSchemaTail: GenericRecord[T]
  ): GenericRecord[FieldType[L, H] :: T] =
    new GenericRecord[FieldType[L, H] :: T] {
      def jsonSchema: Record[FieldType[L, H] :: T] =
        (field(labelHead.value.name)(jsonSchemaHead) zip jsonSchemaTail.jsonSchema)
          .invmap[FieldType[L, H] :: T] { case (h, t) => shapelessField[L](h) :: t }(ht => (ht.head, ht.tail))
    }

}

trait JsonSchemas2 extends algebra.JsonSchemas {

  trait GenericJsonSchema[A] {
    def jsonSchema: JsonSchema[A]
  }

  trait GenericRecord[A] extends GenericJsonSchema[A] {
    def jsonSchema: Record[A]
  }

  implicit def jsonSchemaGeneric[A, R](implicit
    gen: LabelledGeneric.Aux[A, R],
    jsonSchemaGeneric: GenericJsonSchema[R]
  ): GenericJsonSchema[A] =
    new GenericJsonSchema[A] {
      def jsonSchema: JsonSchema[A] = jsonSchemaGeneric.jsonSchema.invmap[A](gen.from)(gen.to)
    }

  def genericJsonSchema[A](implicit genJsonSchema: GenericJsonSchema[A]): JsonSchema[A] = genJsonSchema.jsonSchema

  final class RecordProduct[L <: HList](record: Record[L]) {

    def :*: [H](recordHead: Record[H]): RecordProduct[H :: L] =
      new RecordProduct(
        (recordHead zip record).invmap { case (h, l) => h :: l }(hl => (hl.head, hl.tail))
      )

    def :×: [H](recordHead: Record[H]): RecordProduct[H :: L] = recordHead :*: this

    def as[A](implicit gen: Generic.Aux[A, L]): Record[A] = record.invmap(gen.from)(gen.to)

    def tupled[T](implicit
      tupler: Tupler.Aux[L, T],
      gen: Generic.Aux[T, L]
    ): Record[T] = as[T]

  }

  implicit def toRecordProduct[A](record: Record[A]): RecordProduct[A :: HNil] =
    new RecordProduct[A :: HNil](record.invmap(_ :: HNil)(_.head))

}