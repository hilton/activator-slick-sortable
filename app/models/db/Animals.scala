package models.db

import models.Animal
import play.api.db.slick.Config.driver.simple._

/**
 * Database table mapping, using standard Slick syntax.
 */
class Animals(tag: Tag) extends Table[Animal](tag, "ANIMALS") {

  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def name = column[String]("NAME")
  def position = column[Int]("POSITION")

  // Database constraint to enforce unique position values.
  def positionIndex = index("POSITION_INDEX", (position), unique = true)

  // Slick requires a `*` ‘projection’, to define a row (tuple of columns).
  // `id` is mapped to an `Option[Int]`, hence the use of Slick’s ? method.
  def * = (id.?, name, position) <> ((Animal.apply _).tupled, Animal.unapply)
}
