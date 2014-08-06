import models.Animal
import org.specs2.mutable._

import play.api.test._
import play.api.test.Helpers._

/**
 * Test repositioning operations.
 */
class ApplicationSpec extends Specification {

  "The model" should {
    "initially contain sorted names" in
      new WithApplication(FakeApplication(additionalConfiguration = inMemoryDatabase("test"))) {

        Animal.list.take(3).map(_.name) must beEqualTo(List("Alligator", "Bear", "Cat"))
    }

    "allow repositioning" in
      new WithApplication(FakeApplication(additionalConfiguration = inMemoryDatabase("test"))) {

        Animal.reposition(1, 2)
        Animal.list.take(3).map(_.name) must beEqualTo(List("Bear", "Alligator", "Cat"))
    }

    "allow repositioning backwards" in
      new WithApplication(FakeApplication(additionalConfiguration = inMemoryDatabase("test"))) {

        Animal.reposition(3, 1)
        Animal.list.take(3).map(_.name) must beEqualTo(List("Cat", "Alligator", "Bear"))
    }

    "retain consecutive positions after repositioning" in
      new WithApplication(FakeApplication(additionalConfiguration = inMemoryDatabase("test"))) {

        Animal.reposition(1, 2)
        Animal.reposition(10, 5)
        Animal.reposition(7, 15)
        val animals = Animal.list
        animals.map(_.position) must beEqualTo((1 to animals.size).toList)
    }

    "throw an exception for an out-of-bounds from position" in
      new WithApplication(FakeApplication(additionalConfiguration = inMemoryDatabase("test"))) {
        Animal.reposition(-10, 2) must throwA[IllegalArgumentException]
      }

    "throw an exception for an out-of-bounds to position" in
      new WithApplication(FakeApplication(additionalConfiguration = inMemoryDatabase("test"))) {
        Animal.reposition(1, 100) must throwA[IllegalArgumentException]
      }
  }
}
