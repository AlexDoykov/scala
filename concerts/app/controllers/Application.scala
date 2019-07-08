package controllers

import javax.inject.Inject

import scala.concurrent.{ExecutionContext}
import play.api.mvc._

class Application @Inject()(val controllerComponents: ControllerComponents)(
  implicit ec: ExecutionContext
) extends BaseController {}
