package com.howtographql.scala.sangria.graphql

import com.howtographql.scala.sangria.models.Link
import sangria.execution.deferred.Relation

object Relations {
  val linkByUserRel: Relation[Link, Link, Int] =
    Relation[Link, Int]("byUser", l => Seq(l.postedBy))
}
