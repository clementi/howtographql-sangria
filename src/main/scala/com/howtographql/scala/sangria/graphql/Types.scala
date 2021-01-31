package com.howtographql.scala.sangria.graphql

import akka.http.scaladsl.model.DateTime
import com.howtographql.scala.sangria.AppContext
import com.howtographql.scala.sangria.graphql.Fetchers._
import com.howtographql.scala.sangria.graphql.GraphQLSchema.{Id, Ids}
import com.howtographql.scala.sangria.graphql.Relations._
import com.howtographql.scala.sangria.models.{AuthProviderEmail, AuthProviderSignupData, DateTimeCoerceViolation, Identifiable, Link, User, Vote}
import sangria.ast.StringValue
import sangria.macros.derive._
import sangria.marshalling.sprayJson._
import sangria.schema._
import spray.json.DefaultJsonProtocol.{StringJsonFormat, jsonFormat2, jsonFormat1}
import spray.json.RootJsonFormat

object Types {
  implicit val GraphQLDateTime: ScalarType[DateTime] = ScalarType[DateTime](
    "DateTime",
    coerceOutput = (dt, _) => dt.toString,
    coerceInput = {
      case StringValue(dt, _, _) => DateTime.fromIsoDateTimeString(dt).toRight(DateTimeCoerceViolation)
      case _ => Left(DateTimeCoerceViolation)
    },
    coerceUserInput = {
      case s: String => DateTime.fromIsoDateTimeString(s).toRight(DateTimeCoerceViolation)
      case _ => Left(DateTimeCoerceViolation)
    }
  )

  val IdentifiableType: InterfaceType[Unit, Identifiable] =
    InterfaceType("Identifiable", fields[Unit, Identifiable](Field("id", IntType, resolve = _.value.id)))

  lazy val UserType: ObjectType[Unit, User] = deriveObjectType[Unit, User](
    Interfaces(IdentifiableType),
    AddFields(
      Field("links", ListType(LinkType), resolve = c => linksFetcher.deferRelSeq(linkByUserRel, c.value.id)),
      Field("votes", ListType(VoteType), resolve = c => votesFetcher.deferRelSeq(voteByUserRel, c.value.id))
    )
  )

  lazy val LinkType: ObjectType[Unit, Link] = deriveObjectType[Unit, Link](
    Interfaces(IdentifiableType),
    ReplaceField("createdAt", Field("createdAt", GraphQLDateTime, resolve = _.value.createdAt)),
    ReplaceField("postedBy", Field("postedBy", UserType, resolve = c => usersFetcher.defer(c.value.postedBy))),
    AddFields(
      Field("votes", ListType(VoteType), resolve = c => votesFetcher.deferRelSeq(voteByLinkRel, c.value.id))
    )
  )

  lazy val VoteType: ObjectType[Unit, Vote] = deriveObjectType[Unit, Vote](
    Interfaces(IdentifiableType),
    ExcludeFields("userId", "linkId"),
    AddFields(
      Field("user", UserType, resolve = c => usersFetcher.defer(c.value.userId)),
      Field("link", LinkType, resolve = c => linksFetcher.defer(c.value.linkId))
    )
  )

  implicit val AuthProviderEmailInputType: InputObjectType[AuthProviderEmail] =
    deriveInputObjectType[AuthProviderEmail](
      InputObjectTypeName("AUTH_PROVIDER_EMAIL")
    )

  lazy val AuthProviderSignupDataInputType: InputObjectType[AuthProviderSignupData] =
    deriveInputObjectType[AuthProviderSignupData]()

  implicit val authProviderEmailFormat: RootJsonFormat[AuthProviderEmail] =
    jsonFormat2(AuthProviderEmail)
  implicit val authProviderSignupDataFormat: RootJsonFormat[AuthProviderSignupData] =
    jsonFormat1(AuthProviderSignupData)

  val NameArg: Argument[String] = Argument("name", StringType)
  val AuthProviderArg: Argument[AuthProviderSignupData] = Argument("authProvider", AuthProviderSignupDataInputType)

  val MutationType: ObjectType[AppContext, Unit] = ObjectType(
    "Mutation",
    fields[AppContext, Unit](
      Field(
        "createUser",
        UserType,
        arguments = NameArg :: AuthProviderArg :: Nil,
        resolve = c => c.ctx.dao.createUser(c.arg(NameArg), c.arg(AuthProviderArg))
      )
    )
  )

  val QueryType: ObjectType[AppContext, Unit] = ObjectType(
    "Query",
    fields[AppContext, Unit](
      Field("allLinks", ListType(LinkType), resolve = c => c.ctx.dao.allLinks),
      Field("link", OptionType(LinkType), arguments = Id :: Nil, resolve = c => linksFetcher.deferOpt(c.arg(Id))),
      Field("links", ListType(LinkType), arguments = Ids :: Nil, resolve = c => linksFetcher.deferSeq(c.arg(Ids))),
      Field("user", OptionType(UserType), arguments = Id :: Nil, resolve = c => usersFetcher.deferOpt(c.arg(Id))),
      Field("users", ListType(UserType), arguments = Ids :: Nil, resolve = c => usersFetcher.deferSeq(c.arg(Ids))),
      Field("vote", OptionType(VoteType), arguments = Id :: Nil, resolve = c => votesFetcher.deferOpt(c.arg(Id))),
      Field("votes", ListType(VoteType), arguments = Ids :: Nil, resolve = c => votesFetcher.deferSeq(c.arg(Ids)))
    )
  )
}
