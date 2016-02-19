package utils.security

import org.joda.time.DateTime

/**
  * @author michaeldohr
  * @since 19/02/16
  */
case class Auth0UserProfile(email: String,
                            name: Option[String] = None,
                            given_name: Option[String] = None,
                            family_name: Option[String] = None,
                            picture: Option[String] = None,
                            genre: Option[String] = None,
                            locale: Option[String] = None,
                            updated_at: Option[DateTime] = None,
                            created_at: Option[DateTime] = None)
