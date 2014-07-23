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

  val animals = TableQuery[Animals]

  /**
   * Returns a list of animals, sorted by position.
   */
  def list = DB.withSession { implicit s: Session =>
    animals.sortBy(_.position).list
  }

  /**
   * Move one animal from one position to another, adjusting the position of
   * other items, to preserve consecutive ordering.
   *
   * @param fromPosition The animal’s original position.
   * @param toPosition The animal’s new position in the list.
   */
  def reposition(fromPosition: Int, toPosition: Int): Unit = DB.withSession {
    implicit s: Session =>

    if (fromPosition != toPosition) {

      // Take the moved item out of the ordering.
      animals.filter(_.position === fromPosition).map(_.position).update(-1)

      // Shift the items between the from and to positions.
      shiftIntermediateItems(fromPosition, toPosition)

      // Update the moved item’s position.
      animals.filter(_.position === -1).map(_.position).update(toPosition)
    }
  }

  /**
   * Shift the items between the from and two positions up or down one position.
   * Use a plain SQL update to shift items, because Slick does not supposed the
   * relative update expression, `position = position - 1`.
   */
  private def shiftIntermediateItems(fromPosition: Int, toPosition: Int): Unit = {
    DB.withSession { implicit s: Session =>

      val updateSql = if (fromPosition < toPosition) {
        "update animals set position = position - 1 where position > ? and position <= ?"
      }
      else {
        "update animals set position = position + 1 where position < ? and position >= ?"
      }

      import scala.slick.jdbc.StaticQuery
      val updateQuery = StaticQuery.update[(Int, Int)](updateSql)
      val rowsUpdated = updateQuery((fromPosition, toPosition)).first
      assert(rowsUpdated == Math.abs(fromPosition - toPosition))
    }
  }
}