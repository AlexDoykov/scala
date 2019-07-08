package controllers

import javax.inject._
import models.{Event, EventModel, Events}

import scala.concurrent.duration._
import play.api._
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.{Json, Reads}
import play.api.libs.ws.WSClient
import play.api.mvc._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

case class FilterPrice(money: Int, currency: String)
object FilterPrice {
  implicit val FilterFormat: Reads[FilterPrice] = Json.reads[FilterPrice]
}

/**
  * This controller creates an `Action` to handle HTTP requests to the
  * application's home page.
  */
@Singleton
class HomeController @Inject()(
  ws: WSClient,
  eventService: EventModel,
  cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with play.api.i18n.I18nSupport {

  val form = Form(
    mapping(
      "money" -> number /*(min = 0)*/,
      "currency" -> text /*nonEmptyText(minLength = 1, maxLength = 5)*/
    )(FilterPrice.apply)(FilterPrice.unapply)
  )

  def index() = Action.async { implicit request =>
    eventService.updateEvents()
    eventService.list.map { page: List[Event] =>
      Ok(views.html.index(page, form))
    }
  }

  def convertMoney(money: FilterPrice) = {
    ws.url("https://api.exchangeratesapi.io/latest")
      .withQueryStringParameters("base" -> money.currency)
      .withRequestTimeout(13.seconds)
      .get() onComplete {
      case Success(value) => {
        System.out.println("Syccess");
        System.out.println((value.json \ "rates").get)
      }
      case Failure(exception) => {
        System.out.println("Fail"); System.out.println(exception)
      }
    }
  }

  def filterByPrice() = Action { implicit request =>
    form.bindFromRequest
      .fold(_ => {
        // binding failure, you retrieve the form containing errors:
        BadRequest(views.html.index(List.empty, form))
      }, moneyToSpend => {
        /* binding success, you get the actual value. */
        val getConvertedMoney = convertMoney(moneyToSpend)
        System.out.println(getConvertedMoney)
        //  val getFilteredEvents =
        Ok(views.html.index(List.empty, form))
      })
  }
}
