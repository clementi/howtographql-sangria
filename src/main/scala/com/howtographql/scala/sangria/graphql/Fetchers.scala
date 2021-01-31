package com.howtographql.scala.sangria.graphql

import com.howtographql.scala.sangria.AppContext
import com.howtographql.scala.sangria.graphql.Relations.linkByUserRel
import com.howtographql.scala.sangria.models.{Link, User, Vote}
import sangria.execution.deferred.{DeferredResolver, Fetcher}

object Fetchers {
  val linksFetcher: Fetcher[AppContext, Link, Link, Int] = Fetcher.rel(
    (ctx, ids) => ctx.dao.getLinks(ids),
    (ctx, ids) => ctx.dao.getLinksByUserIds(ids(linkByUserRel))
  )

  val usersFetcher: Fetcher[AppContext, User, User, Int] =
    Fetcher((ctx: AppContext, ids: Seq[Int]) => ctx.dao.getUsers(ids))

  val votesFetcher: Fetcher[AppContext, Vote, Vote, Int] =
    Fetcher((ctx: AppContext, ids: Seq[Int]) => ctx.dao.getVotes(ids))

  val Resolver: DeferredResolver[AppContext] =
    DeferredResolver.fetchers(linksFetcher, usersFetcher, votesFetcher)
}
