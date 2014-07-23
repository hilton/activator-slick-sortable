package controllers

import models.Animal
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, Controller}

object Application extends Controller {

  /**
   * Displays animals in position order.
   */
  def index = Action {
    Ok(views.html.index(Animal.list))
  }

  /**
   * Updates an animal’s position. Use a form so we can generate a URL without
   * paramters in the template, and do validation
   */
  def reposition = Action { implicit request =>
    Logger.info("setPosition")

    case class Position(from: Int, to: Int)

    val form = Form(mapping(
      "from" -> number(min = 1),
      "to" -> number(min = 1)
    )(Position.apply)(Position.unapply))

    form.bindFromRequest.fold(
      formWithErrors => {
        Logger.info("errors: " + formWithErrors.errorsAsJson)
        BadRequest(formWithErrors.errorsAsJson)
      },
      position => {
        Logger.info(s"controller: from ${position.from} to ${position.to}")
        Animal.reposition(position.from, position.to)
        Ok
      })
  }

}