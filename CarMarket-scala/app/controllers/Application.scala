package controllers

import play.api._
import play.api.mvc._
import play.api.i18n._
import play.api.data.Form
import play.api.data.Forms._
import play.api.data.validation.Constraints._
import play.api.libs.json.Json
import models._
import dal._

import scala.concurrent.{ ExecutionContext, Future }

import javax.inject._

class Application @Inject() (repo: CarDAO, val messagesApi: MessagesApi)
                                 (implicit ec: ExecutionContext) extends Controller with I18nSupport{

  /**
   * The mapping for the person form.
   */
  val carForm: Form[CreateCarForm] = Form {
    mapping(
      "name" -> nonEmptyText,
      "color" -> nonEmptyText
    )(CreateCarForm.apply)(CreateCarForm.unapply)
  }

  /**
   * The mapping for the id form.
   */
  val idForm: Form[CreateIdForm] = Form {
    mapping(
      "id" -> number.verifying(min(0), max(140))
    )(CreateIdForm.apply)(CreateIdForm.unapply)
  }

  /**
   * The mapping for the modifyCar form.
   */
  val modifyForm: Form[ModifyCarForm] = Form {
    mapping(
      "id" -> number.verifying(min(0), max(140)),
      "name" -> nonEmptyText,
      "color" -> nonEmptyText
    )(ModifyCarForm.apply)(ModifyCarForm.unapply)
  }

  /**
   * The index action.
   */
  def index = Action {
    Ok(views.html.index(carForm))
  }

  /**
   * The delete action.
   */
  def delete = Action {
    Ok(views.html.delete(idForm))
  }

  /**
   * The delete action.
   */
  def modify = Action {
    Ok(views.html.modify(modifyForm))
  }

  /**
   * The add car action.
   *
   * This is asynchronous, since we're invoking the asynchronous methods on CarDAO.
   */
  def insertCar = Action.async { implicit request =>
    // Bind the form first, then fold the result, passing a function to handle errors, and a function to handle succes.
    carForm.bindFromRequest.fold(
      // The error function. We return the index page with the error form, which will render the errors.
      // We also wrap the result in a successful future, since this action is synchronous, but we're required to return
      // a future because the person creation function returns a future.
      errorForm => {
        Future.successful(Ok(views.html.index(errorForm)))
      },
      // There were no errors in the from, so create the person.
      car => {
        repo.insert(car.name, car.color).map { _ =>
          // If successful, we simply redirect to the index page.
          Redirect(routes.Application.index)
        }
      }
    )
  }

  /**
   * A REST endpoint that gets all the people as JSON.
   */
  def getCars = Action.async {
    repo.all().map { people =>
      Ok(Json.toJson(people))
    }
  }

  def deleteCar = Action.async { implicit request =>
    val id: Int = idForm.bindFromRequest.get.id
    repo.delete(id)
    Future.successful(Ok(views.html.index(carForm)))
  }

  def modifyCar = Action.async { implicit request =>
    val id: Int = modifyForm.bindFromRequest.get.id
    val name: String = modifyForm.bindFromRequest.get.name
    val color: String = modifyForm.bindFromRequest.get.color
    repo.modify(id,name,color)
    Future.successful(Ok(views.html.index(carForm)))
  }
}

/**
 * The create person form.
 *
 * Generally for forms, you should define separate objects to your models, since forms very often need to present data
 * in a different way to your models.  In this case, it doesn't make sense to have an id parameter in the form, since
 * that is generated once it's created.
 */
case class CreateCarForm(name: String, color: String)

case class CreateIdForm(id: Int)

case class ModifyCarForm(id: Int, name: String, color: String)
