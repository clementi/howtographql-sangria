package com.howtographql.scala.sangria.graphql

import com.howtographql.scala.sangria.AppContext
import com.howtographql.scala.sangria.graphql.Types._
import sangria.marshalling.FromInput
import sangria.schema._
import sangria.util.tag.@@

object GraphQLSchema {
  val Id: Argument[Int] = Argument("id", IntType)
  val Ids: Argument[Seq[Int @@ FromInput.CoercedScalaResult]] = Argument("ids", ListInputType(IntType))

  val SchemaDefinition: Schema[AppContext, Unit] = Schema(QueryType, Some(MutationType))
}
