package app

import chronicles.ChronicleFormat
import users.UserFormat
import utils.neo4j.NeoUtils

/**
  * @author michaeldohr
  * @since 20/02/16
  */
trait AppFormats {

  val apiFormats = new utils.json.ApiFormat with UserFormat.ApiFormat with ChronicleFormat.ApiFormat {}

  val storeFormats = new NeoUtils with UserFormat.NeoFormat with ChronicleFormat.NeoFormat {}

}
