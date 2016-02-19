package app

import play.api.mvc.{Action, Controller}

/**
  * @author michaeldohr
  * @since 14/02/16
  */
class RpgAppApi extends Controller {

  def ready = Action {
    Ok("Application is ready")
  }

}
