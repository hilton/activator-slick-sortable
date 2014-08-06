package models

import models.db.Animals
import play.api.Play.current
import play.api.db.slick.Config.driver.simple._
import play.api.db.slick._

/**
 * Domain model - an animal with a manually-set position that defines a total
 * ordering on the collection. The position is 1-based and strictly consecutive.
 */
case class Animal(id: Option[Int], name: String, position: Int)

/**
 * Data access functions.
 */
object Animal {

  // Base Slick database query to use for data access.
  val animals = TableQuery[Animals]

  /**
   * Returns a list of animals, sorted by position.
   */
  def list = DB.withSession { implicit s: Session =>
    animals.sortBy(_.position).list
  }

  /**
   * Move one animal from one position to another, adjusting the position of
   * other items, to preserve consecutive ordering. `shiftIntermediateItems`
   * is responsible for checking input.
   *
   * @param from The animal’s original position.
   * @param to The animal’s new position in the list.
   * @throws[IllegalArgumentException](if either parameter is out of bounds)
   */
  def reposition(from: Int, to: Int): Unit = DB.withSession {
    implicit session: Session =>

    if (from != to) {

      // Invalid position to use during the reposition (valid positions are > 0).
      val NonPosition = -1

      // Wrap the three updates in a transaction blog, so all or none succeed.
      session.withTransaction {

        // Take the moved item out of the ordering.
        animals.filter(_.position === from).map(_.position).update(NonPosition)

        // Shift the items between the from and to positions.
        shiftIntermediateItems(from, to)

        // Update the moved item’s position.
        animals.filter(_.position === NonPosition).map(_.position).update(to)
      }
    }
  }

  /**
   * Shift the items between the from and two positions up or down one position.
   * Use a plain SQL update to shift items, because Slick does not supposed the
   * relative update expression, `position = position - 1`.
   */
  private def shiftIntermediateItems(from: Int, to: Int)(implicit s: Session): Unit = {

    val updateSql = if (from < to) {
      "update animals set position = position - 1 where position > ? and position <= ?"
    }
    else {
      "update animals set position = position + 1 where position < ? and position >= ?"
    }

    import scala.slick.jdbc.StaticQuery
    val updateQuery = StaticQuery.update[(Int, Int)](updateSql)
    val rowsUpdated = updateQuery((from, to)).first

    // Check that the expected number of rows were updated, to catch bad input.
    if(rowsUpdated != Math.abs(from - to)) {
      throw new IllegalArgumentException(s"Invalid reposition (from $from to $to)")
    }
  }
}