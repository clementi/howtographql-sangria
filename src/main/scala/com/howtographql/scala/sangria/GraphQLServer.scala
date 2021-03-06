package com.howtographql.scala.sangria

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCode
import akka.http.scaladsl.model.StatusCodes.{BadRequest, InternalServerError, OK}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server.Route
import com.howtographql.scala.sangria.graphql.AuthMiddleware
import com.howtographql.scala.sangria.graphql.Fetchers._
import com.howtographql.scala.sangria.graphql.GraphQLSchema.SchemaDefinition
import com.howtographql.scala.sangria.models.{AuthenticationException, AuthorizationException}
import sangria.ast.Document
import sangria.execution.{ErrorWithResolver, ExceptionHandler, Executor, HandledException, QueryAnalysisError}
import sangria.execution.deferred.DeferredResolver
import sangria.marshalling.sprayJson._
import sangria.parser.QueryParser
import spray.json.{JsObject, JsString, JsValue}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Success

object GraphQLServer {
  private val dao: DAO = DBSchema.createDatabase

  def endpoint(requestJSON: JsValue)(implicit ec: ExecutionContext): Route = {
    val JsObject(fields) = requestJSON
    val JsString(query) = fields("query")

    QueryParser.parse(query) match {
      case Success(queryAst) =>
        val operation = fields.get("operationName").collect {
          case JsString(op) => op
        }

        val variables = fields.get("variables") match {
          case Some(obj: JsObject) => obj
          case _ => JsObject.empty
        }

        complete(executeGraphQLQuery(queryAst, operation, variables))
    }
  }

  val errorHandler: ExceptionHandler = ExceptionHandler {
    case (_, AuthenticationException(message)) => HandledException(message)
    case (_, AuthorizationException(message)) => HandledException(message)
  }

  private def executeGraphQLQuery(query: Document, operation: Option[String], vars: JsObject)(implicit ec: ExecutionContext): Future[(StatusCode, JsValue)] = {
    Executor.execute(
      SchemaDefinition,
      query,
      AppContext(dao),
      variables = vars,
      operationName = operation,
      deferredResolver = DeferredResolver.fetchers(linksFetcher, usersFetcher, votesFetcher),
      exceptionHandler = errorHandler,
      middleware = AuthMiddleware :: Nil
    )
      .map(OK -> _)
      .recover {
        case error: QueryAnalysisError => BadRequest -> error.resolveError
        case error: ErrorWithResolver => InternalServerError -> error.resolveError
      }
  }
}
