package utils.auth0

/**
  * @author michaeldohr
  * @since 20/02/16
  */
trait Auth0Context {

  val auth0Endpoint: String

  val auth0ClientId: String

  val auth0ClientSecret: String

}
