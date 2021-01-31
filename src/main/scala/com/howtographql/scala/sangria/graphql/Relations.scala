package com.howtographql.scala.sangria.graphql

import com.howtographql.scala.sangria.models.{Link, Vote}
import sangria.execution.deferred.Relation

object Relations {
  val linkByUserRel: Relation[Link, Link, Int] = Relation[Link, Int]("byUser", l => Seq(l.postedBy))

  val voteByUserRel: Relation[Vote, Vote, Int] = Relation[Vote, Int]("byUser", v => Seq(v.userId))

  val voteByLinkRel: Relation[Vote, Vote, Int] = Relation[Vote, Int]("byLink", v => Seq(v.linkId))
}
