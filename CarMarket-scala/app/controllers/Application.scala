package controllers

import java.sql.Date

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
      "title"             -> nonEmptyText,
      "fuel"              -> nonEmptyText,
      "price"             -> number.verifying(min(0), max(140)),
      "isNew"             -> boolean,
      "mileAge"           -> number.verifying(min(0), max(140)),
      "firstRegistration" -> sqlDate
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
      "id"                -> number.verifying(min(0), max(140)),
      "title"             -> nonEmptyText,
      "fuel"              -> nonEmptyText,
      "price"             -> number.verifying(min(0), max(140)),
      "isNew"             -> boolean,
      "mileAge"           -> number.verifying(min(0), max(140)),
      "firstRegistration" -> sqlDate
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
   * The return car action.
   */
  def returnData = Action {
    Ok(views.html.returnData(idForm))
  }

  /**
   * The add car action.
   *
   * This is asynchronous, since we're invoking the asynchronous methods on CarDAO.
   */
  def insertCar = Action.async { implicit request =>
    // Bind the form first, then fold the result, passing a function to handle errors, and a function to handle succes.
    carForm.bindFromRequest.fold(
      errorForm => {
        Future.successful(Ok(views.html.index(errorForm)))
      },
      // There were no errors in the from, so create the car.
      car => {
        repo.insert(car.title,car.fuel,car.price,car.isNew,car.mileAge,car.firstRegistration).map { _ =>
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
    repo.all().map { cars =>
      Ok(Json.toJson(cars))
    }
  }

  def deleteCar = Action.async { implicit request =>
    val id: Int = idForm.bindFromRequest.get.id
    repo.delete(id)
    Future.successful(Ok(views.html.index(carForm)))
  }

  def modifyCar = Action.async { implicit request =>
    val id: Int = modifyForm.bindFromRequest.get.id
    val title: String = modifyForm.bindFromRequest.get.title
    val fuel: String = modifyForm.bindFromRequest.get.fuel
    val price: Int = modifyForm.bindFromRequest.get.price
    val isNew: Boolean = modifyForm.bindFromRequest.get.isNew
    val mileAge: Int = modifyForm.bindFromRequest.get.mileAge
    val firstRegistration: Date = modifyForm.bindFromRequest.get.firstRegistration
    repo.modify(id,title,fuel,price,isNew,mileAge,firstRegistration)
    Future.successful(Ok(views.html.index(carForm)))
  }

  def returnDataCar = Action.async { implicit request =>
    val id: Int = idForm.bindFromRequest.get.id

    repo.returnData(id).map { cars =>
      if(!cars.isEmpty){
        Ok(views.html.car(cars(0)))
      }else{
        Ok(views.html.returnData(idForm))
      }
    }
  }
}

/**
 * The needed forms
 */
case class CreateCarForm(title: String, fuel: String, price: Int, isNew: Boolean, mileAge: Int, firstRegistration: Date)

case class CreateIdForm(id: Int)

case class ModifyCarForm(id: Int, title: String, fuel: String, price: Int, isNew: Boolean, mileAge: Int, firstRegistration: Date)
