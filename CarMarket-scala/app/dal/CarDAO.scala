package dal

import javax.inject.{ Inject, Singleton }
import play.api.db.slick.DatabaseConfigProvider
import slick.driver.JdbcProfile

import models.Car

import scala.concurrent.{ Future, ExecutionContext }

/**
 * A repository for people.
 *
 * @param dbConfigProvider The Play db config provider. Play will inject this for you.
 */
@Singleton
class CarDAO @Inject() (dbConfigProvider: DatabaseConfigProvider)(implicit ec: ExecutionContext) {
  // We want the JdbcProfile for this provider
  private val dbConfig = dbConfigProvider.get[JdbcProfile]

  // These imports are important, the first one brings db into scope, which will let you do the actual db operations.
  // The second one brings the Slick DSL into scope, which lets you define the table and other queries.
  import dbConfig._
  import driver.api._

  /**
   * Here we define the table. It will have a name of people
   */
  private class CarsTable(tag: Tag) extends Table[Car](tag, "cars") {

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def color = column[String]("color")

    /**
     * This is the tables default "projection".
     *
     * It defines how the columns are converted to and from the Person object.
     *
     * In this case, we are simply passing the id, name and page parameters to the Person case classes
     * apply and unapply methods.
     */
    def * = (id, name, color) <> ((Car.apply _).tupled, Car.unapply)
  }

  /**
   * The starting point for all queries on the cars table.
   */
  private val cars = TableQuery[CarsTable]

  /**
   * Create a person with the given params.
   *
   * This is an asynchronous operation, it will return a future of the created person, which can be used to obtain the
   * id for that person.
   */
  def insert(name: String, color: String): Future[Car] = db.run {
    // We create a projection of just the name and age columns, since we're not inserting a value for the id column
    (cars.map(c => (c.name, c.color))
      // Now define it to return the id, because we want to know what id was generated for the person
      returning cars.map(_.id)
      // And we define a transformation for the returned value, which combines our original parameters with the
      // returned id
      into ((nameColor, id) => Car(id, nameColor._1, nameColor._2))
      // And finally, insert the person into the database
      ) += (name, color)
  }

  /**
   * List all the cars in the database.
   */
  def all(): Future[Seq[Car]] = db.run {
    cars.result
  }

  /**
   * Delete a specific car in the database.
   */
  def delete(id: Int): Unit = db.run {
    val q = cars.filter(_.id === id)
    val action = q.delete
    val affectedRowsCount: Future[Int] = db.run(action)
    val sql = action.statements.head
    cars.result
  }

  /**
   * Modify a specific car in the database.
   */
  def modify(id: Int, name: String, color: String): Unit = db.run {
    val q = for { c <- cars if c.id === id } yield (c.name,c.color)
    val updateAction = q.update(name,color)

    // Get the statement without having to specify an updated value:
    //val sql = q.updateStatement
    val affectedRowsCount: Future[Int] = db.run(updateAction)
    val sql = updateAction.statements.head
    cars.result
  }

  /**
   * Get a specific car from the database.
   */
  def returnData(id: Int): Future[Seq[Car]] = db.run {
    val car = cars.filter(_.id === id)
    /*val action = q.delete
    var affectedRowsCount: Future[Int] = db.run(action)
    val sql = action.statements.head*/
    car.result
}
}