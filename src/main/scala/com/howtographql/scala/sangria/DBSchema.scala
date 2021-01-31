package com.howtographql.scala.sangria

import akka.http.scaladsl.model.DateTime
import com.howtographql.scala.sangria.models.{Link, User, Vote}
import slick.ast.BaseTypedType
import slick.jdbc.H2Profile.api._
import slick.jdbc.JdbcType
import slick.lifted.{ForeignKeyQuery, ProvenShape}

import java.sql.Timestamp
import scala.concurrent.duration._
import scala.concurrent.Await
import scala.language.postfixOps

object DBSchema {

  implicit val dateTimeColumnType: JdbcType[DateTime] with BaseTypedType[DateTime] =
    MappedColumnType.base[DateTime, Timestamp](
      dt => new Timestamp(dt.clicks),
      ts => DateTime(ts.getTime)
    )

  class LinksTable(tag: Tag) extends Table[Link](tag, "LINKS") {
    def id: Rep[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def url: Rep[String] = column[String]("URL")
    def description: Rep[String] = column[String]("DESCRIPTION")
    def postedBy: Rep[Int] = column[Int]("USER_ID")
    def postedByFK: ForeignKeyQuery[UsersTable, User] = foreignKey("postedBy_FK", postedBy, Users)(_.id)
    def createdAt: Rep[DateTime] = column[DateTime]("CREATED_AT")

    override def * : ProvenShape[Link] = (id, url, description, postedBy, createdAt).mapTo[Link]
  }

  val Links = TableQuery[LinksTable]

  class UsersTable(tag: Tag) extends Table[User](tag, "USERS") {
    def id: Rep[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def name: Rep[String] = column[String]("NAME")
    def email: Rep[String] = column[String]("EMAIL")
    def password: Rep[String] = column[String]("PASSWORD")
    def createdAt: Rep[DateTime] = column[DateTime]("CREATED_AT")

    override def * : ProvenShape[User] = (id, name, email, password, createdAt).mapTo[User]
  }

  val Users = TableQuery[UsersTable]

  class VotesTable(tag: Tag) extends Table[Vote](tag, "VOTES") {
    def id: Rep[Int] = column[Int]("ID", O.PrimaryKey, O.AutoInc)
    def userId: Rep[Int] = column[Int]("USER_ID")
    def linkId: Rep[Int] = column[Int]("LINK_ID")
    def userFK: ForeignKeyQuery[UsersTable, User] = foreignKey("user_FK", userId, Users)(_.id)
    def linkFF: ForeignKeyQuery[LinksTable, Link] = foreignKey("link_FK", linkId, Links)(_.id)

    override def * : ProvenShape[Vote] = (id, userId, linkId).mapTo[Vote]
  }

  val Votes = TableQuery[VotesTable]

  // Load schema and populate sample data within this Sequence of DBActions
  val databaseSetup = DBIO.seq(
    Links.schema.create,
    Users.schema.create,
    Votes.schema.create,
    Links.forceInsertAll(
      Seq(
        Link(1, "http://howtographql.com", "Awesome community driven GraphQL tutorial", 1, DateTime(2020, 9, 12)),
        Link(2, "http://graphql.org", "Official GraphQL web page", 2, DateTime(2020, 10, 1)),
        Link(3, "https://facebook.github.io/graphql/", "GraphQL specification", 1, DateTime(2020, 12, 30))
      )
    ),
    Users.forceInsertAll(
      Seq(
        User(1, "Murray Rothbard", "mrothbard@mises.org", "kushtr,zjxhfdg", DateTime(2000, 7, 6)),
        User(2, "Ludwig von Mises", "lvmises@mises.org", "zdhf8743hkd", DateTime(2000, 7, 6)),
        User(3, "Friedrich von Hayek", "fvhayek@mises.org", "87hdjfgbzfdg0-", DateTime(2001, 4, 17))
      )
    ),
    Votes.forceInsertAll(Seq(Vote(1, 1, 1), Vote(2, 1, 2), Vote(3, 3, 2)))
  )

  def createDatabase: DAO = {
    val db = Database.forConfig("h2mem")
    Await.result(db.run(databaseSetup), 10 seconds)
    new DAO(db)
  }
}