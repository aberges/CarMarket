package models

import play.api.libs.json._


/**
 * Created by albertbergespeiro on 30/09/15.
 */
case class Car(id: Int, title: String, fuel: String, price: Int, isNew: Boolean, mileAge: Int, firstRegistration: Int)

object Car {

  implicit val carFormat = Json.format[Car]
}
