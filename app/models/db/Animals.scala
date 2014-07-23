package models.db

import models.Animal
import play.api.db.slick.Config.driver.simple._

/**
 * Database table mapping.
 */
class Animals(tag: Tag) extends Table[Animal](tag, "ANIMALS") {

  def id = column[Int]("ID", O.PrimaryKey, O.AutoInc)
  def name = column[String]("NAME")
  def position = column[Int]("POSITION")

  def positionIndex = index("POSITION_INDEX", (position), unique = true)

  def * = (id.?, name, position) <> ((Animal.apply _).tupled, Animal.unapply)
}
