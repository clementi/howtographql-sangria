package com.howtographql.scala.sangria.graphql

import com.howtographql.scala.sangria.AppContext
import com.howtographql.scala.sangria.models.Authorized
import sangria.execution.{Middleware, MiddlewareBeforeField, MiddlewareQueryContext}
import sangria.schema.{Action, Context}

object AuthMiddleware extends Middleware[AppContext] with MiddlewareBeforeField[AppContext] {
  override type QueryVal = Unit
  override type FieldVal = Unit

  override def beforeQuery(context: MiddlewareQueryContext[AppContext, _, _]): Unit = ()

  override def afterQuery(queryVal: QueryVal, context: MiddlewareQueryContext[AppContext, _, _]): Unit =()

  override def beforeField(queryVal: QueryVal, mctx: MiddlewareQueryContext[AppContext, _, _], ctx: Context[AppContext, _]): (Unit, Option[Action[AppContext, _]]) = {
    val requireAuth = ctx.field.tags contains Authorized

    if (requireAuth) ctx.ctx.ensureAuthenticated()

    continue
  }

}
