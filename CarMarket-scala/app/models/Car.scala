package models

import play.api.libs.json._


/**
 * Created by albertbergespeiro on 30/09/15.
 */
case class Car(id: Int, name: String, color: String)

object Car {

  implicit val carFormat = Json.format[Car]
}
