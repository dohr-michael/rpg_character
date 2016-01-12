package models

case class RuleOption(name: String)

case class Meta(min: Option[Int] = None, max: Option[Int] = None, withFree: Option[Boolean] = None, freeCategory: Option[String] = None)

case class Rule(uid: String, name: String, category: String, rules: List[Rule] = List(), links: Map[String, String] = Map(), meta: Meta = Meta())

case class RuleSystem(name: String, rules: List[Rule] = List(), links: List[String] = List())
