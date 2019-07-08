package models

import akka.actor.FSM.State
import akka.http.scaladsl.model.HttpHeader.ParsingResult.Ok
import anorm.{Macro, RowParser}
import javax.inject.Inject
import anorm.SqlParser.{get, scalar}

import scala.concurrent.duration._
import scala.util.{Failure, Success}
import play.api.db._
import org.joda.time.DateTime
import anorm._
import com.fasterxml.jackson.databind.JsonDeserializer
import play.api.libs.functional.syntax._
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._
import play.api.libs.ws.WSClient

import scala.concurrent.Future

case class Event(name: String,
                 url: String,
                 salesStart: DateTime,
                 salesEnd: DateTime,
                 startDate: DateTime,
                 minPrice: Double,
                 maxPrice: Double,
                 currency: String)

object Event {
  val dateFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

  import play.api.libs.json.JodaWrites
  implicit val dateTimeWriter: Writes[DateTime] =
    JodaWrites.jodaDateWrites("dd/MM/yyyy HH:mm:ss")
  import play.api.libs.json.JodaReads
  implicit val dateTimeJsReader = JodaReads.jodaDateReads("yyyyMMddHHmmss")

  implicit val readEvent: Reads[Event] = ((JsPath \ "name")
    .read[String] and (JsPath \ "url")
    .read[String] and (JsPath \ "sales" \ "public" \ "startDateTime")
    .read[DateTime] and (JsPath \ "sales" \ "public" \ "endDateTime")
    .read[DateTime] and (JsPath \ "dates" \ "starts" \ "dateTime")
    .read[DateTime] and (JsPath \ "priceRanges" \ "min")
    .read[Double] and (JsPath \ "priceRanges" \ "max")
    .read[Double] and (JsPath \ "priceRanges" \ "currency")
    .read[String])(Event.apply _)
  //System.out.println("HEREEEEEEEEEE")
  /*val name = (jsonEvent \ "name").as[String]
    val url = (jsonEvent \ "url").as[String]
    val salesStart =
      (jsonEvent \ "sales" \ "public" \ "startDateTime")
        .as[DateTime]
    val salesEnd = (jsonEvent \ "sales" \ "public" \ "endDateTime")
      .as[DateTime]
    val startDate =
      (jsonEvent \ "dates" \ "starts" \ "dateTIme").as[DateTime]
    val minPrice = (jsonEvent \ "priceRanges" \ "min").as[Double]
    val maxPrice = (jsonEvent \ "priceRanges" \ "max").as[Double]
    val currency = (jsonEvent \ "priceRanges" \ "currency").as[String]*/

  /* Event(
      name,
      url,
      salesStart,
      salesEnd,
      startDate,
      minPrice,
      maxPrice,
      currency
    )

  }*/

}

case class Events(lst: List[Event])

/*object Events {
  implicit val eventsRead: Reads[Events] = Json.reads[Events]
}*/

@javax.inject.Singleton
class EventModel @Inject()(ws: WSClient, dbapi: DBApi)(
  implicit ec: DatabaseExecutionContext
) {

  private val db = dbapi.database("default")

  /**
    * Parse a Event from a ResultSet
    */
  val parser: RowParser[Event] = {
    get[String]("name") ~ get[String]("url") ~ get[DateTime]("salesStart") ~ get[
      DateTime
    ]("salesEnd") ~ get[DateTime]("startDate") ~ get[Double]("minPrice") ~ get[
      Double
    ]("maxPrice") ~ get[String]("currency") map {
      case name ~ url ~ salesStart ~ salesEnd ~ startDate ~ minPrice ~ maxPrice ~ currency =>
        Event(
          name,
          url,
          salesStart,
          salesEnd,
          startDate,
          minPrice,
          maxPrice,
          currency
        )
    }
  }

  def list =
    Future {

      db.withConnection { implicit connection =>
        SQL"""
        select * from event
      """.as(parser.*)

      }
    }

  def getEventsFromApi(url: String, apikey: String) =
    ws.url(url)
      .withQueryStringParameters("apikey" -> apikey)
      .withRequestTimeout(13.seconds)
      .get()
      .map { response =>
        /*for {
          event <- */
        System.out.println(
          (response.json \ "_embedded" \ "events")(1).validate[Event]
        ) /*match {
          case JsSuccess(events, _) => println(events)
          case JsError(_)           => println("parsing failed")
        }*/
        /*} yield event*/
      } onComplete {
      case Success(value)     => System.out.println(value)
      case Failure(exception) => System.out.println(exception)
    }

  def updateEvents(): Unit = {
    getEventsFromApi(
      "https://app.ticketmaster.com/discovery/v2/events",
      "KyJTRJ3CkVBO58lMqCiL4O9yAClJQgNp"
    )
  }

  /**
  * Construct the Seq[(String,String)] needed to fill a select options set.
  *
  * Uses `SqlQueryResult.fold` from Anorm streaming,
  * to accumulate the rows as an options list.
  */
}
