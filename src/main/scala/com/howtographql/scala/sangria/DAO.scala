package com.howtographql.scala.sangria
import com.howtographql.scala.sangria.DBSchema.{Links, Users, Votes}
import com.howtographql.scala.sangria.models.{Link, User, Vote}
import slick.jdbc.H2Profile.api._

import scala.concurrent.Future

class DAO(db: Database) {
  def allLinks: Future[Seq[Link]] = db.run(Links.result)

  def getLinks(ids: Seq[Int]): Future[Seq[Link]] = db.run(Links.filter(_.id inSet ids).result)

  def getLinksByUserIds(ids: Seq[Int]): Future[Seq[Link]] = db.run(Links.filter(_.postedBy inSet ids).result)

  def getUsers(ids: Seq[Int]): Future[Seq[User]] = db.run(Users.filter(_.id inSet ids).result)

  def getVotes(ids: Seq[Int]): Future[Seq[Vote]] = db.run(Votes.filter(_.id inSet ids).result)
}
